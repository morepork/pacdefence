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
 *  (C) Liam Byrne, 2008 - 2012.
 */

package logic;


public class Constants {

   public static final int WIDTH = 800;
   public static final int HEIGHT = 600;
   public static final int MAP_WIDTH = WIDTH - 200;
   public static final int MAP_HEIGHT = HEIGHT;
   public static final int CONTROLS_WIDTH = WIDTH - MAP_WIDTH;
   public static final int CONTROLS_HEIGHT = MAP_HEIGHT;

   // Time between each update in ms
   public static final int CLOCK_TICK = 30;
   public static final double CLOCK_TICKS_PER_SECOND = (double)1000 / CLOCK_TICK;

}
