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

package util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Arc2D;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class CircleTest {
   
   private static final Circle c = new Circle();
   private static final Arc2D a = new Arc2D.Double();
   
   @SuppressWarnings("serial")
   public static void main(String... args) {
      // Try this for the different arc types
      a.setArcByCenter(250, 250, 100, 30, 50, Arc2D.OPEN);
      c.setRadius(25);
      JFrame frame = new JFrame("Circle Test");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      final JPanel panel = new JPanel() {
         @Override
         public void paintComponent(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.BLACK);
            ((Graphics2D) g).draw(a);
            ((Graphics2D) g).draw(c);
         }
      };
      panel.addMouseMotionListener(new MouseMotionListener(){
         @Override
         public void mouseMoved(MouseEvent e) {
            c.setCentre(e.getPoint());
            panel.repaint();
         }
         @Override
         public void mouseDragged(MouseEvent e) {
            c.setCentre(e.getPoint());
            panel.repaint();
         }
      });
      panel.addMouseListener(new MouseAdapter(){
         @Override
         public void mouseReleased(MouseEvent e) {
            c.setCentre(e.getPoint());
            System.out.println("Circle.intersects(Arc2D) returns " + c.intersects(a));
         }
      });
      frame.add(panel);
      frame.setSize(600, 600);
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      // Centres the frame on screen
      frame.setLocation((d.width - frame.getWidth())/2, (d.height - frame.getHeight())/2);
      frame.setVisible(true);
   }

}
