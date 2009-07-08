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

import images.ImageHelper;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

// You need to manually tell this to toggle icons
@SuppressWarnings("serial")
public class OverlayToggleButton extends OverlayButton {
   
   private ImageIcon[][] icons;
   private int currentIndex = 0;
   
   public OverlayToggleButton(BufferedImage... images) {
      this(images[0].getWidth(), images[0].getHeight(), images);
   }
   
   public OverlayToggleButton(int width, int height, BufferedImage... images) {
      super(images[0], width, height);
      icons = new ImageIcon[images.length][4];
      for(int i = 0; i < images.length; i++) {
         icons[i][0] = drawOverlay(ImageHelper.resize(images[i], width, height), baseColour);
         icons[i][1] = drawOverlay(ImageHelper.resize(images[i], width, height), rolloverColour);
         icons[i][2] = drawOverlay(ImageHelper.resize(images[i], width, height), pressedColour);
         icons[i][3] = drawOverlay(ImageHelper.resize(images[i], width, height), disabledColour);
      }
   }
   
   public void setToDefault() {
      currentIndex = 0;
      setIcons();
   }
   
   public void toggleIcons(boolean toNext) {
      if(toNext) {
         currentIndex++;
      } else { // Don't subtract one as if on 0 it will be left negative, which ain't good
         currentIndex += icons.length - 1;
      }
      currentIndex %= icons.length;
      setIcons();
   }
   
   private void setIcons() {
      setIcon(icons[currentIndex][0]);
      setRolloverIcon(icons[currentIndex][1]);
      setPressedIcon(icons[currentIndex][2]);
      setDisabledIcon(icons[currentIndex][3]);
   }

}
