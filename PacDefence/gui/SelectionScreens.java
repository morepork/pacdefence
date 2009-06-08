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
 *  (C) Liam Byrne, 2008 - 09.
 */

package gui;

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

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import logic.Game;

@SuppressWarnings("serial")
public class SelectionScreens extends JPanel {
   
   private static final Color textColour = Color.YELLOW;
   private final MyJLabel title = new MyJLabel();
   private final BufferedImage background;
   private final List<GameMap> gameMaps = createGameMapList();
   private final Game.ContinueOn continueOn;

   public SelectionScreens(int width, int height, Game.ContinueOn continueOn) {
      super(new BorderLayout());
      title.setForeground(textColour);
      title.setFontSize(40F);
      title.setHorizontalAlignment(JLabel.CENTER);
      title.setText("Map Selection");
      background = ImageHelper.makeImage("other", "hoops.png");
      this.continueOn = continueOn;
      add(title, BorderLayout.NORTH);
      add(createMapSelections(), BorderLayout.CENTER);
   }
   
   @Override
   public void paintComponent(Graphics g) {
      g.drawImage(background, 0, 0, null);
   }
   
   private List<GameMap> createGameMapList() {
      List<GameMap> list = new ArrayList<GameMap>();
      list.add(MapParser.parse("mosaicPathEasy.xml"));
      list.add(MapParser.parse("mosaicPathMedium.xml"));
      list.add(MapParser.parse("mosaicPathHard.xml"));
      list.add(MapParser.parse("curvyMedium.xml"));
      return list;
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
            public void actionPerformed(ActionEvent e) {
               continueOn.continueOn(g);
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
