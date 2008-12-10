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

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sprites.Sprite;


public class JumpingTower extends AbstractTower {
   
   private int jumps = 1;
   private double jumpRangeDividend = 2;
   
   public JumpingTower() {
      this(new Point(), null);
   }
   
   public JumpingTower(Point p, Polygon path) {
      super(p, path, "Jumper", 40, 100, 5, 4, 50, 20, "jumping.png", "JumpingTower.png");
   }

   @Override
   public String getSpecial() {
      return Integer.toString(jumps);
   }

   @Override
   public String getSpecialName() {
      return "Jumps";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Polygon path) {
      return new JumpingBullet(this, dx, dy, turretWidth, range, speed, damage, p, path);
   }

   @Override
   protected void upgradeSpecial() {
      jumps++;
   }
   
   private class JumpingBullet extends AbstractBullet {
      
      private final Collection<Sprite> hitSprites = new ArrayList<Sprite>();
      private int hitsLeft;
      private final int jumpRange; 

      public JumpingBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, Polygon path) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p, path);
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
                  JumpingBullet b = (JumpingBullet) fireBullet(a, point, false, 0, jumpRange,
                        getBulletSpeed(), getDamage()).get(0);
                  b.addHitSprites(hitSprites);
                  b.setHitsLeft(hitsLeft);
                  shotBy.addExtraBullets(b);               
                  break;
               }
            }
         }
      }
      
      @Override
      protected double doTick(List<Sprite> sprites) {
         List<Sprite> newList = Helper.cloneList(sprites);
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
