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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class OverlayButton extends JButton {

   private static final int TOWER_BUTTON_WIDTH = 30;
   // Need the -1 otherwise they overlap slightly
   private static final int UPGRADE_BUTTON_WIDTH = OuterPanel.CONTROLS_WIDTH / 8 - 1;
   private static final BufferedImage[] towerOverlays = makeOverlays("TowerOverlay", ".png",
         "buttons", "towers");
   private static final BufferedImage[] upgradeOverlays = makeOverlays("UpgradeOverlay", ".png",
         "buttons", "upgrades");

   private OverlayButton(BufferedImage image, BufferedImage[] overlays, int width) {
      setBorderPainted(false);
      setContentAreaFilled(false);
      setOpaque(false);
      // As when the focus is painted it draws a shadow-like thing
      // that looks very out of place
      setFocusPainted(false);
      setIcons(image, overlays, width);
      setPreferredSize(new Dimension(width, width));
      setMultiClickThreshhold(10);
   }
   
   public static OverlayButton makeTowerButton(BufferedImage image) {
      return new OverlayButton(image, towerOverlays, TOWER_BUTTON_WIDTH);
   }

   public static OverlayButton makeUpgradeButton(String imageName) {
      return new OverlayButton(ImageHelper.makeImage("buttons", "upgrades", imageName),
            upgradeOverlays, UPGRADE_BUTTON_WIDTH);
   }

   private void setIcons(BufferedImage image, BufferedImage[] overlays, int width) {
      setIcon(combine(width, image, overlays[0]));
      setRolloverIcon(combine(width, image, overlays[1]));
      setPressedIcon(combine(width, image, overlays[2]));
      setDisabledIcon(combine(width, image, overlays[3]));
      setPreferredSize(new Dimension(width, width));
   }

   private ImageIcon combine(int width, BufferedImage... images) {
      BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB_PRE);
      Graphics2D g = (Graphics2D)image.getGraphics();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      for (BufferedImage b : images) {
         if (b != null) {
            g.drawImage(b, 0, 0, width, width, null);
         }
      }
      return new ImageIcon(image);
   }
   
   private static BufferedImage[] makeOverlays(String fileName, String extension,
         String... folders) {
      BufferedImage[] overlays = new BufferedImage[4];
      String[] strings = new String[folders.length + 1];
      System.arraycopy(folders, 0, strings, 0, folders.length);
      overlays[0] = setLastStringAndMakeImage(fileName + extension, strings);
      overlays[1] = setLastStringAndMakeImage(fileName + "RolledOver" + extension, strings);
      overlays[2] = setLastStringAndMakeImage(fileName + "Pressed" + extension, strings);
      overlays[3] = setLastStringAndMakeImage(fileName + "Disabled" + extension, strings);
      return overlays;
   }
   
   private static BufferedImage setLastStringAndMakeImage(String s, String[] strings) {
      strings[strings.length - 1] = s;
      return ImageHelper.makeImage(strings);
   }

}
