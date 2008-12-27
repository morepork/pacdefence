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
 *  (C) Liam Byrne, 2008.
 */

package logic;

import gui.Circle;
import gui.ControlPanel;
import gui.GameMapPanel;
import gui.SelectionScreens;
import gui.Title;
import gui.Wrapper;
import gui.GameMapPanel.GameMap;
import images.ImageHelper;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import javax.swing.JButton;

import sprites.Pacman;
import sprites.Sprite;
import sprites.Sprite.FirstComparator;
import towers.AidTower;
import towers.BeamTower;
import towers.BomberTower;
import towers.Bullet;
import towers.ChargeTower;
import towers.CircleTower;
import towers.FreezeTower;
import towers.HomingTower;
import towers.JumpingTower;
import towers.LaserTower;
import towers.MultiShotTower;
import towers.OmnidirectionalTower;
import towers.PiercerTower;
import towers.PoisonTower;
import towers.ScatterTower;
import towers.SlowLengthTower;
import towers.Tower;
import towers.WaveTower;
import towers.WeakenTower;
import towers.ZapperTower;
import towers.Tower.Attribute;


public class Game {
   
   public static final int WIDTH = 800;
   public static final int HEIGHT = 600;
   
   public static final int MAP_WIDTH = WIDTH - 200;
   public static final int MAP_HEIGHT = HEIGHT;
   
   public static final int CONTROLS_WIDTH = WIDTH - MAP_WIDTH;
   public static final int CONTROLS_HEIGHT = MAP_HEIGHT;
   
   // Time clock takes to update in ms
   public static final int CLOCK_TICK = 30;
   public static final double CLOCK_TICKS_PER_SECOND = (double)1000 / CLOCK_TICK;
   
   private final boolean debugTimes = true;
   private final boolean debugPath = true;
   
   private final List<Sprite> sprites = Collections.synchronizedList(new ArrayList<Sprite>());
   private final List<Tower> towers = Collections.synchronizedList(new ArrayList<Tower>());
   private final List<Bullet> bullets = Collections.synchronizedList(new ArrayList<Bullet>());
   
   private Clock clock;
   
   private Polygon path;
   private List<Point> pathPoints;
   
   private final Title title;
   private SelectionScreens selectionScreens;
   private ControlPanel cp;
   private GameMapPanel gmp;
   
   private boolean levelInProgress = false;
   
   private final Container container;

   private int level;
   
   private final Map<Attribute, Integer> upgradesSoFar = new EnumMap<Attribute, Integer>(Attribute.class);

   private List<Wrapper<String, Comparator<Sprite>>> comparators = createComparators();
   
   private long money;
   private int lives;
   private int livesLostOnThisLevel;
   private double interestRate;
   private int endLevelUpgradesLeft;
   private static final int upgradeLives = 5;
   private static final int upgradeMoney = 1000;
   private static final double upgradeInterest = 0.005;
   // These should only be set during a level using their set methods
   private Tower selectedTower, buildingTower, rolloverTower, hoverOverTower;
   
   public Game(Container c) {
      container = c;
      container.setLayout(new BorderLayout());
      title = createTitle();
      container.add(title);
   }
   
   public void setSelectedTower(Tower t) {
      if(t == null) {
         if(selectedTower != null) {
            selectedTower.select(false);
         }
      } else {
         t.select(true);
      }
      selectedTower = t;
   }
   
   public void setBuildingTower(Tower t) {
      buildingTower = t;
   }
   
   public void setRolloverTower(Tower t) {
      rolloverTower = t;
   }
   
   public void setHoverOverTower(Tower t) {
      hoverOverTower = t;
   }
   
   public Title createTitle() {
      return new Title(WIDTH, HEIGHT, new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            container.remove(title);
            selectionScreens = new SelectionScreens(WIDTH, HEIGHT, new CarryOn());
            container.add(selectionScreens);
            container.validate();
         }
      });
   }
   
   private List<Tower> getTowerImplementations() {
      List<Tower> towerTypes = new ArrayList<Tower>();
      towerTypes.add(new BomberTower());
      towerTypes.add(new PiercerTower());
      towerTypes.add(new SlowLengthTower());
      towerTypes.add(new FreezeTower());
      towerTypes.add(new JumpingTower());
      towerTypes.add(new CircleTower());
      towerTypes.add(new ScatterTower());
      towerTypes.add(new MultiShotTower());
      towerTypes.add(new LaserTower());
      towerTypes.add(new PoisonTower());
      towerTypes.add(new OmnidirectionalTower());
      towerTypes.add(new WeakenTower());
      towerTypes.add(new WaveTower());
      towerTypes.add(new HomingTower());
      towerTypes.add(new ChargeTower());
      towerTypes.add(new ZapperTower());
      towerTypes.add(new BeamTower());
      towerTypes.add(new AidTower());
      return towerTypes;
   }
   
   private List<Wrapper<String, Comparator<Sprite>>> createComparators() {
      List<Wrapper<String, Comparator<Sprite>>> list = new ArrayList<Wrapper<String,
         Comparator<Sprite>>>();
      list.add(new Wrapper<String, Comparator<Sprite>>("First", new Sprite.FirstComparator()));
      list.add(new Wrapper<String, Comparator<Sprite>>("Last", new Sprite.LastComparator()));
      list.add(new Wrapper<String, Comparator<Sprite>>("Fastest", new Sprite.FastestComparator()));
      list.add(new Wrapper<String, Comparator<Sprite>>("Slowest", new Sprite.SlowestComparator()));
      list.add(new Wrapper<String, Comparator<Sprite>>("Most HP", new Sprite.MostHPComparator()));
      list.add(new Wrapper<String, Comparator<Sprite>>("Least HP", new Sprite.LeastHPComparator()));
      return list;
   }
   
   private long costToUpgradeAllTowers(Attribute a) {
      return costToUpgradeTowers(a, towers);
   }
   
   private long costToUpgradeTowers(Attribute a, List<Tower> towers) {
      long cost = 0;
      for(Tower t : towers) {
         cost += Formulae.upgradeCost(t.getAttributeLevel(a));
      }
      return cost;
   }
   
   public void endLevel() {
      endLevelUpgradesLeft++;
      updateEndLevelUpgradesLabel();
      long moneyBefore = money; 
      multiplyMoney(interestRate);
      long interest = money - moneyBefore;
      int levelEndBonus = Formulae.levelEndBonus(level);
      int noEnemiesThroughBonus = 0;
      StringBuilder text = new StringBuilder();
      text.append("Level ");
      text.append(level);
      text.append(" finished. ");
      text.append(interest);
      text.append(" interest earnt. ");
      text.append(levelEndBonus);
      text.append(" for finishing the level.");
      if(livesLostOnThisLevel == 0) {
         noEnemiesThroughBonus = Formulae.noEnemiesThroughBonus(level);
         text.append(" ");
         text.append(noEnemiesThroughBonus);
         text.append(" bonus for losing no lives.");
      }
      increaseMoney(levelEndBonus + noEnemiesThroughBonus);
      updateMoneyLabel();
      gmp.displayText(text.toString());
      cp.enableStartButton(true);
   }
   
   public boolean canBuildTower(Class<? extends Tower> towerType) {
      return money >= getNextTowerCost(towerType);
   }
   
   public void buildTower(Tower t) {
      for(Attribute a : upgradesSoFar.keySet()) {
         // New towers get half the effect of all the bonus upgrades
         // so far, rounded down
         for(int i = 0; i < upgradesSoFar.get(a) / 2; i++) {
            t.raiseAttributeLevel(a, false);
         }
      }
      decreaseMoney(getNextTowerCost(t.getClass()));
      updateMoneyLabel();
   }
   
   public void increaseMoney(long amount) {
      money += amount;
      updateMoneyLabel();
      // If a tower is selected, more money earnt means it could've done more
      // damage and this may have caused its stats to be upgraded
      updateStats();
   }
   
   public boolean decrementLives(int livesLost) {
      livesLostOnThisLevel += livesLost;
      lives -= livesLost;
      updateLivesLabel();
      return lives <= 0;
   }
   
   private void setStartingStats() {
      level = 0;
      upgradesSoFar.clear();
      selectedTower = null;
      buildingTower = null;
      rolloverTower = null;
      hoverOverTower = null;
      money = 4000;
      lives = 25;
      livesLostOnThisLevel = 0;
      interestRate = 1.03;
      endLevelUpgradesLeft = 0;
      updateAll();
   }
   
   private void multiplyMoney(double factor) {
      money *= factor;
   }
   
   private void decreaseMoney(long amount) {
      money -= amount;
   }
   
   private int getNextTowerCost(Class<? extends Tower> towerType) {
      return Formulae.towerCost(towers.size(), numTowersOfType(towerType));
   }
   
   private int numTowersOfType(Class<? extends Tower> towerType) {
      int num = 0;
      for(Tower t : towers) {
         if(t.getClass() == towerType) {
            num++;
         }
      }
      return num;
   }
   
   private void updateAll() {
      updateLevelStats();
      updateAllButLevelStats();
   }
   
   private void updateAllButLevelStats() {
      updateEndLevelUpgradesLabel();
      updateInterestLabel();
      updateLivesLabel();
      updateMoneyLabel();
      updateStats();
   }
   
   private void updateMoneyLabel() {
      cp.updateMoneyLabel(money);
   }
   
   private void updateLivesLabel() {
      cp.updateLivesLabel(lives);
   }
   
   private void updateLevelStats() {
      int level = this.level;
      if(level == 0) {
         level = 1;
      }
      String levelText = "Level " + level;
      String numSprites = String.valueOf(Formulae.numSprites(level));
      long hp = Formulae.hp(level);
      String hpText = String.valueOf((long)(0.5 * hp) + " - " + hp * 2);
      String timeBetweenSprites = "0 - " + Helper.format(Formulae.
            ticksBetweenAddSprite(level) * 2 / Game.CLOCK_TICKS_PER_SECOND, 2) + "s";
      cp.updateLevelStats(levelText, numSprites, hpText, timeBetweenSprites);
   }
   
   private void updateInterestLabel() {
      cp.updateInterest(Helper.format(((interestRate - 1) * 100), 2) + "%");
   }
   
   private void updateEndLevelUpgradesLabel() {
     cp.updateEndLevelUpgrades(endLevelUpgradesLeft);
   }
   
   private void updateStats() {
      Tower t;
      if(rolloverTower != null) {
         t = rolloverTower;
         cp.updateCurrentCostLabel(rolloverTower.getName() + " Tower", getNextTowerCost(t.getClass()));
      } else if(hoverOverTower != null) {
         t = hoverOverTower;
      } else if(buildingTower != null) {
         t = buildingTower;
         cp.updateCurrentCostLabel(buildingTower.getName() + " Tower", getNextTowerCost(t.getClass()));
      } else {
         t = selectedTower;
      }
      cp.setStats(t);
      updateCurrentTowerInfo();
   }
   
   private void updateCurrentTowerInfo() {
      Tower t = null;
      if(selectedTower != null) {
         t = selectedTower;
      } else {
         if(hoverOverTower != null) {
            t = hoverOverTower;
         }
      }
      cp.setCurrentTowerInfo(t);
   }
   
   private long sellValue(Tower t) {
      return Formulae.sellValue(t, towers.size(), numTowersOfType(t.getClass()));
   }
   
   public class CarryOn {
      public void carryOn(GameMap g) {
         pathPoints = g.getPathPoints();
         path = g.getPath();
         container.remove(selectionScreens);
         gmp = createGameMapPanel(g);
         cp = new ControlPanel(CONTROLS_WIDTH, CONTROLS_HEIGHT,
               ImageHelper.makeImage("control_panel", "blue_lava.jpg"),
               new ControlEventProcessor(), getTowerImplementations());
         container.add(gmp, BorderLayout.WEST);
         container.add(cp, BorderLayout.EAST);
         container.validate();
         setStartingStats();
         clock = new Clock();
         clock.start();
      }
   }
   
   private GameMapPanel createGameMapPanel(GameMap g) {
      GameMapPanel gmp = new GameMapPanel(MAP_WIDTH, MAP_HEIGHT, ImageHelper.makeImage("maps",
            "rainbowColours.jpg"), g, debugTimes, debugPath);
      gmp.addMouseListener(new MouseAdapter(){
         @Override
         public void mouseReleased(MouseEvent e) {
            processMouseReleased(e);
         }
      });
      gmp.addMouseMotionListener(new MouseMotionAdapter(){
         @Override
         public void mouseMoved(MouseEvent e) {
            processMouseMoved(e);
         }
      });
      return gmp;
   }
   
   private void processMouseReleased(MouseEvent e) {
      setSelectedTower(null);
      if(e.getButton() == MouseEvent.BUTTON3) {
         // Stop everything if it's the right mouse button
         setBuildingTower(null);
         return;
      }
      Point p = e.getPoint();
      Tower t = containedInTower(p);
      if(t == null) {
         tryToBuildTower(p);
      } else {
         // Select a tower if one is clicked on
         setSelectedTower(t);
      }    
   }
   
   private void processMouseMoved(MouseEvent e) {
      if(selectedTower == null && buildingTower == null) {
         Tower t = containedInTower(e.getPoint());
         if(hoverOverTower == null && t == null) {
            return;
         }
         setHoverOverTower(t);
      }
   }
   
   /**
    * Notifies the map that a tower button has been pressed.
    * @param t
    * @return
    *        true if a tower is to be shadowed, false if none
    */
   public boolean towerButtonPressed(Tower t) {
      if(buildingTower == null || !buildingTower.getClass().equals(t.getClass())) {
         deselectTower();
         buildingTower = t;
         return true;
      } else {
         setBuildingTower(null);
         return false;
      }
   }
   
   public void selectTower(Tower t) {
      t.select(true);
      selectedTower = t;
      setBuildingTower(null);
   }
   
   public void deselectTower() {
      if(selectedTower != null) {
         selectedTower.select(false);
         selectedTower = null;
      }
   }
   
   private void start() {
      if(!levelInProgress) {
         level++;
         gmp.removeText();
         livesLostOnThisLevel = 0;
         levelInProgress = true;
         clock.spritesToAdd = Formulae.numSprites(level);
         clock.levelHP = Formulae.hp(level);
         clock.ticksBetweenAddSprite = Formulae.ticksBetweenAddSprite(level);
         updateLevelStats();
      }
   }
   
   public void upgradeAll(Attribute a) {
      for(Tower t : towers) {
         t.raiseAttributeLevel(a, false);
      }
   }
   
   public void removeTower(Tower t) {
      if(!towers.remove(t)) {
         throw new RuntimeException("Tower that wasn't on screen sold.");
      }
      if(t == selectedTower) {
         setSelectedTower(null);
      }
   }
   
   private void restart() {
      stopRunning();
      gmp.restart();
      cp.restart();
      setStartingStats();
      clock = new Clock();
      clock.start();
   }
   
   private void stopRunning() {
      clock.end();
      sprites.clear();
      towers.clear();
      bullets.clear();
      levelInProgress = false;
   }
   
   private Tower containedInTower(Point p) {
      for(Tower t : towers) {
         if(t.contains(p)) {
            return t;
         }
      }
      return null;
   }
   
   private void tryToBuildTower(Point p) {
      if(buildingTower == null) {
         return;
      }
      Tower toBuild = buildingTower.constructNew(p, path.getBounds2D());
      for(Tower t : towers) {
         // Checks that the point doesn't clash with another tower
         if(t.doesTowerClashWith(toBuild)) {
            return;
         }
      }
      // Checks that the point isn't on the path
      Shape bounds = toBuild.getBounds();
      if(bounds instanceof Circle) {
         if(((Circle) bounds).intersects(path)) {
            return;
         }
      } else {
         if(path.intersects(bounds.getBounds())) {
            return;
         }
      }
      if(canBuildTower(toBuild.getClass())) {
         buildTower(toBuild);
         // Have to add after telling the control panel otherwise
         // the price will be wrong
         towers.add(toBuild);
         if(toBuild instanceof AidTower) {
            ((AidTower) toBuild).setTowers(Collections.unmodifiableList(towers));
         }
         if(!canBuildTower(buildingTower.getClass())) {
            setBuildingTower(null);
         }
      }
   }
   
   public class ControlEventProcessor {
      
      public void processStartButtonPressed() {
         start();
      }
      
      public void processUpgradeButtonPressed(JButton b, Attribute a) {
         long cost = 0;
         if(selectedTower == null) {
            cost = costToUpgradeTowers(a, towers);
            if(cost <= money) {
               decreaseMoney(cost);
               for(Tower t : towers) {
                  t.raiseAttributeLevel(a, true);
               }
            }
         } else {
            cost = Formulae.upgradeCost(selectedTower.getAttributeLevel(a));
            if(cost <= money) {
               decreaseMoney(cost);
               selectedTower.raiseAttributeLevel(a, true);
               updateStats();
            }
         }
      }
      
      public void processUpgradeButtonChanged(JButton b, Attribute a) {
         if(!checkIfMovedOff(b)) {
            String description = a.toString() + " Upgrade";
            long cost;
            if(selectedTower == null) {
               description += " (all)";
               cost = costToUpgradeAllTowers(a);
            } else {
               cost = Formulae.upgradeCost(selectedTower.getAttributeLevel(a));
            }
            cp.updateCurrentCostLabel(description, cost);
         }
      }
      
      public void processTowerButtonAction(JButton b, Tower t) {
         if(money >= getNextTowerCost(t.getClass())) {
            if(towerButtonPressed(t)) {
               setBuildingTower(t);
               updateStats();
            }
         }
      }
      
      public void processTowerButtonChangeEvent(JButton b, Tower t) {
         setRolloverTower(t);
         if(!checkIfMovedOff(b)) {
            updateStats();
         }
      }
      
      public void processEndLevelUpgradeButtonPress(JButton b, boolean livesUpgrade,
            boolean interestUpgrade, boolean moneyUpgrade, Attribute a) {
         if(endLevelUpgradesLeft > 0) {
            endLevelUpgradesLeft--;
            if(livesUpgrade) {
               lives += upgradeLives;
            } else if(interestUpgrade) {
               interestRate += upgradeInterest;
            } else if(moneyUpgrade) {
               increaseMoney(upgradeMoney);
            }
            if(a != null) {
               int nextValue = 1;
               if(upgradesSoFar.containsKey(a)) {
                  nextValue = upgradesSoFar.get(a) + 1;
               }
               if(nextValue / 2 == (nextValue + 1) / 2) {
                  // It is even, i.e every second time
                  cp.increaseTowersAttribute(a);
               }
               upgradesSoFar.put(a, nextValue);
               upgradeAll(a);
            }
            updateAllButLevelStats();
         }
      }
      
      public void processEndLevelUpgradeButtonChanged(JButton b, boolean livesUpgrade,
            boolean interestUpgrade, boolean moneyUpgrade, Attribute a) {
         if(!checkIfMovedOff(b)) {
            String description = " ";
            String cost = "Free";
            if(a != null) {
               description = a.toString() + " Upgrade (all)";
            } else if(livesUpgrade) {
               description = upgradeLives + " bonus lives";
            } else if(interestUpgrade) {
               description = "+" + upgradeInterest * 100 + "% interest rate";
            } else if(moneyUpgrade) {
               description = upgradeMoney + " bonus money";
            }
            cp.updateCurrentCostLabel(description, cost);
         }
      }
      
      public void processSellButtonPressed(JButton b) {
         if(selectedTower != null) {
            removeTower(selectedTower);
            increaseMoney(sellValue(selectedTower));
            setSelectedTower(null);
         }
      }
      
      public void processSellButtonChanged(JButton b) {
         if(selectedTower != null && !checkIfMovedOff(b)) {
            cp.updateCurrentCostLabel("Sell " + selectedTower.getName() + " Tower",
                  sellValue(selectedTower));
         }
      }
      
      public void processTargetButtonPressed(JButton b) {
         String s = b.getText();
         for(int i = 0; i < comparators.size(); i++) {
            if(comparators.get(i).getT1().equals(s)) {
               int nextIndex = (i + 1) % comparators.size();
               Wrapper<String, Comparator<Sprite>> w = comparators.get(nextIndex);
               b.setText(w.getT1());
               selectedTower.setSpriteComparator(w.getT2());
               return;
            }
         }
      }
      
      public void processTitleButtonPressed() {
         stopRunning();
         container.remove(gmp);
         container.remove(cp);
         container.add(title);
         container.validate();
         container.repaint();
      }
      
      public void processRestartPressed() {
         restart();
      }
      
      private boolean checkIfMovedOff(JButton b) {
         if(b.getMousePosition() == null) {
            // This means the cursor isn't over the button, so the mouse was moved off it
            setRolloverTower(null);
            cp.updateCurrentCostLabel(" ", " ");
            updateStats();
            return true;
         } else {
            return false;
         }
      }
   }
   
   private class Clock extends Thread {
      
      private int spritesToAdd;
      private long levelHP;
      
      private int ticksBetweenAddSprite;
      private int addSpriteIn = 0;
      private final int timesLength = (int)(CLOCK_TICKS_PER_SECOND / 2);
      private int timesPos = 0;
      // In each of these the last position is used to store the last time
      // and is not used for calculating the average
      private final long[] processTimes = new long[timesLength + 1];
      private final long[] processSpritesTimes = new long[timesLength + 1];
      private final long[] processBulletsTimes = new long[timesLength + 1];
      private final long[] processTowersTimes = new long[timesLength + 1];
      private final long[] drawTimes = new long[timesLength + 1];
      private boolean keepRunning = true;
      private boolean isWaiting;
      private final int numThreads;
      private final BulletTickThread[] bulletTickThreads;
      private final boolean[] isThreadRunning;
      private final List<Integer> toRemove = new ArrayList<Integer>();
      private double moneyEarnt = 0;
      
      private long processTime = 0;
      private long processSpritesTime = 0;
      private long processBulletsTime = 0;
      private long processTowersTime = 0;
      private long drawTime = 0;
      
      private boolean gameOver = false;
      
      public Clock() {
         numThreads = Runtime.getRuntime().availableProcessors();
         if(numThreads > 1) {
            bulletTickThreads = new BulletTickThread[numThreads];
            isThreadRunning = new boolean[numThreads];
            for(int i = 0; i < numThreads; i++) {
               bulletTickThreads[i] = new BulletTickThread(i);
               bulletTickThreads[i].start();
               isThreadRunning[i] = false;
            }
         } else {
            // If there is only one core, use the original single threaded version.
            bulletTickThreads = null;
            isThreadRunning = null;
         }
      }
            
      @Override
      public void run() {
         while(keepRunning) {
            try {
               // Used nanoTime as many OS, notably windows, don't record ms times less than 10ms
               long beginTime = System.nanoTime();
               if(!gameOver) {
                  if(debugTimes) {
                     calculateTimesTaken();
                  }
                  tick();
               }
               if(debugTimes) {
                  processTimes[timesLength] = calculateElapsedTime(beginTime);
               }
               long drawingBeginTime = gmp.draw(Collections.unmodifiableList(towers),
                     selectedTower, buildingTower, Collections.unmodifiableList(sprites),
                     Collections.unmodifiableList(bullets), processTime, processSpritesTime,
                     processBulletsTime, processTowersTime, drawTime, bullets.size());
               drawTimes[timesLength] = calculateElapsedTime(drawingBeginTime);
               gmp.repaint();
               long elapsedTime = calculateElapsedTime(beginTime);
               if(elapsedTime < CLOCK_TICK) {
                  try {
                     Thread.sleep(CLOCK_TICK - elapsedTime);
                  } catch(InterruptedException e) {
                     e.printStackTrace();
                     // The sleep should never be interrupted
                  }
               }
            } catch(Exception e) {
               e.printStackTrace();
               // This means that the game will continue even if there is some strange bug
            }
         }
      }
      
      public void end() {
         keepRunning = false;
      }
      
      private void tick() {
         // I don't want to sort the actual list of sprites as that would affect
         // the order they're drawn which looks weird, and the order can change
         // tick by tick so it's easiest to sort them once each time.
         List<Sprite> sortedSprites = new ArrayList<Sprite>(sprites);
         Collections.sort(sortedSprites, new FirstComparator());
         List<Sprite> unmodifiableSprites = Collections.unmodifiableList(sortedSprites);
         if(cp != null) {
            long beginTime;
            if(debugTimes) {
               beginTime = System.nanoTime();
            }
            if(decrementLives(tickSprites())) {
               gmp.signalGameOver();
            }
            if(debugTimes) {
               processSpritesTimes[timesLength] = calculateElapsedTime(beginTime);
               beginTime = System.nanoTime();
            }
            increaseMoney(tickBullets(unmodifiableSprites));
            if(debugTimes) {
               processBulletsTimes[timesLength] = calculateElapsedTime(beginTime);
               beginTime = System.nanoTime();
            }
            tickTowers(unmodifiableSprites);
            if(debugTimes) {
               processTowersTimes[timesLength] = calculateElapsedTime(beginTime);
            }
         }
      }
      
      private long calculateElapsedTime(long beginTime) {
         // Returns in ms, though the beginTime is in ns
         return (System.nanoTime() - beginTime) / 1000000;
      }
      
      private void calculateTimesTaken() {
         processTime = insertAndReturnSum(processTimes);
         processSpritesTime = insertAndReturnSum(processSpritesTimes);
         processBulletsTime = insertAndReturnSum(processBulletsTimes);
         processTowersTime = insertAndReturnSum(processTowersTimes);
         drawTime = insertAndReturnSum(drawTimes);
         timesPos = (timesPos + 1) % timesLength;
      }
      
      private long insertAndReturnSum(long[] array) {
         array[timesPos] = array[timesLength];
         array[timesLength] = 0;
         long sum = 0;
         for(int i = 0; i < timesLength; i++) {
            sum += array[i];
         }
         return sum / timesLength;
      }
      
      private int tickSprites() {
         int livesLost = 0;
         if(levelInProgress && sprites.isEmpty() && spritesToAdd <= 0) {
            endLevel();
            levelInProgress = false;
            // If the level is just finished it's a good time to run the garbage
            // collector rather than have it run during a level.
            System.gc();
         }
         if(spritesToAdd > 0) {
            if(addSpriteIn < 1) {
               sprites.add(new Pacman(level, levelHP, clonePathPoints()));
               // Adds a sprite in somewhere between 0 and twice the designated time
               addSpriteIn = (int)(Math.random() * (ticksBetweenAddSprite * 2 + 1));
               spritesToAdd--;
               cp.setNumberLeft(spritesToAdd);
            } else {
               addSpriteIn--;
            }
         }
         List<Integer> toRemove = new ArrayList<Integer>();
         for(int i = 0; i < sprites.size(); i++) {
            Sprite s = sprites.get(i);
            if(s.tick()) {
               toRemove.add(i);
               if(s.isAlive()) {
                  // If the sprite is being removed and is still alive, it went
                  // off the edge of the screen
                  livesLost++;
               }
            }
         }
         Helper.removeAll(sprites, toRemove);
         return livesLost;
      }
      
      private List<Point> clonePathPoints() {
         List<Point> clone = new ArrayList<Point>(pathPoints.size());
         for(Point p : pathPoints) {
            clone.add(new Point(p));
         }
         return clone;
      }
      
      private void tickTowers(List<Sprite> unmodifiableSprites) {
         // Only do this if there's a level going, to decrease load and
         // so towers such as charge towers don't charge between levels
         if(levelInProgress) {
            // Don't use for each loop here as a new tower can be built
            for(int i = 0; i < towers.size(); i++ ) {
               bullets.addAll(towers.get(i).tick(unmodifiableSprites));
            }
         }
      }
      
      private long tickBullets(List<Sprite> unmodifiableSprites) {
         if(numThreads == 1 || bullets.size() <= 1) {
            // Use single thread version if only one processor or 1 or fewer bullets
            // as it will be faster.
            return tickBulletsSingleThread(unmodifiableSprites);
         }
         isWaiting = true;
         int bulletsPerThread = bullets.size() / numThreads;
         int remainder = bullets.size() - bulletsPerThread * numThreads;
         Arrays.fill(isThreadRunning, true);
         int firstPos, lastPos = 0;
         for(int i = 0; i < numThreads; i++) {
            firstPos = lastPos;
            lastPos = firstPos + bulletsPerThread + (i < remainder ? 1 : 0);
            // Copying the list should reduce the lag of each thread trying to access
            // the same list
            bulletTickThreads[i].tickBullets(firstPos, lastPos, bullets,
                  new ArrayList<Sprite>(unmodifiableSprites));
         }
         while(isWaiting) {
            LockSupport.park();
         }
         Collections.sort(toRemove);
         Helper.removeAll(bullets, toRemove);
         toRemove.clear();
         double toReturn = moneyEarnt;
         moneyEarnt = 0;
         return (long)toReturn;
      }
      
      private synchronized void informFinished(int threadNumber, double moneyEarnt,
            List<Integer> toRemove) {
         this.moneyEarnt += moneyEarnt;
         this.toRemove.addAll(toRemove);
         isThreadRunning[threadNumber] = false;
         for(boolean b : isThreadRunning) {
            if(b) {
               return;
            }
         }
         isWaiting = false;
         LockSupport.unpark(this);
      }
      
      private long tickBulletsSingleThread(List<Sprite> unmodifiableSprites) {
         double moneyEarnt = 0;
         List<Integer> toRemove = new ArrayList<Integer>();
         for(int i = 0; i < bullets.size(); i++) {
            double money = bullets.get(i).tick(unmodifiableSprites);
            if(money >= 0) {
               moneyEarnt += money;
               toRemove.add(i);
            }
         }
         Helper.removeAll(bullets, toRemove);
         return (long)moneyEarnt;
      }
      
      private class BulletTickThread extends Thread {
         
         private final int threadNumber;
         private int firstPos, lastPos;
         private List<Bullet> bulletsToTick;
         private List<Sprite> sprites;
         private boolean doTick;
         
         public BulletTickThread(int number) {
            super();
            this.threadNumber = number;
         }
         
         public void tickBullets(int firstPos, int lastPos, List<Bullet> bulletsToTick,
               List<Sprite> sprites) {
            this.firstPos = firstPos;
            this.lastPos = lastPos;
            this.bulletsToTick = bulletsToTick;
            this.sprites = sprites;
            doTick = true;
            LockSupport.unpark(this);
         }
         
         @Override
         public void run() {
            while(true) {
               if(doTick) {
                  double moneyEarnt = 0;
                  List<Integer> toRemove = new ArrayList<Integer>();
                  for(int i = firstPos; i < lastPos; i++) {
                     double money = bulletsToTick.get(i).tick(sprites);
                     if(money >= 0) {
                        moneyEarnt += money;
                        toRemove.add(i);
                     }
                  }
                  doTick = false;
                  informFinished(threadNumber, moneyEarnt, toRemove);
               }
               LockSupport.park();
            }
         }
      }
   }

}
