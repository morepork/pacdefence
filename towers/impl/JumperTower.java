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
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logic.Helper;
import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;
import util.Vector2D;
import creeps.Creep;
import creeps.Creep.DistanceComparator;


public class JumperTower extends AbstractTower {
   
   private int jumps = 2;
   
   public JumperTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Jumper", 40, 100, 5, 5, 50, 20, true);
   }

   @Override
   public String getSpecial() {
      return String.valueOf(jumps);
   }

   @Override
   public String getSpecialName() {
      return "Jumps";
   }

   @Override
   protected Bullet makeBullet(Vector2D dir, int turretWidth, int range, double speed,
         double damage, Point p, Creep c, List<Shape> pathBounds) {
      return new JumpingBullet(this, dir, turretWidth, range, speed, damage, p, pathBounds, jumps);
   }

   @Override
   protected void upgradeSpecial() {
      jumps++;
   }
   
   private class JumpingBullet extends BasicBullet {
      
      private Creep lastHit;
      private int hitsLeft;
      private final int jumpRange;
      private static final double jumpRangeDividend = 1.5;

      public JumpingBullet(Tower shotBy, Vector2D dir, int turretWidth, int range,
            double speed, double damage, Point p, List<Shape> pathBounds, int jumps) {
         super(shotBy, dir, turretWidth, range, speed, damage, p, pathBounds);
         hitsLeft = jumps;
         jumpRange = (int)(range / jumpRangeDividend);
      }
      
      @Override
      protected void specialOnHit(Point2D p, Creep hitCreep, List<Creep> creeps) {
         if(hitsLeft > 0) {
            // Add the last hit creep as it was removed in doTick() and can now be hit, as it is
            // now the creep hit before last
            if(lastHit != null) {
               creeps.add(lastHit);
            }
            Point point = Helper.toPoint(p);
            // Make it so creeps closest to this point will be targetted first
            Collections.sort(creeps, new DistanceComparator(point, true));
            for(Creep c : creeps) {
               if(!hitCreep.equals(c) && checkDistance(c, point, jumpRange)) {
                  JumpingBullet b = (JumpingBullet) fireBulletsAt(c, point, false, 0, jumpRange,
                        getBulletSpeed(), getDamage()).get(0);
                  b.hitsLeft = hitsLeft - 1;
                  b.lastHit = hitCreep;
                  addExtraBullets(b);
                  break;
               }
            }
         }
      }
      
      @Override
      protected double doTick(List<Creep> creeps) {
         // Remove the last hit creep so that it won't get hit again
         List<Creep> newList = new ArrayList<Creep>(creeps);
         newList.remove(lastHit);
         return super.doTick(newList);
      }
      
   }

}
