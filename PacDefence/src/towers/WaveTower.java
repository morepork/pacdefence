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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import logic.Helper;
import sprites.Sprite;


public class WaveTower extends AbstractTower {
   
   private int angle = 30;
   private final double upgradeIncreaseAngle = angle / 10;
   
   public WaveTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Wave", 40, 100, 5, 5.5, 50, 6, true);
      // This is a grossly overpowered (but with really low damage) version for
      // performance testing purposes
      /*super(p, pathBounds, "Wave", 1, 500, 25, 0.1, 50, 6, true);
      for(int i = 0; i < 20; i++) {
         upgradeSpecial();
      }*/
   }

   @Override
   public String getSpecial() {
      // \u00b0 is the degree symbol
      return angle + "\u00b0";
   }
   
   @Override
   public String getStatName(Attribute a) {
      if(a == Attribute.Speed) {
         return "Wave Speed";
      } else {
         return super.getStatName(a);
      }
   }

   @Override
   public String getSpecialName() {
      return "Wave angle";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, List<Shape> pathBounds) {
      return new WaveBullet(this, dx, dy, turretWidth, range, speed, damage, p, pathBounds, angle);
   }

   @Override
   protected void upgradeSpecial() {
      angle += upgradeIncreaseAngle;
   }
   
   private static class WaveBullet extends BasicBullet {
      
      private final double startAngle, extentAngle;
      private final Arc2D arc = new Arc2D.Double();
      private double lastRadius = 0, currentRadius = 0;
      private final Point2D start;
      private final Collection<Sprite> hitSprites = new ArrayList<Sprite>();
      private double moneyEarnt = 0;
      private final int turretWidth;
      private final List<Shape> pathBounds;
      
      public WaveBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, List<Shape> pathBounds, int angle) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p, pathBounds);
         double midAngle = ImageHelper.vectorAngle(dx, dy);
         startAngle = Math.toDegrees(midAngle - Math.PI / 2) - angle / 2;
         extentAngle = angle;
         start = p;
         this.turretWidth = turretWidth;
         this.pathBounds = pathBounds;
      }
      
      @Override
      public void draw(Graphics g) {
         Graphics2D g2D = (Graphics2D) g;
         g2D.setColor(Color.PINK);
         Stroke s = g2D.getStroke();
         g2D.setStroke(new BasicStroke(4));
         g2D.draw(arc);
         g2D.setStroke(s);
         // Debug code that draws squares on each point from getPointsFromArc
         // to make sure they're in the right place
         /*g2D.setColor(Color.RED);
         for(Point2D p : Helper.getPointsOnArc(arc)) {
            g2D.drawRect((int)p.getX(), (int)p.getY(), 1, 1);
         }*/
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
         if(sprites.isEmpty()) {
            return -1;
         }
         List<Point2D> points = new ArrayList<Point2D>();
         //Arc2D a = new Arc2D.Double();
         double radAngleStart = Math.toRadians(startAngle + 90);
         double sinAngle = Math.sin(radAngleStart);
         double cosAngle = Math.cos(radAngleStart);
         double numPointsMult = Math.abs(2 * Math.PI * extentAngle / 360);
         for(double d = lastRadius; d < currentRadius; d++) {
            // I tried converting the Point2Ds to points and using a set to
            // eliminate duplicates but it only reduced the number of points
            // in the list by around 10% so didn't deem it worth the overhead
            //setArc(a, d);
            //points.addAll(Helper.getPointsOnArc(a, pathBounds);
            points.addAll(Helper.getPointsOnArc(start.getX(), start.getY(), d, d * numPointsMult,
                  sinAngle, cosAngle, pathBounds));
         }
         double d = 0;
         for(Sprite s : sprites) {
            if(!hitSprites.contains(s) && s.intersects(points) != null) {
               hitSprites.add(s);
               d += processDamageReport(s.hit(damage));
            }
         }
         return d == 0 ? -1 : d;
      }
      
      @Override
      protected boolean checkIfBulletCanBeRemovedAsOffScreen() {
         for(Point2D p : Helper.getPointsOnArc(arc)) {
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
   }
}
