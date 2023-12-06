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


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;
import util.Circle;
import util.Helper;
import util.Vector2D;
import creeps.Creep;
import creeps.Creep.DamageReport;

public class BomberTower extends AbstractTower {
   
   private static final double startingBlastRadius = 30;
   // If the radius increases exponentially it just gets silly
   private static final double blastRadiusIncrease = startingBlastRadius * .1;
   private double blastRadius = startingBlastRadius;
   private static final double bombDamageDividend = 2;

   public BomberTower(Point p) {
      super(p, "Bomber", 40, 100, 5, 10, 50, 15, true);
   }

   @Override
   public String getSpecial() {
      return Helper.format(blastRadius, 0);
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
   protected Bullet makeBullet(Vector2D dir, int turretWidth, int range, double speed,
         double damage, Point p, Creep c) {
      return new Bomb(this, dir, turretWidth, range, speed, damage, p, blastRadius);
   }

   public static class Bomb extends BasicBullet {

      private boolean exploding = false;
      private boolean expanding = true;
      private Set<Creep> hitCreeps = new HashSet<Creep>();
      private final Color blastColour = new Color(255, 0, 0, 75);
      private final double blastRadius;
      private final double blastSizeIncrement;
      private final int frames = 5;
      private final Circle blast = new Circle(new Point(0, 0), 0);
      private double moneyEarned;

      public Bomb(Tower shotBy, Vector2D dir, int turretWidth, int range, double speed,
            double damage, Point p, double blastRadius) {
         super(shotBy, dir, turretWidth, range, speed, damage, p);
         this.blastRadius = blastRadius;
         blastSizeIncrement = blastRadius / frames;
      }

      @Override
      public double doTick(List<Creep> creeps) {
         if(exploding) {
            double radius = blast.getRadius();
            if(expanding) {
               blast.setRadius(radius + blastSizeIncrement);
               if(blast.getRadius() >= blastRadius) {
                  blast.setRadius(blastRadius);
                  expanding = false;
               }
            } else {
               // Shrinks twice as fast as it expands
               double newRadius = radius - blastSizeIncrement * 2;
               if(newRadius < 0) {
                  return moneyEarned;
               } else {
                  blast.setRadius(newRadius);
               }
            }
            checkIfCreepIsHitByBlast(creeps);
            return -1;
         } else { // If it's not exploding, the bullet is still travelling
            double earnings = super.doTick(creeps);
            if(earnings <= 0) {
               // <= 0 means it has either it is still going, or it got to the edge of its range
               return earnings;
            } else {
               // If it does hit something, record the money earned, and then return -1, the bullet
               // should not be removed just yet
               moneyEarned = earnings;
               return -1;
            }
         }
      }

      @Override
      public void draw(Graphics2D g) {
         if(exploding) {
            g.setColor(blastColour);
            blast.fill(g);
         } else {
            super.draw(g);
         }
      }

      @Override
      protected void specialOnHit(Point2D p, Creep c, List<Creep> creeps) {
         // System.out.println(p.getX() + " " + p.getY());
         blast.setCentre(p);
         exploding = true;
         hitCreeps.add(c);
      }

      private void checkIfCreepIsHitByBlast(List<Creep> creeps) {
         for(Creep c : creeps) {
            // Creeps are only affected by the blast once
            if(!hitCreeps.contains(c) && blast.intersects(c.getBounds())) {
               hitCreeps.add(c);
               DamageReport d = c.hit(damage / bombDamageDividend, shotBy.getClass());
               if(d != null) {
                  moneyEarned += processDamageReport(d);
               }
            }
         }
      }

      public boolean isExploding() {
         return this.exploding;
      }

   }

}
