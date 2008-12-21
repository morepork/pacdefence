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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class OverlayButton extends JButton {

   static final Color baseColour = new Color(11, 0, 160);
   static final Color pressedColour = new Color(225, 229, 82);
   static final Color rolloverColour = new Color(237, 201, 0);
   static final Color disabledColour = new Color(188, 188, 188, 100);
   private static final int overlayWidth = 2;
   
   private static final int TOWER_BUTTON_WIDTH = 30;
   // Need the -1 otherwise they overlap slightly
   private static final int UPGRADE_BUTTON_WIDTH = OuterPanel.CONTROLS_WIDTH / 8 - 1;
   
   public OverlayButton(String... foldersAndFileName) {
      this(ImageHelper.makeImage(foldersAndFileName));
   }

   public OverlayButton(BufferedImage image, int width, int height) {
      this(ImageHelper.resize(image, width, height));
   }
   
   public OverlayButton(BufferedImage image) {
      setBorderPainted(false);
      setContentAreaFilled(false);
      setOpaque(false);
      // As when the focus is painted it draws a shadow-like thing
      // that looks very out of place
      setFocusPainted(false);
      setIcons(image);//, overlays, width);
      setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
      setMultiClickThreshhold(10);
   }
   
   public static OverlayButton makeTowerButton(BufferedImage image) {
      return new OverlayButton(image, TOWER_BUTTON_WIDTH, TOWER_BUTTON_WIDTH);
   }

   public static OverlayButton makeUpgradeButton(String imageName) {
      return new OverlayButton(ImageHelper.makeImage("buttons", "upgrades", imageName),
            UPGRADE_BUTTON_WIDTH, UPGRADE_BUTTON_WIDTH);
   }
   
   private void setIcons(BufferedImage image) {
      setIcon(drawOverlay(image, baseColour));
      setRolloverIcon(drawOverlay(image, rolloverColour));
      setPressedIcon(drawOverlay(image, pressedColour));
      setDisabledIcon(drawOverlay(image, disabledColour));
   }
   
   public static ImageIcon drawOverlay(BufferedImage image, Color colour) {
      BufferedImage clone = ImageHelper.cloneImage(image);
      Graphics g = clone.getGraphics();
      g.setColor(colour);
      g.fillRect(0, 0, overlayWidth, image.getHeight() - overlayWidth);
      g.fillRect(overlayWidth, 0, image.getWidth() - overlayWidth, overlayWidth);
      g.fillRect(image.getWidth() - overlayWidth, overlayWidth, overlayWidth,
            image.getHeight() - overlayWidth);
      g.fillRect(0, image.getHeight() - overlayWidth, image.getWidth() - overlayWidth,
            overlayWidth);
      return new ImageIcon(clone);
   }
}
