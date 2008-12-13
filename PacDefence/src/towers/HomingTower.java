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
import images.ImageHelper;

import java.awt.Point;
import java.awt.Polygon;
import java.text.DecimalFormat;
import java.util.List;

import sprites.Sprite;


public class HomingTower extends AbstractTower {
   
   private static DecimalFormat ONE_DP = new DecimalFormat("#0.0");
   // Max angle the bullet redirects each tick in degrees
   private double maxRedirectAngle = 1;
   private static final double upgradeIncreaseAngle = 0.1;

   public HomingTower() {
      this(new Point(), null);
   }
   
   public HomingTower(Point p, Polygon path) {
      super(p, path, "Homing", 40, 100, 5, 10, 50, 18, "homing.png", "HomingTower.png");
   }
   
   @Override
   public String getSpecial() {
      return ONE_DP.format(maxRedirectAngle * GameMap.CLOCK_TICKS_PER_SECOND) + "°/s";
   }

   @Override
   public String getSpecialName() {
      return "Angle Change";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Polygon path) {
      return new HomingBullet(this, dx, dy, turretWidth, range, speed, damage, p, path, s,
            maxRedirectAngle);
   }

   @Override
   protected void upgradeSpecial() {
      maxRedirectAngle += upgradeIncreaseAngle;
   }
   
   private static class HomingBullet extends AbstractBullet {
      
      private final Sprite target;
      // This is in radians for convenience
      private final double maxRedirectAngle;
      
      private HomingBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, Polygon path, Sprite s,
            double maxRedirectAngle) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p, path);
         target = s;
         this.maxRedirectAngle = Math.toRadians(maxRedirectAngle);
      }
      
      @Override
      protected double doTick(List<Sprite> sprites) {
         double value = super.doTick(sprites);
         if(target.isAlive()) {
            double currentAngle = ImageHelper.vectorAngle(dir[0], dir[1]);
            double angleToTarget = ImageHelper.vectorAngle(
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
         return value;
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
