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
 *  (C) Liam Byrne, 2008 - 10.
 */

package gui;

import gui.maps.MapParser.GameMap;
import images.ImageHelper;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import towers.AbstractTower;
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

import logic.Constants;
import logic.Game;
import logic.MyExecutor;


public class PacDefence {

   private final boolean debugTimes;
   private final boolean debugPath;

   private final Container outerContainer;
   private Title title = createTitle();
   private Thread asynchronousLoader;
   private SelectionScreens selectionScreens;
   private ControlPanel controlPanel;
   
   private Game game;

   public PacDefence(Container c, boolean debugTimes, boolean debugPath) {
      this.debugTimes = debugTimes;
      this.debugPath = debugPath;
      outerContainer = c;
      outerContainer.setLayout(new BorderLayout());
      outerContainer.add(title);
      MyExecutor.initialiseExecutor();
      loadSelectionScreens();
   }
   
   public void end() {
      game.stopRunning();
      MyExecutor.terminateExecutor();
      outerContainer.setVisible(false);
   }
   
   public boolean getDebugTimes() {
      return debugTimes;
   }
   
   public boolean getDebugPath() {
      return debugPath;
   }
   
   public void returnToTitle(JPanel... panelsToRemove) {
      // Need to create a new title screen as we got rid of the old one to free up memory
      title = createTitle();
      for(JPanel panel : panelsToRemove) {
         outerContainer.remove(panel);
      }
      outerContainer.add(title);
      outerContainer.invalidate();
      outerContainer.validate();
      outerContainer.repaint();
      loadSelectionScreens();
      AbstractTower.flushImageCache();
   }

   private Title createTitle() {
      return new Title(Constants.WIDTH, Constants.HEIGHT, new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               asynchronousLoader.join();
            } catch(InterruptedException ex) {
               // Should never happen
               throw new RuntimeException(ex);
            }
            ImageHelper.setSkin(title.getSelectedSkin());
            outerContainer.remove(title);
            outerContainer.add(selectionScreens);
            outerContainer.invalidate(); // This is needed for it to work with java7
            outerContainer.validate();
            // Free up memory used by title (around 2-3 MB)
            title = null;
            loadControlPanel();
         }
      });
   }
   
   /**
    * Loads the selection screens asynchronously.
    * 
    * It's worth doing this as previously it would pause for around a second while it was loaded,
    * but it may as well be loaded in the background because nothing is happening while you're at
    * the title screen.
    */
   private void loadSelectionScreens() {
      final PacDefence pd = this;
      asynchronousLoader = new Thread() {
         @Override
         public void run() {
            selectionScreens = new SelectionScreens(Constants.WIDTH, Constants.HEIGHT,
                    new GameStarter(pd));
         }
      };
      asynchronousLoader.start();
   }
   
   /**
    * Load the control panel asynchronously.
    * 
    * This is fine as like on the title screen, while the selection screens are shown nothing is
    * really happening.
    */
   private void loadControlPanel() {
      asynchronousLoader = new Thread() {
         @Override
         public void run() {
            controlPanel = new ControlPanel(Constants.CONTROLS_WIDTH, Constants.CONTROLS_HEIGHT,
                  ImageHelper.loadImage("control_panel", "blue_lava_blurred.jpg"),
                  createTowerImplementations());
            addKeyboardShortcuts(controlPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                  controlPanel.getActionMap());
         }
      };
      asynchronousLoader.start();
   }
   
   private List<Tower> createTowerImplementations() {
      List<Tower> towerTypes = new ArrayList<Tower>();
      towerTypes.add(new BomberTower(new Point(), null));
      towerTypes.add(new SlowLengthTower(new Point(), null));
      towerTypes.add(new FreezeTower(new Point(), null));
      towerTypes.add(new JumperTower(new Point(), null));
      towerTypes.add(new CircleTower(new Point(), null));
      towerTypes.add(new ScatterTower(new Point(), null));
      towerTypes.add(new MultiShotTower(new Point(), null));
      towerTypes.add(new LaserTower(new Point(), null));
      towerTypes.add(new PoisonTower(new Point(), null));
      towerTypes.add(new OmnidirectionalTower(new Point(), null));
      towerTypes.add(new WeakenTower(new Point(), null));
      towerTypes.add(new WaveTower(new Point(), null));
      towerTypes.add(new HomingTower(new Point(), null));
      towerTypes.add(new ChargeTower(new Point(), null));
      towerTypes.add(new ZapperTower(new Point(), null));
      towerTypes.add(new BeamTower(new Point(), null));
      towerTypes.add(new AidTower(new Point(), null));
      towerTypes.add(new Ghost(new Point()));
      return towerTypes;
   }
   
   @SuppressWarnings("serial")
   private void addKeyboardShortcuts(InputMap inputMap, ActionMap actionMap) {
      // Sets 1, 2, 3, etc. as the keyboard shortcuts for the tower upgrades
      for(int i = 1; i <= Attribute.values().length; i++) {
         final Attribute a = Attribute.values()[i - 1];
         Character c = Character.forDigit(i, 10);
         inputMap.put(KeyStroke.getKeyStroke(c), a);
         actionMap.put(a, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
               controlPanel.clickTowerUpgradeButton(a);
            }
         });
      }
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "Change target up");
      actionMap.put("Change target up", new AbstractAction() {
         @Override
        public void actionPerformed(ActionEvent e) {
            controlPanel.clickTargetButton(true);
         }
      });
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "Change target down");
      actionMap.put("Change target down", new AbstractAction() {
         @Override
        public void actionPerformed(ActionEvent e) {
            controlPanel.clickTargetButton(false);
         }
      });
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "Speed Up");
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "Speed Up");
      actionMap.put("Speed Up", new AbstractAction() {
         @Override
        public void actionPerformed(ActionEvent e) {
            controlPanel.clickFastButton(true);
         }
      });
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "Slow Down");
      actionMap.put("Slow Down", new AbstractAction() {
         @Override
        public void actionPerformed(ActionEvent e) {
            controlPanel.clickFastButton(false);
         }
      });
      inputMap.put(KeyStroke.getKeyStroke('s'), "Sell");
      actionMap.put("Sell", new AbstractAction() {
         @Override
        public void actionPerformed(ActionEvent e) {
            controlPanel.clickSellButton();
         }
      });
   }
   
   public class GameStarter {
      
      final PacDefence pacDefence;
      
      public GameStarter(PacDefence pd) {
         this.pacDefence = pd;
      }
      
      public void startGame(GameMap gm) {
         try {
            asynchronousLoader.join();
         } catch(InterruptedException e) {
            // Should never happen
            throw new RuntimeException(e);
         }
         
         // Start the game
         game = new Game(pacDefence, controlPanel, gm);
         
         outerContainer.remove(selectionScreens);
         // Releases memory used by the images in the GameMaps, ~20 MB when last checked
         selectionScreens = null;
         outerContainer.add(game.getGameMapPanel(), BorderLayout.WEST);
         outerContainer.add(game.getControlPanel(), BorderLayout.EAST);
         outerContainer.validate();
         outerContainer.repaint();
         controlPanel.requestFocus();
      }
      
   }

}
