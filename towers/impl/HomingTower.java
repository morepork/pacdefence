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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logic.Constants;
import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;
import util.Helper;
import util.Vector2D;
import creeps.Creep;
import creeps.Creep.DistanceComparator;


public class HomingTower extends AbstractTower {
   
   // Max angle the bullet redirects each tick in degrees
   private double maxRedirectAngle = 2;
   // Only increase the redirect angle by a fixed amount otherwise it quickly gets silly
   private final double upgradeIncreaseAngle = maxRedirectAngle * (upgradeIncreaseFactor - 1);

   public HomingTower(Point p) {
      super(p, "Homing", 40, 100, 3, 20, 50, 18, true);
   }
   
   @Override
   public String getSpecial() {
      // \u00b0 is the degree symbol
      return Helper.format(maxRedirectAngle * Constants.CLOCK_TICKS_PER_SECOND, 1) + "\u00b0/s";
   }

   @Override
   public String getSpecialName() {
      return "Angle Change";
   }

   @Override
   protected Bullet makeBullet(Vector2D dir, int turretWidth, int range, double speed,
         double damage, Point p, Creep c) {
      return new HomingBullet(this, dir, turretWidth, range, speed, damage, p, c, maxRedirectAngle);
   }

   @Override
   protected void upgradeSpecial() {
      maxRedirectAngle += upgradeIncreaseAngle;
   }
   
   private class HomingBullet extends BasicBullet {
      
      private Creep target;
      // This is in radians for convenience
      private final double maxRedirectAngle;
      
      private HomingBullet(Tower shotBy, Vector2D dir, int turretWidth, int range,
            double speed, double damage, Point p, Creep c, double maxRedirectAngle) {
         super(shotBy, dir, turretWidth, range, speed, damage, p);
         target = c;
         this.maxRedirectAngle = Math.toRadians(maxRedirectAngle);
      }
      
      @Override
      protected double doTick(List<Creep> creeps) {
         double value = super.doTick(creeps);
         
         selectNewTarget(creeps); // Selects a new target if necessary
         retarget();
         
         return value;
      }
      
      @Override
      protected boolean isOutOfRange() {
         return getCentre().distance(position) > range + turretWidth;
      }
      
      @Override
      protected boolean canBulletBeRemovedAsOffScreen() {
         return false; // The bullet homes so can come back on screen
      }
      
      private void selectNewTarget(List<Creep> creeps) {
         // If the bullet can't target its current target, find the closest creep that it can
         if(!canTarget(target)) {
            // Copy the list as the passed list can't be modified
            creeps = new ArrayList<Creep>(creeps);
            Collections.sort(creeps, new DistanceComparator(position, true));
            
            for(Creep c : creeps) {
               if(canTarget(c)) {
                  target = c;
                  break;
               }
            }
         }
      }
      
      private void retarget() {
         // Make sure can actually target this creep, this may still be false as their could be no
         // creeps that it can target
         if(canTarget(target)) {
            double currentAngle = dir.getAngle();
            double angleToTarget = Vector2D.angle(position, target.getPosition());
            // Normalise the angle to between -pi and pi so that deltaAngle
            // is actually the best change in angle
            double deltaAngle = normaliseAngle(angleToTarget - currentAngle);
            double angleToChangeTo;
            if(Math.abs(deltaAngle) <= maxRedirectAngle) {
               angleToChangeTo = angleToTarget;
            } else {
               angleToChangeTo = currentAngle + (deltaAngle > 0 ? 1 : -1) * maxRedirectAngle;
            }
            setDirection(Vector2D.createFromAngle(angleToChangeTo, 1));
         }
      }
      
      private boolean canTarget(Creep c) {
         return c.isAlive() && !c.isFinished() && checkDistance(c);
      }
      
      private double normaliseAngle(double angle) {
         // This shouldn't be used if the angle is too large as the approximation
         // of pi causes inaccuracies.
         while(angle > Math.PI) {
            angle -= 2 * Math.PI;
         }
         while(angle < -Math.PI) {
            angle += 2 * Math.PI;
         }
         return angle;
      }
      
   }

}
