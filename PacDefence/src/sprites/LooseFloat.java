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

package sprites;


public abstract class LooseFloat implements Comparable<LooseFloat> {
   
   private final Float f;
   
   public LooseFloat(float f) {
      this.f = f;
   }
   
   public LooseFloat(double d) {
      this.f = (float)d;
   }
   
   @Override
   public boolean equals(Object obj) {
      if(obj instanceof LooseFloat) {
         return Math.abs(this.f - ((LooseFloat)obj).f) < getPrecision();
      } else {
         return false;
      }
   }

   @Override
   public int compareTo(LooseFloat lf) {
      return (int)((this.f - lf.f) / getPrecision());
   }
   
   protected abstract float getPrecision();
   
}
