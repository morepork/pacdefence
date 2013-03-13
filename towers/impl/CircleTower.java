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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;
import util.Circle;
import util.Vector2D;
import creeps.Creep;

public class CircleTower extends AbstractTower {

   private int hits = 1;

   public CircleTower(Point p) {
      super(p, "Circle", 40, 100, 5, 12, 50, 0, true);
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
   protected Bullet makeBullet(Vector2D dir, int turretWidth, int range, double speed,
         double damage, Point p, Creep c) {
      return new CirclingBullet(this, dir, turretWidth, range, speed, damage, p, c);
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
      private double moneyEarnedSoFar = 0;
      private Collection<Creep> hitCreeps = new ArrayList<Creep>();

      public CirclingBullet(Tower shotBy, Vector2D dir, int turretWidth, int range,
            double speed, double damage, Point p, Creep c) {
         super(shotBy, dir, turretWidth, range, speed, damage, p);
         double distance = p.distance(c.getPosition()) - c.getHalfWidth();
         double angleToCreep = dir.getAngle();
         double theta = angleToCreep - Math.acos(distance / getRange());
         double halfRange = getRange() / 2.0;
         Vector2D delta = Vector2D.createFromAngle(theta, halfRange);
         route = new Circle(Vector2D.add(p, delta), halfRange);
         arcLengthPerTick = getBulletSpeed();
         deltaTheta = 2 * Math.PI * arcLengthPerTick / route.calculateCircumference();
         angle = Math.PI + theta;
         endAngle = angle + 2 * Math.PI;
      }

      @Override
      public void draw(Graphics2D g) {
         super.draw(g);
      }

      @Override
      public double doTick(List<Creep> creeps) {
         if (angle >= endAngle || hitsLeft <= 0) {
            return moneyEarnedSoFar;
         }
         List<Creep> hittableCreeps = new ArrayList<Creep>(creeps);
         hittableCreeps.removeAll(hitCreeps);
         moneyEarnedSoFar += checkIfCreepIsHit(hittableCreeps);
         angle += deltaTheta;
         position.setLocation(route.getPointAt(angle));
         return -1;
      }

      @Override
      protected double checkIfCreepIsHit(List<Creep> creeps) {
         List<Point2D> points = makeArcPoints();
         double moneyEarned = 0;
         if(!points.isEmpty()) {
            for (Creep c : creeps) {
               for (Point2D p : points) {
                  if (c.intersects(p)) {
                     specialOnHit(p, c, creeps);
                     moneyEarned += processDamageReport(c.hit(getDamage(), shotBy.getClass()));
                     hitCreeps.add(c);
                     hitsLeft--;
                     if (hitsLeft <= 0) {
                        return moneyEarned;
                     }
                     break;
                  }
               }
            }
         }
         return moneyEarned;
      }
      
      @Override
      protected boolean canBulletBeRemovedAsOffScreen() {
         // Circle bullets always come back so shouldn't be removed
         return false;
      }

      private List<Point2D> makeArcPoints() {
         List<Point2D> points = new ArrayList<Point2D>();
         double delta = deltaTheta / arcLengthPerTick;
         for (double a = angle + delta; a <= angle + deltaTheta; a += delta) {
            points.add(route.getPointAt(a));
         }
         return points;
      }

   }

}
