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
import java.awt.geom.Rectangle2D;
import java.util.List;

import logic.Game;
import logic.Helper;
import sprites.Sprite;


public class WeakenTower extends AbstractTower {
   
   private double extraDamageTicks = Game.CLOCK_TICKS_PER_SECOND / 2;
   private double upgradeIncreaseTicks = Game.CLOCK_TICKS_PER_SECOND / 10;
   private double increaseDamageFactor = 2;
   
   public WeakenTower(Point p, Rectangle2D pathBounds) {
      super(p, pathBounds, "Weaken", 40, 100, 5, 1, 50, 19, true);
   }

   @Override
   public String getSpecial() {
      return Helper.format(extraDamageTicks / Game.CLOCK_TICKS_PER_SECOND, 1) + "s";
   }

   @Override
   public String getSpecialName() {
      return "Weaken time";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Rectangle2D pathBounds) {
      return new BasicBullet(this, dx, dy, turretWidth, range, speed, damage, p, pathBounds){
         @Override
         protected void specialOnHit(Point2D p, Sprite s, List<Sprite> sprites) {
            s.setDamageMultiplier(increaseDamageFactor, (int)extraDamageTicks);
         }
      };
   }

   @Override
   protected void upgradeSpecial() {
      extraDamageTicks += upgradeIncreaseTicks;
   }

}
