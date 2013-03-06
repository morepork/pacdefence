/*
 *  This file is part of Pac Defence.
 *
 *  Pac Defence is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Pac Defence is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  (C) Liam Byrne, 2008 - 2012.
 */

package logic;

import gui.ControlPanel;
import gui.Drawable;
import gui.GameMapPanel;
import gui.GameMapPanel.DebugStats;
import gui.PacDefence.ReturnToTitleCallback;
import gui.maps.MapParser.GameMap;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.prefs.BackingStoreException;

import javax.swing.JPanel;

import towers.AbstractTower;
import towers.Tower;
import towers.Tower.Attribute;
import util.Helper;
import creeps.Creep;
import creeps.Pacman;


public class Game {
   
   private final Scene scene = new Scene();

   private Clock clock;

   private GameMap gameMap;
   private final ReturnToTitleCallback returnToTitleCallback;
   private final Options options;
   private GameMapPanel gameMapPanel;
   private Future<ControlPanel> controlPanelFuture;
   private ControlPanel controlPanel;
   
   private final Map<Attribute, Integer> upgradesSoFar =
         new EnumMap<Attribute, Integer>(Attribute.class);

   private static final List<Comparator<Creep>> comparators = createComparators();
   
   // Last position inside GameMapPanel, should be null otherwise
   private Point lastMousePosition;
   
   private int level;
   private boolean levelInProgress;
   private long money;
   private int lives;
   private int livesLostOnThisLevel;
   private double interestRate;
   private int endLevelUpgradesLeft;
   private static final int upgradeLives = 5;
   private static final int upgradeMoney = 1000;
   private static final double upgradeInterest = 0.01;
   
   // These should only be set during a level using their set methods. Only one should be non null
   // at any particular time
   // The currently selected tower
   private Tower selectedTower;
   // The tower that is being built
   private Tower buildingTower;
   // The tower whose button is rolled over in the control panel
   private Tower rolloverTower;
   // The tower that is being hovered over on the map
   private Tower hoverOverTower;
   
   private Creep selectedCreep;
   private Creep hoverOverCreep;
   
   public Game(ReturnToTitleCallback returnToTitleCallback, Options options) {
      this.returnToTitleCallback = returnToTitleCallback;
      this.options = options;
      loadControlPanel();
   }
   
   public JPanel getGameMapPanel() {
      return gameMapPanel;
   }
   
   public JPanel getControlPanel() {
      return controlPanel;
   }
   
   public void startGame(GameMap gm) {
      try {
         controlPanel = controlPanelFuture.get();
      } catch(InterruptedException e) {
         // This shouldn't ever happen
         throw new RuntimeException(e);
      } catch(ExecutionException e) {
         // Hopefully this won't happen either
         throw new RuntimeException(e);
      }
      gameMap = gm;
      gameMapPanel = createGameMapPanel(gm);
      setStartingStats();
      clock = new Clock();
   }
   
   public void stopRunning() {
      clock.end();
      scene.clear();
      levelInProgress = false;
   }
   
   /**
    * Load the control panel asynchronously.
    * 
    * This saves time when the player clicks a map to when they can start playing.
    */
   private void loadControlPanel() {
      controlPanelFuture = MyExecutor.submit(new Callable<ControlPanel>() {
         @Override
         public ControlPanel call() {
            ControlPanel cp = new ControlPanel();
            cp.setEventProcessor(new ControlEventProcessor());
            cp.addMouseMotionListener(new MouseMotionListener() {
               @Override
               public void mouseMoved(MouseEvent e) {
                  lastMousePosition = null;
               }
               @Override
               public void mouseDragged(MouseEvent e) {
                  lastMousePosition = null;
               }
            });
            return cp;
         }
      });
   }
   
   private void setSelectedTower(Tower t) {
      if(selectedTower != null) {
         // If there was a selected tower before deselect it
         selectedTower.select(false);
      }
      selectedTower = t;
      if(selectedTower != null) {
         // and if a tower has been selected, select it
         selectedTower.select(true);
      }
   }
   
   private void setBuildingTower(Tower t) {
      if(t != null) {
         setSelectedTower(null);
      }
      controlPanel.enableTowerStatsButtons(t == null);
      buildingTower = t;
   }
   
   private void setRolloverTower(Tower t) {
      rolloverTower = t;
   }
   
   private void setHoverOverTower(Tower t) {
      hoverOverTower = t;
   }
   
   private void setSelectedCreep(Creep c) {
      selectedCreep = c;
   }
   
   private void setHoverOverCreep(Creep c) {
      hoverOverCreep = c;
   }

   private GameMapPanel createGameMapPanel(GameMap gm) {
      GameMapPanel gmp = new GameMapPanel(gm, options.isDebugTimes(), options.isDebugPath());
      gmp.addMouseListener(new MouseAdapter(){
         @Override
         public void mouseReleased(MouseEvent e) {
            processMouseReleased(e);
         }
      });
      gmp.addMouseMotionListener(new MouseMotionListener() {
         @Override
         public void mouseMoved(MouseEvent e) {
            lastMousePosition = e.getPoint();
         }
         @Override
         public void mouseDragged(MouseEvent e) {
            lastMousePosition = e.getPoint();
         }
      });
      return gmp;
   }
   
   private static List<Comparator<Creep>> createComparators() {
      List<Comparator<Creep>> list = new ArrayList<Comparator<Creep>>();
      list.add(new Creep.FirstComparator());
      list.add(new Creep.LastComparator());
      list.add(new Creep.FastestComparator());
      list.add(new Creep.SlowestComparator());
      list.add(new Creep.MostHPComparator());
      list.add(new Creep.LeastHPComparator());
      list.add(new Creep.DistanceComparator(new Point(), true));
      list.add(new Creep.DistanceComparator(new Point(), false));
      list.add(new Creep.RandomComparator());
      return list;
   }
   
   private void endLevel() {
      levelInProgress = false;
      endLevelUpgradesLeft++;
      
      long interest = (long)(money * interestRate);
      int levelEndBonus = Formulae.levelEndBonus(level);
      int noEnemiesThroughBonus = 0;
      
      String text = "Level " + level + " complete! ";
      text += "Earned " + levelEndBonus + " + ";
      if(livesLostOnThisLevel == 0) {
         text += Formulae.noEnemiesThroughBonus(level) + " (perfect) + ";
      }
      text += interest + " (interest)";
      
      increaseMoney(interest + levelEndBonus + noEnemiesThroughBonus);
      updateAllButLevelStats();
      
      gameMapPanel.displayText(text);
      controlPanel.enableStartButton(true);
      
      // Remove all the ghosts at the end of the level
      scene.removeAllGhosts();
      
      // If the level is just finished it's a good time to run the garbage
      // collector rather than have it run during a level.
      System.gc();
   }
   
   private boolean canBuildTower(Class<? extends Tower> towerType) {
      return money >= scene.getTowerCost(towerType);
   }
   
   private void buildTower(Tower t) {
      // New towers get half the effect of all the bonus upgrades so far, rounded down
      for(Attribute a : upgradesSoFar.keySet()) {
         for(int i = 0; i < upgradesSoFar.get(a) / 2; i++) {
            t.upgrade(a, false);
         }
      }
      decreaseMoney(scene.getTowerCost(t.getClass()));
      updateMoney();
   }
   
   private void increaseMoney(long amount) {
      money += amount;
      updateMoney();
   }
   
   private void decreaseMoney(long amount) {
      money -= amount;
   }
   
   private void setStartingStats() {
      level = 0;
      scene.clear();
      upgradesSoFar.clear();
      selectedTower = null;
      buildingTower = null;
      rolloverTower = null;
      hoverOverTower = null;
      money = 4000;
      lives = 25;
      livesLostOnThisLevel = 0;
      interestRate = 0.03;
      endLevelUpgradesLeft = 0;
      updateAll();
   }
   
   private void updateAll() {
      updateLevelStats();
      updateAllButLevelStats();
   }
   
   private void updateAllButLevelStats() {
      // Level stats are a bit slower, and only need to be done once per level
      updateEndLevelUpgradesLabel();
      updateInterestLabel();
      updateLives();
      updateMoney();
      updateTowerStats();
   }
   
   private void updateMoney() {
      controlPanel.updateMoney(money);
   }
   
   private void updateLives() {
      controlPanel.updateLives(lives);

      if(lives <= 0 && !clock.gameOver) {
         signalGameOver();
      }
   }
   
   private void updateLevelStats() {
      int level = this.level;
      if(level == 0) {
         level = 1;
      }
      String levelText = "Level " + level;
      String numCreeps = String.valueOf(Formulae.numCreeps(level));
      long hp = Formulae.hp(level);
      String hpText = String.valueOf((long)(0.5 * hp) + " - " + hp * 2);
      String timeBetweenCreeps = "0 - " + Helper.format(Formulae.
            ticksBetweenAddCreep(level) * 2 / Constants.CLOCK_TICKS_PER_SECOND, 2) + "s";
      controlPanel.updateLevelStats(levelText, numCreeps, hpText, timeBetweenCreeps);
   }
   
   private void updateInterestLabel() {
      controlPanel.updateInterest(Helper.format((interestRate * 100), 0) + "%");
   }
   
   private void updateEndLevelUpgradesLabel() {
     controlPanel.updateEndLevelUpgrades(endLevelUpgradesLeft);
   }
   
   private void updateTowerStats() {
      Tower t = null;
      if(rolloverTower != null || buildingTower != null) {
         // This needs to be first as rollover tower takes precedence over selected
         t = rolloverTower != null ? rolloverTower : buildingTower;
         controlPanel.updateCurrentCost(t.getName(), scene.getTowerCost(t.getClass()));
         controlPanel.setCurrentInfoToTower(null);
      } else if(selectedTower != null || hoverOverTower != null) {
         t = selectedTower != null ? selectedTower : hoverOverTower;
         controlPanel.setCurrentInfoToTower(t);
      } else {
         updateCreepInfo();
      }
      controlPanel.setStats(t);
   }
   
   private void updateCreepInfo() {
      Creep c = null;
      if(selectedCreep != null) {
         c = selectedCreep;
      }
      if(c == null && hoverOverCreep != null) {
         c = hoverOverCreep;
      }
      controlPanel.setCurrentInfoToCreep(c);
   }
   
   private void processMouseReleased(MouseEvent e) {
      if(clock.gameOver) {
         return;
      }
      setSelectedTower(null);
      setSelectedCreep(null);
      if(e.getButton() == MouseEvent.BUTTON3) {
         // Stop everything if it's the right mouse button
         setBuildingTower(null);
         return;
      }
      Point p = e.getPoint();
      Tower t = scene.getTowerContaining(p);
      if(t == null) {
         if(buildingTower == null) {
            setSelectedCreep(scene.getCreepContaining(p));
         } else {
            tryToBuildTower(p);
         }
      } else {
         // Select a tower if one is clicked on
         setSelectedTower(t);
         setBuildingTower(null);
      }
      updateTowerStats();
   }
   
   private void updateHoverOverStuff(Point p) {
      if(p == null) {
         setHoverOverTower(null);
         setHoverOverCreep(null);
      } else if (selectedTower == null && buildingTower == null) {
         setHoverOverTower(scene.getTowerContaining(p));
         if(hoverOverTower == null) {
            setHoverOverCreep(scene.getCreepContaining(p));
         }
      }
   }
   
   private void tryToBuildTower(Point p) {
      if(buildingTower == null) {
         return;
      }
      if(isValidTowerPos(p)) {
         Tower toBuild = buildingTower.constructNew(p, gameMap.getPathBounds());
         if(canBuildTower(toBuild.getClass())) {
            buildTower(toBuild);
            // Have to add after telling the control panel otherwise the price will be wrong
            scene.addTower(toBuild);
            // If another tower can't be built, set the building tower to null
            if(!canBuildTower(buildingTower.getClass())) {
               setBuildingTower(null);
            }
         }
      }
   }
   
   private boolean isValidTowerPos(Point p) {
      if(buildingTower == null || p == null) {
         return false;
      }
      Tower toBuild = buildingTower.constructNew(p, gameMap.getPathBounds());
      // Checks that the point isn't on the path
      if(!toBuild.canTowerBeBuilt(gameMap.getPath())) {
         return false;
      }
      // Checks that the point doesn't clash with another tower
      if(!scene.canBuildTower(toBuild)) {
         return false;
      }
      return true;
   }
   
   private void signalGameOver() {
      clock.gameOver = true;
      
      int highScorePosition = 0;
      try {
         highScorePosition = HighScores.addScore(gameMap.getDescription(), level);
      } catch(BackingStoreException e) {
         // Print the error and continue if this happens
         e.printStackTrace();
      }
      gameMapPanel.signalGameOver(highScorePosition);
   }
   
   private class Clock extends Thread {
      
      private final int[] fastModes = new int[]{1, 2, 5};
      private int currentMode = 0;
      
      private int creepsToAdd;
      private long levelHP;
      
      private int ticksBetweenAddCreep;
      private int addCreepIn = 0;
      
      private final int timesLength = (int)(Constants.CLOCK_TICKS_PER_SECOND / 2);
      private int timesPos = 0;
      // In each of these the last position is used to store the last time
      // and is not used for calculating the average
      private final long[] processTimes        = new long[timesLength + 1];
      private final long[] processCreepsTimes  = new long[timesLength + 1];
      private final long[] processBulletsTimes = new long[timesLength + 1];
      private final long[] processTowersTimes  = new long[timesLength + 1];
      private final long[] drawTimes           = new long[timesLength + 1];
      // These are used to store the calculated average value
      private long processTime = 0;
      private long processCreepsTime = 0;
      private long processBulletsTime = 0;
      private long processTowersTime = 0;
      private long drawTime = 0;
      
      private boolean keepRunning = true;

      private boolean gameOver = false;
      
      // I know I shouldn't really use a double, but it should be fine.
      // This is used so fractional amounts can be saved between ticks.
      private double moneyEarned = 0;
      
      public Clock() {
         super("Pac Defence Clock");
//         System.out.println("Using " + numCallables + " callables.");
         start();
      }
            
      @Override
      public void run() {
         while(keepRunning) {
            // Used nanoTime as many OS, notably windows, don't record ms times less than 10ms
            long beginTime = System.nanoTime();
            if(!gameOver) {
               doTicks();
               if(options.isDebugTimes()) {
                  processTimes[timesLength] = calculateElapsedTimeMillis(beginTime);
               }
            }
            long drawingBeginTime = draw();
            if(options.isDebugTimes()) {
               drawTimes[timesLength] = calculateElapsedTimeMillis(drawingBeginTime);
               calculateTimesTaken();
            }
            long elapsedTime = calculateElapsedTimeMillis(beginTime);
            if(elapsedTime < Constants.CLOCK_TICK) {
               try {
                  Thread.sleep(Constants.CLOCK_TICK - elapsedTime);
               } catch(InterruptedException e) {
                  // The sleep should never be interrupted
                  e.printStackTrace();
               }
            }
         }
      }
      
      public void end() {
         keepRunning = false;
      }
      
      public void switchFastMode(boolean b) {
         // Can't just subtract one as the % operator would leave it negative
         currentMode += b ? 1 : fastModes.length - 1;
         currentMode %= fastModes.length;
      }
      
      private void doTicks() {
         int ticksToDo = fastModes[currentMode];
         for(int i = 0; i < ticksToDo; i++) {
            if(levelInProgress && scene.getNumCreeps() == 0 && creepsToAdd <= 0) {
               endLevel();
            }
            tickScene();
         }
         // Catches any new creeps that may have moved under the cursor
         // Save the mouse position from mouseMotionListeners rather than use getMousePosition as it
         // is much faster
         updateHoverOverStuff(lastMousePosition);
         updateTowerStats();
      }
      
      private void tickScene() {
         Scene.DebugTimes debugTimes = null;
         if(options.isDebugTimes()) {
            debugTimes = scene.new DebugTimes();
         }
         Scene.TickResult result = scene.tick(debugTimes, levelInProgress, getNewCreep());
         
         // Deselect this creep if it is dead/finished
         if(selectedCreep != null && (!selectedCreep.isAlive() || selectedCreep.isFinished())) {
            setSelectedCreep(null);
         }
         
         // Update the number of lives
         controlPanel.updateNumberLeft(creepsToAdd + scene.getNumCreeps());
         livesLostOnThisLevel += result.livesLost;
         lives -= result.livesLost;
         updateLives();
         
         // Update the amount of money
         moneyEarned += result.moneyEarned;
         increaseMoney((long)moneyEarned);
         // Fractional amounts of money are kept until the next tick
         moneyEarned -= (long)moneyEarned;
      }
      
      private Creep getNewCreep() {
         Creep creep = null;
         if(creepsToAdd > 0) {
            if(addCreepIn < 1) { // If the time has got to zero, add a creep
               creep = new Pacman(level, levelHP,
                     Collections.unmodifiableList(gameMap.getPathPoints()));
               // Adds a creep in somewhere between 0 and twice the designated time
               addCreepIn = (int)(Math.random() * (ticksBetweenAddCreep * 2 + 1));
               creepsToAdd--;
            } else { // Otherwise decrement the time until the next creep will be added
               addCreepIn--;
            }
         }
         return creep;
      }
      
      private long draw() {
         long drawingBeginTime = System.nanoTime();
         drawingBeginTime -= gameMapPanel.redraw(getDrawables(),
               new DebugStats(processTime, processCreepsTime, processBulletsTime,
                     processTowersTime, drawTime, scene.getNumBullets()));
         return drawingBeginTime;
      }
      
      private List<Drawable> getDrawables() {
         List<Drawable> drawables = new ArrayList<Drawable>();
         drawables.addAll(scene.getDrawables());
         // Displays the tower on the cursor that could be built
         drawables.add(new Drawable() {
            @Override
            public void draw(Graphics2D g) {
               Tower t = buildingTower;
               Point p = lastMousePosition;
               if(t != null && p != null) {
                  t.drawShadowAt(g, p, isValidTowerPos(p));
               }
            }
            @Override
            public ZCoordinate getZ() {
               return ZCoordinate.SelectedTower;
            }
         });
         return drawables;
      }
      
      private long calculateElapsedTimeMillis(long beginTime) {
         // Convert to ms, as beginTime is in ns
         return (System.nanoTime() - beginTime) / 1000000;
      }
      
      private void calculateTimesTaken() {
         processTime        = insertAndReturnAverage(processTimes);
         processCreepsTime  = insertAndReturnAverage(processCreepsTimes);
         processBulletsTime = insertAndReturnAverage(processBulletsTimes);
         processTowersTime  = insertAndReturnAverage(processTowersTimes);
         drawTime           = insertAndReturnAverage(drawTimes);
         timesPos = (timesPos + 1) % timesLength;
      }
      
      private long insertAndReturnAverage(long[] array) {
         array[timesPos] = array[timesLength];
         array[timesLength] = 0;
         long sum = 0;
         for(int i = 0; i < timesLength; i++) {
            sum += array[i];
         }
         return sum / timesLength;
      }
   }
   
   public class ControlEventProcessor {
      
      public void processStartButtonPressed() {
         if(!levelInProgress) {
            controlPanel.enableStartButton(false);
            level++;
            gameMapPanel.removeText();
            livesLostOnThisLevel = 0;
            levelInProgress = true;
            clock.creepsToAdd = Formulae.numCreeps(level);
            clock.levelHP = Formulae.hp(level);
            clock.ticksBetweenAddCreep = Formulae.ticksBetweenAddCreep(level);
            updateLevelStats();
         }
      }
      
      public void processUpgradeButtonPressed(Attribute a, boolean ctrl) {
         int numTimes = ctrl ? 5 : 1;
         Tower toAffect = towerToAffect();
         for(int i = 0; i < numTimes; i++) {
            if(toAffect == null) {
               long cost = scene.getUpgradeAllTowersCost(a);
               if(cost <= money) {
                  decreaseMoney(cost);
                  scene.upgradeAllTowers(a, true);
               }
            } else {
               long cost = Formulae.upgradeCost(toAffect.getAttributeLevel(a));
               if(cost <= money) {
                  decreaseMoney(cost);
                  toAffect.upgrade(a, true);
               }
            }
         }
         updateTowerStats();
      }
      
      public void processUpgradeButtonRollover(Attribute a, boolean on) {
         if(on) {
            String description = a.toString() + " Upgrade";
            Tower toAffect = towerToAffect();
            long cost = 0;
            if(toAffect == null) {
               description += " (all)";
               cost = scene.getUpgradeAllTowersCost(a);
            } else {
               cost = Formulae.upgradeCost(toAffect.getAttributeLevel(a));
            }
            controlPanel.updateCurrentCost(description, cost);
         } else {
            controlPanel.clearCurrentCost();
         }
      }
      
      public void processTowerButtonPressed(Tower t) {
         if(money >= scene.getTowerCost(t.getClass())) {
            setSelectedTower(null);
            setBuildingTower(t);
            updateTowerStats();
         }
      }
      
      public void processTowerButtonRollover(Tower t, boolean on) {
         if(!on) {
            t = null;
            if(buildingTower == null) {
               controlPanel.clearCurrentCost();
            }
         }
         setRolloverTower(t);
         updateTowerStats();
      }
      
      public void processEndLevelUpgradeButtonPress(boolean livesUpgrade, boolean interestUpgrade,
            boolean moneyUpgrade, Attribute a) {
         if(endLevelUpgradesLeft > 0) {
            endLevelUpgradesLeft--;
            if(a != null) {
               int nextValue = upgradesSoFar.containsKey(a) ? upgradesSoFar.get(a) + 1 : 1;
               if(nextValue % 2 == 0) {
                  // It is even, i.e every second time
                  controlPanel.increaseTowersAttribute(a);
               }
               upgradesSoFar.put(a, nextValue);
               scene.upgradeAllTowers(a, false);
            } else if(livesUpgrade) {
               lives += upgradeLives;
            } else if(interestUpgrade) {
               interestRate += upgradeInterest;
            } else if(moneyUpgrade) {
               increaseMoney(upgradeMoney);
            }
            updateAllButLevelStats();
         }
      }
      
      public void processEndLevelUpgradeButtonRollover(boolean livesUpgrade,
            boolean interestUpgrade, boolean moneyUpgrade, Attribute a, boolean on) {
         if(on) {
            String description = new String();
            String cost = "Free";
            if(a != null) {
               description = a.toString() + " Upgrade (all)";
            } else if(livesUpgrade) {
               description = upgradeLives + " bonus lives";
            } else if(interestUpgrade) {
               description = "+" + Helper.format(upgradeInterest * 100, 0) + "% interest rate";
            } else if(moneyUpgrade) {
               description = upgradeMoney + " bonus money";
            }
            controlPanel.updateCurrentCost(description, cost);
         } else {
            controlPanel.clearCurrentCost();
         }
      }
      
      public void processSellButtonPressed() {
         Tower toAffect = towerToAffect();
         if(toAffect != null) {
            increaseMoney(scene.getTowerSellValue(toAffect));
            toAffect.sell();
            scene.removeTower(toAffect);
            setSelectedTower(null);
         }
      }
      
      public void processSellButtonRollover(boolean on) {
         Tower toAffect = towerToAffect();
         if(toAffect != null && on) {
            controlPanel.updateCurrentCost("Sell " + toAffect.getName(),
                  scene.getTowerSellValue(toAffect));
         } else {
            controlPanel.clearCurrentCost();
         }
      }
      
      public String processTargetButtonPressed(boolean direction) {
         Tower currentTower = hoverOverTower != null ? hoverOverTower : selectedTower;
         Comparator<Creep> currentComparator = currentTower.getCreepComparator();
         int nextIndex = comparators.indexOf(currentComparator) + (direction ? 1 : -1);
         if(nextIndex >= comparators.size()) {
            nextIndex -= comparators.size();
         } else if(nextIndex < 0) {
            nextIndex += comparators.size();
         }
         Comparator<Creep> c = comparators.get(nextIndex);
         selectedTower.setCreepComparator(c);
         return c.toString();
      }
      
      public void processFastButtonPressed(boolean wasLeftClick) {
         clock.switchFastMode(wasLeftClick);
      }
      
      public void processTitleButtonPressed() {
         stopRunning();
         returnToTitleCallback.returnToTitle(gameMapPanel, controlPanel);
      }
      
      public void processRestartPressed() {
         stopRunning(); // This ends the old clock
         gameMapPanel.restart();
         controlPanel.restart();
         setStartingStats();
         clock = new Clock();
         // Flush the cache here, so only the directions that towers actually use are put into it
         // and the extra time for rotating images is fine at the beginning of the level
         AbstractTower.flushImageCache();
      }
      
      private Tower towerToAffect() {
         return selectedTower != null ? selectedTower :
               hoverOverTower != null ? hoverOverTower : null;
      }
   }
   
}
