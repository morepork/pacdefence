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

package towers.impl;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import logic.Constants;
import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import util.Helper;
import util.Vector2D;
import creeps.Creep;


public abstract class SlowTower extends AbstractTower {
   
   // The amount this tower slows creeps by, generally in the range 0 - 1
   protected double slowFactor;
   // The number of ticks the slow effect lasts for
   protected double slowTicks;
   private final double upgradeIncreaseTicks;

   protected SlowTower(Point p, String name, int fireRate, int range, double bulletSpeed,
         double damage, int width, int turretWidth, boolean hasOverlay, double slowFactor,
         double slowTicks) {
      super(p, name, fireRate, range, bulletSpeed, damage, width, turretWidth, hasOverlay);
      this.slowFactor = slowFactor;
      this.slowTicks = slowTicks;
      // This makes the increase decent, the base increase is just not enough
      // Also having it increase by a fixed amount, rather than go up exponentially stops freeze
      // towers being ridiculously powerful if they get upgraded a lot
      upgradeIncreaseTicks = slowTicks * 4 * (upgradeIncreaseFactor - 1);
   }

   @Override
   public String getSpecial() {
      return Helper.format(slowTicks / Constants.CLOCK_TICKS_PER_SECOND, 1) + "s";
   }
   
   @Override
   protected Bullet makeBullet(Vector2D dir, int turretWidth, int range, double speed,
         double damage, Point p, Creep c) {
      return new BasicBullet(this, dir, turretWidth, range, speed, damage, p) {
         @Override
         public void specialOnHit(Point2D p, Creep c, List<Creep> creeps) {
            c.slow(slowFactor, (int)slowTicks);
         }
      };
   }

   @Override
   protected void upgradeSpecial() {
      slowTicks += upgradeIncreaseTicks;
   }

}
