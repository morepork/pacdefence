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
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;
import util.Vector2D;
import creeps.Creep;
import creeps.Creep.DamageReport;


public class ZapperTower extends AbstractTower {
   
   private int numZaps = 5;
   private static final int upgradeIncreaseZaps = 1;
   
   public ZapperTower(Point p) {
      super(p, "Zapper", 40, 100, 1, 1.25, 50, 21, true);
   }


   @Override
   public String getSpecial() {
      return String.valueOf(numZaps);
   }

   @Override
   public String getSpecialName() {
      return "Zaps";
   }

   @Override
   protected Bullet makeBullet(Vector2D dir, int turretWidth, int range, double speed,
         double damage, Point p, Creep c) {
      return new ZapperBullet(this, dir, turretWidth, range, speed, damage, p, numZaps);
   }

   @Override
   protected void upgradeSpecial() {
      numZaps += upgradeIncreaseZaps;
   }
   
   private static class ZapperBullet extends BasicBullet {
      
      private static final Random rand = new Random();
      private static final Color zapColour = new Color(255, 147, 147);
      private static final Stroke zapStroke = new BasicStroke(3);
      private double moneyEarned = 0;
      private int numZapsLeft;
      private Line2D zap;
      private final double zapRange;
      private final int offScreenFudgeDistance;
   
      public ZapperBullet(Tower shotBy, Vector2D dir, int turretWidth, int range,
            double speed, double damage, Point p, int numZaps) {
         super(shotBy, dir, turretWidth, range, speed, damage, p);
         numZapsLeft = numZaps;
         zapRange = range / 4;
         // Bullet shouldn't be removed if it can still zap creeps
         offScreenFudgeDistance = super.getOffScreenFudgeDistance() + (int)zapRange;
      }
      
      @Override
      public void draw(Graphics2D g) {
         // The field zap can be set to null by the tick method, messing
         // up the actual drawing, so take a copy of the pointer
         Line2D zap = this.zap;
         if(zap != null) {
            Graphics2D g2D = (Graphics2D) g.create();
            g2D.setColor(zapColour);
            g2D.setStroke(zapStroke);
            g2D.draw(zap);
            g2D.dispose();
         }
         // Draw the actual bullet over the zap, so the zap appears to come from the edge of the
         // bullet
         super.draw(g);
      }
      
      @Override
      protected double doTick(List<Creep> creeps) {
         double value = super.doTick(creeps);
         // Only actually fire once every two ticks
         if(zap == null && numZapsLeft > 0) {
            tryToFireZap(creeps);
         } else {
            zap = null;
         }
         // If value is 0, the bullet reached the edge of its range
         return value == 0 ? moneyEarned : -1;
      }
      
      @Override
      protected double checkIfCreepIsHit(Point2D p1, Point2D p2, List<Creep> creeps) {
         // The actual bullet never hits
         return -1;
      }
      
      @Override
      protected int getOffScreenFudgeDistance() {
         return offScreenFudgeDistance;
      }
      
      private void tryToFireZap(List<Creep> creeps) {
         List<Creep> hittableCreeps = new ArrayList<Creep>();
         for(Creep c : creeps) {
            if(position.distance(c.getPosition()) < zapRange + c.getHalfWidth()) {
               hittableCreeps.add(c);
            }
         }
         fireZap(hittableCreeps);
      }
         
      private void fireZap(List<Creep> hittableCreeps) {
         if(hittableCreeps.isEmpty()) {
            // It didn't fire, so remove the last zap
            zap = null;
            return;
         }
         int index = rand.nextInt(hittableCreeps.size());
         Creep c = hittableCreeps.get(index);
         DamageReport d = c.hit(damage, shotBy.getClass());
         if(d != null) {
            moneyEarned += processDamageReport(d);
            numZapsLeft--;
            zap = new Line2D.Double(position, c.getPosition());
         } else {
            // It didn't actually hit after all so try the other creeps
            hittableCreeps.remove(index);
            fireZap(hittableCreeps);
         }
      }
      
   }

}
