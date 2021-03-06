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
import util.Vector2D;
import creeps.Creep;


public class MultiShotTower extends AbstractTower {
   
   private static final double speedIncreaseFactor = 1.1;
   private int shots = 5;
   
   public MultiShotTower(Point p) {
      super(p, "Multi Shot", 40, 100, 3, 1.5, 50, 5, true);
   }

   @Override
   public String getSpecial() {
      return Integer.toString(shots);
   }
   
   @Override
   public String getStatName(Attribute a) {
      if(a == Attribute.Speed) {
         return "Min " + a.toString();
      } else {
         return super.getStatName(a);
      }
   }

   @Override
   public String getSpecialName() {
      return "Shots";
   }

   @Override
   protected void upgradeSpecial() {
      shots++;
   }
   
   @Override
   protected List<Bullet> makeBullets(Vector2D dir,  int turretWidth, int range,
         double bulletSpeed, double damage, Point p, Creep c) {
      List<Bullet> bullets = new ArrayList<Bullet>();
      for(int i = 0; i < shots; i++) {
         bullets.add(makeBullet(dir, turretWidth, range, bulletSpeed, damage, p, c));
         bulletSpeed *= speedIncreaseFactor;
      }
      return bullets;
   }

}
