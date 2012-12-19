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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.List;

import logic.Constants;
import logic.Helper;
import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;
import util.Vector2D;
import creeps.Creep;


public class PoisonTower extends AbstractTower {
   
   // Half the damage is by poison, and half normal damage at the start
   private static final double baseDamage = 10;
   private double damagePerTick = baseDamage / Constants.CLOCK_TICKS_PER_SECOND;
   private double poisonTicks = Constants.CLOCK_TICKS_PER_SECOND;
   private final double poisonTicksUpgrade = Constants.CLOCK_TICKS_PER_SECOND / 10;
   
   public PoisonTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Poison", 40, 100, 5, baseDamage, 50, 20, true);
   }

   @Override
   public String getSpecial() {
      StringBuilder s = new StringBuilder();
      s.append(Helper.format(damagePerTick * Constants.CLOCK_TICKS_PER_SECOND, 2));
      s.append(" hp/s for ");
      s.append(Helper.format(poisonTicks / Constants.CLOCK_TICKS_PER_SECOND, 1));
      s.append("s");
      return s.toString();
   }

   @Override
   public String getSpecialName() {
      return "Poison";
   }
   
   @Override
   protected void upgradeDamage() {
      super.upgradeDamage();
      damagePerTick = getDamage() / Constants.CLOCK_TICKS_PER_SECOND;
   }

   @Override
   protected Bullet makeBullet(Vector2D dir, int turretWidth, int range, double speed,
         double damage, Point p, Creep c, List<Shape> pathBounds) {
      return new PoisonBullet(this, dir, turretWidth, range, speed, damage, p, pathBounds);
   }

   @Override
   protected void upgradeSpecial() {
      poisonTicks += poisonTicksUpgrade;
   }
   
   private class PoisonBullet extends BasicBullet {
      
      private Creep poisonedCreep = null;
      private double moneyEarnt = 0;
      private int poisonTicksLeft;
      
      public PoisonBullet(Tower shotBy, Vector2D dir, int turretWidth, int range,
            double speed, double damage, Point p, List<Shape> pathBounds) {
         super(shotBy, dir, turretWidth, range, speed, damage, p, pathBounds);
         poisonTicksLeft = (int)poisonTicks;
      }
      
      @Override
      public void draw(Graphics g) {
         if(poisonedCreep == null) {
            // Creep should only be drawn if it's yet to hit a creep
            super.draw(g);
         }
      }
      
      @Override
      public double doTick(List<Creep> creeps) {
         if(poisonedCreep == null) {
            double tickMoney = super.doTick(creeps);
            if(tickMoney > 0) {
               moneyEarnt += tickMoney;
            } else if(tickMoney == 0) {
               return 0;
            }
            return -1;
         } else {
            if(poisonTicksLeft <= 0 || !poisonedCreep.isAlive()) {
               return moneyEarnt;
            }
            poisonTicksLeft--;
            // Don't count each poisoning as a new hit
            moneyEarnt += processDamageReport(poisonedCreep.hit(damagePerTick, null));
            return -1;
         }
      }
      
      @Override
      protected void specialOnHit(Point2D p, Creep c, List<Creep> creeps) {
         c.poison(poisonTicksLeft);
         poisonedCreep = c;
      }
      
   }

}
