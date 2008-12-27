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

package images;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class ImageHelper {
   
   public static URL createImageURL(String... foldersAndFileName) {
      String path = "";
      for (String s : foldersAndFileName) {
         path += s + "/";
      }
      // Removes the extra "/"
      path = path.substring(0, path.length() - 1);
      // System.out.println(path);
      return new ImageHelper().getClass().getResource(path);
   }

   public static BufferedImage makeImage(String... foldersAndFileName) {
      URL imageURL = createImageURL(foldersAndFileName);
      if (imageURL == null) {
         throw new IllegalArgumentException("Image: '" +
               foldersAndFileName[foldersAndFileName.length - 1] + "' not found.");
      }
      try {
         return ImageIO.read(imageURL);
         // return ImageIO.read(new File(path));
      } catch (IOException e) {
         throw new RuntimeException("Error occured while reading: " + imageURL);
      }
   }

   public static BufferedImage makeImage(int width, int height, String... foldersAndFileName) {
      return resize(makeImage(foldersAndFileName), width, height);
   }

   /**
    * Makes a new BufferedImage that is a scaled version of that given with
    * specified quality.
    * 
    * @param image
    *        The image to scale.
    * @param width
    *        The width to scale the image to.
    * @param height
    *        The height to scale the image to.
    * @return A scaled version of image.
    */
   public static BufferedImage resize(BufferedImage image, int width, int height) {
      BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
      Graphics2D g = temp.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.drawImage(image, 0, 0, width, height, null);
      return temp;
   }

   public static BufferedImage rotateImage(BufferedImage image, double angle) {
      BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(),
            BufferedImage.TYPE_INT_ARGB_PRE);
      AffineTransform at = AffineTransform.getRotateInstance(angle, image.getWidth() / 2,
            image.getHeight() / 2);
      Graphics2D g = (Graphics2D) temp.getGraphics();
      g.setTransform(at);
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.drawImage(image, 0, 0, null);
      return temp;
   }

   public static BufferedImage rotateImage(BufferedImage image, double dx, double dy) {
      return rotateImage(image, vectorAngle(dx, dy));
   }
   
   public static double vectorAngle(double dx, double dy) {
      if (dx > 0) {
         if (dy > 0) {
            return Math.atan(dx / dy);
         } else {
            return Math.atan(-dy / dx) + Math.PI / 2;
         }
      } else {
         if (dy > 0) {
            return 2 * Math.PI - Math.atan(-dx / dy);
         } else {
            return Math.atan(dx / dy) + Math.PI;
         }
      }
   }
   
   public static BufferedImage cloneImage(BufferedImage image) {
      int type = image.getType();
      if(type == BufferedImage.TYPE_CUSTOM) {
         type = BufferedImage.TYPE_INT_ARGB_PRE;
      }
      BufferedImage clone = new BufferedImage(image.getWidth(), image.getHeight(), type);
      clone.getGraphics().drawImage(image, 0, 0, null);
      return clone;
   }
   
   public static void clearImage(BufferedImage image) {
      WritableRaster r = image.getRaster();
      Object o = image.getColorModel().getDataElements(0, null);
      for(int x = 0; x < image.getWidth(); x++) {
         for(int y = 0; y < image.getHeight(); y++) {
            r.setDataElements(x, y, o);
         }
      }
   }
}
