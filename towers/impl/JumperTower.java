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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;
import util.Vector2D;
import creeps.Creep;
import creeps.Creep.DistanceComparator;


public class JumperTower extends AbstractTower {
   
   private int jumps = 2;
   
   public JumperTower(Point p) {
      super(p, "Jumper", 40, 100, 5, 5, 50, 20, true);
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
         double damage, Point p, Creep c) {
      return new JumpingBullet(this, dir, turretWidth, range, speed, damage, p, jumps);
   }

   @Override
   protected void upgradeSpecial() {
      jumps++;
   }
   
   private class JumpingBullet extends BasicBullet {
      
      private Creep lastHit;
      private int jumpsLeft;
      private int moneyEarned = 0;

      public JumpingBullet(Tower shotBy, Vector2D dir, int turretWidth, int range,
            double speed, double damage, Point p, int jumps) {
         super(shotBy, dir, turretWidth, range, speed, damage, p);
         jumpsLeft = jumps;
      }
      
      @Override
      protected void specialOnHit(Point2D p, Creep hitCreep, List<Creep> creeps) {
         // If there are jumps left, target the closest creep, so the bullet jumps to it
         if(jumpsLeft > 0) {
            // Can't target the creep that was just hit, but can target the one that was hit
            // previously
            creeps.remove(hitCreep);
            if(lastHit != null) {
               creeps.add(lastHit);
            }
            
            // Make it so creeps closest to this point will be targetted first
            Collections.sort(creeps, new DistanceComparator(p, true));
            for(Creep c : creeps) {
               if(checkDistance(c, p, range)) {
                  super.setDirection(Vector2D.createFromPoints(p, c.getPosition()));
                  distanceTravelled = 0;
               }
            }
         }
         lastHit = hitCreep;
         jumpsLeft--;
      }
      
      @Override
      protected double doTick(List<Creep> creeps) {
         // Remove the last hit creep so that it won't get hit again
         List<Creep> newList = new ArrayList<Creep>(creeps);
         if (lastHit != null) {
            newList.remove(lastHit);
         }
         double result = super.doTick(newList);
         if(result < 0) {
            // Bullet didn't hit anything
            return result;
         } else if(result == 0) {
           // Bullet has reached the edge of its range
           return moneyEarned;
         } else {
            // Bullet hit something
            moneyEarned += result;
            // (jumpsLeft == 0) means the bullet has just done its final jump,
            // so wait until it hits something before finishing it
            if(jumpsLeft < 0) {
               return moneyEarned;
            } else {
               return -1;
            }
         }
      }
      
   }

}
