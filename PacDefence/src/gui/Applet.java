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

import javax.swing.JApplet;

import logic.Game;

@SuppressWarnings("serial")
public class Applet extends JApplet {
   
   private Game game;
   
   @Override
   public void init() {
      final JApplet ja = this;
      // Start in its own thread so as not to slow the page loading down
      new Thread() {
         public void run() {
            game = new Game(ja, false, false);
            setSize(new Dimension(Game.WIDTH, Game.HEIGHT));
            setVisible(true);
         }
      }.start();
   }
   
   @Override
   public void destroy() {
      removeAll();
      game.end();
      game = null;
   }

}
