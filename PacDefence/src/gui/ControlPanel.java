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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import towers.BasicTower;
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
import towers.SlowFactorTower;
import towers.SlowLengthTower;
import towers.Tower;
import towers.WaveTower;
import towers.WeakenTower;
import towers.ZapperTower;
import towers.Tower.Attribute;


@SuppressWarnings("serial")
public class ControlPanel extends JPanel {
   
   //private static final int BASE_TOWER_PRICE = 1000;
   
   private final BufferedImage backgroundImage = ImageHelper.makeImage("control_panel",
         "blue_lava.jpg");
   private int level = 0;
   // These labels are in the top stats box
   private MyJLabel levelLabel, moneyLabel, livesLabel, interestLabel, upgradesLabel;
   private final Map<OverlayButton, Tower> towerTypes = new HashMap<OverlayButton, Tower>();
   private OverlayButton damageUpgrade, rangeUpgrade, rateUpgrade, speedUpgrade,
         specialUpgrade, livesUpgrade, interestUpgrade, moneyUpgrade;
   private Map<Attribute, Integer> upgradesSoFar =
         new EnumMap<Attribute, Integer>(Attribute.class);
   // These labels are in the tower info box
   private MyJLabel towerNameLabel, towerLevelLabel, damageDealtLabel, killsLabel;
   // These are in the current tower stats box
   private final List<TowerStat> towerStats = new ArrayList<TowerStat>();
   // These labels are in the level stats box
   private MyJLabel numSpritesLabel, avgHPLabel;
   private MyJLabel currentCostStringLabel, currentCostLabel;
   private final ImageButton start = new ImageButton("start", ".png");
   private final GameMap map;
   private Tower selectedTower, buildingTower, rolloverTower, hoverOverTower;
   private final Color defaultTextColour = Color.YELLOW;
   private final float defaultTextSize = 12F;
   // This is the initial money
   private long money = 4000;
   private int lives = 25;
   private int livesLostOnThisLevel;
   private double interestRate = 1.02;
   private int endLevelUpgradesLeft = 0;
   private static final int upgradeLives = 5;
   private static final int upgradeMoney = 1000;
   private static final float upgradeInterest = 0.0025f;

   public ControlPanel(int width, int height, GameMap map) {
      this.map = map;
      setPreferredSize(new Dimension(width, height));      
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      // Reflective method to set up some fields
      setUpJLabels();
      // Creates each of the sub panels of this panel
      setUpTopStatsBox();
      setUpNewTowers();
      setUpEndLevelUpgrades();
      setUpCurrentTowerInfo();
      setUpCurrentTowerStats();
      setUpLevelStats();
      setUpCurrentCost();
      setUpStartButton();
      // Updates it all
      updateAll();      
      map.setControlPanel(this);
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
   
   private void multiplyMoney(double factor) {
      money *= factor;
   }
   
   private void decreaseMoney(long amount) {
      money -= amount;
   }
   
   private int getNextTowerCost(Class<? extends Tower> towerType) {
      int num = 0;
      for(Tower t : map.getTowers()) {
         if(t.getClass() == towerType) {
            num++;
         }
      }
      return Formulae.towerCost(map.getTowers().size(), num);
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
      avgHPLabel.setText(Formulae.hp(level));
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
      } else if(hoverOverTower != null) {
         t = hoverOverTower;
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
      } else {
         towerNameLabel.setText(t.getName() + " Tower");
         towerLevelLabel.setText("Level: " + t.getExperienceLevel());
         killsLabel.setText("Kills: " + t.getKills() + " (" + t.getKillsForUpgrade() + ")");
         damageDealtLabel.setText("Dmg: " + t.getDamageDealt() + " (" +
               t.getDamageDealtForUpgrade() + ")");
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
   
   private void processUpgradeButtonPressed(TowerUpgradeButton b) {
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
   
   private void processUpgradeButtonChanged(TowerUpgradeButton b) {
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
      for(TowerStat ts : towerStats) {
         ts.setText(t);
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
      for(TowerStat ts : towerStats) {
         ts.enableButton(enable);
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

   // -----------------------------------------------------
   // The remaining methods set up the gui bit on the side
   // -----------------------------------------------------

   private void setUpTopStatsBox() {
      float textSize = defaultTextSize + 1;
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 20));
      panel.setOpaque(false);
      panel.add(createLevelLabel(defaultTextColour));
      panel.add(createLeftRightPanel("Money", textSize, defaultTextColour, moneyLabel));
      panel.add(createLeftRightPanel("Lives", textSize, defaultTextColour, livesLabel));
      panel.add(createLeftRightPanel("Interest", textSize, defaultTextColour, interestLabel));
      panel.add(createLeftRightPanel("Bonuses", textSize, defaultTextColour, upgradesLabel));
      updateInterestLabel();

      add(panel);
   }

   private JPanel createLevelLabel(Color textColour) {
      levelLabel.setForeground(textColour);
      levelLabel.setHorizontalAlignment(JLabel.CENTER);
      levelLabel.setFontSize(25);
      updateLevelStats();
      return createBorderLayedOutJPanel(levelLabel, BorderLayout.CENTER);
   }

   private JPanel createLeftRightPanel(String text, float textSize, Color textColour,
         MyJLabel label) {
      MyJLabel leftText = createJLabel(text, textSize, textColour);
      return createLeftRightPanel(leftText, textSize, textColour, label);
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
      return createLeftRightPanel(c, label);
   }
   
   private JPanel createLeftRightPanel(Component left, Component right) {
      JPanel panel = createBorderLayedOutJPanel();
      panel.add(left, BorderLayout.WEST);
      panel.add(right, BorderLayout.EAST);
      return panel;
   }

   private void setUpNewTowers() {
      JPanel panel = new JPanel();
      panel.setOpaque(false);
      panel.setBorder(BorderFactory.createEmptyBorder(7, 0, 5, 0));
      int numX = 6;
      int numY = 3;
      GridLayout gl = new GridLayout(numY, numX);
      gl.setVgap(2);
      panel.setLayout(gl);
      List<Tower> towers = getTowerImplementations();
      for (int a = 0; a < numY * numX; a++) {
         Tower t = new BasicTower();
         if(a < towers.size()) {
            t = towers.get(a);
         }
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
      towerTypes.add(new SlowFactorTower());
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
      // TODO add tower implementations as I code them
      return towerTypes;
   }
   
   private void setUpEndLevelUpgrades() {
      setEndLevelUpgradeButtons();
      JPanel panel = new JPanel();
      panel.setOpaque(false);
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
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
         panel.add(b);
      }
      enableEndLevelUpgradeButtons(endLevelUpgradesLeft > 0);
      add(panel);
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
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
      panel.setOpaque(false);
      for(Attribute a : Attribute.values()) {
         TowerUpgradeButton b = createTowerUpgradeButton(defaultTextColour, defaultTextSize);
         MyJLabel l = new MyJLabel();
         l.setFontSize(defaultTextSize);
         l.setForeground(defaultTextColour);
         towerStats.add(new TowerStat(b, l, a));
         int emptyWidth = 1;
         panel.add(createLeftRightPanel(createWrapperPanel(b, emptyWidth),
               createWrapperPanel(l, emptyWidth)));
      }
      add(panel);
   }
   
   private TowerUpgradeButton createTowerUpgradeButton(Color textColour, float textSize) {
      TowerUpgradeButton b = new TowerUpgradeButton(textColour, textSize);
      b.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            processUpgradeButtonPressed((TowerUpgradeButton)e.getSource());
         }
      });
      b.addChangeListener(new ChangeListener(){
         public void stateChanged(ChangeEvent e) {
            processUpgradeButtonChanged((TowerUpgradeButton)e.getSource());
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
      JPanel panel = createBoxLayedOutJPanel(BoxLayout.Y_AXIS);
      panel.add(createWrapperPanel(towerNameLabel));
      JPanel p = createLeftRightPanel(towerLevelLabel, killsLabel);
      p.setBorder(BorderFactory.createEmptyBorder(-1, 10, -1, 10));
      panel.add(p);
      panel.add(createWrapperPanel(damageDealtLabel));
      add(panel);
   }
   
   private void setUpLevelStats() {
      float textSize = defaultTextSize;
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
      panel.setOpaque(false);
      panel.add(createLeftRightPanel("Number", textSize, defaultTextColour, numSpritesLabel));
      panel.add(createLeftRightPanel("Average HP", textSize, defaultTextColour, avgHPLabel));
      
      add(panel);
   }

   private void setUpStartButton() {
      start.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            startPressed();
         }
      });
      add(createWrapperPanel(start));
   }
   
   private void setUpCurrentCost() {
      JPanel panel = createBorderLayedOutJPanel();
      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
      panel.setOpaque(false);
      panel.add(currentCostStringLabel, BorderLayout.WEST);
      panel.add(currentCostLabel, BorderLayout.EAST);
      add(panel);
   }
   
   private JPanel createJPanel() {
      JPanel panel = new JPanel();
      panel.setOpaque(false);   
      return panel;
   }
   
   private JPanel createBoxLayedOutJPanel(int alignment) {
      JPanel panel = createJPanel();
      panel.setLayout(new BoxLayout(panel, alignment));
      return panel;
   }

   private JPanel createBorderLayedOutJPanel() {
      JPanel panel = createJPanel();
      panel.setLayout(new BorderLayout());
      return panel;
   }

   private JPanel createBorderLayedOutJPanel(Component comp, String pos) {
      JPanel panel = createBorderLayedOutJPanel();
      panel.add(comp, pos);
      return panel;
   }

   /**
    * Creates an empty border with specified width.
    */
   private Border createEmptyBorder(int width) {
      return BorderFactory.createEmptyBorder(width, width, width, width);
   }
   
   private JPanel createWrapperPanel(JComponent c) {
      JPanel panel = new JPanel();
      panel.add(c);
      panel.setOpaque(false);
      return panel;
   }

   /**
    * Creates a panel that wraps this component and has a empty border around
    * it.
    */
   private JPanel createWrapperPanel(JComponent c, int borderWidth) {
      JPanel panel = new JPanel();
      panel.add(c);
      panel.setOpaque(false);
      panel.setBorder(createEmptyBorder(borderWidth));
      return panel;
   }
   
   private class MyJLabel extends JLabel {
      // This class just makes it easier to set the values of the text fields as I
      // can just pass a number instead of changing them to Strings all the time
      public MyJLabel() {
         super();
      }
      
      public MyJLabel(String text) {
         super(text);
      }
      
      public void setFontSize(float size) {
         setFont(getFont().deriveFont(size));
      }
      
      public void setText(int i) {
         setText(String.valueOf(i));
      }
      
      public void setText(long l) {
         setText(String.valueOf(l));
      }
      
      public void setText(double d){
         setText(String.valueOf(d));
      }      
   }

   private static class TowerStat {
      
      private static final Map<TowerUpgradeButton, Attribute> buttonAttributeMap =
            new HashMap<TowerUpgradeButton, Attribute>();
      private final TowerUpgradeButton button;
      private final JLabel label;
      private final Attribute attrib;
      
      private TowerStat(TowerUpgradeButton b, JLabel l, Attribute a) {
         if(buttonAttributeMap.put(b, a) != null) {
            throw new IllegalArgumentException("This button has already been used.");
         }
         button = b;
         label = l;
         attrib = a;
      }
      
      private static Attribute getButtonsAttribute(TowerUpgradeButton b) {
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
      
      protected TowerUpgradeButton getButton() {
         return button;
      }
      
      protected JLabel getLabel() {
         return label;
      }
      
      protected Attribute getAttrib() {
         return attrib;
      }
   }
   
}
