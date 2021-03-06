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
import towers.DamageNotifier;
import util.Helper;
import util.Vector2D;
import creeps.Creep;


public class WeakenTower extends AbstractTower {
   
   private double extraDamageTicks = Constants.CLOCK_TICKS_PER_SECOND / 2;
   private double upgradeIncreaseTicks = Constants.CLOCK_TICKS_PER_SECOND / 10;
   private double increaseDamageFactor = 2;
   private final DamageNotifier damageNotifier = new DamageNotifier(this);
   
   public WeakenTower(Point p) {
      super(p, "Weaken", 40, 100, 5, 1, 50, 19, true);
   }

   @Override
   public String getSpecial() {
      return Helper.format(extraDamageTicks / Constants.CLOCK_TICKS_PER_SECOND, 1) + "s";
   }

   @Override
   public String getSpecialName() {
      return "Weaken time";
   }

   @Override
   protected Bullet makeBullet(Vector2D dir, int turretWidth, int range, double speed,
         double damage, Point p, Creep c) {
      return new BasicBullet(this, dir, turretWidth, range, speed, damage, p){
         @Override
         protected void specialOnHit(Point2D p, Creep c, List<Creep> creeps) {
            c.setDamageMultiplier(damageNotifier, increaseDamageFactor, (int)extraDamageTicks);
         }
      };
   }

   @Override
   protected void upgradeSpecial() {
      extraDamageTicks += upgradeIncreaseTicks;
   }

}
