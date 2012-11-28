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

package towers.impl;

import java.awt.Point;
import java.awt.Shape;
import java.util.List;

import logic.Constants;


public class SlowLengthTower extends SlowTower {
   
   public SlowLengthTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Slow", 40, 100, 5, 1, 50, 23, true, 0.5,
              Constants.CLOCK_TICKS_PER_SECOND);
      // Halves the speed of the creeps it hits, and lasts for 1s
   }

   @Override
   public String getSpecialName() {
      return "Slow Length";
   }

}
