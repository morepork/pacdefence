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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import logic.Game;

@SuppressWarnings("serial")
public class OverlayButton extends JButton {

   static final Color baseColour = new Color(11, 0, 160);
   static final Color pressedColour = new Color(225, 229, 82);
   static final Color rolloverColour = new Color(237, 201, 0);
   static final Color disabledColour = new Color(188, 188, 188, 100);
   private static final int overlayWidth = 2;
   
   private static final int TOWER_BUTTON_WIDTH = 30;
   // Need the -1 otherwise they overlap slightly
   private static final int UPGRADE_BUTTON_WIDTH = Game.CONTROLS_WIDTH / 8 - 1;
   
   private static final Font defaultFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
   private static final FontRenderContext frc = new FontRenderContext(null, true, true);
   
   private static final int defaultSideMargin = 6;
   private static final int defaultTopBottomMargin = 6;
   
   private Font font;
   private Color textColour;
   private float sideMargin;
   private float topBottomMargin;
   private float textHeight;
   
   public OverlayButton(String text) {
      this(text, Color.YELLOW);
   }
   
   public OverlayButton(String text, Color textColour) {
      this(text, 14, textColour, defaultSideMargin, defaultTopBottomMargin);
   }
   
   public OverlayButton(String text, float fontSize) {
      this(text, fontSize, Color.YELLOW, defaultSideMargin, defaultTopBottomMargin);
   }
   
   public OverlayButton(String text, float fontSize, float sideMargin, float topBottomMargin) {
      this(text, fontSize, Color.YELLOW, sideMargin, topBottomMargin);
   }
   
   public OverlayButton(String text, float fontSize, Color textColour, float sideMargin,
         float topBottomMargin) {
      this.font = defaultFont.deriveFont(fontSize);
      this.textColour = textColour;
      this.sideMargin = sideMargin;
      this.topBottomMargin = topBottomMargin;
      // Use W, a full height capital letter so the height doesn't change depending on the string
      textHeight = (float)(new TextLayout("W", font, frc).getBounds().getHeight() +
            topBottomMargin * 2);
      setUpButton();
      setText(text);
   }
   
   public OverlayButton(String... foldersAndFileName) {
      this(ImageHelper.makeImage(foldersAndFileName));
   }

   public OverlayButton(BufferedImage image, int width, int height) {
      this(ImageHelper.resize(image, width, height));
   }

   public OverlayButton(BufferedImage image) {
      setUpButton();
      setIcons(image);
   }
   
   public static OverlayButton makeTowerButton(BufferedImage image) {
      return new OverlayButton(image, TOWER_BUTTON_WIDTH, TOWER_BUTTON_WIDTH);
   }

   public static OverlayButton makeUpgradeButton(String imageName) {
      return new OverlayButton(ImageHelper.makeImage("buttons", "upgrades", imageName),
            UPGRADE_BUTTON_WIDTH, UPGRADE_BUTTON_WIDTH);
   }
   
   @Override
   public void setText(String text) {
      TextLayout tl = new TextLayout(text, font, frc);
      double width = tl.getBounds().getWidth() + sideMargin * 2;
      
      BufferedImage b = new BufferedImage((int)(width + 0.5), (int)(textHeight + 0.5),
            BufferedImage.TYPE_INT_ARGB_PRE);
      Graphics2D g = b.createGraphics();
      g.setColor(textColour);
      // For some reason it seems to draw slightly too far to the right, so correct for this
      tl.draw(g, (float)(sideMargin * 0.9), textHeight - topBottomMargin);
      g.dispose();
      
      setIcons(b);
   }
   
   private void setUpButton() {
      setBorderPainted(false);
      setContentAreaFilled(false);
      setOpaque(false);
      // As when the focus is painted it draws a shadow-like thing that looks very out of place
      setFocusPainted(false);
      setMultiClickThreshhold(10);
   }
   
   private void setIcons(BufferedImage image) {
      setIcon(drawOverlay(image, baseColour));
      setRolloverIcon(drawOverlay(image, rolloverColour));
      setPressedIcon(drawOverlay(image, pressedColour));
      setDisabledIcon(drawOverlay(image, disabledColour));
      setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
      
      validate();
   }
   
   public static ImageIcon drawOverlay(BufferedImage image, Color colour) {
      BufferedImage clone = ImageHelper.cloneImage(image);
      Graphics2D g = clone.createGraphics();
      g.setColor(colour);
      g.fillRect(0, 0, overlayWidth, image.getHeight() - overlayWidth);
      g.fillRect(overlayWidth, 0, image.getWidth() - overlayWidth, overlayWidth);
      g.fillRect(image.getWidth() - overlayWidth, overlayWidth, overlayWidth,
            image.getHeight() - overlayWidth);
      g.fillRect(0, image.getHeight() - overlayWidth, image.getWidth() - overlayWidth,
            overlayWidth);
      g.dispose();
      return new ImageIcon(clone);
   }
}
