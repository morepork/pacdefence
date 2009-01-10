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

package towers;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import sprites.Sprite;


public class ScatterTower extends AbstractTower {
   
   private int shots = 2;
   
   public ScatterTower(Point p, Rectangle2D pathBounds) {
      super(p, pathBounds, "Scatter", 40, 100, 5, 4.5, 50, 0, true);
   }

   @Override
   public String getSpecial() {
      return Integer.toString(shots);
   }

   @Override
   public String getSpecialName() {
      return "Shots";
   }
   
   @Override
   protected List<Bullet> fireBullets(List<Sprite> sprites) {
      List<Bullet> fired = new ArrayList<Bullet>();
      for (Sprite s : sprites) {
         if (checkDistance(s)) {
            fired.addAll(fireBulletsAt(s, false));
            if(fired.size() >= shots) {
               return fired;
            }
         }
      }
      return fired;
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Rectangle2D pathBounds) {
      return new BasicBullet(this, dx, dy, turretWidth, range, speed, damage, p, pathBounds);
   }

   @Override
   protected void upgradeSpecial() {
      shots++;
   }

}
