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

package towers;

import gui.GameMap;
import gui.Helper;

import java.awt.Point;
import java.awt.Polygon;


public class FreezeTower extends SlowTower {
   
   private final double upgradeIncreaseTicks = GameMap.CLOCK_TICKS_PER_SECOND / 10;
      
   public FreezeTower() {
      this(new Point(), null);
   }
   
   public FreezeTower(Point p, Polygon path) {
      super(p, path, "Freeze", 40, 100, 5, 1, 50, 22, "freeze.png", "FreezeTower.png");
      slowTicks = GameMap.CLOCK_TICKS_PER_SECOND / 2.0;
      slowFactor = 0;
   }

   @Override
   public String getSpecial() {
      return Helper.format(slowTicks / GameMap.CLOCK_TICKS_PER_SECOND, 1) + "s";
   }

   @Override
   public String getSpecialName() {
      return "Freeze Time";
   }

   @Override
   protected void upgradeSpecial() {
      slowTicks += upgradeIncreaseTicks;
   }

}
