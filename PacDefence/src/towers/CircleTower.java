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

import gui.Circle;
import images.ImageHelper;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sprites.Sprite;

public class CircleTower extends AbstractTower {

   private int hits = 1;

   public CircleTower() {
      this(new Point(), null);
   }

   public CircleTower(Point p, Rectangle2D pathBounds) {
      super(p, pathBounds, "Circle", 40, 100, 5, 10, 50, 0, "circle.png", "CircleTower.png",
            false);
   }

   @Override
   public String getSpecial() {
      return Integer.toString(hits);
   }

   @Override
   public String getSpecialName() {
      return "Hits";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Rectangle2D pathBounds) {
      return new CirclingBullet(this, dx, dy, turretWidth, range, speed, damage, p, s, pathBounds);
   }

   @Override
   protected void upgradeSpecial() {
      hits++;
   }

   private class CirclingBullet extends BasicBullet {

      private final Circle route;
      private final double deltaTheta;
      private final double arcLengthPerTick;
      private final double endAngle;
      private double angle;
      private int hitsLeft = hits;
      private double moneyEarntSoFar = 0;
      private Collection<Sprite> hitSprites = new ArrayList<Sprite>();

      public CirclingBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, Sprite s, Rectangle2D pathBounds) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p, pathBounds);
         double distance = p.distance(s.getPosition()) - s.getHalfWidth();
         double angleToSprite = ImageHelper.vectorAngle(dx, dy);
         double theta = angleToSprite - Math.acos(distance / getRange());
         double halfRange = getRange() / 2.0;
         double deltaX = halfRange * Math.sin(theta);
         double deltaY = halfRange * Math.cos(theta);
         route = new Circle(new Point2D.Double(p.getX() + deltaX, p.getY() + deltaY), halfRange);
         arcLengthPerTick = getBulletSpeed();
         deltaTheta = 2 * Math.PI * arcLengthPerTick / route.calculateCircumference();
         angle = Math.PI + theta;
         endAngle = angle + 2 * Math.PI;
      }

      @Override
      public void draw(Graphics g) {
         super.draw(g);
      }

      @Override
      public double doTick(List<Sprite> sprites) {
         if (angle >= endAngle || hitsLeft <= 0) {
            return moneyEarntSoFar;
         }
         List<Sprite> hittableSprites = new ArrayList<Sprite>(sprites);
         hittableSprites.removeAll(hitSprites);
         moneyEarntSoFar += checkIfSpriteIsHit(hittableSprites);
         angle += deltaTheta;
         position.setLocation(route.getPointAt(angle));
         return -1;
      }

      @Override
      protected double checkIfSpriteIsHit(List<Sprite> sprites) {
         List<Point2D> points = makeArcPoints();
         double moneyEarnt = 0;
         if(!points.isEmpty()) {
            for (Sprite s : sprites) {
               for (Point2D p : points) {
                  if (s.intersects(p)) {
                     specialOnHit(p, s, sprites);
                     moneyEarnt += processDamageReport(s.hit(getDamage()));
                     hitSprites.add(s);
                     hitsLeft--;
                     if (hitsLeft <= 0) {
                        return moneyEarnt;
                     }
                     break;
                  }
               }
            }
         }
         return moneyEarnt;
      }
      
      @Override
      protected boolean checkIfBulletCanBeRemovedAsOffScreen() {
         // Circle bullets always come back so shouldn't be removed
         return false;
      }

      private List<Point2D> makeArcPoints() {
         List<Point2D> points = new ArrayList<Point2D>();
         double delta = deltaTheta / arcLengthPerTick;
         for (double a = angle + delta; a <= angle + deltaTheta; a += delta) {
            Point2D p = route.getPointAt(a);
            if(getPathBounds().contains(p)) {
               points.add(p);
            }
         }
         return points;
      }

   }

}
