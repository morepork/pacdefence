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

import java.awt.Point;
import java.text.DecimalFormat;


public class SlowFactorTower extends SlowTower {
   
   private static final DecimalFormat TWO_DP = new DecimalFormat("0.00");
   
   public SlowFactorTower() {
      this(new Point());
   }
   
   public SlowFactorTower(Point p) {
      super(p, "Slow (factor)", 40, 100, 5, 1, 50, 23, "slowFactor.png", "SlowFactorTower.png");
   }

   @Override
   public String getSpecial() {
      return TWO_DP.format(slowFactor);
   }

   @Override
   public String getSpecialName() {
      return "Slow Factor";
   }

   @Override
   protected void upgradeSpecial() {
      slowFactor /= upgradeIncreaseFactor;
   }

}