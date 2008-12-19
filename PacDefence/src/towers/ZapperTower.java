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

import gui.Helper;
import gui.OuterPanel;

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
import java.util.List;
import java.util.Random;

import sprites.Sprite;
import sprites.Sprite.DamageReport;


public class ZapperTower extends AbstractTower {
   
   private int numZaps = 5;
   private static final int upgradeIncreaseZaps = 1;
   
   public ZapperTower() {
      this(new Point(), null);
   }

   public ZapperTower(Point p, Polygon path) {
      super(p, path, "Zapper", 40, 100, 2, 1.3, 50, 21, "zapper.png", "ZapperTower.png");
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
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Polygon path) {
      return new ZapperBullet(this, dx, dy, turretWidth, range, speed, damage, p, path, numZaps);
   }

   @Override
   protected void upgradeSpecial() {
      numZaps += upgradeIncreaseZaps;
   }
   
   private static class ZapperBullet extends BasicBullet {
      
      private static final Random rand = new Random();
      private static final Color zapColour = new Color(255, 147, 147);
      private double moneyEarnt = 0;
      private int numZapsLeft;
      private Line2D zap;
      private final double zapRange;
   
      public ZapperBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, Polygon path, int numZaps) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p, path);
         numZapsLeft = numZaps;
         zapRange = range / 4;
      }
      
      @Override
      public void draw(Graphics g) {
         // The field zap can be set to null by the tick method, messing
         // up the actual drawing, so take a copy of the pointer
         Line2D zap = this.zap;
         if(zap != null) {
            Graphics2D g2D = (Graphics2D) g;
            g2D.setColor(zapColour);
            Stroke s = g2D.getStroke();
            g2D.setStroke(new BasicStroke(2));
            g2D.draw(zap);
            g2D.setStroke(s);
         }
         super.draw(g);
      }
      
      @Override
      protected double doTick(List<Sprite> sprites) {
         double value = super.doTick(sprites);
         // Only actually fire once every two ticks
         if(zap == null && numZapsLeft > 0) {
            tryToFireZap(sprites);
         } else {
            zap = null;
         }
         // If value is 0, the bullet reached the edge of its range
         return value == 0 ? moneyEarnt : -1;
      }
      
      @Override
      protected double checkIfSpriteIsHit(Point2D p1, Point2D p2, List<Sprite> sprites) {
         // The actual bullet never hits
         return -1;
      }
      
      @Override
      protected boolean checkIfBulletCanBeRemovedAsOffScreen() {
         // Bullet shouldn't be removed if it can still zap sprites
         return position.getX() < -zapRange || position.getY() < -zapRange ||
               position.getX() > OuterPanel.MAP_WIDTH + zapRange ||
               position.getY() > OuterPanel.MAP_HEIGHT + zapRange;
      }
      
      private void tryToFireZap(List<Sprite> sprites) {
         List<Sprite> hittableSprites = new ArrayList<Sprite>();
         for(Sprite s : sprites) {
            if(Helper.distance(position, s.getPosition()) < zapRange + s.getHalfWidth()) {
               hittableSprites.add(s);
            }
         }
         fireZap(hittableSprites);
      }
         
      private void fireZap(List<Sprite> hittableSprites) {
         if(hittableSprites.isEmpty()) {
            // It didn't fire, so remove the last zap
            zap = null;
            return;
         }
         int index = rand.nextInt(hittableSprites.size());
         Sprite s = hittableSprites.get(index);
         DamageReport d = s.hit(damage);
         if(d != null) {
            moneyEarnt += processDamageReport(d);
            numZapsLeft--;
            zap = new Line2D.Double(position, s.getPosition());
         } else {
            // It didn't actually hit after all so try the other sprites
            hittableSprites.remove(index);
            fireZap(hittableSprites);
         }
      }
      
   }

}
