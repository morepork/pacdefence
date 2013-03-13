/*
 * This file is part of Pac Defence.
 *
 * Pac Defence is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pac Defence is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * (C) Liam Byrne, 2008 - 2013.
 */

package logic;

import gui.Drawable;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import towers.AbstractTower;
import towers.Buildable;
import towers.Bullet;
import towers.Ghost;
import towers.Tower;
import towers.Tower.Attribute;
import towers.impl.AidTower;
import util.Helper;
import creeps.Creep;

public class Scene {
   
   // I tried 1x, 2x, 8x, 10x, 16x, and 20x and 16x was the best (then 10x, then 8x) on my dual
   // core machine - as some callables will be more work, even though they have the same number of
   // bullets as other ones.
   // Set it to zero if only one processor to signal that single threaded versions should be used.
   private final int numCallables = MyExecutor.singleThreaded() ? 0 :
      MyExecutor.getNumThreads() * 16;
   
   private final List<Creep> creeps = Collections.synchronizedList(new ArrayList<Creep>());
   private final List<Tower> towers = Collections.synchronizedList(new ArrayList<Tower>());
   private final List<Ghost> ghosts = Collections.synchronizedList(new ArrayList<Ghost>());
   private final List<Bullet> bullets = new ArrayList<Bullet>();
   
   private List<Tower> towersToAdd = Collections.synchronizedList(new ArrayList<Tower>());
   private List<Tower> towersToRemove = Collections.synchronizedList(new ArrayList<Tower>());
   private List<Ghost> ghostsToAdd = Collections.synchronizedList(new ArrayList<Ghost>());
   
   private int nextGhostCost;
   
   // For performance testing, goes with the code in tickBullets
//    private long time = 0;
//    private int ticks = 0;
   
   public void clear() {
      creeps.clear();
      towers.clear();
      ghosts.clear();
      bullets.clear();
      towersToAdd.clear();
      towersToRemove.clear();
      ghostsToAdd.clear();
      nextGhostCost = Formulae.towerCost(0, 0);
   }
   
   public void addBuilding(Buildable b) {
      if (b instanceof Tower) {
         towersToAdd.add((Tower) b);
         if(b instanceof AidTower) {
            ((AidTower) b).setTowers(Collections.unmodifiableList(towers));
         }
      }
      if(b instanceof Ghost) {
         nextGhostCost *= 2;
         ghostsToAdd.add((Ghost) b);
      }
   }
   
   public void removeTower(Tower t) {
      towersToRemove.add(t);
   }
   
   public long getUpgradeAllTowersCost(Attribute a) {
      long cost = 0;
      synchronized(towers) {
         for(Tower t : towers) {
            cost += Formulae.upgradeCost(t.getAttributeLevel(a));
         }
      }
      return cost;
   }
   
   public void upgradeAllTowers(Attribute a, boolean boughtUpgrade) {
      synchronized(towers) {
         for(Tower t : towers) {
            t.upgrade(a, boughtUpgrade);
         }
      }
   }
   
   public int getNumBullets() {
      return bullets.size();
   }
   
   public int getNumCreeps() {
      return creeps.size();
   }
   
   public void removeAllGhosts() {
      ghosts.clear();
   }
   
   public int getBuildCost(Buildable b) {
      if(b instanceof Ghost) {
         return nextGhostCost;
      } else if(b instanceof Tower) {
         return Formulae.towerCost(getNumTowers(), getNumTowersOfType(((Tower)b).getClass()));
      }
      throw new RuntimeException("Unknown Buildable implementation: " + b);
   }
   
   public long getTowerSellValue(Tower t) {
      return Formulae.sellValue(t, getNumTowers(), getNumTowersOfType(t.getClass()));
   }
   
   private int getNumTowers() {
      // Include the towers to be added/remove in the next tick
      return towers.size() + towersToAdd.size() - towersToRemove.size();
   }
   
   private int getNumTowersOfType(Class<? extends Tower> towerType) {
      int num = 0;
      synchronized(towers) {
         for(Tower t : towers) {
            if(t.getClass() == towerType) {
               num++;
            }
         }
      }
      // Include the towers that are to be added (will be added next tick)
      synchronized(towersToAdd) {
         for(Tower t : towersToAdd) {
            if(t.getClass() == towerType) {
               num++;
            }
         }
      }
      synchronized(towersToRemove) {
         // Likewise for those to be removed
         for(Tower t : towersToRemove) {
            if(t.getClass() == towerType) {
               num--;
            }
         }
      }
      return num;
   }
   
   public Tower getTowerContaining(Point p) {
      synchronized(towers) {
         for(Tower t : towers) {
            if(t.contains(p)) {
               return t;
            }
         }
      }
      return null;
   }
   
   public Creep getCreepContaining(Point p) {
      synchronized(creeps) {
         for(Creep c : creeps) {
            if(c.intersects(p)) {
               // intersects returns false if the creep is dead so don't have to check that
               return c;
            }
         }
      }
      return null;
   }
   
   public boolean canBuild(Buildable b) {
      if (b instanceof Tower) {
         synchronized(towers) {
            for(Tower t : towers) {
               // Checks that the point doesn't clash with another tower
               if(t.doesTowerClashWith((Tower)b)) {
                  return false;
               }
            }
         }
      }
      return true;
   }
   
   public List<Drawable> getDrawables() {
      List<Drawable> drawables = new ArrayList<Drawable>();
      drawables.addAll(creeps);
      drawables.addAll(towers);
      drawables.addAll(ghosts);
      drawables.addAll(bullets);
      return drawables;
   }
   
   public TickResult tick(DebugTimes debugTimes, boolean levelInProgress, Creep newCreep) {
      int livesLost;
      double moneyEarned = 0;
      List<Creep> creepsCopy = new ArrayList<Creep>(creeps);
      // Sort with the default comparator here (should be FirstComparator) for two reasons:
      // Firstly, most towers should use the default comparator so don't need to resort this.
      // Secondly, bullets will hit creeps closest to the end first when they could hit two
      // which is a slight aid.
      Collections.sort(creepsCopy, AbstractTower.DEFAULT_CREEP_COMPARATOR);
      List<Creep> unmodifiableCreeps = Collections.unmodifiableList(creepsCopy);
      if(debugTimes != null) {
         // Make sure any changes here or below are reflected in both, bar the timing bits
         long beginTime = System.nanoTime();
         livesLost = tickCreeps(newCreep);
         debugTimes.processCreepsTime = System.nanoTime() - beginTime;
         beginTime = System.nanoTime();
         moneyEarned += tickBullets(unmodifiableCreeps);
         debugTimes.processBulletsTime = System.nanoTime() - beginTime;
         beginTime = System.nanoTime();
         tickTowers(levelInProgress, unmodifiableCreeps);
         moneyEarned += tickGhosts(unmodifiableCreeps);
         debugTimes.processTowersTime = System.nanoTime() - beginTime;
      } else {
         livesLost = tickCreeps(newCreep);
         moneyEarned += tickBullets(unmodifiableCreeps);
         tickTowers(levelInProgress, unmodifiableCreeps);
         moneyEarned += tickGhosts(unmodifiableCreeps);
      }
      return new TickResult(livesLost, moneyEarned);
   }
   
   private int tickCreeps(Creep newCreep) {
      if (newCreep != null) {
         creeps.add(newCreep);
      }
      int livesLost = 0;
      // Count down as creeps are being removed
      synchronized(creeps) {
         for(int i = creeps.size() - 1; i >= 0; i--) {
            Creep c = creeps.get(i);
            // True if creep has either been killed and is gone from screen or has finished
            if(c.tick()) {
               creeps.remove(i);
               if(c.isFinished()) { // As opposed to being killed
                  livesLost++;
               }
            }
         }
      }
      return livesLost;
   }
   
   private void tickTowers(boolean levelInProgress, List<Creep> unmodifiableCreeps) {
      // Use these rather than addAll/removeAll and then clear as there's a chance a tower could
      // be added to one of the lists before they are cleared but after the addAll/removeAll
      // methods are finished, meaning it'd do nothing but still take/give you money.
      if (!towersToRemove.isEmpty()) {
         List<Tower> toRemove = towersToRemove;
         towersToRemove = Collections.synchronizedList(new ArrayList<Tower>());
         towers.removeAll(toRemove);
      }
      if (!towersToAdd.isEmpty()) {
         List<Tower> toAdd = towersToAdd;
         towersToAdd = Collections.synchronizedList(new ArrayList<Tower>());
         towers.addAll(toAdd);
      }
      // I tried multi-threading this but it made it slower in my limited testing
      synchronized(towers) {
         for(Tower t : towers) {
            bullets.addAll(t.tick(unmodifiableCreeps, levelInProgress));
         }
      }
   }
   
   private double tickGhosts(List<Creep> unmodifiableCreeps) {
      if (!ghostsToAdd.isEmpty()) {
         List<Ghost> toAdd = ghostsToAdd;
         ghostsToAdd = Collections.synchronizedList(new ArrayList<Ghost>());
         ghosts.addAll(toAdd);
      }
      double totalMoneyEarned = 0;
      synchronized(ghosts) {
         // Iterate backwards as ghosts are being removed
         for(int i = ghosts.size() - 1; i >= 0; i--) {
            Ghost g = ghosts.get(i);
            double moneyEarned = g.tick(unmodifiableCreeps);
            if (moneyEarned < 0) {
               ghosts.remove(i);
            } else {
               totalMoneyEarned += moneyEarned;
            }
         }
      }
      return totalMoneyEarned;
   }
   
   private double tickBullets(List<Creep> unmodifiableCreeps) {
      // This and the bit at the end is performance testing - times the first x ticks
//      if(bullets.size() > 0) {
//         ticks++;
//         time -= System.nanoTime();
//      }
      double moneyEarned;
      if(numCallables < 2 || bullets.size() <= 1) {
         // If only one processor or 1 or fewer bullets this will be faster.
         moneyEarned = tickBulletsSingleThread(unmodifiableCreeps);
      } else {
         moneyEarned = tickBulletsMultiThread(unmodifiableCreeps);
      }
//      if(bullets.size() > 0) {
//         time += System.nanoTime();
//         if(ticks == 300) {
//            System.out.println(time / 1000000.0);
//         }
//      }
      return moneyEarned;
   }
   
   private double tickBulletsMultiThread(List<Creep> unmodifiableCreeps) {
      int bulletsPerThread = bullets.size() / numCallables;
      int remainder = bullets.size() % numCallables;
      int firstPos, lastPos = 0;
      // Each Callable returns a List of Integer positions of Bullets to be removed
      List<Future<BulletTickResult>> futures = new ArrayList<>();
      // No point in making more callables than there are bullets
      int n = Math.min(numCallables, bullets.size());
      for(int i = 0; i < n; i++) {
         firstPos = lastPos;
         // Add 1 to callables 1, 2, ... , (remainder - 1), remainder
         lastPos = firstPos + bulletsPerThread + (i < remainder ? 1 : 0);
         // Copying the list should reduce the lag of each thread trying to access the same list
         futures.add(MyExecutor.submit(new BulletTickCallable(firstPos, lastPos, bullets,
               new ArrayList<Creep>(creeps))));
      }
      return processTickFutures(futures);
   }
   
   private double processTickFutures(List<Future<BulletTickResult>> futures) {
      double moneyEarned = 0;
      List<Integer> bulletsToRemove = new ArrayList<>();
      for(Future<BulletTickResult> f : futures) {
         BulletTickResult result;
         try {
            result = f.get();
         } catch(InterruptedException e) {
            // Should never happen
            throw new RuntimeException(e);
         } catch(ExecutionException e) {
            // Should never happen
            throw new RuntimeException(e);
         }
         moneyEarned += result.moneyEarned;
         bulletsToRemove.addAll(result.bulletsToRemove);
      }
      // bulletsToRemove will be sorted smallest to largest as each List returned by a
      // BulletTickCallable is ordered, and the ordering is kept in the list of Futures, so no
      // need to sort it here.
      
      // Remove all the completed bullets from the main list
      Helper.removeAll(bullets, bulletsToRemove);
      return moneyEarned;
   }
   
   private double tickBulletsSingleThread(List<Creep> unmodifiableCreeps) {
      // Run through from last to first as bullets are being removed
      double moneyEarned = 0;
      for(int i = bullets.size() - 1; i >= 0; i--) {
         double money = bullets.get(i).tick(unmodifiableCreeps);
         if(money >= 0) {
            moneyEarned += money;
            bullets.remove(i);
         }
      }
      return moneyEarned;
   }
   
   public class TickResult {
      public final int livesLost;
      public final double moneyEarned;
      
      public TickResult(int livesLost, double moneyEarned) {
         this.livesLost = livesLost;
         this.moneyEarned = moneyEarned;
      }
   }
   
   public class DebugTimes {
      // Elapsed times in nanoseconds
      public long processCreepsTime;
      public long processBulletsTime;
      public long processTowersTime;
   }
   
   private class BulletTickResult {
      public double moneyEarned = 0;
      public List<Integer> bulletsToRemove = new ArrayList<>();
   }
   
   private class BulletTickCallable implements Callable<BulletTickResult> {
      
      private final int firstPos, lastPos;
      private final List<Bullet> bullets;
      private final List<Creep> creeps;
      
      public BulletTickCallable(int firstPos, int lastPos, List<Bullet> bullets,
            List<Creep> creeps) {
         this.firstPos = firstPos;
         this.lastPos = lastPos;
         this.bullets = bullets;
         this.creeps = creeps;
      }

      @Override
      public BulletTickResult call() {
         BulletTickResult result = new BulletTickResult();
         for(int i = firstPos; i < lastPos; i++) {
            double money = bullets.get(i).tick(creeps);
            if(money >= 0) {
               result.moneyEarned += money;
               result.bulletsToRemove.add(i);
            }
         }
         return result;
      }
   }
   
}
