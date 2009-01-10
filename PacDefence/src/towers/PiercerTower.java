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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sprites.Sprite;


public class PiercerTower extends AbstractTower {
   
   private int pierces = 1;

   public PiercerTower(Point p, Rectangle2D pathBounds) {
      super(p, pathBounds, "Piercer", 40, 100, 5, 8, 50, 20, true);
   }

   @Override
   public String getSpecial() {
      return Integer.toString(pierces);
   }

   @Override
   public String getSpecialName() {
      return "Pierces";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Rectangle2D pathBounds) {
      return new PiercingBullet(this, dx, dy, turretWidth, range, speed, damage, p, pathBounds);
   }

   @Override
   protected void upgradeSpecial() {
      pierces++;
   }
   
   private class PiercingBullet extends BasicBullet {
      
      private int piercesSoFar = 0;
      private Collection<Sprite> spritesHit = new ArrayList<Sprite>();
      private int moneyEarnt;

      public PiercingBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, Rectangle2D pathBounds) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p, pathBounds);
      }
      
      @Override
      public double doTick(List<Sprite> sprites) {
         List<Sprite> newSprites = new ArrayList<Sprite>(sprites);
         // Removes all the previously hit sprites so they aren't hit again
         newSprites.removeAll(spritesHit);
         return processShotResult(super.doTick(newSprites), newSprites);
      }
      
      @Override
      public void specialOnHit(Point2D p, Sprite s, List<Sprite> sprites) {
         spritesHit.add(s);
      }
      
      private double processShotResult(double shotResult, List<Sprite> sprites) {
         if(shotResult < 0) {
            // Bullet didn't hit anything
            return shotResult;
         } else if(shotResult == 0) {
           // Bullet has reached the edge of its range
           return moneyEarnt;
         } else {
            // Bullet hit something
            moneyEarnt += shotResult;
            if(piercesSoFar >= pierces) {
               return moneyEarnt;
            } else {
               piercesSoFar++;
               // Removes the sprite that was last hit so it can't be hit again
               sprites.removeAll(spritesHit);
               // Checks if any other sprites were hit between the last and
               // current points, and recursively processes them.
               return processShotResult(super.checkIfSpriteIsHit(sprites), sprites);
            }
         }
      }
      
   }

}
