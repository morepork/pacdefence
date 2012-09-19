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

import gui.PacDefence.GameStarter;
import gui.maps.MapParser;
import gui.maps.MapParser.GameMap;
import images.ImageHelper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import logic.MyExecutor;

@SuppressWarnings("serial")
public class SelectionScreens extends JPanel {
   
   private static final Color textColour = Color.YELLOW;
   private final MyJLabel title = new MyJLabel();
   private final BufferedImage background;
   private final List<GameMap> gameMaps = createGameMapList();
   private final GameStarter gameStarter;

   public SelectionScreens(int width, int height, GameStarter gameStarter) {
      super(new BorderLayout());
      
      title.setForeground(textColour);
      title.setFontSize(40F);
      title.setHorizontalAlignment(JLabel.CENTER);
      title.setText("Map Selection");
      
      background = ImageHelper.loadImage("other", "hoops.png");
      
      this.gameStarter = gameStarter;
      
      add(title, BorderLayout.NORTH);
      add(createMapSelections(), BorderLayout.CENTER);
   }
   
   @Override
   public void paintComponent(Graphics g) {
      g.drawImage(background, 0, 0, null);
   }
   
   private List<GameMap> createGameMapList() {
      final String[] maps = new String[]{"mosaicPathEasy.xml", "mosaicPathMedium.xml",
            "mosaicPathHard.xml", "curvyEasy.xml", "curvyMedium.xml", "curvyHard.xml"};
      final List<GameMap> gameMaps = new ArrayList<GameMap>(maps.length);
      // Using multiple threads here makes it around 25% faster on my dual core, which is decent
      // seeing there is noticeable lag (when 'Continue' is pressed, before the maps show)
      if(MyExecutor.singleThreaded()) {
         for(String s: maps) {
            gameMaps.add(MapParser.parse(s));
         }
      } else {
         List<Future<GameMap>> futures = new ArrayList<Future<GameMap>>(maps.length);
         for(final String s : maps) {
            futures.add(MyExecutor.submit(new Callable<GameMap>() {
               @Override
               public GameMap call() {
                  return MapParser.parse(s);
               }
            }));
         }
         try {
            for(Future<GameMap> f : futures) {
               gameMaps.add(f.get());
            }
         } catch(InterruptedException e) {
            // Should never happen
            throw new RuntimeException(e);
         } catch(ExecutionException e) {
            // Should never happen
            throw new RuntimeException(e);
         }
      }
      return gameMaps;
   }
   
   private JComponent createMapSelections() {
      int cols = 3;
      JPanel panel = new JPanel(new GridLayout(0, cols));
      panel.setOpaque(false);
      for(int i = 0; i < gameMaps.size(); i++) {
         final GameMap g = gameMaps.get(i);
         Box b = Box.createVerticalBox();
         // Improves the spacing above
         b.add(Box.createVerticalStrut(5));
         if(i < cols) {
            // Only add glue to the top of the first row, otherwise you get glue twice in the centre
            b.add(Box.createVerticalGlue());
         }
         OverlayButton button = new OverlayButton(g.getImage(), 225, 225);
         button.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
               gameStarter.startGame(g);
            }
         });
         b.add(SwingHelper.createHorizontalWrapperBoxWithGlue(button));
         MyJLabel label = new MyJLabel(g.getDescription());
         label.setFontSize(18F);
         label.setForeground(textColour);
         b.add(SwingHelper.createHorizontalWrapperBoxWithGlue(label));
         b.add(Box.createVerticalGlue());
         panel.add(b);
      }
      return panel;
   }
   
}
