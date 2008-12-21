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

package gui;


public class Wrapper<T1, T2> {
   
   private final T1 t1;
   private final T2 t2;
   
   public Wrapper(T1 t1, T2 t2) {
      this.t1 = t1;
      this.t2 = t2;
   }

   
   public T1 getT1() {
      return t1;
   }

   
   public T2 getT2() {
      return t2;
   }
   
}
