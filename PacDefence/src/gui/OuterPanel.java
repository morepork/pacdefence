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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class OuterPanel extends JPanel {
   
   // TODO Perhaps change this to have most of the logic with GameMap just
   // dealing with drawing stuff and ControlPanel just dealing with the
   // controls.
   
   public static final int WIDTH = 800;
   public static final int HEIGHT = 600;
   
   public static final int MAP_WIDTH = WIDTH - 200;
   public static final int MAP_HEIGHT = HEIGHT;
   
   public static final int CONTROLS_WIDTH = WIDTH - MAP_WIDTH;
   public static final int CONTROLS_HEIGHT = MAP_HEIGHT;

   private final Title title;
   private final GameMap map;
   private final ControlPanel controlPanel; 
   
   public OuterPanel() {
      map = new GameMap(MAP_WIDTH, MAP_HEIGHT, ImageHelper.makeImage("maps",
            "rainbowColours.jpg"), ImageHelper.makeImage("maps", "mosaicPathMedium.png"));
      controlPanel = new ControlPanel(CONTROLS_WIDTH, CONTROLS_HEIGHT, map);
      title = new Title(WIDTH, HEIGHT, new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            remove(title);
            add(map, BorderLayout.WEST);
            add(controlPanel, BorderLayout.EAST);
            revalidate();
         }
      });
      setLayout(new BorderLayout());
      setPreferredSize(new Dimension(WIDTH, HEIGHT));
      add(title);
   }
}
