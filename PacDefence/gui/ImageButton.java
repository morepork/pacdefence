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

package gui;

import images.ImageHelper;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class ImageButton extends JButton {

   public ImageButton(String imageName, String extension) {
      this(imageName, extension, false);
   }
   
   public ImageButton(String imageName, String extension, boolean withOverlays) {
      setBorderPainted(false);
      setContentAreaFilled(false);
      setFocusPainted(false);
      setButtonImages(withOverlays, imageName, extension, "buttons", imageName + extension);
      setMultiClickThreshhold(10);
   }

   private void setButtonImages(boolean withOverlays, String fileName, String extension,
         String... foldersAndFileName) {
      BufferedImage mainImage = ImageHelper.makeImage(foldersAndFileName);
      Color nextOverlayColour = OverlayButton.baseColour;
      setIcon(makeIcon(withOverlays, nextOverlayColour, foldersAndFileName));
      try {
         nextOverlayColour = OverlayButton.disabledColour;
         foldersAndFileName[foldersAndFileName.length - 1] = fileName + "Disabled" + extension;
         setDisabledIcon(makeIcon(withOverlays, nextOverlayColour, foldersAndFileName));
      } catch (NullPointerException e) {
         // NummPointerException is thrown if the image isn't found, so if there are
         // no overlays, just have no specific icon set, otherwise put the overlay
         // on the main image.
         if(withOverlays) {
            setDisabledIcon(OverlayButton.drawOverlay(mainImage, nextOverlayColour));
         }
      }
      try {
         nextOverlayColour = OverlayButton.rolloverColour;
         foldersAndFileName[foldersAndFileName.length - 1] = fileName + "RolledOver" + extension;
         setRolloverIcon(makeIcon(withOverlays, nextOverlayColour, foldersAndFileName));
      } catch (NullPointerException e) {
         if(withOverlays) {
            setRolloverIcon(OverlayButton.drawOverlay(mainImage, nextOverlayColour));
         }
      }
      try {
         nextOverlayColour = OverlayButton.pressedColour;
         foldersAndFileName[foldersAndFileName.length - 1] = fileName + "Pressed" + extension;
         setPressedIcon(makeIcon(withOverlays, nextOverlayColour, foldersAndFileName));
      } catch (NullPointerException e) {
         if(withOverlays) {
            setPressedIcon(OverlayButton.drawOverlay(mainImage, nextOverlayColour));
         }
      }
   }
   
   private ImageIcon makeIcon(boolean withOverlay, Color overlayColour,
         String[] foldersAndFileName) {
      BufferedImage image = ImageHelper.makeImage(foldersAndFileName);
      return withOverlay ? OverlayButton.drawOverlay(image, overlayColour) : new ImageIcon(image);
   }

}
