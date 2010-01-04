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

package logic;

import static org.junit.Assert.*;

import org.junit.Test;


public class HelperTest {

   @Test
   public void testVectorAngle() {
      for(double a = 0; a < 2 * Math.PI; a += 0.001) {
         double x = Math.sin(a);
         double y = Math.cos(a);
         double v = Helper.vectorAngle(x, y);
         if(doesNotEqual(a, v, 0.0001)) {
            fail(a + " != " + v + " for x=" + x + " & y=" + y);
         }
      }
   }
   
   private boolean doesNotEqual(double d1, double d2, double tolerance) {
      return Math.abs(d1 - d2) > tolerance;
   }

}
