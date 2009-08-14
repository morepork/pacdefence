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

package towers.impl;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sprites.Sprite;
import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;


public class JumperTower extends AbstractTower {
   
   private int jumps = 1;
   
   public JumperTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Jumper", 40, 100, 5, 5.8, 50, 20, true);
   }

   @Override
   public String getSpecial() {
      return String.valueOf(jumps);
   }

   @Override
   public String getSpecialName() {
      return "Jumps";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, List<Shape> pathBounds) {
      return new JumpingBullet(this, dx, dy, turretWidth, range, speed, damage, p, pathBounds, jumps);
   }

   @Override
   protected void upgradeSpecial() {
      jumps++;
   }
   
   private class JumpingBullet extends BasicBullet {
      
      private final Collection<Sprite> hitSprites = new ArrayList<Sprite>();
      private int hitsLeft;
      private final int jumpRange;
      private static final double jumpRangeDividend = 1.5;

      public JumpingBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, List<Shape> pathBounds, int jumps) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p, pathBounds);
         hitsLeft = jumps;
         jumpRange = (int)(range / jumpRangeDividend);
      }
      
      @Override
      protected void specialOnHit(Point2D p, Sprite s, List<Sprite> sprites) {
         hitSprites.add(s);
         if(hitsLeft > 0) {
            Point point = new Point((int)p.getX(), (int)p.getY());
            for(Sprite a : sprites) {
               if(!hitSprites.contains(a) && checkDistance(a, point, jumpRange)) {
                  hitsLeft--;
                  JumpingBullet b = (JumpingBullet) fireBulletsAt(a, point, false, 0, jumpRange,
                        getBulletSpeed(), getDamage()).get(0);
                  b.addHitSprites(hitSprites);
                  b.setHitsLeft(hitsLeft);
                  addExtraBullets(b);
                  break;
               }
            }
         }
      }
      
      @Override
      protected double doTick(List<Sprite> sprites) {
         List<Sprite> newList = new ArrayList<Sprite>(sprites);
         newList.removeAll(hitSprites);
         return super.doTick(newList);
      }
      
      private void addHitSprites(Collection<Sprite> sprites) {
         hitSprites.addAll(sprites);
      }
      
      private void setHitsLeft(int hitsLeft) {
         this.hitsLeft = hitsLeft;
      }
      
   }

}
