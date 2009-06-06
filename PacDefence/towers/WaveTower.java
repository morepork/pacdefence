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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import logic.Game;
import logic.Helper;
import sprites.Sprite;


public class WaveTower extends AbstractTower {
   
   private double angle = 25;
   private final double upgradeIncreaseAngle = angle / 10;
   
   public WaveTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Wave", 40, 100, 5, 5, 50, 6, true);
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
      private final Arc2D arc = new Arc2D.Double(Arc2D.PIE);
      private final Arc2D lastArc = new Arc2D.Double(Arc2D.PIE);
      private final Point2D start;
      // Use an ArrayList here as the overhead of a more complicated set
      // isn't really worth it as it'll never grow much larger than 50
      private final Collection<Sprite> hitSprites = new ArrayList<Sprite>();
      private double moneyEarnt = 0;
      private final int turretWidth;
      
      public WaveBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, List<Shape> pathBounds, double angle) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p, pathBounds);
         double midAngle = Helper.vectorAngle(dx, dy);
         startAngle = Math.toDegrees(midAngle) - 90 - angle / 2;
         extentAngle = angle;
         start = p;
         this.turretWidth = turretWidth;
      }
      
      @Override
      public void draw(Graphics g) {
         Graphics2D g2D = (Graphics2D) g;
         g2D.setColor(Color.PINK);
         Stroke s = g2D.getStroke();
         g2D.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
         g2D.draw(arc);
         g2D.setStroke(s);
      }
      
      @Override
      protected double doTick(List<Sprite> sprites) {
         double value = super.doTick(sprites);
         setArc(arc, distanceTravelled + turretWidth);
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
         double d = 0;
         Arc2D closerArc = (Arc2D)lastArc.clone();
         closerArc.setArcType(Arc2D.OPEN);
         for(Sprite s : sprites) {
            // It has to intersect the current arc, but not the last arc unless
            // it intersects the open arc with the same radius as the last arc
            if(!hitSprites.contains(s) && s.intersects(arc) &&
                  (s.intersects(closerArc) || !s.intersects(lastArc))) {
               hitSprites.add(s);
               d += processDamageReport(s.hit(damage, shotBy.getClass()));
            }
         }
         return d == 0 ? -1 : d;
      }
      
      @Override
      protected boolean checkIfBulletCanBeRemovedAsOffScreen() {
         // Need to check that the arc isn't empty as it may not have been set yet
         return !arc.isEmpty() && !arc.intersects(0, 0, Game.WIDTH, Game.HEIGHT);
      }
      
      private void setArc(Arc2D a, double radius) {
         lastArc.setArc(a);
         a.setArcByCenter(start.getX(), start.getY(), radius, startAngle, extentAngle,
               Arc2D.OPEN);
      }
   }
}
