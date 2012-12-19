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
import java.util.ArrayList;
import java.util.List;

import towers.AbstractTower;
import towers.Bullet;
import util.Vector2D;
import creeps.Creep;


public class OmnidirectionalTower extends AbstractTower {
   
   private int numShots = 3;
   
   public OmnidirectionalTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Omnidirectional", 40, 100, 5, 5.5, 50, 0, true);
      // Testing tower with way too many bullets
      /*super(p, pathBounds, "Omnidirectional", 0, 1000, 5, 0.005, 50, 0, true);
      for(int i = 0; i < 200; i++) {
         upgradeSpecial();
      }*/
   }

   @Override
   public String getSpecial() {
      return String.valueOf(numShots);
   }

   @Override
   public String getSpecialName() {
      return "Number of shots";
   }
   
   @Override
   protected List<Bullet> makeBullets(Vector2D dir, int turretWidth, int range,
         double speed, double damage, Point p, Creep c, List<Shape> pathBounds) {
      List<Bullet> bullets = new ArrayList<Bullet>();
      double angle = dir.getAngle();
      double dTheta = 2 * Math.PI / numShots;
      for(int i = 0; i < numShots; i++) {
         Vector2D bulletDir = new Vector2D(Math.sin(angle), Math.cos(angle));
         angle += dTheta;
         bullets.add(makeBullet(bulletDir, turretWidth, range, speed, damage, p, c, pathBounds));
      }
      return bullets;
   }

   @Override
   protected void upgradeSpecial() {
      numShots++;
   }

}
