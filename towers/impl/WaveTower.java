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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import logic.Constants;
import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;
import util.Helper;
import util.Vector2D;
import creeps.Creep;


public class WaveTower extends AbstractTower {
   
   private double angle = 25;
   private final double upgradeIncreaseAngle = angle / 10;
   
   public WaveTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Wave", 40, 100, 5, 5.2, 50, 6, true);
      // This is a grossly overpowered (but with really low damage) version for
      // performance testing purposes
      /*super(p, pathBounds, "Wave", 1, 500, 25, 0.05, 50, 6, true);
      for(int i = 0; i < 20; i++) {
         upgradeSpecial();
      }*/
   }

   @Override
   public String getSpecial() {
      // \u00b0 is the degree symbol
      return Helper.format(angle, 1) + "\u00b0";
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
   protected Bullet makeBullet(Vector2D dir, int turretWidth, int range, double speed,
         double damage, Point p, Creep c, List<Shape> pathBounds) {
      return new WaveBullet(this, dir, turretWidth, range, speed, damage, p, pathBounds, angle);
   }

   @Override
   protected void upgradeSpecial() {
      angle += upgradeIncreaseAngle;
   }
   
   private static class WaveBullet extends BasicBullet {
      
      private final double startAngle, extentAngle;
      private final Arc2D arc = new Arc2D.Double(Arc2D.PIE);
      private final Arc2D lastArc = new Arc2D.Double(Arc2D.PIE);
      private final Point2D start;
      // Use an ArrayList here as the overhead of a more complicated set
      // isn't really worth it as it'll never grow much larger than 50
      private final Collection<Creep> hitCreeps = new ArrayList<Creep>();
      private double moneyEarnt = 0;
      private final int turretWidth;
      
      public WaveBullet(Tower shotBy, Vector2D dir, int turretWidth, int range,
            double speed, double damage, Point p, List<Shape> pathBounds, double angle) {
         super(shotBy, dir, turretWidth, range, speed, damage, p, pathBounds);
         startAngle = Math.toDegrees(dir.getAngle()) - 90 - angle / 2;
         extentAngle = angle;
         start = p;
         this.turretWidth = turretWidth;
      }
      
      @Override
      public void draw(Graphics2D g) {
         Graphics2D g2D = (Graphics2D) g;
         g2D.setColor(Color.PINK);
         Stroke s = g2D.getStroke();
         g2D.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
         g2D.draw(arc);
         g2D.setStroke(s);
      }
      
      @Override
      protected double doTick(List<Creep> creeps) {
         double value = super.doTick(creeps);
         setArc(arc, distanceTravelled + turretWidth);
         if(value > 0) {
            moneyEarnt += value;
         } else if(value == 0) {
            return moneyEarnt;
         }
         return -1;
      }
      
      @Override
      protected double checkIfCreepIsHit(List<Creep> creeps) {
         if(creeps.isEmpty()) {
            return -1;
         }
         double d = 0;
         Arc2D closerArc = (Arc2D)lastArc.clone();
         closerArc.setArcType(Arc2D.OPEN);
         for(Creep c : creeps) {
            // It has to intersect the current arc, but not the last arc unless
            // it intersects the open arc with the same radius as the last arc
            if(!hitCreeps.contains(c) && c.intersects(arc) &&
                  (c.intersects(closerArc) || !c.intersects(lastArc))) {
               hitCreeps.add(c);
               d += processDamageReport(c.hit(damage, shotBy.getClass()));
            }
         }
         return d == 0 ? -1 : d;
      }
      
      @Override
      protected boolean canBulletBeRemovedAsOffScreen() {
         // Need to check that the arc isn't empty as it may not have been set yet
         return !arc.isEmpty() && !arc.intersects(0, 0, Constants.WIDTH, Constants.HEIGHT);
      }
      
      private void setArc(Arc2D a, double radius) {
         lastArc.setArc(a);
         a.setArcByCenter(start.getX(), start.getY(), radius, startAngle, extentAngle,
               Arc2D.OPEN);
      }
   }
}
