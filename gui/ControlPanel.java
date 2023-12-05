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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import logic.Constants;
import logic.Game.ControlEventProcessor;
import towers.Buildable;
import towers.ExperienceReport;
import towers.Ghost;
import towers.Tower;
import towers.Tower.Attribute;
import towers.impl.AidTower;
import towers.impl.BeamTower;
import towers.impl.BomberTower;
import towers.impl.ChargeTower;
import towers.impl.CircleTower;
import towers.impl.FreezeTower;
import towers.impl.HomingTower;
import towers.impl.JumperTower;
import towers.impl.LaserTower;
import towers.impl.MultiShotTower;
import towers.impl.OmnidirectionalTower;
import towers.impl.PoisonTower;
import towers.impl.ScatterTower;
import towers.impl.SlowLengthTower;
import towers.impl.WaveTower;
import towers.impl.WeakenTower;
import towers.impl.ZapperTower;
import util.Helper;
import creeps.Creep;


@SuppressWarnings("serial")
public class ControlPanel extends JPanel {
   
   // The tower implementations to use for the game, there should be exactly 18 here
   private static final Buildable[] buildables = {
      new BomberTower(new Point()),
      new SlowLengthTower(new Point()),
      new FreezeTower(new Point()),
      new JumperTower(new Point()),
      new CircleTower(new Point()),
      new ScatterTower(new Point()),
      new MultiShotTower(new Point()),
      new LaserTower(new Point()),
      new PoisonTower(new Point()),
      new OmnidirectionalTower(new Point()),
      new WeakenTower(new Point()),
      new WaveTower(new Point()),
      new HomingTower(new Point()),
      new ChargeTower(new Point()),
      new ZapperTower(new Point()),
      new BeamTower(new Point()),
      new AidTower(new Point()),
      new Ghost(new Point()),
   };
   
   private static final Color defaultTextColour = Color.YELLOW;
   private static final Color costLabelsColour = Color.GREEN;
   private static final float defaultTextSize = 12F;
   
   private final BufferedImage backgroundImage;
   
   // These are in the top stats box
   private OverlayToggleButton fastButton = createFastButton();
   private MyJLabel levelLabel, moneyLabel, livesLabel, interestLabel, upgradesLabel;
   private final Map<OverlayButton, Buildable> buildableTypes = new HashMap<>();
   private OverlayButton damageUpgrade, rangeUpgrade, rateUpgrade, speedUpgrade,
         specialUpgrade, livesUpgrade, interestUpgrade, moneyUpgrade;
   
   // These labels and the sell button below are in the tower info box
   private MyJLabel towerNameLabel, towerLevelLabel, damageDealtLabel, killsLabel;
   private MyJLabel targetLabel;
   private JButton targetButton = createTargetButton();
   private final JButton sellButton = createSellButton();
   
   // These are in the current tower stats box
   private final List<TowerStat> towerStats = new ArrayList<TowerStat>();
   private final Map<JButton, Attribute> buttonAttributeMap = new HashMap<JButton, Attribute>();
   private final Map<Attribute, JButton> attributeButtonMap = new EnumMap<>(Attribute.class);
   
   // These labels are in the level stats box
   private MyJLabel numCreepsLabel, timeBetweenCreepsLabel, hpLabel;
   private MyJLabel currentCostStringLabel, currentCostLabel;
   
   private final ImageButton start = new ImageButton("start", ".png", true);

   private ControlEventProcessor eventProcessor;

   public ControlPanel() {
      int width = Constants.CONTROLS_WIDTH;
      int height = Constants.CONTROLS_HEIGHT;
      backgroundImage = ImageHelper.loadImage(width, height, "control_panel",
            "blue_lava_blurred.jpg");
      setPreferredSize(new Dimension(width, height));
      // Reflective method to set up the MyJLabels
      setUpJLabels();
      // Create each of the sub panels of this panel, from top to bottom
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setUpTopStats();
      setUpNewTowers();
      setUpEndLevelUpgrades();
      setUpCurrentTowerInfo();
      setUpCurrentTowerStats();
      setUpLevelStats();
      setUpCurrentCost();
      setUpStartButton();
      setUpBottomButtons();
      addKeyboardShortcuts(getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), getActionMap());
   }

   @Override
   public void paintComponent(Graphics g) {
      g.drawImage(backgroundImage, 0, 0, null);
   }
   
   public void updateNumberLeft(int number) {
      numCreepsLabel.setText(number);
   }
   
   public void setStats(Tower t) {
      for(int i = 0; i < towerStats.size(); i++) {
         towerStats.get(i).setText(t);
      }
   }
   
   public void setCurrentInfoToTower(Tower t) {
      if(t == null) {
         blankCurrentTowerInfo();
      } else {
         towerNameLabel.setText(t.getName());
         ExperienceReport report = t.getExperienceReport();
         towerLevelLabel.setText("Level: " + report.level);
         killsLabel.setText("Kills: " + report.kills + " (" + report.killsForUpgrade + ")");
         damageDealtLabel.setText("Dmg: " + report.damage + " (" + report.damageForUpgrade + ")");
         sellButton.setEnabled(true);
         Comparator<Creep> c = t.getCreepComparator();
         if(c != null) {
            targetLabel.setText("Target");
            targetButton.setEnabled(true);
            targetButton.setText(c.toString());
         }
      }
   }
   
   public void setCurrentInfoToCreep(Creep c) {
      if(c == null) {
         blankCurrentTowerInfo();
      } else {
         towerNameLabel.setText("HP Left: " + Helper.format(c.getHPLeft(), 2));
         towerLevelLabel.setText(" ");
         killsLabel.setText(" ");
         damageDealtLabel.setText("Speed: " + Helper.format(c.getSpeed() *
                 Constants.CLOCK_TICKS_PER_SECOND, 0) + " pixels/s ");
         sellButton.setEnabled(false);
         targetLabel.setText(" ");
         targetButton.setEnabled(false);
      }
   }
   
   public void setEventProcessor(ControlEventProcessor eventProcessor) {;
      if(this.eventProcessor != null) {
         throw new IllegalStateException("Control even proccessor already set.");
      }
      this.eventProcessor = eventProcessor;
   }
   
   public void enableTowerStatsButtons(boolean enable) {
      for(int i = 0; i < towerStats.size(); i++) {
         towerStats.get(i).enableButton(enable);
      }
   }
   
   public void updateLevelStats(String level, String numCreeps, String hp,
         String timeBetweenCreeps) {
      levelLabel.setText(level);
      numCreepsLabel.setText(numCreeps);
      hpLabel.setText(hp);
      timeBetweenCreepsLabel.setText(timeBetweenCreeps);
   }
   
   public void updateCurrentCost(String description, long cost) {
      updateCurrentCost(description, String.valueOf(cost));
   }
   
   public void updateCurrentCost(String description, String cost) {
      currentCostStringLabel.setText(description);
      currentCostLabel.setText(cost);
   }
   
   public void clearCurrentCost() {
      updateCurrentCost(" ", " ");
   }
   
   public void updateEndLevelUpgrades(long upgradesLeft) {
      upgradesLabel.setText(upgradesLeft);
      enableEndLevelUpgradeButtons(upgradesLeft > 0);
   }
   
   public void updateInterest(String value) {
      interestLabel.setText(value);
   }
   
   public void updateMoney(long money) {
      moneyLabel.setText(money);
   }
   
   public void updateLives(int lives) {
      livesLabel.setText(lives);
   }
   
   public void enableStartButton(boolean enable) {
      start.setEnabled(enable);
   }
   
   public void increaseTowersAttribute(Attribute a) {
      for(Buildable b : buildableTypes.values()) {
         if (b instanceof Tower) {
            // So that the upgrades are shown when you are building a new tower
            ((Tower) b).upgrade(a, false);
         }
      }
   }
   
   public void restart() {
      for(OverlayButton b : buildableTypes.keySet()) {
         buildableTypes.put(b, buildableTypes.get(b).constructNew(new Point()));
      }
      start.setEnabled(true);
      fastButton.setToDefault();
   }
   
   public void clickSellButton() {
      sellButton.doClick();
   }
   
   public void clickTowerUpgradeButton(Attribute a) {
      attributeButtonMap.get(a).doClick();
   }
   
   public void clickFastButton(boolean normalClick) {
      eventProcessor.processFastButtonPressed(normalClick);
      fastButton.toggleIcons(normalClick);
   }
   
   private void addKeyboardShortcuts(InputMap inputMap, ActionMap actionMap) {
      // Sets 1, 2, 3, etc. as the keyboard shortcuts for the tower upgrades
      for(int i = 1; i <= Attribute.values().length; i++) {
         final Attribute a = Attribute.values()[i - 1];
         Character c = Character.forDigit(i, 10);
         inputMap.put(KeyStroke.getKeyStroke(c), a);
         actionMap.put(a, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               clickTowerUpgradeButton(a);
            }
         });
      }
      
      // Page Up/Down change what the selected tower is targetting
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "Change target up");
      actionMap.put("Change target up", new AbstractAction() {
         @Override
        public void actionPerformed(ActionEvent e) {
            processTargetButtonClicked(targetButton, true);
         }
      });
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "Change target down");
      actionMap.put("Change target down", new AbstractAction() {
         @Override
        public void actionPerformed(ActionEvent e) {
            processTargetButtonClicked(targetButton, false);
         }
      });
      
      // + and = increase the speed, - decreases it
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "Speed Up");
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "Speed Up");
      actionMap.put("Speed Up", new AbstractAction() {
         @Override
        public void actionPerformed(ActionEvent e) {
            clickFastButton(true);
         }
      });
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "Slow Down");
      actionMap.put("Slow Down", new AbstractAction() {
         @Override
        public void actionPerformed(ActionEvent e) {
            clickFastButton(false);
         }
      });
      
      // s sells the currently selected tower
      inputMap.put(KeyStroke.getKeyStroke('s'), "Sell");
      actionMap.put("Sell", new AbstractAction() {
         @Override
        public void actionPerformed(ActionEvent e) {
            clickSellButton();
         }
      });
   }
   
   private void blankCurrentTowerInfo() {
      // Don't use an empty string here so as not to collapse the label
      towerNameLabel.setText(" ");
      towerLevelLabel.setText(" ");
      killsLabel.setText(" ");
      damageDealtLabel.setText(" ");
      sellButton.setEnabled(false);
      targetLabel.setText(" ");
      targetButton.setEnabled(false);
   }
   
   private void enableEndLevelUpgradeButtons(boolean enable) {
      JButton[] buttons = new JButton[]{damageUpgrade, rangeUpgrade, rateUpgrade, speedUpgrade,
               specialUpgrade, livesUpgrade, interestUpgrade, moneyUpgrade};
      for(JButton b : buttons) {
         b.setEnabled(enable);
      }
   }

   // ---------------------------------------
   // The remaining methods set up the gui
   // ---------------------------------------

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
   
   private void setUpTopStats() {
      float textSize = defaultTextSize + 1;
      Box hBox = Box.createHorizontalBox();
      hBox.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
      hBox.setOpaque(false);
      hBox.add(Box.createHorizontalStrut(4));
      Box buttonBox = Box.createVerticalBox();
      buttonBox.add(Box.createVerticalStrut(4));
      buttonBox.add(fastButton);
      buttonBox.add(Box.createVerticalGlue());
      hBox.add(buttonBox);
      Box vBox = Box.createVerticalBox();
      vBox.setOpaque(false);
      vBox.add(createLevelLabel(defaultTextColour));
      vBox.add(createLeftRightPanel("Money", textSize, defaultTextColour, moneyLabel));
      vBox.add(createLeftRightPanel("Lives", textSize, defaultTextColour, livesLabel));
      vBox.add(createLeftRightPanel("Interest", textSize, defaultTextColour, interestLabel));
      vBox.add(createLeftRightPanel("Bonuses", textSize, defaultTextColour, upgradesLabel));
      hBox.add(vBox);
      hBox.add(Box.createHorizontalStrut(24));
      add(hBox);
   }
   
   private OverlayToggleButton createFastButton() {
      BufferedImage[] images = new BufferedImage[3];
      for(int i = 1; i <= images.length; i++) {
         images[i - 1] = ImageHelper.loadImage("buttons", "fast" + i + ".png");
      }
      OverlayToggleButton b = new OverlayToggleButton(images);
      b.addActionListener(new ActionListener() {
         @Override
        public void actionPerformed(ActionEvent e) {
            clickFastButton(true);
         }
      });
      // Go back on a right click
      b.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            // Don't consolidate left click in here as there are ways to press a button besides a
            // mouse click (selected, then enter for instance)
            if(e.getButton() == MouseEvent.BUTTON3) {
               clickFastButton(false);
            }
         }
      });
      return b;
   }

   private JPanel createLevelLabel(Color textColour) {
      levelLabel.setForeground(textColour);
      levelLabel.setHorizontalAlignment(JLabel.CENTER);
      levelLabel.setFontSize(25);
      return SwingHelper.createBorderLayedOutWrapperPanel(levelLabel, BorderLayout.CENTER);
   }

   private JPanel createLeftRightPanel(String text, float textSize, Color textColour,
         MyJLabel label) {
      MyJLabel leftText = createJLabel(text, textSize, textColour);
      return createLeftRightPanel(leftText, textSize, textColour, label);
   }
   
   @SuppressWarnings("unused")
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
      assert buildables.length == total : "Number of tower implementations is " +
            "different to number of buttons.";
      for (int a = 0; a < numY * numX; a++) {
         Buildable t = buildables[a];
         OverlayButton button = OverlayButton.makeTowerButton(t.getButtonImage());
         buildableTypes.put(button, t);
         button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
               JButton b = (JButton) e.getSource();
               eventProcessor.processTowerButtonPressed(buildableTypes.get(b));
            }
         });
         button.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e) {
               JButton b = (JButton) e.getSource();
               eventProcessor.processTowerButtonRollover(buildableTypes.get(b), isRollover(b));
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
   
   private void setUpEndLevelUpgrades() {
      setEndLevelUpgradeButtons();
      Box box = Box.createHorizontalBox();
      box.setOpaque(false);
      OverlayButton[] buttons = new OverlayButton[]{damageUpgrade, rangeUpgrade, rateUpgrade,
            speedUpgrade, specialUpgrade, livesUpgrade, interestUpgrade, moneyUpgrade};
      for(OverlayButton b : buttons) {
         b.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
               OverlayButton o = (OverlayButton) e.getSource();
               eventProcessor.processEndLevelUpgradeButtonPress(o.equals(livesUpgrade),
                     o.equals(interestUpgrade), o.equals(moneyUpgrade), getAttributeFromButton(o));
            }
         });
         b.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
               OverlayButton o = (OverlayButton) e.getSource();
               eventProcessor.processEndLevelUpgradeButtonRollover(o.equals(livesUpgrade),
                     o.equals(interestUpgrade), o.equals(moneyUpgrade), getAttributeFromButton(o),
                     isRollover(o));
            }
         });
         box.add(b);
      }
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
      box.setBorder(BorderFactory.createEmptyBorder(0, 1, 1, 5));
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
      // The text is actually set later
      OverlayButton b = new OverlayButton(" ", 13);
      
      b.addActionListener(new ActionListener(){
         @Override
        public void actionPerformed(ActionEvent e) {
            JButton b = (JButton)e.getSource();
            boolean ctrl = (e.getModifiers() & InputEvent.CTRL_MASK) != 0 ;
            eventProcessor.processUpgradeButtonPressed(buttonAttributeMap.get(b), ctrl);
         }
      });
      b.addChangeListener(new ChangeListener(){
         @Override
        public void stateChanged(ChangeEvent e) {
            JButton b = (JButton)e.getSource();
            eventProcessor.processUpgradeButtonRollover(buttonAttributeMap.get(b), isRollover(b));
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
      centralRow.add(Box.createHorizontalGlue());
      centralRow.add(towerLevelLabel);
      centralRow.add(Box.createHorizontalGlue());
      centralRow.add(sellButton);
      centralRow.add(Box.createHorizontalGlue());
      centralRow.add(killsLabel);
      centralRow.add(Box.createHorizontalGlue());
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
      // The text gets set later
      OverlayButton b = new OverlayButton(" ", 12, 6, 5) {
         @Override
         public void setText(String text) {
            super.setText(text);
            // So when disabled it is blank but keeps its size, and it stays each time the text is
            // changed
            setDisabledIcon(new ImageIcon());
         }
      };
      
      b.addActionListener(new ActionListener() {
         @Override
        public void actionPerformed(ActionEvent e) {
            processTargetButtonClicked((JButton)e.getSource(), true);
         }
      });
      // Go back on a right click
      b.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON3) {
               processTargetButtonClicked((JButton)e.getSource(), false);
            }
         }
      });
      return b;
   }
   
   private void processTargetButtonClicked(JButton b, boolean left) {
      b.setText(eventProcessor.processTargetButtonPressed(left));
   }
   
   private void setUpLevelStats() {
      float textSize = defaultTextSize;
      Box box = Box.createVerticalBox();
      box.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
      box.setOpaque(false);
      box.add(createLeftRightPanel("Number Left", textSize, defaultTextColour, numCreepsLabel));
      box.add(createLeftRightPanel("Between Creeps", textSize, defaultTextColour,
            timeBetweenCreepsLabel));
      box.add(createLeftRightPanel("HP", textSize, defaultTextColour, hpLabel));
      add(box);
   }

   private void setUpStartButton() {
      start.addActionListener(new ActionListener() {
         @Override
        public void actionPerformed(ActionEvent e) {
            eventProcessor.processStartButtonPressed();
         }
      });
      start.setMnemonic(KeyEvent.VK_S);
      add(SwingHelper.createWrapperPanel(start));
   }
   
   private void setUpCurrentCost() {
      float textSize = defaultTextSize - 1;
      currentCostStringLabel.setFontSize(textSize);
      currentCostLabel.setFontSize(textSize);
      currentCostStringLabel.setForeground(costLabelsColour);
      currentCostLabel.setForeground(costLabelsColour);
      JPanel panel = SwingHelper.createBorderLayedOutJPanel();
      panel.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));
      panel.add(currentCostStringLabel, BorderLayout.WEST);
      panel.add(currentCostLabel, BorderLayout.EAST);
      add(panel);
   }
   
   private JButton createSellButton() {
      JButton b = new OverlayButton("Sell", 13, Color.RED, 6, 5);
      // So when disabled it is blank but keeps its size
      b.setDisabledIcon(new ImageIcon());
      b.addActionListener(new ActionListener() {
         @Override
        public void actionPerformed(ActionEvent e) {
            eventProcessor.processSellButtonPressed();
         }
      });
      b.addChangeListener(new ChangeListener() {
         @Override
        public void stateChanged(ChangeEvent e) {
            eventProcessor.processSellButtonRollover(isRollover((JButton) e.getSource()));
         }
      });
      return b;
   }
   
   private void setUpBottomButtons() {
      JPanel panel = SwingHelper.createBorderLayedOutJPanel();
      JButton title = new OverlayButton("Title");
      title.setMnemonic(KeyEvent.VK_T);
      title.addActionListener(new ActionListener() {
         @Override
        public void actionPerformed(ActionEvent e) {
            eventProcessor.processTitleButtonPressed();
         }
      });
      JButton restart = new OverlayButton("Restart");
      restart.setMnemonic(KeyEvent.VK_R);
      restart.addActionListener(new ActionListener() {
         @Override
        public void actionPerformed(ActionEvent e) {
            eventProcessor.processRestartPressed();
         }
      });
      panel.add(title, BorderLayout.WEST);
      panel.add(restart, BorderLayout.EAST);
      panel.setBorder(BorderFactory.createEmptyBorder(0, 13, 1, 13));
      add(panel);
   }
   
   private boolean isRollover(JButton b) {
      return b.getModel().isRollover();
   }

   private class TowerStat {
      
      private final JButton button;
      private final JLabel label;
      private final Attribute attrib;
      
      private TowerStat(JButton b, JLabel l, Attribute a) {
         if(buttonAttributeMap.put(b, a) != null) {
            throw new IllegalArgumentException("This button has already been used.");
         } else if(attributeButtonMap.put(a, b) != null) {
            throw new IllegalArgumentException("This attribute has already been used.");
         }
         button = b;
         label = l;
         attrib = a;
      }
      
      private void setText(Tower t) {
         if(t == null) {
            button.setText(attrib.toString());
            label.setText(" ");
         } else {
            button.setText(t.getStatName(attrib));
            label.setText(t.getStat(attrib) + " [" + t.getAttributeLevel(attrib) + "]");
         }
      }
      
      private void enableButton(boolean b) {
         button.setEnabled(b);
      }
   }
   
}
