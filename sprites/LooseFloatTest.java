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

package sprites;

import junit.framework.Assert;

import org.junit.Test;


// Not really the greatest test method...
public class LooseFloatTest {
   
   @Test
   public void testPointOneShouldNotEqual() {
      for(int i = 0; i < 100; i++) {
         double random = Math.random();
         LooseFloat lf1 = new PointOne(random);
         LooseFloat lf2 = new PointOne(random + 0.11 + Math.random());
         Assert.assertFalse(lf1.equals(lf2));
      }
   }
   
   @Test
   public void testPointOneShouldEqual() {
      for(int i = 0; i < 100; i++) {
         double random = Math.random();
         LooseFloat lf1 = new PointOne(random);
         LooseFloat lf2 = new PointOne(random + Math.random() * 0.2);
         compare(lf1, lf2);
      }
   }
   
   private void compare(LooseFloat lf1, LooseFloat lf2) {
      if(lf1.equals(lf2)) {
         Assert.assertEquals(lf1.compareTo(lf2), 0);
         Assert.assertEquals(lf1.hashCode(), lf2.hashCode());
      }
   }
   
   private class PointOne extends LooseFloat {

      public PointOne(float f) {
         super(f);
      }
      
      public PointOne(double d) {
         super(d);
      }

      @Override
      protected float getPrecision() {
         return 0.1F;
      }
      
   }

}
