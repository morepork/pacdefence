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

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class ImageButton extends JButton {

   public ImageButton(String imageName, String extension) {
      setBorderPainted(false);
      setContentAreaFilled(false);
      setButtonImages(imageName, extension, "buttons", imageName + extension);
      setPreferredSize(new Dimension(getIcon().getIconWidth(), getIcon().getIconHeight()));
      setMultiClickThreshhold(10);
   }

   private void setButtonImages(String fileName, String extension, String... foldersAndFileName) {
      
      setIcon(new ImageIcon(ImageHelper.createImageURL(foldersAndFileName)));
      foldersAndFileName[foldersAndFileName.length - 1] = fileName + "Disabled" + extension;
      try {
         setDisabledIcon(new ImageIcon(ImageHelper.createImageURL(foldersAndFileName)));
      } catch (NullPointerException e) {
         // There is no disabled icon, so it'll just use the main one
      }
      foldersAndFileName[foldersAndFileName.length - 1] = fileName + "RolledOver" + extension;
      try {
         setRolloverIcon(new ImageIcon(ImageHelper.createImageURL(foldersAndFileName)));
      } catch (NullPointerException e) {}
      foldersAndFileName[foldersAndFileName.length - 1] = fileName + "Pressed" + extension;
      try {
         setPressedIcon(new ImageIcon(ImageHelper.createImageURL(foldersAndFileName)));
      } catch (NullPointerException e) {}
   }

}
