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
import java.awt.image.BufferedImage;

import javax.swing.JRadioButton;

@SuppressWarnings("serial")
public class OverlayRadioButton extends JRadioButton {
   
   public OverlayRadioButton(BufferedImage b) {
      this(b, b.getWidth(), b.getHeight());
   }
   
   public OverlayRadioButton(BufferedImage b, int width, int height) {
      b = ImageHelper.resize(b, width, height);
      setIcon(OverlayButton.drawOverlay(b, OverlayButton.baseColour));
      setRolloverIcon(OverlayButton.drawOverlay(b, OverlayButton.rolloverColour));
      setPressedIcon(OverlayButton.drawOverlay(b, OverlayButton.pressedColour));
      setSelectedIcon(OverlayButton.drawOverlay(b, Color.GREEN));
      setBorderPainted(false);
      setContentAreaFilled(false);
      setOpaque(false);
   }

}
