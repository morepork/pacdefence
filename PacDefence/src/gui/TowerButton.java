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
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;


public class TowerButton extends JButton {
   
   private static final int width = 30;
   private static final BufferedImage normalOverlay = ImageHelper.makeImage("buttons",
         "TowerOverlay.png");
   private static final BufferedImage rolledOverOverlay = ImageHelper.makeImage("buttons",
         "TowerOverlayRolledOver.png");
   private static final BufferedImage pressedOverlay = ImageHelper.makeImage("buttons",
         "TowerOverlayPressed.png");
   
   public TowerButton(BufferedImage image) {
      setBorderPainted(false);
      setContentAreaFilled(false);
      setIcon(combine(image, normalOverlay));
      setRolloverIcon(combine(image, rolledOverOverlay));
      setPressedIcon(combine(image, pressedOverlay));
      setPreferredSize(new Dimension(width, width));
      setMultiClickThreshhold(10);
   }
   
   private ImageIcon combine(BufferedImage... images) {
      BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB_PRE);
      Graphics g = image.getGraphics();
      for(BufferedImage b : images) {
         if(b != null) {
            g.drawImage(b, 0, 0, width, width, null);
         }
      }
      return new ImageIcon(image);
   }

}
