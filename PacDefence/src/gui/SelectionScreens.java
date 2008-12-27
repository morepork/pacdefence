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

import gui.GameMapPanel.GameMap;
import images.ImageHelper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import logic.Game;

@SuppressWarnings("serial")
public class SelectionScreens extends JPanel {
   
   private static final Color textColour = Color.YELLOW;
   private final MyJLabel title = new MyJLabel();
   private final BufferedImage background;
   private int selectedGameMap;
   private List<GameMap> gameMaps = new ArrayList<GameMap>();
   private final Game.CarryOn carryOn;

   public SelectionScreens(int width, int height, Game.CarryOn carryOn) {
      super(new BorderLayout());
      title.setForeground(textColour);
      title.setFontSize(40F);
      title.setHorizontalAlignment(JLabel.CENTER);
      background = ImageHelper.makeImage("maps", "theGreenPlace.png");
      addGameMaps();
      this.carryOn = carryOn;
      add(title, BorderLayout.NORTH);
      add(createMapSelections());
   }
   
   @Override
   public void paintComponent(Graphics g) {
      g.drawImage(background, 0, 0, null);
   }
   
   public GameMap getSelectedGameMap() {
      return gameMaps.get(selectedGameMap);
   }
   
   private void addGameMaps() {
      gameMaps.add(loadMap("mosaicPathEasy", "mosaicPathEasy.png"));
      gameMaps.add(loadMap("mosaicPathMedium", "mosaicPathMedium.png"));
      gameMaps.add(loadMap("mosaicPathHard", "mosaicPathHard.png"));
   }
   
   private Component createMapSelections() {
      title.setText("Map Selection");
      Box box = Box.createHorizontalBox();
      Component gap = null;
      for(final GameMap g : gameMaps) {
         JPanel b = SwingHelper.createBorderLayedOutJPanel();
         OverlayButton button = new OverlayButton(g.getImage(), 250, 250);
         button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
               carryOn.carryOn(g);
            }
         });
         b.add(button, BorderLayout.NORTH);
         MyJLabel label = new MyJLabel(g.getDescription());
         label.setFontSize(20F);
         label.setForeground(textColour);
         label.setHorizontalAlignment(JLabel.CENTER);
         b.add(label, BorderLayout.SOUTH);
         box.add(b);
         gap = Box.createRigidArea(new Dimension(15, 0));
         box.add(gap);
      }
      box.remove(gap);
      Box verticalBox = Box.createVerticalBox();
      verticalBox.add(Box.createRigidArea(new Dimension(0, 130)));
      verticalBox.add(SwingHelper.createWrapperPanel(box));
      return verticalBox;
   }
   
   private GameMap loadMap(String textFileName, String imageName) {
      Scanner scan = new Scanner(getClass().getResourceAsStream("maps/" + textFileName));
      String description = scan.nextLine();
      String currentToken = skipComments(scan, scan.next());
      List<Point> polygonPoints = new ArrayList<Point>();
      while(!isComment(currentToken)) {
         polygonPoints.add(new Point(Integer.valueOf(currentToken), Integer.valueOf(scan.next())));
         currentToken = scan.next();
      }
      Polygon path = new Polygon();
      for(Point p : polygonPoints) {
         path.addPoint(p.x, p.y);
      }
      currentToken = skipComments(scan, currentToken);
      List<Point> pathPoints = new ArrayList<Point>();
      while(!isComment(currentToken)) {
         pathPoints.add(new Point(Integer.valueOf(currentToken), Integer.valueOf(scan.next())));
         if(scan.hasNext()) {
            currentToken = scan.next();
         } else {
            break;
         }
      }
      return new GameMap(description, pathPoints, path, ImageHelper.makeImage("maps", imageName));
   }

   private String skipComments(Scanner scan, String currentToken) {
      if(isComment(currentToken)) {
         scan.nextLine();
         return skipComments(scan, scan.next());
      }
      return currentToken;
   }
   
   private boolean isComment(String s) {
      if(s.length() < 2) {
         return false;
      }
      return s.substring(0, 2).equals("//");
   }
   
}
