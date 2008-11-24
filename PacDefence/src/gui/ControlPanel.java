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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
import towers.CircleTower;
import towers.JumpingTower;
import towers.MultiShotTower;
import towers.PiercerTower;
import towers.ScatterTower;
import towers.SlowFactorTower;
import towers.SlowLengthTower;
import towers.Tower;
import towers.Tower.Attribute;


@SuppressWarnings("serial")
public class ControlPanel extends JPanel {
   
   //private static final int BASE_TOWER_PRICE = 1000;
   
   private static final DecimalFormat ONE_DP = new DecimalFormat("#0.0");
   private static final DecimalFormat ZERO_DP = new DecimalFormat("#0");

   private final BufferedImage backgroundImage = ImageHelper.makeImage("control_panel",
         "blue_lava.jpg");;
   private int level = 0;
   // These labels are in the top stats box
   private MyJLabel levelLabel, moneyLabel, livesLabel, interestLabel, upgradesLabel;
   private final Map<OverlayButton, Tower> towerTypes = new HashMap<OverlayButton, Tower>();
   private OverlayButton damageUpgrade, rangeUpgrade, rateUpgrade, speedUpgrade,
         specialUpgrade, livesUpgrade, interestUpgrade, moneyUpgrade;
   // These labels are in the current tower stats box
   private MyJLabel damageLabel, rangeLabel, rateLabel, speedLabel, specialLabel;
   private MyJLabel damageDealtLabel, killsLabel;
   // These buttons are in the current tower stats box
   private TowerUpgradeButton damageButton, rangeButton, rateButton, speedButton, specialButton;
   private final List<TowerUpgradeButton> towerStatsButtons = new ArrayList<TowerUpgradeButton>();
   private final Map<TowerUpgradeButton, Tower.Attribute> buttonAttributes =
         new HashMap<TowerUpgradeButton, Tower.Attribute>();
   // These labels are in the level stats box
   private MyJLabel numSpritesLabel, avgHPLabel;
   private MyJLabel currentCostStringLabel, currentCostLabel;
   private final ImageButton start = new ImageButton("start", ".png");
   private final GameMap map;
   private Tower selectedTower, buildingTower, rolloverTower;
   private final Color defaultTextColour = Color.YELLOW;
   private final float defaultTextSize = 12F;
   // This is the initial money
   private int money = 2500;
   private int lives = 10;
   private int livesLostOnThisLevel;
   private double interestRate = 1.01;
   private int endLevelUpgradesLeft = 0;
   private static final int upgradeLives = 5;
   private static final int upgradeMoney = 1000;
   private static final float upgradeInterest = 0.001f;

   public ControlPanel(int width, int height, GameMap map) {
      this.map = map;
      setPreferredSize(new Dimension(width, height));
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setUpJLabels();
      setUpTowerStatsButtons();
      setUpTopStatsBox();
      setUpNewTowers();
      setUpEndLevelUpgrades();
      setUpCurrentTowerStats();
      setUpLevelStats();
      setUpCurrentCost();
      setUpStartButton();
      updateAll();      
      map.setControlPanel(this);
   }
   
   public void endLevel() {
      endLevelUpgradesLeft++;
      enableEndLevelUpgradeButtons(true);
      updateEndLevelUpgradesLabel();
      int moneyBefore = money; 
      multiplyMoney(interestRate);
      int interest = money - moneyBefore;
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
      updateDamageAndKillsLabels();
      clearBuildingTower();
   }
   
   public void deselectTower() {
      if(selectedTower != null) {
         updateDamageAndKillsLabels();
         selectedTower = null;
      }
   }
   
   public boolean canBuildTower() {
      return money >= getNextTowerCost();
   }
   
   public void buildTower() {
      decreaseMoney(getNextTowerCost());
      updateMoneyLabel();
   }
   
   public void increaseMoney(int amount) {
      money += amount;
      updateMoneyLabel();
      // If a tower is selected, more money earnt means it could've done more damage
      updateDamageAndKillsLabels();
      // And this may have caused its stats to be upgraded
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
   
   private void multiplyMoney(double factor) {
      money *= factor;
   }
   
   private void decreaseMoney(int amount) {
      money -= amount;
   }
   
   private int getNextTowerCost() {
      return Formulae.towerCost(map.getTowers().size());
   }
   
   private void updateAll() {
      // Deliberately doesn't upgrade level stats to avoid confusion
      updateDamageAndKillsLabels();
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
      interestLabel.setText(ONE_DP.format(((interestRate - 1) * 100)) + "%");
   }
   
   private void updateEndLevelUpgradesLabel() {
      upgradesLabel.setText(endLevelUpgradesLeft);
   }
   
   private void updateCurrentCostLabel(String description, int cost) {
      updateCurrentCostLabel(description, Integer.toString(cost));
   }
   
   private void updateCurrentCostLabel(String description, String cost) {
      currentCostStringLabel.setText(description);
      currentCostLabel.setText(cost);
   }
   
   private void updateStats() {
      if(rolloverTower != null) {
         setStats(rolloverTower);
         updateCurrentCostLabel(rolloverTower.getName() + " Tower", getNextTowerCost());
      } else if(buildingTower != null) {
         setStats(buildingTower);
         updateCurrentCostLabel(buildingTower.getName() + " Tower", getNextTowerCost());
      } else {
         setStats(selectedTower);
      }
   }
   
   private void updateDamageAndKillsLabels() {
      if(selectedTower == null) {
         killsLabel.setText(" ");
         damageDealtLabel.setText(" ");
      } else {
         killsLabel.setText("Kills: " + selectedTower.getKills() + " (" +
               selectedTower.getKillsForUpgrade() + ")");
         damageDealtLabel.setText("Dmg: " + ZERO_DP.format(selectedTower.getDamageDealt())
               + " (" + selectedTower.getDamageDealtForUpgrade() + ")");
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
               f.set(this, j);
            } catch(IllegalAccessException e) {
               // This shouldn't ever be thrown
               System.err.println(e);
            }
         }
      }
   }
   
   private void setUpTowerStatsButtons() {
      for (Field f : getClass().getDeclaredFields()) {
         if (f.getType().equals(TowerUpgradeButton.class)) {
            try {
               String name = f.getName();
               // Sets its name to be the name of the field, without the
               // button and the first character capitalised
               String text = String.valueOf(name.charAt(0)).toUpperCase();
               text += name.substring(1, name.length() - 6);
               TowerUpgradeButton v = new TowerUpgradeButton(text, defaultTextColour);
               v.setMultiClickThreshhold(5);
               v.addActionListener(new ActionListener(){
                  public void actionPerformed(ActionEvent e) {
                     processUpgradeButtonPressed((TowerUpgradeButton)e.getSource());
                  }
               });
               v.addChangeListener(new ChangeListener(){
                  public void stateChanged(ChangeEvent e) {
                     processUpgradeButtonChanged((TowerUpgradeButton)e.getSource());
                  }
               });
               f.set(this, v);
               towerStatsButtons.add(v);
               buttonAttributes.put(v, Tower.Attribute.fromString(text));
            } catch(IllegalAccessException e) {
               // This shouldn't ever be thrown
               System.err.println(e);
            }
         }
      }
   }
   
   private void processUpgradeButtonPressed(TowerUpgradeButton b) {
      Tower.Attribute a = buttonAttributes.get(b);
      int cost = 0;
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
         Tower.Attribute a = buttonAttributes.get(b);
         String description = a.toString() + " Upgrade";
         int cost;
         if(selectedTower == null) {
            description += " (all)";
            cost = costToUpgradeAllTowers(a);
         } else {
            cost = Formulae.upgradeCost(selectedTower.getAttributeLevel(a));
         }
         updateCurrentCostLabel(description, cost);
      }
   }
   
   private int costToUpgradeAllTowers(Attribute a) {
      return costToUpgradeTowers(a, map.getTowers());
   }
   
   private int costToUpgradeTowers(Attribute a, List<Tower> towers) {
      int cost = 0;
      for(Tower t : towers) {
         cost += Formulae.upgradeCost(t.getAttributeLevel(a));
      }
      return cost;
   }
   
   private void setStats(Tower t) {
      if(t == null) {
         damageLabel.setText(" ");
         rangeLabel.setText(" ");
         rateLabel.setText(" ");
         speedLabel.setText(" ");
         specialButton.setText("Special");
         specialLabel.setText(" ");
      } else {
         damageLabel.setText(ONE_DP.format(t.getDamage()));
         rangeLabel.setText(t.getRange());
         rateLabel.setText(t.getFireRate());
         speedLabel.setText(ONE_DP.format(t.getBulletSpeed()));
         specialButton.setText(t.getSpecialName());
         specialLabel.setText(t.getSpecial());
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
      for(JButton b : towerStatsButtons) {
         b.setEnabled(enable);
      }
   }
   
   private void processTowerButtonAction(OverlayButton b) {
      Tower t = towerTypes.get(b);
      if(map.towerButtonPressed(t)) {
         setBuildingTower(t);
         updateStats();
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
   
   /*private void setCurrentCostAndStatsToBuildingTower() {
      setCurrentCostAndStatsToTower(buildingTower);
   }*/
   
   /*private void setCurrentCostAndStatsToTower(Tower t) {
      if(t != null) {
         setStats(t);
         updateCurrentCostLabel(t.getName() + " Tower", getNextTowerCost());
      }
   }*/
   
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
      float textSize = defaultTextSize + 3;
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
      levelLabel.setFont(levelLabel.getFont().deriveFont(25F));
      updateLevelStats();
      return createBorderLayedOutJPanel(levelLabel, BorderLayout.CENTER);
   }

   private JPanel createLeftRightPanel(String text, float textSize, Color textColour,
         JLabel label) {
      JLabel leftText = createJLabel(text, textSize, textColour);
      return createLeftRightPanel(leftText, textSize, textColour, label);
   }
   
   private MyJLabel createJLabel(String text, float textSize, Color textColour) {
      MyJLabel label = new MyJLabel(text);
      Font font = label.getFont().deriveFont(textSize);
      label.setFont(font);
      label.setForeground(textColour);
      return label;
   }
   
   private JPanel createLeftRightPanel(Component c, float textSize, Color textColour,
         JLabel label) {
      Font font = label.getFont().deriveFont(textSize);
      JPanel panel = createBorderLayedOutJPanel();
      panel.add(c, BorderLayout.WEST);
      label.setForeground(textColour);
      label.setFont(font);
      panel.add(label, BorderLayout.EAST);
      return panel;
   }

   private void setUpNewTowers() {
      JPanel panel = new JPanel();
      panel.setOpaque(false);
      int height = 3;
      int width = 6;
      panel.setLayout(new GridLayout(height, width));
      List<Tower> towers = getTowerImplementations();
      for (int a = 0; a < height * width; a++) {
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
         panel.add(createWrapperPanel(button));
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
      towerTypes.add(new JumpingTower());
      towerTypes.add(new CircleTower());
      towerTypes.add(new ScatterTower());
      towerTypes.add(new MultiShotTower());
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
      setUpDamageAndKillsLabels();
      float textSize = defaultTextSize;
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(BorderFactory.createEmptyBorder(1, 20, 1, 20));
      panel.setOpaque(false);
      panel.add(createLeftRightButtonPanel(damageButton, textSize, defaultTextColour, damageLabel));
      panel.add(createLeftRightButtonPanel(rangeButton, textSize, defaultTextColour, rangeLabel));
      panel.add(createLeftRightButtonPanel(rateButton, textSize, defaultTextColour, rateLabel));
      panel.add(createLeftRightButtonPanel(speedButton, textSize, defaultTextColour, speedLabel));
      panel.add(createLeftRightButtonPanel(specialButton, textSize, defaultTextColour, specialLabel));

      add(panel);
   }
   
   private void setUpDamageAndKillsLabels() {
      MyJLabel[] labels = new MyJLabel[]{killsLabel, damageDealtLabel};
      Font f = killsLabel.getFont().deriveFont(defaultTextSize - 1.5f);
      for(MyJLabel a : labels) {
         a.setForeground(defaultTextColour);
         a.setFont(f);
         a.setHorizontalAlignment(JLabel.CENTER);
      }
      JPanel panel = createBorderLayedOutJPanel();
      panel.add(createWrapperPanel(killsLabel), BorderLayout.WEST);
      panel.add(createWrapperPanel(damageDealtLabel), BorderLayout.EAST);
      add(panel);
   }
   
   private void setUpLevelStats() {
      float textSize = defaultTextSize;
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 30));
      panel.setOpaque(false);
      panel.add(createLeftRightPanel("Number", textSize, defaultTextColour, numSpritesLabel));
      panel.add(createLeftRightPanel("Average HP", textSize, defaultTextColour, avgHPLabel));
      
      add(panel);
   }

   private void setUpStartButton() {
      start.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            //System.out.println("Start pressed");
            startPressed();
         }
      });
      add(createWrapperPanel(start, 5));
   }
   
   private void setUpCurrentCost() {
      JPanel panel = createBorderLayedOutJPanel();
      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
      panel.setOpaque(false);
      panel.add(currentCostStringLabel, BorderLayout.WEST);
      panel.add(currentCostLabel, BorderLayout.EAST);
      add(panel);
   }
   
   private JPanel createLeftRightButtonPanel(JButton button, float textSize, Color textColour,
         JLabel damageLabel) {
      return createLeftRightPanel(createWrapperPanel(button), textSize, textColour,
            damageLabel);
   }

   private JPanel createBorderLayedOutJPanel() {
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      panel.setOpaque(false);
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
      
      public void setText(int i) {
         setText(String.valueOf(i));
      }
      
      public void setText(double d){
         setText(String.valueOf(d));
      }      
   }

}
