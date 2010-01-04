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
 *  (C) Liam Byrne, 2008 - 10.
 */

package towers.impl;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import logic.Game;
import logic.Helper;
import sprites.Sprite;
import sprites.Sprite.DamageReport;
import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;


public class BeamTower extends AbstractTower {
   
   // The number of ticks the beam lasts for
   private static final double startingBeamLastTicks = Game.CLOCK_TICKS_PER_SECOND / 2;
   private static final double upgradeBeamLastTicks = startingBeamLastTicks / 10;
   private double beamLastTicks = startingBeamLastTicks;
   
   public BeamTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Beam", 40, 80, 40, 3.9, 50, 0, false);
      // This is a grossly overpowered version for testing performance.
      /*super(p, pathBounds, "Beam", 0, 1000, 100, 0.05, 50, 0, false);
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
         double damage, Point p, Sprite s, List<Shape> pathBounds) {
      double angle = Helper.vectorAngle(dx, dy);
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
      private final Tower launchedBy;
      // Tried using a HashSet and a TreeMap here but there was no noticeable performance
      // improvement even with a large number of sprites.
      private final Collection<Sprite> hitSprites = new ArrayList<Sprite>();
      //private final Line2D beam = new Line2D.Double();
      private final Point2D centre;
      private final Arc2D arc = new Arc2D.Double(Arc2D.PIE);
      // arcAngle and extentAngle are specific to the arc
      private double arcAngle;
      private final double deltaAngle;
      private final int range;
      private int ticksLeft;
      private final double damage;
      private double moneyEarnt = 0;
      
      private Beam(Tower t, Point2D centre, double angle, int range, double speed, double damage,
            List<Shape> pathBounds, Sprite target, int numTicks) {
         deltaAlpha = (1 - minAlpha) / numTicks;
         this.centre = centre;
         this.launchedBy = t;
         this.arcAngle = Math.toDegrees(angle) - 90;
         this.deltaAngle = speed / Game.CLOCK_TICKS_PER_SECOND * getDirectionModifier(target);
         this.range = range;
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
         
         g2D.draw(new Line2D.Double(centre, arc.getStartPoint()));
         
         g2D.setComposite(c);
         g2D.setStroke(s);
         
         // Debugging code to make sure the arc is in the right place
         /*g2D.setColor(Color.RED);
         g2D.draw(arc);*/
      }

      @Override
      public double tick(List<Sprite> sprites) {
         if(ticksLeft <= 0) {
            return moneyEarnt;
         }
         ticksLeft--;
         currentAlpha -= deltaAlpha;
         hitSprites(sprites);
         arcAngle += deltaAngle;
         setBeam();
         return -1;
      }
      
      private void setBeam() {
         // Negate deltaAngle here as the arc extends behind the beam
         arc.setArcByCenter(centre.getX(), centre.getY(), range, arcAngle, -deltaAngle,
               Arc2D.PIE);
      }
      
      private int getDirectionModifier(Sprite s) {
         Point2D p = s.getPosition();
         double angleBetween = Helper.vectorAngle(p.getX() - centre.getX(),
               p.getY() - centre.getY());
         // Two points just ahead of where the sprite is now
         double nextX = p.getX() + Math.sin(s.getCurrentAngle());
         double nextY = p.getY() - Math.cos(s.getCurrentAngle());
         double nextAngleBetween = Helper.vectorAngle(nextX - centre.getX(),
               nextY - centre.getY());
         return (nextAngleBetween > angleBetween) ? -1 : 1;
      }
      
      private void hitSprites(List<Sprite> sprites) {
         for(Sprite s : sprites) {
            if(!hitSprites.contains(s) && s.intersects(arc)) {
               DamageReport d = s.hit(damage, launchedBy.getClass());
               if(d != null) {
                  moneyEarnt += BasicBullet.processDamageReport(d, launchedBy);
                  hitSprites.add(s);
               }
            }
         }
      }
      
   }

}
