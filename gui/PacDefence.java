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

import gui.maps.MapParser.GameMap;
import images.ImageHelper;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JPanel;

import logic.Constants;
import logic.Game;
import logic.MyExecutor;
import logic.Options;
import towers.AbstractTower;

public class PacDefence {

   private final Options options;

   private final Container outerContainer;
   private Title title = createTitle();
   private Future<SelectionScreens> selectionScreensFuture;
   private SelectionScreens selectionScreens;
   
   private Game game;

   public PacDefence(Container c, Options options) {
      this.options = options;
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

   private Title createTitle() {
      return new Title(Constants.WIDTH, Constants.HEIGHT, new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               selectionScreens = selectionScreensFuture.get();
               selectionScreensFuture = null; // Let the memory be reclaimed
            } catch(InterruptedException ex) {
               // Should never happen
               throw new RuntimeException(ex);
            } catch(ExecutionException ex) {
               // Hopefully this will never happen either
               throw new RuntimeException(ex);
            }
            ImageHelper.setSkin(title.getSelectedSkin());
            outerContainer.remove(title);
            outerContainer.add(selectionScreens);
            outerContainer.invalidate(); // This is needed for it to work with java7
            outerContainer.validate();
            // Free up memory used by title (around 2-3 MB)
            title = null;
            // Create a new game so it can load stuff asynchronously
            game = new Game(new ReturnToTitleCallback(), options);
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
      selectionScreensFuture = MyExecutor.submit(new Callable<SelectionScreens>() {
         @Override
         public SelectionScreens call() {
            return new SelectionScreens(Constants.WIDTH, Constants.HEIGHT, new GameStarter());
         }
      });
   }
   
   public class GameStarter {
      
      public void startGame(GameMap gm) {
         // Start the game
         game.startGame(gm);
         
         outerContainer.remove(selectionScreens);
         // Releases memory used by the images in the GameMaps, ~20 MB when last checked
         selectionScreens = null;
         outerContainer.add(game.getGameMapPanel(), BorderLayout.WEST);
         outerContainer.add(game.getControlPanel(), BorderLayout.EAST);
         outerContainer.validate();
         outerContainer.repaint();
         
         game.getControlPanel().requestFocus();
      }
      
   }

   public class ReturnToTitleCallback {

      public void returnToTitle(JPanel... panelsToRemove) {
         // Need to create a new title screen as we got rid of the old one to
         // free up memory
         title = createTitle();
         for (JPanel panel : panelsToRemove) {
            outerContainer.remove(panel);
         }
         outerContainer.add(title);
         outerContainer.invalidate();
         outerContainer.validate();
         outerContainer.repaint();
         loadSelectionScreens();
         AbstractTower.flushImageCache();
      }

   }

}
