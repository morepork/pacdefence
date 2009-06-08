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

package sprites;


public abstract class LooseFloat implements Comparable<LooseFloat> {
   
   private final Double value;
   
   public LooseFloat(float f) {
      value = Math.floor(f / getPrecision());
   }
   
   public LooseFloat(double d) {
      this((float)d);
   }
   
   @Override
   public boolean equals(Object obj) {
      if(obj instanceof LooseFloat) {
         return value.equals(((LooseFloat) obj).value);
      }
      return false;
   }
   
   @Override
   public int hashCode() {
      return value.hashCode();
   }
   
   @Override
   public int compareTo(LooseFloat lf) {
      return (int)(value - lf.value);
   }
   
   protected abstract float getPrecision();
   
}
