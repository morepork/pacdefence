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
import gui.Helper;
import images.ImageHelper;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sprites.Sprite;


public class CircleTower extends AbstractTower {
   
   private int hits = 1;
   
   public CircleTower() {
      this(new Point());
   }
   
   public CircleTower(Point p) {
      super(p, "Circle", 30, 100, 5, 10, 50, 0, "circle.png", "CircleTower.png");
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
         double damage, Point p, Sprite s) {
      return new CirclingBullet(this, dx, dy, turretWidth, range, speed, damage, p, s);
   }

   @Override
   protected void upgradeSpecial() {
      if(hits + 1 >= hits * upgradeIncreaseFactor) {
         hits++;
      } else {
         hits *= upgradeIncreaseFactor;
      }
   }
   
   private class CirclingBullet extends AbstractBullet {
      
      private final Circle path;
      private final double deltaTheta;
      private final double arcLength;
      private final double endAngle;
      private double angle;
      private int hitsLeft = hits;
      private double moneyEarntSoFar = 0;
      private Collection<Sprite> hitSprites = new ArrayList<Sprite>();

      public CirclingBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, Sprite s) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p);
         double distance = Helper.distance(p, s.getPosition()) - s.getHalfWidth();
         double theta = ImageHelper.vectorAngle(dx, dy) - Math.acos(distance / getRange());
         double halfRange = getRange() / 2.0;
         double deltaX = halfRange * Math.sin(theta);
         double deltaY = halfRange * Math.cos(theta);
         //System.out.println(theta + " " + deltaX + " " + deltaY);
         //int prodX = dx < 0 ? 1 : -1;
         //int prodY = dy < 0 ? 1 : -1;
         path = new Circle(new Point2D.Double(p.getX() + deltaX, p.getY() + deltaY), halfRange);
         arcLength = getBulletSpeed();
         deltaTheta = 2 * Math.PI * arcLength / path.calculateCircumference();
         angle = Math.PI + theta;
         endAngle = angle + 2 * Math.PI;
      }
      
      @Override
      public void draw(Graphics g) {
         super.draw(g);
         //path.draw(g);
      }
      
      @Override
      public double tick(List<Sprite> sprites) {
         if(angle >= endAngle) {
            return moneyEarntSoFar;
         }
         if(hitsLeft > 0) {
            List<Sprite> hittableSprites = Helper.cloneList(sprites);
            hittableSprites.removeAll(hitSprites);
            moneyEarntSoFar += checkIfSpriteIsHit(hittableSprites);
         }
         angle += deltaTheta;
         position.setLocation(path.getPointAt(angle));
         return -1;
      }
      
      @Override
      protected double checkIfSpriteIsHit(List<Sprite> sprites) {
         List<Point2D> points = makeArcPoints();
         double moneyEarnt = 0;
         for(Sprite s : sprites) {
            for(Point2D p : points) {
               if(s.intersects(p)) {
                  specialOnHit(p, s, sprites);
                  moneyEarnt += processDamageReport(s.hit(getDamage()));
                  hitSprites.add(s);
                  hitsLeft--;
                  if(hitsLeft <= 0) {
                     return moneyEarnt;
                  }
                  break;
               }
            }
         }
         return moneyEarnt;
      }
      
      private List<Point2D> makeArcPoints() {
         List<Point2D> points = new ArrayList<Point2D>();
         double delta = deltaTheta / arcLength;
         for(double a = angle + delta; a <= angle + deltaTheta; a+= delta) {
            points.add(path.getPointAt(a));
         }
         return points;
      }
      
   }

}
