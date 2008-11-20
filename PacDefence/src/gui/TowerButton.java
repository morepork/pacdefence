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
   
   private static final int TOWER_BUTTON_WIDTH = 30;
   private static final int UPGRADE_BUTTON_WIDTH = OuterPanel.CONTROLS_WIDTH / 8;
   private static final BufferedImage normalOverlay = ImageHelper.makeImage("buttons", "towers",
         "TowerOverlay.png");
   private static final BufferedImage rolledOverOverlay = ImageHelper.makeImage("buttons",
         "towers", "TowerOverlayRolledOver.png");
   private static final BufferedImage pressedOverlay = ImageHelper.makeImage("buttons", "towers",
         "TowerOverlayPressed.png");
   
   public TowerButton(BufferedImage image) {
      this(image, TOWER_BUTTON_WIDTH);
   }
   
   public TowerButton(BufferedImage image, int width) {
      setBorderPainted(false);
      setContentAreaFilled(false);
      setIcons(image, width);
      setMultiClickThreshhold(10);
   }
   
   public static TowerButton makeUpgradeButton(String imageName) {
      return new TowerButton(ImageHelper.makeImage("buttons", "upgrades", imageName),
            UPGRADE_BUTTON_WIDTH);
   }
   
   private void setIcons(BufferedImage image, int width) {
      setIcon(combine(width, image, normalOverlay));
      setRolloverIcon(combine(width, image, rolledOverOverlay));
      setPressedIcon(combine(width, image, pressedOverlay));
      setPreferredSize(new Dimension(width, width));
   }
   
   private ImageIcon combine(int width, BufferedImage... images) {
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
