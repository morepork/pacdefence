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

import gui.GameMap;
import gui.Helper;

import java.awt.Point;
import java.awt.Polygon;
import java.util.List;

import sprites.Sprite;


public class ChargeTower extends AbstractTower {
   
   private double maxDamageMultiplier = 2;
   private double nextDamageMultiplier = 1;
   private static final int ticksToCharge = (int)(2 * GameMap.CLOCK_TICKS_PER_SECOND);

   public ChargeTower() {
      this(new Point(), null);
   }
   
   public ChargeTower(Point p, Polygon path) {
      super(p, path, "Charge", 40, 100, 5, 8, 50, 16, "charge.png", "ChargeTower.png");
   }
   
   @Override
   public List<Bullet> tick(List<Sprite> sprites) {
      List<Bullet> bullets = super.tick(sprites);
      if(bullets.isEmpty()) {
         if(timeToNextShot <= 0) {
            increaseNextDamageMultiplier();
         }
      } else {
         nextDamageMultiplier = 1;
      }
      return bullets;
   }
   
   @Override
   public String getSpecial() {
      return Helper.format(getNextDamage(), 2) + " (" +
            Helper.format(maxDamageMultiplier * getDamage(), 2) + ")";
   }

   @Override
   public String getSpecialName() {
      return "Damage (Max)";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Polygon path) {
      return new BasicBullet(this, dx, dy, turretWidth, range, speed, getNextDamage(), p, path);
   }

   @Override
   protected void upgradeSpecial() {
      maxDamageMultiplier *= upgradeIncreaseFactor;
   }
   
   private double getNextDamage() {
      return nextDamageMultiplier * getDamage();
   }
   
   private void increaseNextDamageMultiplier() {
      if(nextDamageMultiplier < maxDamageMultiplier) {
         nextDamageMultiplier += (maxDamageMultiplier - 1) / ticksToCharge;
         if(nextDamageMultiplier > maxDamageMultiplier) {
            nextDamageMultiplier = maxDamageMultiplier;
         }
      }
   }

}
