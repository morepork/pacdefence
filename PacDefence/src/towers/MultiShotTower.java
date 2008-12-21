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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import sprites.Sprite;


public class MultiShotTower extends AbstractTower {
   
   private static final double speedDividend = 1.07;
   private int shots = 5;
   
   public MultiShotTower() {
      this(new Point(), null);
   }
   
   public MultiShotTower(Point p, Rectangle2D pathBounds) {
      super(p, pathBounds, "Multi-Shot", 40, 100, 7.5, 1.3, 50, 5, "multiShot.png",
            "MultiShotTower.png");
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
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Rectangle2D pathBounds) {
      return new BasicBullet(this, dx, dy, turretWidth, range, speed, damage, p, pathBounds);
   }

   @Override
   protected void upgradeSpecial() {
      shots++;
   }
   
   @Override
   protected List<Bullet> makeBullets(double dx, double dy,  int turretWidth, int range,
         double bulletSpeed, double damage, Point p, Sprite s, Rectangle2D pathBounds) {
      List<Bullet> bullets = new ArrayList<Bullet>();
      for(int i = 0; i < shots; i++) {
         bullets.add(makeBullet(dx, dy, turretWidth, range, bulletSpeed, damage, p, s, pathBounds));
         bulletSpeed /= speedDividend;
      }
      return bullets;
   }

}
