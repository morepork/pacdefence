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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import sprites.Sprite;


public class LaserTower extends AbstractTower {
   
   private double beamLength = 20;
   private final double beamLengthUpgrade = beamLength * (upgradeIncreaseFactor - 1);

   public LaserTower(Point p, Rectangle2D pathBounds) {
      super(p, pathBounds, "Laser", 40, 100, 7.5, 1.5, 50, 24, true);
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
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Rectangle2D pathBounds) {
      double divisor = Math.sqrt(dx * dx + dy * dy);
      Point2D firstPoint = new Point2D.Double(p.getX() + turretWidth * dx / divisor,
            p.getY() + turretWidth * dy / divisor);
      Point2D lastPoint = new Point2D.Double(p.getX() + range * dx / divisor,
            p.getY() + range * dy / divisor);
      return new Laser(pathBounds, this, firstPoint, lastPoint, speed, damage, range - turretWidth, beamLength);
   }

   @Override
   protected void upgradeSpecial() {
      beamLength += beamLengthUpgrade;
   }
   
   private class Laser extends BasicBullet {
      
      private final Point2D lastPoint;
      private final Line2D laser;
      private final double length;
      private final Color beamColour = new Color(210, 255, 0);
      private final double xStep, yStep;
      private double moneyEarnt = 0;
      
      public Laser(Rectangle2D pathBounds, Tower shotBy, Point2D firstPoint, Point2D lastPoint,
            double speed, double damage, int range, double length) {
         super(pathBounds, shotBy, damage, range, speed);
         this.lastPoint = lastPoint;
         this.length = length;
         double distance = firstPoint.distance(lastPoint);
         double stepFraction = speed / distance;
         xStep = (lastPoint.getX() - firstPoint.getX()) * stepFraction;
         yStep = (lastPoint.getY() - firstPoint.getY()) * stepFraction;
         double mult = length / speed;
         laser = new Line2D.Double(firstPoint, new Point2D.Double(firstPoint.getX() + xStep * mult,
               firstPoint.getY() + yStep * mult));
         distanceTravelled = length;
      }

      @Override
      public void draw(Graphics g) {
         Graphics2D g2D = (Graphics2D) g;
         g2D.setColor(beamColour);
         Stroke old = g2D.getStroke();
         g2D.setStroke(new BasicStroke(AbstractTower.turretThickness));
         g2D.draw(laser);
         g2D.setStroke(old);
      }

      @Override
      public double doTick(List<Sprite> sprites) {
         Point2D oldP1 = laser.getP1();
         laser.setLine(laser.getX1() + xStep, laser.getY1() + yStep, laser.getX2() + xStep,
               laser.getY2() + yStep);
         distanceTravelled += speed;
         if(distanceTravelled >= range) {
            if(distanceTravelled >= range + length) {
               return moneyEarnt;
            } else {
               laser.setLine(laser.getP1(), lastPoint);
            }
         }
         double tickMoney = checkIfSpriteIsHit(oldP1, laser.getP2(), sprites);
         if(tickMoney > 0) {
            moneyEarnt += tickMoney;
         }
         return -1;
      }
      
   }
   
}
