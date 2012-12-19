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

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;
import util.Vector2D;
import creeps.Creep;

// The laser tower is very similar to this so it is unneeded really
@Deprecated
public class PiercerTower extends AbstractTower {
   
   private int pierces = 1;

   public PiercerTower(Point p, List<Shape> pathBounds) {
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
   protected Bullet makeBullet(Vector2D dir, int turretWidth, int range, double speed,
         double damage, Point p, Creep c, List<Shape> pathBounds) {
      return new PiercingBullet(this, dir, turretWidth, range, speed, damage, p, pathBounds);
   }

   @Override
   protected void upgradeSpecial() {
      pierces++;
   }
   
   private class PiercingBullet extends BasicBullet {
      
      private int piercesSoFar = 0;
      private Collection<Creep> creepsHit = new ArrayList<Creep>();
      private int moneyEarnt;

      public PiercingBullet(Tower shotBy, Vector2D dir, int turretWidth, int range,
            double speed, double damage, Point p, List<Shape> pathBounds) {
         super(shotBy, dir, turretWidth, range, speed, damage, p, pathBounds);
      }
      
      @Override
      public double doTick(List<Creep> creeps) {
         List<Creep> newCreeps = new ArrayList<Creep>(creeps);
         // Removes all the previously hit creeps so they aren't hit again
         newCreeps.removeAll(creepsHit);
         return processShotResult(super.doTick(newCreeps), newCreeps);
      }
      
      @Override
      public void specialOnHit(Point2D p, Creep c, List<Creep> creeps) {
         creepsHit.add(c);
      }
      
      private double processShotResult(double shotResult, List<Creep> creeps) {
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
               // Removes the creep that was last hit so it can't be hit again
               creeps.removeAll(creepsHit);
               // Checks if any other creeps were hit between the last and
               // current points, and recursively processes them.
               return processShotResult(super.checkIfCreepIsHit(creeps), creeps);
            }
         }
      }
      
   }

}
