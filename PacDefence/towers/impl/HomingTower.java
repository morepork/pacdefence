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

package towers.impl;

import java.awt.Point;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logic.Game;
import logic.Helper;
import sprites.Sprite;
import sprites.Sprite.DistanceComparator;
import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;


public class HomingTower extends AbstractTower {
   
   // Max angle the bullet redirects each tick in degrees
   private double maxRedirectAngle = 2;
   // Only increase the redirect angle by a fixed amount otherwise it quickly gets silly
   private final double upgradeIncreaseAngle = maxRedirectAngle * (upgradeIncreaseFactor - 1);

   public HomingTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Homing", 40, 100, 3, 20, 50, 18, true);
   }
   
   @Override
   public String getSpecial() {
      // \u00b0 is the degree symbol
      return Helper.format(maxRedirectAngle * Game.CLOCK_TICKS_PER_SECOND, 1) + "\u00b0/s";
   }

   @Override
   public String getSpecialName() {
      return "Angle Change";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, List<Shape> pathBounds) {
      return new HomingBullet(this, dx, dy, turretWidth, range, speed, damage, p, pathBounds, s,
            maxRedirectAngle);
   }

   @Override
   protected void upgradeSpecial() {
      maxRedirectAngle += upgradeIncreaseAngle;
   }
   
   private class HomingBullet extends BasicBullet {
      
      private Sprite target;
      // This is in radians for convenience
      private final double maxRedirectAngle;
      
      private HomingBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, List<Shape> pathBounds, Sprite s,
            double maxRedirectAngle) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p, pathBounds);
         target = s;
         this.maxRedirectAngle = Math.toRadians(maxRedirectAngle);
      }
      
      @Override
      protected double doTick(List<Sprite> sprites) {
         double value = super.doTick(sprites);
         
         selectNewTarget(sprites); // Selects a new target if necessary
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
      
      private void selectNewTarget(List<Sprite> sprites) {
         // If the target has died or is out of range, start targetting the closest sprite that is
         // in range (if there is any)
         if(!target.isAlive() || !checkDistance(target)) {
            // Copy the list as the passed list can't be modified
            sprites = new ArrayList<Sprite>(sprites);
            Collections.sort(sprites, new DistanceComparator(Helper.toPoint(position), true));
            
            for(Sprite s : sprites) {
               if(s.isAlive() && checkDistance(s)) {
                  target = s;
                  break;
               }
            }
         }
      }
      
      private void retarget() {
         // Only redirect if the target is alive, and is in range
         if(target.isAlive() && checkDistance(target)) {
            double currentAngle = Helper.vectorAngle(dir[0], dir[1]);
            double angleToTarget = Helper.vectorAngle(
                  target.getPosition().getX() - position.getX(),
                  target.getPosition().getY() - position.getY());
            // Normalise the angle to between -pi and pi so that deltaAngle
            // is actually the best change in angle
            double deltaAngle = normaliseAngle(angleToTarget - currentAngle);
            double angleToChangeTo;
            if(Math.abs(deltaAngle) <= maxRedirectAngle) {
               angleToChangeTo = angleToTarget;
            } else {
               angleToChangeTo = currentAngle + (deltaAngle > 0 ? 1 : -1) * maxRedirectAngle;
            }
            setDirections(Math.sin(angleToChangeTo), Math.cos(angleToChangeTo));
         }
      }
      
      private double normaliseAngle(double angle) {
         // This shouldn't be used if the angle is too large as the approximation
         // of pi causes inaccuracies.
         if(angle > Math.PI) {
            return normaliseAngle(angle - 2 * Math.PI);
         } else if(angle < -Math.PI) {
            return normaliseAngle(angle + 2 * Math.PI);
         } else {
            return angle;
         }
      }
      
   }

}
