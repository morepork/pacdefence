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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sprites.Sprite;


public class WaveTower extends AbstractTower {
   
   private int angle = 30;
   private final double upgradeIncreaseAngle = angle / 10;
   
   public WaveTower() {
      this(new Point(), null);
   }

   public WaveTower(Point p, Polygon path) {
      super(p, path, "Wave", 40, 100, 5, 5.25, 50, 6, "wave.png", "WaveTower.png");
   }

   @Override
   public String getSpecial() {
      return angle + "°";
   }

   @Override
   public String getSpecialName() {
      return "Wave angle";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Polygon path) {
      return new WaveBullet(this, dx, dy, turretWidth, range, speed, damage, p, path, angle);
   }

   @Override
   protected void upgradeSpecial() {
      angle += upgradeIncreaseAngle;
   }
   
   private static class WaveBullet extends AbstractBullet {
      
      private final double startAngle, extentAngle;
      private final Arc2D arc = new Arc2D.Double();
      private double lastRadius = 0, currentRadius = 0;
      private final Point2D start;
      private final Collection<Sprite> hitSprites = new ArrayList<Sprite>();
      private double moneyEarnt = 0;
      private final int turretWidth;
      
      public WaveBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, Polygon path, int angle) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p, path);
         double midAngle = ImageHelper.vectorAngle(dx, dy);
         startAngle = Math.toDegrees(midAngle - Math.PI / 2) - angle / 2;
         extentAngle = angle;
         start = p;
         this.turretWidth = turretWidth;
      }
      
      @Override
      public void draw(Graphics g) {
         Graphics2D g2D = (Graphics2D) g;
         g2D.setColor(Color.PINK);
         Stroke s = g2D.getStroke();
         g2D.setStroke(new BasicStroke(4));
         // Debug method that draws squares on each point from getPointsFromArc
         // to make sure they're in the right place
         /*for(Point2D p : getPointsFromArc(arc, currentRadius, false)) {
            g2D.drawRect((int)p.getX(), (int)p.getY(), 1, 1);
         }*/
         g2D.draw(arc);
         g2D.setStroke(s);
      }
      
      @Override
      protected double doTick(List<Sprite> sprites) {
         double value = super.doTick(sprites);
         lastRadius = currentRadius;
         currentRadius = distanceTravelled + turretWidth;
         setArc(arc, currentRadius);
         if(value > 0) {
            moneyEarnt += value;
         } else if(value == 0) {
            return moneyEarnt;
         }
         return -1;
      }
      
      @Override
      protected double checkIfSpriteIsHit(List<Sprite> sprites) {
         List<Point2D> points = new ArrayList<Point2D>();
         Arc2D a = new Arc2D.Double();
         for(double d = lastRadius; d < currentRadius; d++) {
            setArc(a, d);
            // I tried converting the Point2Ds to points and using a set to
            // eliminate duplicates but it only reduced the number of points
            // in the list by around 10% so didn't deem it worth the overhead
            points.addAll(getPointsFromArc(a, d, true));
         }
         double d = 0;
         for(Sprite s : sprites) {
            if(!hitSprites.contains(s) && s.intersects(points) != null) {
               hitSprites.add(s);
               d += processDamageReport(s.hit(getDamage()));
            }
         }
         return d == 0 ? -1 : d;
      }
      
      @Override
      protected boolean checkIfBulletCanBeRemovedAsOffScreen() {
         for(Point2D p : getPointsFromArc()) {
            if(!checkIfPointIsOffScreen(p)) {
               return false;
            }
         }
         // Can only be removed if none of the points in the arc are onscreen
         return true;
      }
      
      private void setArc(Arc2D a, double radius) {
         a.setArcByCenter(start.getX(), start.getY(), radius, startAngle, extentAngle,
               Arc2D.OPEN);
      }
      
      private List<Point2D> getPointsFromArc() {
         return getPointsFromArc(arc, currentRadius, false);
      }
      
      private List<Point2D> getPointsFromArc(Arc2D a, double radius, boolean onPathOnly) {
         /*double circumference = 2 * Math.PI * radius;
         double numPoints = circumference * a.getAngleExtent() / 360;
         double deltaAngle = Math.toRadians(a.getAngleExtent() / numPoints);*/
         // Left the above code in as it's easier to understand, though the two
         // lines below should be faster and do the same thing.
         double numPoints = Math.PI * radius * a.getAngleExtent() / 180;
         double deltaAngle = 1 / radius;
         double angle = Math.toRadians(a.getAngleStart() + 90);
         Circle c = new Circle(a.getCenterX(), a.getCenterY(), radius);
         List<Point2D> points = new ArrayList<Point2D>();
         Point2D p = a.getStartPoint();
         if(!onPathOnly || path.contains(p)) {
            points.add(p);
         }
         for(int i = 0; i < numPoints; i++) {
            angle += deltaAngle;
            p = c.getPointAt(angle);
            if(!onPathOnly || path.contains(p)) {
               points.add(p);
            }
         }
         p = a.getEndPoint();
         if(!onPathOnly || path.contains(p)) {
            points.add(p);
         }
         return points;
      }
   }
}
