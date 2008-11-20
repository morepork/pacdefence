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
import java.awt.geom.Point2D;
import java.util.List;

import sprites.Sprite;


public abstract class SlowTower extends AbstractTower {
   
   protected double slowFactor = 0.75;
   protected int slowTicks = 10;

   public SlowTower(Point p, String name, int fireRate, int range, double bulletSpeed,
         double damage, int width, int turretWidth, String imageName, String buttonImageName) {
      super(p, name, fireRate, range, bulletSpeed, damage, width, turretWidth, imageName,
            buttonImageName);
   }
   
   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p) {
      return new AbstractBullet(this, dx, dy, turretWidth, range, speed, damage, p) {
         @Override
         public void specialOnHit(Point2D p, Sprite s, List<Sprite> sprites) {
            s.slow(slowFactor, slowTicks);
         }         
      };
   }

}
