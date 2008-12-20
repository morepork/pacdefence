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

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Title extends JPanel {
   
   private final BufferedImage background;
   
   public Title(int width, int height, ActionListener continueListener) {
      super(new BorderLayout());
      background = ImageHelper.makeImage(width, height, "other", "title.png");
      JButton continueButton = new OverlayButton("buttons", "continue.png");
      continueButton.addActionListener(continueListener);
      add(SwingHelper.createWrapperPanel(continueButton, 10), BorderLayout.SOUTH);
   }
   
   @Override
   public void paintComponent(Graphics g) {
      g.drawImage(background, 0, 0, null);
   }

}
