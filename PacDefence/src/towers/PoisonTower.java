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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.List;

import sprites.Sprite;


public class PoisonTower extends AbstractTower {
   
   // Half the damage is by poison, and half normal damage at the start
   private static final double baseDamage = 5;
   private double damagePerTick = baseDamage / GameMap.CLOCK_TICKS_PER_SECOND;
   private double poisonTicks = GameMap.CLOCK_TICKS_PER_SECOND;
   private final double poisonTicksUpgrade = poisonTicks * (upgradeIncreaseFactor - 1);
   
   public PoisonTower() {
      this(new Point(), null);
   }
   
   public PoisonTower(Point p, Polygon path) {
      super(p, path, "Poison", 40, 100, 5, baseDamage, 50, 20, "poison.png", "PoisonTower.png");
   }

   @Override
   public String getSpecial() {
      return Helper.format(damagePerTick * GameMap.CLOCK_TICKS_PER_SECOND, 1) + " hp/s for " +
            Helper.format(poisonTicks / GameMap.CLOCK_TICKS_PER_SECOND, 1) + "s";
   }

   @Override
   public String getSpecialName() {
      return "Poison";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Polygon path) {
      return new PoisonBullet(this, dx, dy, turretWidth, range, speed, damage, p, path);
   }

   @Override
   protected void upgradeSpecial() {
      damagePerTick *= upgradeIncreaseFactor;
      poisonTicks += poisonTicksUpgrade;
   }
   
   private class PoisonBullet extends BasicBullet {
      
      private Sprite poisonedSprite = null;
      private double moneyEarnt = 0;
      private int poisonTicksLeft;
      
      public PoisonBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, Polygon path) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p, path);
         poisonTicksLeft = (int)poisonTicks;
      }
      
      @Override
      public void draw(Graphics g) {
         if(poisonedSprite == null) {
            // Sprite should only be drawn if it's yet to hit a sprite
            super.draw(g);
         }
      }
      
      @Override
      public double doTick(List<Sprite> sprites) {
         if(poisonedSprite == null) {
            double tickMoney = super.doTick(sprites);
            if(tickMoney > 0) {
               moneyEarnt += tickMoney;
            } else if(tickMoney == 0) {
               return 0;
            }
            return -1;
         } else {
            if(poisonTicksLeft <= 0 || !poisonedSprite.isAlive()) {
               return moneyEarnt;
            }
            poisonTicksLeft--;
            moneyEarnt += processDamageReport(poisonedSprite.hit(damagePerTick));
            return -1;
         }
      }
      
      @Override
      protected void specialOnHit(Point2D p, Sprite s, List<Sprite> sprites) {
         poisonedSprite = s;
      }
      
   }

}
