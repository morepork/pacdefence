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

import images.ImageHelper;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import logic.Game;
import logic.Helper;
import sprites.Sprite;
import sprites.Sprite.DamageReport;

public class BasicBullet implements Bullet {

   // The direction of the bullet, first is dx, second is dy. Should be normalised
   // then multiplied by the speed.
   protected final double[] dir = new double[2];
   protected final double speed;
   protected double distanceTravelled = 0;
   protected final Point2D lastPosition;
   protected final Point2D position;
   protected final int range;
   public static final int radius = 3;
   protected final double damage;
   private boolean draw = true;
   protected final Tower shotBy;
   private static final BufferedImage image = ImageHelper.makeImage(radius * 2, radius * 2,
         "other", "bullet.png");
   private final Rectangle2D pathBounds;
   
   /**
    * Creates a new BasicBuller with fields set, but tick and draw
    * methods must be overrided.
    *  
    * @param path
    * @param shotBy
    * @param damage
    * @param range
    * @param speed
    */
   protected BasicBullet(Rectangle2D pathBounds, Tower shotBy, double damage, int range,
         double speed) {
      this.pathBounds = pathBounds;
      this.shotBy = shotBy;
      this.damage = damage;
      this.range = range;
      this.speed = speed;
      lastPosition = null;
      position = null;
   }
   
   public BasicBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
         double speed, double damage, Point p, Rectangle2D pathBounds) {
      this.shotBy = shotBy;
      int turretWidthPlusRadius = turretWidth + radius;
      this.range = range - turretWidthPlusRadius;
      this.speed = speed;
      this.damage = damage;
      setDirections(dx, dy);
      double divisor = Math.sqrt(dx * dx + dy * dy);
      position = new Point2D.Double(p.getX() + turretWidthPlusRadius * dx / divisor,
            p.getY() + turretWidthPlusRadius * dy / divisor);
      lastPosition = new Point2D.Double(position.getX(), position.getY());
      this.pathBounds = pathBounds;
   }

   public final double tick(List<Sprite> sprites) {
      double tick = doTick(sprites);
      draw = tick < 0;
      return tick;
   }

   public void draw(Graphics g) {
      if(draw && !checkIfBulletIsOffScreen()) {
         g.drawImage(image, (int) position.getX() - radius, (int) position.getY() - radius, null);
      }
   }
   
   public static double processDamageReport(DamageReport d, Tower t) {
      if(d == null) {
         return 0;
      }
      if(d.wasKill()) {
         t.increaseKills(1);
      }
      t.increaseDamageDealt(d.getDamage());
      return d.getMoneyEarnt();
   }
   
   protected void setDirections(double dx, double dy) {
      double divisor = Math.sqrt(dx * dx + dy * dy);
      dir[0] = speed * dx / divisor;
      dir[1] = speed * dy / divisor;
   }
   
   protected double checkIfSpriteIsHit(List<Sprite> sprites) {
      return checkIfSpriteIsHit(lastPosition, position, sprites);
   }
   
   protected double checkIfSpriteIsHit(Point2D p1, Point2D p2, List<Sprite> sprites) {
      if(sprites.isEmpty()) {
         return -1;
      }
      List<Point2D> points = Helper.getPointsOnLine(p1, p2, pathBounds);
      if(!points.isEmpty()) {
         // A sprite can only be hit if the bullet is on the path
         // The path check is more for optimisation than anything
         for(Sprite s : sprites) {
            Point2D p = s.intersects(points);
            if(p != null) {
               DamageReport d = s.hit(damage);
               if(d != null) { // Sprite is not already dead
                  specialOnHit(p, s, sprites);
                  return processDamageReport(d);
               }
            }
         }
      }
      return -1;
   }
   
   /**
    * To be overriden by subclasses whose bullets do something special on a hit
    * 
    * @param p
    */
   protected void specialOnHit(Point2D p, Sprite s, List<Sprite> sprites) {}
   
   protected double processDamageReport(DamageReport d) {
      return processDamageReport(d, shotBy);
   }
   
   protected boolean checkIfBulletCanBeRemovedAsOffScreen() {
      return checkIfBulletIsOffScreen();
   }
   
   protected boolean checkIfBulletIsOffScreen() {
      return checkIfPointIsOffScreen(position);
   }
   
   protected boolean checkIfPointIsOffScreen(Point2D p) {
      return p.getX() < -radius || p.getY() < -radius || p.getX() > Game.MAP_WIDTH + radius ||
            p.getY() > Game.MAP_HEIGHT + radius;
   }
   
   protected double doTick(List<Sprite> sprites) {
      if(distanceTravelled >= range || checkIfBulletCanBeRemovedAsOffScreen()) {
         return 0;
      }
      distanceTravelled += speed;
      lastPosition.setLocation(position);
      if(distanceTravelled > range) {
         double extraFraction = (distanceTravelled - range) / speed;
         position.setLocation(position.getX() + extraFraction * dir[0],
               position.getY() + extraFraction * dir[1]);
         double result = checkIfSpriteIsHit(sprites);
         // Bullet has exceeded range so should be removed no matter what
         return result > 0 ? result : 0;
      } else {
         position.setLocation(position.getX() + dir[0], position.getY() + dir[1]);
         return checkIfSpriteIsHit(sprites);
      }
   }
   
   protected Rectangle2D getPathBounds() {
      return pathBounds;
   }

}
