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

package towers.impl;

import java.awt.Point;
import java.awt.Shape;
import java.util.List;

import logic.Constants;


public class FreezeTower extends SlowTower {
      
   public FreezeTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Freeze", 40, 100, 5, 1, 50, 22, true, 0,
              Constants.CLOCK_TICKS_PER_SECOND / 2.0);
      // Reduces the speed of the sprites it hits to 0, and lasts for 0.5s
   }

   @Override
   public String getSpecialName() {
      return "Freeze Time";
   }

}
