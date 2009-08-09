/*
 * This file is part of Pac Defence.
 * 
 * Pac Defence is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Pac Defence is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Pac Defence. If not,
 * see <http://www.gnu.org/licenses/>.
 * 
 * (C) Liam Byrne, 2008 - 09.
 */

package gui.maps;

import images.ImageHelper;

import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * A helper class for analysing path images to get polygons for their bounds
 * 
 * @author Liam Byrne
 * 
 */
public class PathAnalyser {

   private static final int step = 10;
   // True will make it do runs up/down, false left/right
   private static final boolean upDown = true;

   public static void main(String[] args) {
      BufferedImage image = ImageHelper.makeImage("maps", "curvyEasy.png");
      if(1 == 1) {
         doPathBounds(image);
      } else {
         doPathPoints(image);
      }
   }
   
   /**
    * Runs down from the edges until it hits a non-transparent pixel, and prints the co-ordinates
    * of that, the edge of the path.
    * 
    * @param image
    */
   private static void doPathBounds(BufferedImage image) {
      int lastPos = getLastPos(image);
      // For each coordinate, run down until a non-alpha pixel is hit
      int i;
      for(i = 0; i < lastPos; i += step) {
         printPoint(runIncreasing(image, i));
      }
      // Make sure the far right point is done
      if(i != lastPos - 1) {
         printPoint(runIncreasing(image, lastPos - 1));
      }
      // Do the same in reverse
      for(i = lastPos - 1; i >= 0; i -= step) {
         printPoint(runDecreasing(image, i));
      }
      // Make sure the far left point is done
      if(i != 0) {
         printPoint(runDecreasing(image, 0));
      }
   }
   
   /**
    * Averages runs from opposite edges to get the centre.
    * 
    * @param image
    */
   private static void doPathPoints(BufferedImage image) {
      int lastPos = getLastPos(image);
      int i;
      for(i = 0; i < lastPos; i += step) {
         // Prints the point half way between the top and bottom of the edges of the path
         printAverage(image, i);
      }
      // Make sure the far point is done
      if(i != lastPos - 1) {
         printAverage(image, lastPos - 1);
      }
   }
   
   private static void printAverage(BufferedImage image, int i) {
      Point p1 = runIncreasing(image, i);
      Point p2 = runDecreasing(image, i);
      printPoint(new Point((p1.x + p2.x)/2, (p1.y + p2.y)/2));
   }
   
   private static int getLastPos(BufferedImage image) {
      return upDown ? image.getWidth() : image.getHeight();
   }
   
   private static Point runIncreasing(BufferedImage image, int i) {
      if(upDown) {
         return runDown(image, i);
      } else {
         return runRight(image, i);
      }
   }
   
   private static Point runDecreasing(BufferedImage image, int i) {
      if(upDown) {
         return runUp(image, i);
      } else {
         return runLeft(image, i);
      }
   }

   private static Point runDown(BufferedImage image, int x) {
      int y = 0;
      while(y < image.getHeight() && !ImageHelper.isCompletelyTransparent(image, x, y)) {
         y++;
      }
      return new Point(x, y);
   }

   private static Point runUp(BufferedImage image, int x) {
      int y = image.getHeight() - 1;
      while(y > 0 && !ImageHelper.isCompletelyTransparent(image, x, y)) {
         y--;
      }
      return new Point(x, y);
   }
   
   private static Point runRight(BufferedImage image, int y) {
      int x = 0;
      while(x < image.getWidth() && !ImageHelper.isCompletelyTransparent(image, x, y)) {
         x++;
      }
      return new Point(x, y);
   }
   
   private static Point runLeft(BufferedImage image, int y) {
      int x = image.getWidth() - 1;
      while(x > 0 && !ImageHelper.isCompletelyTransparent(image, x, y)) {
         x--;
      }
      return new Point(x, y);
   }

   private static void printPoint(Point p) {
      System.out.println("<point x=\"" + p.x + "\" y=\"" + p.y + "\" />");
   }

}
