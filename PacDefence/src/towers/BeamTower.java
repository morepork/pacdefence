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
import gui.GameMap;
import gui.Helper;
import images.ImageHelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import sprites.Sprite;
import sprites.Sprite.DamageReport;


public class BeamTower extends AbstractTower {
   
   // The number of ticks the beam lasts for
   private double beamLastTicks = GameMap.CLOCK_TICKS_PER_SECOND / 2;
   private static final double upgradeBeamLastTicks = GameMap.CLOCK_TICKS_PER_SECOND / 20;
   
   public BeamTower() {
      this(new Point(), null);
   }

   public BeamTower(Point p, Polygon path) {
      super(p, path, "Beam", 40, 100, 40, 4.5, 50, 0, "beam.png", "BeamTower.png");
   }
   
   @Override
   public String getStat(Attribute a) {
      if(a == Attribute.Speed) {
         return super.getStat(a) + "�/s";
      } else {
         return super.getStat(a);
      }
   }

   @Override
   protected String getSpecial() {
      return Helper.format(beamLastTicks / GameMap.CLOCK_TICKS_PER_SECOND, 2) + "s";
   }

   @Override
   protected String getSpecialName() {
      return "Beam time";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Polygon path) {
      double angle = ImageHelper.vectorAngle(dx, dy);
      return new Beam(this, p, angle, range, speed, damage, path, s, (int)beamLastTicks);
   }
   
   @Override
   protected void upgradeSpecial() {
      beamLastTicks += upgradeBeamLastTicks;
   }
   
   private static class Beam implements Bullet {

      private Color beamColour = new Color(138, 138, 138);
      private static final int minAlpha = 60;
      private final int deltaAlpha;
      private final Polygon path;
      private final Tower launchedBy;
      private final Set<Sprite> hitSprites = new CopyOnWriteArraySet<Sprite>();
      private final Line2D beam = new Line2D.Double();
      private final Circle circle;      
      private double currentAngle;
      private double deltaAngle;
      private double deltaAngleOverSpeed;
      private Sprite target;
      private int ticksLeft;
      private final int sizeOfSetOfPoints;
      private final double damage;
      private double moneyEarnt = 0;
      
      private Beam(Tower t, Point2D centre, double angle, int range, double speed, double damage,
            Polygon path, Sprite target, int numTicks) {
         deltaAlpha = (beamColour.getAlpha() - minAlpha) / numTicks;
         this.path = path;
         this.launchedBy = t;
         currentAngle = angle;
         deltaAngle = Math.toRadians(speed / GameMap.CLOCK_TICKS_PER_SECOND);
         deltaAngleOverSpeed = deltaAngle / speed;
         circle = new Circle(centre, range);
         this.target = target;
         ticksLeft = numTicks;
         setBeam();
         sizeOfSetOfPoints = (int)(range * speed);
         this.damage = damage;
      }

      @Override
      public void draw(Graphics g) {
         Graphics2D g2D = (Graphics2D) g;
         g2D.setColor(beamColour);
         Stroke s = g2D.getStroke();
         g2D.setStroke(new BasicStroke(4));
         g2D.draw(beam);
         g2D.setStroke(s);
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
         if(beamColour.getAlpha() > minAlpha) {
            beamColour = new Color(beamColour.getRed(), beamColour.getGreen(), beamColour.getBlue(),
                     beamColour.getAlpha() - deltaAlpha);
         }
         List<Sprite> hittableSprites = new ArrayList<Sprite>(sprites);
         hittableSprites.removeAll(hitSprites);
         List<Point2D> points = makePoints();
         hitSprites(hittableSprites, points);
         currentAngle += deltaAngle;
         setBeam();
         return -1;
      }
      
      private void setBeam() {
         beam.setLine(circle.getCentre(), circle.getPointAt(currentAngle));
      }
      
      private void setCorrectDirection() {
         Point2D p = target.getPosition();
         Point2D centre = circle.getCentre();
         double angleBetween = ImageHelper.vectorAngle(p.getX() - centre.getX(),
               p.getY() - centre.getY());
         int mult = (angleBetween > currentAngle) ? -1 : 1;
         deltaAngle *= mult;
         deltaAngleOverSpeed *= mult;
         target = null;
      }
      
      private List<Point2D> makePoints() {
         // Internally use a set of points to eliminate duplicates that'll slow it
         // when checking if the points hit the sprite.
         Set<Point> points = new HashSet<Point>(sizeOfSetOfPoints);
         for(double d = currentAngle; d <= currentAngle + deltaAngle; d += deltaAngleOverSpeed) {
            List<Point2D> linePoints = Helper.getPointsOnLine(circle.getCentre(),
                  circle.getPointAt(d));
            for(Point2D p : linePoints) {
               if(path.getBounds2D().contains(p)) {
                  points.add(new Point((int)p.getX(), (int)p.getY()));
               }
            }
         }
         // Returns a list rather than the set as it should be faster to iterate through
         return new ArrayList<Point2D>(points);
      }
      
      private void hitSprites(List<Sprite> sprites, List<Point2D> points) {
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
