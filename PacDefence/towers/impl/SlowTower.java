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

package towers.impl;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;

import logic.Game;
import logic.Helper;
import sprites.Sprite;
import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;


public abstract class SlowTower extends AbstractTower {
   
   // The amount this tower slows sprites by, generally in the range 0 - 1
   protected double slowFactor;
   // The number of ticks the slow effect lasts for
   protected double slowTicks;
   private final double upgradeIncreaseTicks;

   protected SlowTower(Point p, List<Shape> pathBounds, String name, int fireRate, int range,
         double bulletSpeed, double damage, int width, int turretWidth, boolean hasOverlay,
         double slowFactor, double slowTicks) {
      super(p, pathBounds, name, fireRate, range, bulletSpeed, damage, width, turretWidth,
            hasOverlay);
      this.slowFactor = slowFactor;
      this.slowTicks = slowTicks;
      // This makes the increase decent, the base increase is just not enough
      // Also having it increase by a fixed amount, rather than go up exponentially stops freeze
      // towers being ridiculously powerful if they get upgraded a lot
      upgradeIncreaseTicks = slowTicks * 4 * (upgradeIncreaseFactor - 1);
   }

   @Override
   public String getSpecial() {
      return Helper.format(slowTicks / Game.CLOCK_TICKS_PER_SECOND, 1) + "s";
   }
   
   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, List<Shape> pathBounds) {
      return new BasicBullet(this, dx, dy, turretWidth, range, speed, damage, p, pathBounds) {
         @Override
         public void specialOnHit(Point2D p, Sprite s, List<Sprite> sprites) {
            s.slow(slowFactor, (int)slowTicks);
         }
      };
   }

   @Override
   protected void upgradeSpecial() {
      slowTicks += upgradeIncreaseTicks;
   }

}
