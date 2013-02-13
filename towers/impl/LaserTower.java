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
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;
import util.Vector2D;
import creeps.Creep;


public class LaserTower extends AbstractTower {
   
   private double beamLength = 20;
   private final double beamLengthUpgrade = beamLength * 2 * (upgradeIncreaseFactor - 1);

   public LaserTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Laser", 40, 100, 7.5, 1.9, 50, 24, true);
   }

   @Override
   public String getSpecial() {
      return String.valueOf((int)beamLength);
   }

   @Override
   public String getSpecialName() {
      return "Beam Length";
   }
   
   @Override
   public String getStatName(Attribute a) {
      if(a == Attribute.Speed) {
         return "Beam speed";
      } else {
         return super.getStatName(a);
      }
   }

   @Override
   protected Bullet makeBullet(Vector2D dir, int turretWidth, int range, double speed,
         double damage, Point p, Creep c, List<Shape> pathBounds) {
      Point2D firstPoint = Vector2D.add(p, Vector2D.createFromVector(dir, turretWidth));
      Point2D lastPoint = Vector2D.add(p, Vector2D.createFromVector(dir, range));
      return new Laser(pathBounds, this, firstPoint, lastPoint, speed, damage, range - turretWidth,
            beamLength);
   }

   @Override
   protected void upgradeSpecial() {
      beamLength += beamLengthUpgrade;
   }
   
   private static class Laser extends BasicBullet {
      
      private static final Stroke beamStroke = new BasicStroke(AbstractTower.turretThickness);
      private static final Color beamColour = new Color(210, 255, 0);
      private final Point2D lastPoint;
      private final Line2D laser;
      private final double length;
      private final Vector2D step;
      private double moneyEarned = 0;
      
      public Laser(List<Shape> pathBounds, Tower shotBy, Point2D firstPoint, Point2D lastPoint,
            double speed, double damage, int range, double length) {
         super(pathBounds, shotBy, damage, range, speed);
         this.lastPoint = lastPoint;
         this.length = length;
         step = Vector2D.createFromVector(Vector2D.createFromPoints(firstPoint, lastPoint), speed);
         laser = Vector2D.createLine(firstPoint, Vector2D.createFromVector(step, length));
         distanceTravelled = length;
      }

      @Override
      public void draw(Graphics2D g) {
         Graphics2D g2D = (Graphics2D) g;
         g2D.setColor(beamColour);
         Stroke old = g2D.getStroke();
         g2D.setStroke(beamStroke);
         g2D.draw(laser);
         g2D.setStroke(old);
      }

      @Override
      public double doTick(List<Creep> creeps) {
         Point2D oldP1 = laser.getP1();
         laser.setLine(Vector2D.add(laser.getP1(), step), Vector2D.add(laser.getP2(), step));
         distanceTravelled += speed;
         if(distanceTravelled >= range) {
            // Only actually finish when the point at the back of the laser crosses the range
            if(distanceTravelled >= range + length) {
               return moneyEarned;
            } else {
               laser.setLine(laser.getP1(), lastPoint);
            }
         }
         double tickMoney = checkIfCreepIsHit(oldP1, laser.getP2(), creeps);
         if(tickMoney > 0) {
            moneyEarned += tickMoney;
         }
         return -1;
      }
      
   }
   
}
