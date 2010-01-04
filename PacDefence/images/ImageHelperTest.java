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

package images;


import static org.junit.Assert.assertEquals;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class ImageHelperTest {
   
   private static JFrame frame = new JFrame("ImageHelperTest");
   private static MyPanel panel = new MyPanel();
   private static Scanner scan = new Scanner(System.in);
   private BufferedImage tower;
   private static List<BufferedImage> toDraw = new ArrayList<BufferedImage>();

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      frame.setSize(new Dimension(800, 600));
      frame.add(panel);
      frame.setVisible(true);
      
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
      frame.dispose();
   }

   @Before
   public void setUp() throws Exception {
      tower = ImageHelper.makeImage("towers", "basic.png");
      toDraw.clear();
   }

   @After
   public void tearDown() throws Exception {
      frame.repaint();
      frame.toFront();
      assertEquals(scan.next(), "y");
   }
   
   @Test
   public void testRotatePiOver2() {
      toDraw.add(tower);
      toDraw.add(ImageHelper.rotateImage(tower, Math.PI / 2));
      System.out.println("Are two towers drawn, the right one rotated through pi/2 ? (y/n)");
   }
   
   @Test
   public void testRotateOneRadian() {
      toDraw.add(tower);
      toDraw.add(ImageHelper.rotateImage(tower, 1));
      System.out.println("Are two towers drawn, the right one rotated through 1 radian? (y/n)");
   }
   
   @Test
   public void testClearImage() {
      ImageHelper.clearImage(tower);
      toDraw.add(tower);
      System.out.println("Is the frame blank? (y/n)");
   }
   
   @SuppressWarnings("serial")
   private static class MyPanel extends JPanel {
      
      @Override
      public void paint(Graphics g) {
         int lastX = 5;
         for(BufferedImage b : toDraw) {
            g.drawImage(b, lastX, 5, null);
            lastX += b.getWidth() + 5;
         }
      }
      
   }

}
