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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sprites.Sprite;
import sprites.Sprite.DamageReport;

public class BomberTower extends AbstractTower {
   
   private int blastRadius = 20;
   // If the radius increases exponentially it just gets silly
   private final int blastRadiusIncrease = (int) (blastRadius * upgradeIncreaseFactor)
         - blastRadius;
   private static final double bombDamageDividend = 5;

   public BomberTower() {
      this(new Point(), null);
   }

   public BomberTower(Point p, Polygon path) {
      super(p, path, "Bomber", 40, 100, 5, 7, 50, 15, "bomber.png", "BomberTower.png");
   }

   @Override
   public String getSpecial() {
      return Integer.toString(blastRadius);
   }

   @Override
   public String getSpecialName() {
      return "Blast Radius";
   }

   @Override
   protected void upgradeSpecial() {
      blastRadius += blastRadiusIncrease;
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Polygon path) {
      return new Bomb(this, dx, dy, turretWidth, range, speed, damage, p, path);
   }

   private class Bomb extends AbstractBullet {

      private boolean exploding = false;
      private boolean expanding = true;
      private Set<Sprite> hitSprites = new HashSet<Sprite>();
      private final Color blastColour = new Color(255, 0, 0, 100);
      private final int blastSizeIncrement;
      private final int frames = 5;
      private final Circle blast = new Circle(new Point(0, 0), 0);
      private double moneyEarnt;

      public Bomb(Tower shotBy, double dx, double dy, int turretWidth, int range, double speed,
            double damage, Point p, Polygon path) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p, path);
         blastSizeIncrement = blastRadius / frames;
      }

      @Override
      public double tick(List<Sprite> sprites) {
         if (exploding) {
            double radius = blast.getRadius();
            if (expanding) {
               blast.setRadius(radius + blastSizeIncrement);
               if (blast.getRadius() >= blastRadius) {
                  blast.setRadius(blastRadius);
                  expanding = false;
               }
            } else {
               blast.setRadius(radius - blastSizeIncrement * 2);
               if (blast.getRadius() < 0) {
                  return moneyEarnt;
               }
            }
            checkIfSpriteIsHitByBlast(sprites);
            return -1;
         } else {
            double earnings = super.tick(sprites);
            if (earnings <= 0) {
               return earnings;
            } else {
               moneyEarnt = earnings;
               return -1;
            }
         }
      }

      @Override
      public void draw(Graphics g) {
         if (exploding) {
            g.setColor(blastColour);
            blast.fill(g);
         } else {
            super.draw(g);
         }
      }

      @Override
      protected void specialOnHit(Point2D p, Sprite s, List<Sprite> sprites) {
         // System.out.println(p.getX() + " " + p.getY());
         blast.setCentre(p);
         exploding = true;
      }

      private void checkIfSpriteIsHitByBlast(List<Sprite> sprites) {
         for (Sprite s : sprites) {
            if (!hitSprites.contains(s)) {
               // Sprites are only affected by the blast once
               if (blast.intersects(s.getBounds())) {
                  hitSprites.add(s);
                  DamageReport d = s.hit(getDamage() / bombDamageDividend);
                  if (d != null) {
                     moneyEarnt += processDamageReport(d);
                  }
               }
            }
         }
      }

   }

}
