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

import images.ImageHelper;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import logic.Game;
import logic.Helper;
import sprites.Sprite;
import sprites.Sprite.DamageReport;


public class BeamTower extends AbstractTower {
   
   // The number of ticks the beam lasts for
   private double beamLastTicks = Game.CLOCK_TICKS_PER_SECOND / 2;
   private static final double upgradeBeamLastTicks = Game.CLOCK_TICKS_PER_SECOND / 20;
   
   public BeamTower(Point p, Rectangle2D pathBounds) {
      super(p, pathBounds, "Beam", 40, 80, 40, 4, 50, 0, "beam.png", "BeamTower.png", false);
      // This is a grossly overpowered version for testing performance.
      /*super(p, pathBounds, "Beam", 0, 1000, 100, 0.1, 50, 0, "beam.png", "BeamTower.png", false);
      for(int i = 0; i < 20; i++) {
         upgradeSpecial();
      }*/
   }
   
   @Override
   public String getStat(Attribute a) {
      if(a == Attribute.Speed) {
         // \u00b0 is the degree symbol
         return super.getStat(a) + "\u00b0/s";
      } else {
         return super.getStat(a);
      }
   }
   
   @Override
   public String getStatName(Attribute a) {
      if(a == Attribute.Speed) {
         return "Beam Speed";
      } else {
         return super.getStatName(a);
      }
   }

   @Override
   protected String getSpecial() {
      return Helper.format(beamLastTicks / Game.CLOCK_TICKS_PER_SECOND, 2) + "s";
   }

   @Override
   protected String getSpecialName() {
      return "Beam time";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Rectangle2D pathBounds) {
      double angle = ImageHelper.vectorAngle(dx, dy);
      return new Beam(this, p, angle, range, speed, damage, pathBounds, s, (int)beamLastTicks);
   }
   
   @Override
   protected void upgradeSpecial() {
      beamLastTicks += upgradeBeamLastTicks;
   }
   
   private static class Beam implements Bullet {

      private static final Color beamColour = new Color(138, 138, 138);
      private float currentAlpha = 1F;
      private static final float minAlpha = 0.2F;
      private static final Stroke stroke = new BasicStroke(4);
      private final float deltaAlpha;
      private final Rectangle2D pathBounds;
      private final Tower launchedBy;
      // Tried using a HashSet and a TreeMap here but there was no noticeable performance
      // improvement even with a large number of sprites.
      private final Collection<Sprite> hitSprites = new ArrayList<Sprite>();
      private final Line2D beam = new Line2D.Double();
      private final Point2D centre;
      private final double startingAngle;
      private double sinAngle;
      private double cosAngle;
      private double deltaAngle;
      private double sinDeltaAngle;
      private double cosDeltaAngle;
      private final int range;
      private final double numPointsMult;
      private Sprite target;
      private int ticksLeft;
      private final double damage;
      private double moneyEarnt = 0;
      //private List<Point2D> points = Collections.emptyList();
      
      private Beam(Tower t, Point2D centre, double angle, int range, double speed, double damage,
            Rectangle2D pathBounds, Sprite target, int numTicks) {
         deltaAlpha = (1 - minAlpha) / numTicks;
         this.centre = centre;
         this.pathBounds = pathBounds;
         this.launchedBy = t;
         startingAngle = angle;
         sinAngle = Math.sin(angle);
         cosAngle = Math.cos(angle);
         deltaAngle = Math.toRadians(speed / Game.CLOCK_TICKS_PER_SECOND);
         numPointsMult = Math.abs(deltaAngle);
         this.range = range;
         this.target = target;
         ticksLeft = numTicks;
         setBeam();
         this.damage = damage;
      }

      @Override
      public void draw(Graphics g) {
         Graphics2D g2D = (Graphics2D) g;
         
         g2D.setColor(beamColour);
         Composite c = g2D.getComposite();
         g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, currentAlpha));
         Stroke s = g2D.getStroke();
         g2D.setStroke(stroke);
         
         g2D.draw(beam);
         
         g2D.setComposite(c);
         g2D.setStroke(s);
         
         // Debugging code to make sure the points are in the right place
         /*g2D.setColor(Color.RED);
         for(Point2D p : points) {
            g2D.drawRect((int)p.getX(), (int)p.getY(), 1, 1);
         }*/
      }

      @Override
      public double tick(List<Sprite> sprites) {
         if(target != null) {
            setCorrectDirection();
         }
         if(ticksLeft <= 0) {
            return moneyEarnt;
         }
         ticksLeft--;
         currentAlpha -= deltaAlpha;
         List<Sprite> hittableSprites = new ArrayList<Sprite>(sprites);
         hittableSprites.removeAll(hitSprites);
         if(!hittableSprites.isEmpty()) {
            hitSprites(hittableSprites, makePoints());
         }
         // Use trig identities here as they should be faster than calling sin and cos.
         // sinAngle and cosAngle become sin and cos of (previous angle + deltaAngle).
         double newSinAngle = sinAngle * cosDeltaAngle + cosAngle * sinDeltaAngle;
         cosAngle = cosAngle * cosDeltaAngle - sinAngle * sinDeltaAngle;
         sinAngle = newSinAngle;
         setBeam();
         return -1;
      }
      
      private void setBeam() {
         beam.setLine(centre, new Point2D.Double(centre.getX() + range * sinAngle,
               centre.getY() + range * cosAngle));
      }
      
      private void setCorrectDirection() {
         Point2D p = target.getPosition();
         double angleBetween = ImageHelper.vectorAngle(p.getX() - centre.getX(),
               p.getY() - centre.getY());
         deltaAngle *= (angleBetween > startingAngle) ? -1 : 1;
         sinDeltaAngle = Math.sin(deltaAngle);
         cosDeltaAngle = Math.cos(deltaAngle);
         target = null;
      }
      
      private List<Point2D> makePoints() {
         List<Point2D> points = new ArrayList<Point2D>();
         for(int i = 1; i <= range; i++) {
            points.addAll(Helper.getPointsOnArc(centre.getX(), centre.getY(), i,
                  i * numPointsMult, sinAngle, cosAngle, pathBounds));
         }
         //this.points = points;
         return points;
      }
      
      private void hitSprites(List<Sprite> sprites, List<Point2D> points) {
         // TODO Optimise this somehow? This is the biggest bottleneck
         for(Sprite s : sprites) {
            if(s.intersects(points) != null) {
               DamageReport d = s.hit(damage);
               if(d != null) {
                  moneyEarnt += BasicBullet.processDamageReport(d, launchedBy);
                  hitSprites.add(s);
               }
            }
         }
      }
      
   }

}
