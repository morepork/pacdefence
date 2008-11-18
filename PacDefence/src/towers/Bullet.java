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

import java.awt.Graphics;
import java.util.List;

import sprites.Sprite;


public interface Bullet {
   
   /**
    * 
    * @param sprites
    * @return The money earnt for the bullet hitting, 0 if it reaches the edge
    *         of its range and -1 if the Bullet is still going
    */
   public double tick(List<Sprite> sprites);
   
   public void draw(Graphics g);
   
   public double getDamage();
   

}
