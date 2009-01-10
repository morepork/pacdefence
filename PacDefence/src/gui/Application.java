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

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import logic.Game;


public class Application {
   
   public static void main(String... args) {
      JFrame frame = new JFrame("Pac Defence");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setResizable(false);
      //frame.add(new OuterPanel());
      new Game(frame);
      frame.pack();
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      // Centres the frame on screen
      frame.setLocation((d.width - frame.getWidth())/2, (d.height - frame.getHeight())/2);
      frame.setVisible(true);
   }

}
