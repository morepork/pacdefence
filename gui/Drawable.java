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

package gui;

import java.awt.Graphics2D;


public interface Drawable {

   /**
    * The Z coordinate of a Drawable object.
    * 
    * Objects with higher Z coordinate are drawn later, and are thus shown on
    * top of objects with lower Z.
    */
   public enum ZCoordinate {
      Tower,
      Ghost,
      Creep,
      SelectedTower,
      Bullet,
   }
   
   public void draw(Graphics2D g);
   public ZCoordinate getZ();

}