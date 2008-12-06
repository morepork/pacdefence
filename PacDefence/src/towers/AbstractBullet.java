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

import gui.Helper;
import gui.OuterPanel;
import images.ImageHelper;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

import sprites.Sprite;
import sprites.Sprite.DamageReport;

public abstract class AbstractBullet implements Bullet {

   // The direction of the bullet, first is dx, second is dy. Should be normalised
   // then multiplied by the speed.
   private final double[] dir = new double[2];
   @SuppressWarnings("unused")
   protected final double speed;
   protected double distanceTravelled = 0;
   protected final Point2D lastPosition;
   protected final Point2D position;
   protected final int range;
   private static final int radius = 3;
   private final double damage;
   private boolean draw = true;
   protected final Tower shotBy;
   private static final BufferedImage image = ImageHelper.makeImage(radius * 2, radius * 2,
         "other", "bullet.png");
   private static final int halfWidth = image.getWidth() / 2;
   private final Polygon path;
   
   /**
    * Creates a new AbstractBuller with fields set, but tick and draw
    * methods must be overrided.
    *  
    * @param path
    * @param shotBy
    * @param damage
    * @param range
    * @param speed
    */
   public AbstractBullet(Polygon path, Tower shotBy, double damage, int range, double speed) {
      this.path = path;
      this.shotBy = shotBy;
      this.damage = damage;
      this.range = range;
      this.speed = speed;
      lastPosition = null;
      position = null;
   }
   
   public AbstractBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
         double speed, double damage, Point p, Polygon path) {
      this.shotBy = shotBy;
      int turretWidthPlusRadius = turretWidth + radius;
      this.range = range - turretWidthPlusRadius;
      this.speed = speed;
      this.damage = damage;
      double divisor = Math.sqrt(dx * dx + dy * dy);
      dir[0] = speed * dx / divisor;
      dir[1] = speed * dy / divisor;
      position = new Point2D.Double(p.getX() + turretWidthPlusRadius * dx / divisor,
            p.getY() + turretWidthPlusRadius * dy / divisor);
      lastPosition = new Point2D.Double(position.getX(), position.getY());
      this.path = path;
   }

   public double tick(List<Sprite> sprites) {
      double tick = doTick(sprites);
      draw = tick < 0;
      return tick;
   }

   public void draw(Graphics g) {
      if(draw && !checkIfBulletIsOffScreen()) {
         g.drawImage(image, (int) position.getX() - radius, (int) position.getY() - radius, null);
      }
   }
   
   public double getDamage() {
      return damage;
   }
   
   protected double checkIfSpriteIsHit(List<Sprite> sprites) {
      return checkIfSpriteIsHit(lastPosition, position, sprites);
   }
   
   protected double checkIfSpriteIsHit(Point2D p1, Point2D p2, List<Sprite> sprites) {
      return checkIfSpriteIsHit(new Line2D.Double(p1, p2), sprites);
   }
   
   protected double checkIfSpriteIsHit(Line2D line, List<Sprite> sprites) {
      List<Point2D> points = Helper.getPointsOnLine(line);
      if(doPointsIntersectPath(points)) {
         // A sprite can only be hit if the bullet is on the path
         // The path check is more for optimisation than anything
         for(Sprite s : sprites) {
            Point2D p = s.intersects(points);
            if(p != null) {
               DamageReport d = s.hit(getDamage());
               specialOnHit(p, s, sprites);
               if(d != null) { // Sprite is not already dead
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
      if(d.wasKill()) {
         shotBy.increaseKills(1);
      }
      shotBy.increaseDamageDealt(d.getDamage());
      return d.getMoneyEarnt();
   }
   
   protected boolean doesLineIntersectPath(Line2D line) {
      return doPointsIntersectPath(Helper.getPointsOnLine(line));
   }
   
   protected boolean doPointsIntersectPath(List<Point2D> points) {
      for(Point2D p : points) {
         if(path.contains(p)) {
            return true;
         }
      }
      return false;
   }
   
   protected boolean checkIfBulletCanBeRemovedAsOffScreen() {
      return checkIfBulletIsOffScreen();
   }
   
   private boolean checkIfBulletIsOffScreen() {
      return position.getX() < -halfWidth || position.getY() < -halfWidth ||
            position.getX() > OuterPanel.MAP_WIDTH + halfWidth ||
            position.getY() > OuterPanel.MAP_HEIGHT + halfWidth;
   }
   
   private double doTick(List<Sprite> sprites) {
      if(distanceTravelled >= range || checkIfBulletCanBeRemovedAsOffScreen()) {
         return 0;
      }
      distanceTravelled += speed;
      //System.out.println(distanceTravelled);
      lastPosition.setLocation(position);
      if(distanceTravelled > range) {
         double extraFraction = (distanceTravelled - range) / speed;
         position.setLocation(position.getX() + extraFraction * dir[0],
               position.getY() + extraFraction * dir[1]);
         double result = checkIfSpriteIsHit(sprites);
         if(result > 0) {
            return result;
         } else {
            return 0;
         }
      } else {
         position.setLocation(position.getX() + dir[0], position.getY() + dir[1]);
         return checkIfSpriteIsHit(sprites);
      }
   }

}
