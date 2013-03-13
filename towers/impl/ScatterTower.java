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
import java.util.ArrayList;
import java.util.List;

import towers.AbstractTower;
import towers.Bullet;
import creeps.Creep;


public class ScatterTower extends AbstractTower {
   
   private int shots = 2;
   
   public ScatterTower(Point p) {
      super(p, "Scatter", 40, 100, 5, 5, 50, 0, true);
   }

   @Override
   public String getSpecial() {
      return Integer.toString(shots);
   }

   @Override
   public String getSpecialName() {
      return "Shots";
   }
   
   @Override
   protected List<Bullet> fireBullets(List<Creep> creeps) {
      List<Bullet> fired = new ArrayList<Bullet>();
      for (Creep c : creeps) {
         if (checkDistance(c)) {
            fired.addAll(fireBulletsAt(c, false));
            if(fired.size() >= shots) {
               return fired;
            }
         }
      }
      return fired;
   }

   @Override
   protected void upgradeSpecial() {
      shots++;
   }

}
