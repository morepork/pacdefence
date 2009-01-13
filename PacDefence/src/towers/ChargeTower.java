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
import java.awt.Shape;
import java.util.List;

import logic.Game;
import logic.Helper;
import sprites.Sprite;


public class ChargeTower extends AbstractTower {
   
   private double maxDamageMultiplier = 5;
   private double nextDamageMultiplier = 1;
   private static final int ticksToCharge = (int)(2 * Game.CLOCK_TICKS_PER_SECOND);

   public ChargeTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Charge", 40, 100, 5, 10, 50, 16, true);
   }
   
   @Override
   public List<Bullet> tick(List<Sprite> sprites, boolean levelInProgress) {
      List<Bullet> bullets = super.tick(sprites, levelInProgress);
      if(bullets.isEmpty()) {
         // Only charge during levels
         if(levelInProgress && getTimeToNextShot() <= 0) {
            increaseNextDamageMultiplier();
         }
      } else {
         nextDamageMultiplier = 1;
      }
      return bullets;
   }
   
   @Override
   public String getSpecial() {
      StringBuilder s = new StringBuilder();
      s.append(Helper.format(getNextDamage(), 0));
      s.append(" (");
      s.append(Helper.format(maxDamageMultiplier * getDamage(), 0));
      s.append(")");
      return s.toString();
   }

   @Override
   public String getSpecialName() {
      return "Damage (Max)";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, List<Shape> pathBounds) {
      return new BasicBullet(this, dx, dy, turretWidth, range, speed, getNextDamage(), p, pathBounds);
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
