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

import java.awt.Dimension;

import javax.swing.JApplet;

import logic.Constants;

@SuppressWarnings("serial")
public class Applet extends JApplet {

   private static boolean isApplet = false;
   
   private boolean debugTimes;
   private PacDefence pacDefence;
   
   @Override
   public void init() {
      isApplet = true;
      String debugTimesParam = getParameter("DebugTimes");
      debugTimes = debugTimesParam == null ? false : Boolean.parseBoolean(debugTimesParam);
      
      startGame();
   }
   
   @Override
   public void start() {
      if(pacDefence == null) {
         startGame();
      }
   }
   
   @Override
   public void destroy() {
      removeAll();
      pacDefence.end();
      pacDefence = null;
   }
   
   public static boolean isApplet() {
      return isApplet;
   }
   
   private void startGame() {
      pacDefence = new PacDefence(this, debugTimes, false);
      setSize(new Dimension(Constants.WIDTH, Constants.HEIGHT));
      setVisible(true);
   }

}
