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

import java.awt.image.BufferedImage;

/**
 * A helper class for analysing path images to get polygons for their bounds
 * 
 * @author Liam Byrne
 * 
 */
public class PathAnalyser {

   private static final int step = 25;

   public static void main(String[] args) {
      BufferedImage image = ImageHelper.makeImage("maps", "curvyMedium.png");
      if(1 == 0) {
         doPathBounds(image);
      } else {
         doPath(image);
      }
   }
   
   private static void doPath(BufferedImage image) {
      int x;
      for(x = 0; x < image.getWidth(); x += step) {
         // Prints the point half way between the top and bottom of the edges of the path
         printAverageY(image, x);
      }
      // Make sure the far right point is done
      if(x != image.getWidth() - 1) {
         printAverageY(image, image.getWidth() - 1);
      }
   }
   
   private static void printAverageY(BufferedImage image, int x) {
      printPoint(x, (runUp(image, x) + runDown(image, x)) / 2);
   }
   
   private static void doPathBounds(BufferedImage image) {
      // For each x coordinate, run down until a non-alpha pixel is hit
      int x;
      for(x = 0; x < image.getWidth(); x += step) {
         printPoint(x, runDown(image, x));
      }
      // Make sure the far right point is done
      if(x != image.getWidth() - 1) {
         printPoint(x, runDown(image, image.getWidth() - 1));
      }
      // Do the same in reverse
      for(x = image.getWidth() - 1; x >= 0; x -= step) {
         printPoint(x, runUp(image, x));
      }
      // Make sure the far left point is done
      if(x != 0) {
         printPoint(x, runUp(image, 0));
      }
   }

   private static int runDown(BufferedImage image, int x) {
      int y = 0;
      while(image.getRGB(x, y) == 0 && y <= image.getHeight()) {
         y++;
      }
      return y;
   }

   private static int runUp(BufferedImage image, int x) {
      int y = image.getHeight() - 1;
      while(image.getRGB(x, y) == 0 && y >= 0) {
         y--;
      }
      return y;
   }

   private static void printPoint(int x, int y) {
      System.out.println("<point x=\"" + x + "\" y=\"" + y + "\" />");
   }

}
