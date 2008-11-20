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
   private static final BufferedImage[] towerOverlays = new BufferedImage[]{
         ImageHelper.makeImage("buttons", "towers", "TowerOverlay.png"),
         ImageHelper.makeImage("buttons", "towers", "TowerOverlayRolledOver.png"),
         ImageHelper.makeImage("buttons", "towers", "TowerOverlayPressed.png")};
   private static final BufferedImage[] upgradeOverlays = new BufferedImage[]{
         ImageHelper.makeImage("buttons", "upgrades", "UpgradeOverlay.png"),
         ImageHelper.makeImage("buttons", "upgrades", "UpgradeOverlayRolledOver.png"),
         ImageHelper.makeImage("buttons", "upgrades", "UpgradeOverlayPressed.png")};

   private OverlayButton(BufferedImage image, BufferedImage[] overlays, int width) {
      setBorderPainted(false);
      setContentAreaFilled(false);
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

}
