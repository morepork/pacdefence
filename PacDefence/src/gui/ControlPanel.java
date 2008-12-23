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

package gui;

import images.ImageHelper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalButtonUI;

import sprites.Sprite;
import towers.AidTower;
import towers.BeamTower;
import towers.BomberTower;
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


@SuppressWarnings("serial")
public class ControlPanel extends JPanel {
   
   //private static final int BASE_TOWER_PRICE = 1000;
   
   private final BufferedImage backgroundImage;
   private int level;
   // These labels are in the top stats box
   private MyJLabel levelLabel, moneyLabel, livesLabel, interestLabel, upgradesLabel;
   private final Map<OverlayButton, Tower> towerTypes = new HashMap<OverlayButton, Tower>();
   private OverlayButton damageUpgrade, rangeUpgrade, rateUpgrade, speedUpgrade,
         specialUpgrade, livesUpgrade, interestUpgrade, moneyUpgrade;
   private final Map<Attribute, Integer> upgradesSoFar =
         new EnumMap<Attribute, Integer>(Attribute.class);
   // These labels and the sell button below are in the tower info box
   private MyJLabel towerNameLabel, towerLevelLabel, damageDealtLabel, killsLabel;
   private List<Wrapper<String, Comparator<Sprite>>> comparators = createComparators();
   private MyJLabel targetLabel;
   private JButton targetButton = createTargetButton();
   private final JButton sellButton = createSellButton();
   // These are in the current tower stats box
   private final List<TowerStat> towerStats = new ArrayList<TowerStat>();
   // These labels are in the level stats box
   private MyJLabel numSpritesLabel, timeBetweenSpritesLabel, hpLabel;
   private MyJLabel currentCostStringLabel, currentCostLabel;
   private final ImageButton start = new ImageButton("start", ".png", true);
   private final GameMapPanel map;
   private Tower selectedTower, buildingTower, rolloverTower, hoverOverTower;
   private static final Color defaultTextColour = Color.YELLOW;
   private static final float defaultTextSize = 12F;
   // This is the initial money
   private long money;
   private int lives;
   private int livesLostOnThisLevel;
   private double interestRate;
   private int endLevelUpgradesLeft;
   private static final int upgradeLives = 5;
   private static final int upgradeMoney = 1000;
   private static final double upgradeInterest = 0.005;

   public ControlPanel(int width, int height, BufferedImage backgroundImage, GameMapPanel map,
         ActionListener toTitle) {
      this.backgroundImage = ImageHelper.resize(backgroundImage, width, height);
      // Reflective method to set up the MyJLabels
      setUpJLabels();
      // I know this is terrible coupling, I plan to fix it later
      map.setControlPanel(this);
      this.map = map;
      setPreferredSize(new Dimension(width, height));      
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      // Sets the stats to their starting values
      setStartingStats();
      // Creates each of the sub panels of this panel
      setUpTopStatsBox();
      setUpNewTowers();
      setUpEndLevelUpgrades();
      setUpCurrentTowerInfo();
      setUpCurrentTowerStats();
      setUpLevelStats();
      setUpCurrentCost();
      setUpStartButton();
      setUpBottomButtons(toTitle);
      // Updates it all
      updateAll();
   }
   
   public void endLevel() {
      endLevelUpgradesLeft++;
      enableEndLevelUpgradeButtons(true);
      updateEndLevelUpgradesLabel();
      long moneyBefore = money; 
      multiplyMoney(interestRate);
      long interest = money - moneyBefore;
      int levelEndBonus = Formulae.levelEndBonus(level);
      int noEnemiesThroughBonus = 0;
      String text = "Level " + level + " finished. " + interest + " interest earnt. " +
            levelEndBonus + " for finishing the level.";
      if(livesLostOnThisLevel == 0) {
         noEnemiesThroughBonus = Formulae.noEnemiesThroughBonus(level);
         text += " " + noEnemiesThroughBonus + " bonus for losing no lives.";
      }
      increaseMoney(levelEndBonus + noEnemiesThroughBonus);
      updateMoneyLabel();
      map.displayText(text);
      start.setEnabled(true);
   }

   @Override
   public void paintComponent(Graphics g) {
      g.drawImage(backgroundImage, 0, 0, null);
   }
   
   public void selectTower(Tower t) {
      selectedTower = t;
      updateStats();
      clearBuildingTower();
      hoverOverTower = null;
   }
   
   public void deselectTower() {
      if(selectedTower != null) {
         selectedTower = null;
         updateStats();
      }
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
   
   public void clearBuildingTower() {
      setBuildingTower(null);
   }
   
   public void hoverOverTower(Tower t) {
      if(selectedTower == null && buildingTower == null) {
         hoverOverTower = t;
         updateStats();
      }
   }
   
   public int getLevel() {
      return level;
   }
   
   public void setNumberLeft(int number) {
      numSpritesLabel.setText(number);
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
   }
   
   private void multiplyMoney(double factor) {
      money *= factor;
   }
   
   private void decreaseMoney(long amount) {
      money -= amount;
   }
   
   private int getNextTowerCost(Class<? extends Tower> towerType) {
      return Formulae.towerCost(map.getTowers().size(), numTowersOfType(towerType));
   }
   
   private int numTowersOfType(Class<? extends Tower> towerType) {
      int num = 0;
      for(Tower t : map.getTowers()) {
         if(t.getClass() == towerType) {
            num++;
         }
      }
      return num;
   }
   
   private void updateAll() {
      // Deliberately doesn't upgrade level stats to avoid confusion
      updateEndLevelUpgradesLabel();
      updateInterestLabel();
      updateLivesLabel();
      updateMoneyLabel();
      updateStats();
   }
   
   private void updateMoneyLabel() {
      moneyLabel.setText(money);
   }
   
   private void updateLivesLabel() {
      livesLabel.setText(lives);
   }
   
   private void updateLevelStats() {
      int level = this.level;
      if(level == 0) {
         level = 1;
      }
      levelLabel.setText("Level " + level);
      numSpritesLabel.setText(Formulae.numSprites(level));
      long hp = Formulae.hp(level);
      hpLabel.setText((long)(0.5 * hp) + " - " + hp * 2);
      timeBetweenSpritesLabel.setText("0 - " + Helper.format(Formulae.
            ticksBetweenAddSprite(level) * 2 / GameMapPanel.CLOCK_TICKS_PER_SECOND, 2) + "s");
   }
   
   private void updateInterestLabel() {
      interestLabel.setText(Helper.format(((interestRate - 1) * 100), 2) + "%");
   }
   
   private void updateEndLevelUpgradesLabel() {
      upgradesLabel.setText(endLevelUpgradesLeft);
   }
   
   private void updateCurrentCostLabel(String description, long cost) {
      updateCurrentCostLabel(description, String.valueOf(cost));
   }
   
   private void updateCurrentCostLabel(String description, String cost) {
      currentCostStringLabel.setText(description);
      currentCostLabel.setText(cost);
   }
   
   private void updateStats() {
      Tower t;
      if(rolloverTower != null) {
         t = rolloverTower;
         updateCurrentCostLabel(rolloverTower.getName() + " Tower", getNextTowerCost(t.getClass()));
      } else if(hoverOverTower != null) {
         t = hoverOverTower;
      } else if(buildingTower != null) {
         t = buildingTower;
         updateCurrentCostLabel(buildingTower.getName() + " Tower", getNextTowerCost(t.getClass()));
      } else {
         t = selectedTower;
      }
      setStats(t);
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
      setCurrentTowerInfo(t);
   }
   
   private void setCurrentTowerInfo(Tower t) {
      if(t == null) {
         // Don't use an empty string here so as not to collapse the label
         towerNameLabel.setText(" ");
         towerLevelLabel.setText(" ");
         killsLabel.setText(" ");
         damageDealtLabel.setText(" ");
         sellButton.setEnabled(false);
         targetLabel.setText(" ");
         targetButton.setEnabled(false);
      } else {
         towerNameLabel.setText(t.getName() + " Tower");
         towerLevelLabel.setText("Level: " + t.getExperienceLevel());
         killsLabel.setText("Kills: " + t.getKills() + " (" + t.getKillsForUpgrade() + ")");
         damageDealtLabel.setText("Dmg: " + t.getDamageDealt() + " (" +
               t.getDamageDealtForUpgrade() + ")");
         Comparator<Sprite> c = t.getSpriteComparator();
         if(c != null) {
            sellButton.setEnabled(true);
            targetLabel.setText("Target");
            targetButton.setEnabled(true);
            targetButton.setText(getNameOfComparator(c));
         }
      }
   }
   
   private void setUpJLabels() {
      for (Field f : getClass().getDeclaredFields()) {
         if (f.getType().equals(MyJLabel.class)) {
            try {
               MyJLabel j = new MyJLabel();
               // This means it's actually drawn
               j.setText(" ");
               j.setForeground(defaultTextColour);
               j.setFontSize(defaultTextSize);
               f.set(this, j);
            } catch(IllegalAccessException e) {
               // This shouldn't ever be thrown
               System.err.println(e);
            }
         }
      }
   }
   
   private void processUpgradeButtonPressed(JButton b) {
      Tower.Attribute a = TowerStat.getButtonsAttribute(b);
      long cost = 0;
      if(selectedTower == null) {
         List<Tower> towers = map.getTowers();
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
   
   private void processUpgradeButtonChanged(JButton b) {
      if(!checkIfMovedOff(b)) {
         Tower.Attribute a = TowerStat.getButtonsAttribute(b);
         String description = a.toString() + " Upgrade";
         long cost;
         if(selectedTower == null) {
            description += " (all)";
            cost = costToUpgradeAllTowers(a);
         } else {
            cost = Formulae.upgradeCost(selectedTower.getAttributeLevel(a));
         }
         updateCurrentCostLabel(description, cost);
      }
   }
   
   private long costToUpgradeAllTowers(Attribute a) {
      return costToUpgradeTowers(a, map.getTowers());
   }
   
   private long costToUpgradeTowers(Attribute a, List<Tower> towers) {
      long cost = 0;
      for(Tower t : towers) {
         cost += Formulae.upgradeCost(t.getAttributeLevel(a));
      }
      return cost;
   }
   
   private void setStats(Tower t) {
      for(int i = 0; i < towerStats.size(); i++) {
         towerStats.get(i).setText(t);
      }
   }
   
   private void setBuildingTower(Tower t) {
      buildingTower = t;
      enableTowerStatsButtons(t == null);
      enableEndLevelUpgradeButtons(t == null);
   }
   
   private void startPressed() {
      if (map.start(level + 1)) {
         level++;
         map.removeText();
         updateLevelStats();
         livesLostOnThisLevel = 0;
      }
   }
   
   private void enableTowerStatsButtons(boolean enable) {
      for(int i = 0; i < towerStats.size(); i++) {
         towerStats.get(i).enableButton(enable);
      }
   }
   
   private void processTowerButtonAction(OverlayButton b) {
      Tower t = towerTypes.get(b);
      if(money >= getNextTowerCost(t.getClass())) {
         if(map.towerButtonPressed(t)) {
            setBuildingTower(t);
            updateStats();
         }
      }
   }
   
   private void processTowerButtonChangeEvent(OverlayButton b) {
      rolloverTower = towerTypes.get(b);
      if(!checkIfMovedOff(b)) {
         updateStats();
      }
   }
   
   private boolean checkIfMovedOff(JButton b) {
      if(b.getMousePosition() == null) {
         // This means the cursor isn't over the button, so the mouse was moved off it
         rolloverTower = null;
         updateCurrentCostLabel(" ", " ");
         updateStats();
         return true;
      } else {
         return false;
      }
   }
   
   private void processEndLevelUpgradeButtonPress(OverlayButton b) {
      if(endLevelUpgradesLeft > 0) {
         endLevelUpgradesLeft--;
         Attribute upgradeAttrib = getAttributeFromButton(b);
         if(b.equals(livesUpgrade)) {
            lives += upgradeLives;
         } else if(b.equals(interestUpgrade)) {
            interestRate += upgradeInterest;
         } else if(b.equals(moneyUpgrade)) {
            increaseMoney(upgradeMoney);
         }
         if(upgradeAttrib != null) {
            int nextValue = 1;
            if(upgradesSoFar.containsKey(upgradeAttrib)) {
               nextValue = upgradesSoFar.get(upgradeAttrib) + 1;
            }
            if(nextValue / 2 == (nextValue + 1) / 2) {
               // It is even, i.e every second time
               for(Tower t : towerTypes.values()) {
                  // So that the upgrades are shown when you are building
                  // a new tower
                  t.raiseAttributeLevel(upgradeAttrib, false);
               }
            }
            upgradesSoFar.put(upgradeAttrib, nextValue);
            map.upgradeAll(upgradeAttrib);
         }
         updateAll();
         if(endLevelUpgradesLeft <= 0) {
            enableEndLevelUpgradeButtons(true);
         }
      }
   }
   
   private void processEndLevelUpgradeButtonChanged(OverlayButton b) {
      if(!checkIfMovedOff(b)) {
         String description = " ";
         String cost = "Free, " + endLevelUpgradesLeft +" left";
         Attribute a = getAttributeFromButton(b);
         if(a != null) {
            description = a.toString() + " Upgrade (all)";
         } else if(b.equals(livesUpgrade)) {
            description = upgradeLives + " bonus lives";
         } else if(b.equals(interestUpgrade)) {
            description = "+" + upgradeInterest * 100 + "% interest rate";
         } else if(b.equals(moneyUpgrade)) {
            description = upgradeMoney + " bonus money";
         }
         updateCurrentCostLabel(description, cost);
      }
   }
   
   private void enableEndLevelUpgradeButtons(boolean enable) {
      if(endLevelUpgradesLeft <= 0) {
         enable = false;
      }
      JButton[] buttons = new JButton[]{damageUpgrade, rangeUpgrade, rateUpgrade, speedUpgrade,
               specialUpgrade, livesUpgrade, interestUpgrade, moneyUpgrade};
      for(JButton b : buttons) {
         b.setEnabled(enable);
      }
   }
   
   private void processSellButtonPressed() {
      if(selectedTower != null) {
         map.removeTower(selectedTower);
         increaseMoney(sellValue(selectedTower));
         selectedTower = null;
      }
   }
   
   private void processSellButtonChanged() {
      if(selectedTower != null && !checkIfMovedOff(sellButton)) {
         updateCurrentCostLabel("Sell " + selectedTower.getName() + " Tower",
               sellValue(selectedTower));
      }
   }
   
   private long sellValue(Tower t) {
      return Formulae.sellValue(t, map.getTowers().size(), numTowersOfType(t.getClass()));
   }
   
   private void restart() {
      setStartingStats();
      for(OverlayButton b : towerTypes.keySet()) {
         towerTypes.put(b, towerTypes.get(b).constructNew(new Point(), null));
      }
      updateAll();
      map.restart();
   }
   
   private void processTargetButtonPressed(JButton b) {
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
   
   private String getNameOfComparator(Comparator<Sprite> c) {
      for(int i = 0; i < comparators.size(); i++) {
         Wrapper<String, Comparator<Sprite>> w = comparators.get(i);
         if(w.getT2().equals(c)) {
            return w.getT1();
         }
      }
      throw new RuntimeException("Comparator not found");
   }

   // -----------------------------------------------------
   // The remaining methods set up the gui bit on the side
   // -----------------------------------------------------

   private void setUpTopStatsBox() {
      float textSize = defaultTextSize + 1;
      Box box = Box.createVerticalBox();
      box.setBorder(BorderFactory.createEmptyBorder(2, 20, 0, 20));
      box.setOpaque(false);
      box.add(createLevelLabel(defaultTextColour));
      box.add(createLeftRightPanel("Money", textSize, defaultTextColour, moneyLabel));
      box.add(createLeftRightPanel("Lives", textSize, defaultTextColour, livesLabel));
      box.add(createLeftRightPanel("Interest", textSize, defaultTextColour, interestLabel));
      box.add(createLeftRightPanel("Bonuses", textSize, defaultTextColour, upgradesLabel));
      updateInterestLabel();
      add(box);
   }

   private JPanel createLevelLabel(Color textColour) {
      levelLabel.setForeground(textColour);
      levelLabel.setHorizontalAlignment(JLabel.CENTER);
      levelLabel.setFontSize(25);
      updateLevelStats();
      return SwingHelper.createBorderLayedOutWrapperPanel(levelLabel, BorderLayout.CENTER);
   }

   private JPanel createLeftRightPanel(String text, float textSize, Color textColour,
         MyJLabel label) {
      MyJLabel leftText = createJLabel(text, textSize, textColour);
      return createLeftRightPanel(leftText, textSize, textColour, label);
   }
   
   private MyJLabel createJLabel(String text) {
      return createJLabel(text, defaultTextSize, defaultTextColour);
   }
   
   private MyJLabel createJLabel(String text, float textSize, Color textColour) {
      MyJLabel label = new MyJLabel(text);;
      label.setFontSize(textSize);
      label.setForeground(textColour);
      return label;
   }
   
   private JPanel createLeftRightPanel(Component c, float textSize, Color textColour,
         MyJLabel label) {
      label.setForeground(textColour);
      label.setFontSize(textSize);
      return SwingHelper.createLeftRightPanel(c, label);
   }

   private void setUpNewTowers() {
      JPanel panel = new JPanel();
      panel.setOpaque(false);
      panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 4, 0));
      int numX = 6;
      int numY = 3;
      int total = numX * numY;
      GridLayout gl = new GridLayout(numY, numX);
      gl.setVgap(2);
      panel.setLayout(gl);
      List<Tower> towers = getTowerImplementations();
      assert towers.size() == total : "Number of tower implementations is different to number" +
            " of buttons.";
      for (int a = 0; a < numY * numX; a++) {
         Tower t = towers.get(a);
         OverlayButton button = OverlayButton.makeTowerButton(t.getButtonImage());
         towerTypes.put(button, t);
         button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
               processTowerButtonAction((OverlayButton) e.getSource());
            }
         });
         button.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
               processTowerButtonChangeEvent((OverlayButton) e.getSource());
            }
         });
         panel.add(button);
      }
      add(panel);
   }
   
   private Attribute getAttributeFromButton(OverlayButton b) {
      Attribute a = null;
      if(b.equals(damageUpgrade)) {
         a = Attribute.Damage;
      } else if(b.equals(rangeUpgrade)) {
         a = Attribute.Range;
      } else if(b.equals(rateUpgrade)) {
         a = Attribute.Rate;
      } else if(b.equals(speedUpgrade)) {
         a = Attribute.Speed;
      } else if(b.equals(specialUpgrade)) {
         a = Attribute.Special;
      }
      return a;
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
   
   private void setUpEndLevelUpgrades() {
      setEndLevelUpgradeButtons();
      Box box = Box.createHorizontalBox();
      box.setOpaque(false);
      OverlayButton[] buttons = new OverlayButton[]{damageUpgrade, rangeUpgrade, rateUpgrade,
            speedUpgrade, specialUpgrade, livesUpgrade, interestUpgrade, moneyUpgrade};
      for(OverlayButton b : buttons) {
         b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
               processEndLevelUpgradeButtonPress((OverlayButton) e.getSource());
            }
         });
         b.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
               processEndLevelUpgradeButtonChanged((OverlayButton) e.getSource());
            }
         });
         box.add(b);
      }
      enableEndLevelUpgradeButtons(endLevelUpgradesLeft > 0);
      add(box);
   }
   
   private void setEndLevelUpgradeButtons() {
      damageUpgrade = OverlayButton.makeUpgradeButton("DamageUpgrade.png");
      rangeUpgrade = OverlayButton.makeUpgradeButton("RangeUpgrade.png");
      rateUpgrade = OverlayButton.makeUpgradeButton("RateUpgrade.png");
      speedUpgrade = OverlayButton.makeUpgradeButton("SpeedUpgrade.png");
      specialUpgrade = OverlayButton.makeUpgradeButton("SpecialUpgrade.png");
      livesUpgrade = OverlayButton.makeUpgradeButton("LivesUpgrade.png");
      interestUpgrade = OverlayButton.makeUpgradeButton("InterestUpgrade.png");
      moneyUpgrade = OverlayButton.makeUpgradeButton("MoneyUpgrade.png");
   }

   private void setUpCurrentTowerStats() {
      Box box = Box.createVerticalBox();
      box.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
      box.setOpaque(false);
      for(int i = 0; i < Attribute.values().length; i++) {
         if(i != 0) {
            box.add(Box.createRigidArea(new Dimension(0, 2)));
         }
         JButton b = createTowerUpgradeButton(defaultTextColour, defaultTextSize);
         MyJLabel l = new MyJLabel();
         l.setFontSize(defaultTextSize);
         l.setForeground(defaultTextColour);
         towerStats.add(new TowerStat(b, l, Attribute.values()[i]));
         box.add(SwingHelper.createLeftRightPanel(b, l));
      }
      add(box);
   }
   
   private JButton createTowerUpgradeButton(final Color textColour, float textSize) {
      JButton b = new JButton();
      // Hack to set the disabled text colour. If you change this UI do the same
      // for the target button so they're consistent.
      b.setUI(new MetalButtonUI(){
         @Override
         public Color getDisabledTextColor() {
            return textColour;
         }
      });
      b.setFont(b.getFont().deriveFont(textSize));
      b.setForeground(textColour);
      b.setOpaque(false);
      b.setContentAreaFilled(false);
      // Hack to make the buttons slightly smaller
      b.setBorder(BorderFactory.createCompoundBorder(b.getBorder(),
            BorderFactory.createEmptyBorder(-2, -5, -2, -5)));
      b.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            processUpgradeButtonPressed((JButton)e.getSource());
         }
      });
      b.addChangeListener(new ChangeListener(){
         public void stateChanged(ChangeEvent e) {
            processUpgradeButtonChanged((JButton)e.getSource());
         }
      });
      return b;
   }
   
   private void setUpCurrentTowerInfo() {
      MyJLabel[] labels = new MyJLabel[]{towerNameLabel, towerLevelLabel, killsLabel,
            damageDealtLabel};
      float textSize = defaultTextSize - 1;
      for(MyJLabel a : labels) {
         a.setFontSize(textSize);
         a.setHorizontalAlignment(JLabel.CENTER);
      }
      towerNameLabel.setFontSize(textSize + 1);
      Box box = Box.createVerticalBox();
      box.add(SwingHelper.createWrapperPanel(towerNameLabel));
      Box centralRow = Box.createHorizontalBox();
      centralRow.add(towerLevelLabel);
      centralRow.add(sellButton);
      centralRow.add(killsLabel);
      box.add(centralRow);
      box.add(SwingHelper.createWrapperPanel(damageDealtLabel));
      targetLabel.setFontSize(textSize);
      targetButton.setFont(targetButton.getFont().deriveFont(textSize));
      JPanel p = SwingHelper.createLeftRightPanel(targetLabel, targetButton);
      p.setBorder(BorderFactory.createEmptyBorder(0, 40, 3, 40));
      box.add(p);
      add(box);
   }
   
   private JButton createTargetButton() {
      JButton b = new JButton(){
         @Override
         public void setEnabled(boolean b) {
            super.setEnabled(b);
            // If the border isn't painted and there is no text it's effectively invisible
            setBorderPainted(b);
            if(!b) {
               setText(" ");
            }
         }
      };
      // Changing this should result in changing the tower upgrade buttons for consistency
      b.setUI(new MetalButtonUI());
      b.setForeground(defaultTextColour);
      b.setOpaque(false);
      b.setContentAreaFilled(false);
      b.setFocusPainted(false);
      // Hack to make the button smaller
      b.setBorder(BorderFactory.createCompoundBorder(b.getBorder(),
            BorderFactory.createEmptyBorder(-10, -5, -10, -5)));
      b.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            processTargetButtonPressed((JButton)e.getSource());
         }
      });
      return b;
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
   
   private void setUpLevelStats() {
      float textSize = defaultTextSize;
      Box box = Box.createVerticalBox();
      box.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
      box.setOpaque(false);
      box.add(createLeftRightPanel("Number Left", textSize, defaultTextColour, numSpritesLabel));
      box.add(createLeftRightPanel("Between Sprites", textSize, defaultTextColour,
            timeBetweenSpritesLabel));
      box.add(createLeftRightPanel("HP", textSize, defaultTextColour, hpLabel));      
      add(box);
   }

   private void setUpStartButton() {
      start.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            startPressed();
         }
      });
      add(SwingHelper.createWrapperPanel(start));
   }
   
   private void setUpCurrentCost() {
      JPanel panel = SwingHelper.createBorderLayedOutJPanel();
      panel.setBorder(BorderFactory.createEmptyBorder(4, 5, 0, 5));
      panel.add(currentCostStringLabel, BorderLayout.WEST);
      panel.add(currentCostLabel, BorderLayout.EAST);
      add(panel);
   }
   
   private JButton createSellButton() {
      JButton b = new OverlayButton("buttons", "sell.png");
      // So when disabled it is blank but keeps its size
      b.setDisabledIcon(new ImageIcon());
      b.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            processSellButtonPressed();
         }
      });
      b.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
            processSellButtonChanged();
         }
      });
      return b;
   }
   
   private void setUpBottomButtons(ActionListener toTitle) {
      JPanel panel = SwingHelper.createBorderLayedOutJPanel();
      JButton title = new OverlayButton("buttons", "title.png");
      title.addActionListener(toTitle);
      JButton restart = new OverlayButton("buttons", "restart.png");
      restart.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            restart();
         }
      });
      panel.add(title, BorderLayout.WEST);
      panel.add(restart, BorderLayout.EAST);
      panel.setBorder(BorderFactory.createEmptyBorder(0, 13, 1, 13));
      add(panel);
   }

   private static class TowerStat {
      
      private static final Map<JButton, Attribute> buttonAttributeMap =
            new HashMap<JButton, Attribute>();
      private final JButton button;
      private final JLabel label;
      private final Attribute attrib;
      
      private TowerStat(JButton b, JLabel l, Attribute a) {
         if(buttonAttributeMap.put(b, a) != null) {
            throw new IllegalArgumentException("This button has already been used.");
         }
         button = b;
         label = l;
         attrib = a;
      }
      
      private static Attribute getButtonsAttribute(JButton b) {
         return buttonAttributeMap.get(b);
      }
      
      private void setText(Tower t) {
         if(t == null) {
            button.setText(attrib.toString());
            label.setText(" ");
         } else {
            button.setText(t.getStatName(attrib));
            label.setText(t.getStat(attrib));
         }
      }
      
      private void enableButton(boolean b) {
         button.setEnabled(b);
      }
   }
   
}
