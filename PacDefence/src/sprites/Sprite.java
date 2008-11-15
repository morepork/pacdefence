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

package sprites;

import java.awt.Graphics;
import java.awt.geom.Point2D;

import towers.Bullet;


/**
 * Is a sprite, one of the things that is meant to be killed
 * 
 * @author Byrne
 * 
 */
public interface Sprite {
   
   public void draw(Graphics g);
   public boolean tick();
   public int getHalfWidth();
   public Point2D getPosition();
   public double getTotalDistanceTravelled();
   public boolean isAlive();
   public DamageReport hitBy(Bullet b);
   public boolean intersects(Point2D p);
   
   /**
    * Returns a double representing initial totalHP / baseHP
    */
   public double getHPFactor();
   
   
   public class DamageReport {
      
      private final double damage;
      private final double moneyEarnt;
      private final boolean kill;
      
      public DamageReport(double damage, double moneyEarnt, boolean kill) {
         this.damage = damage;
         this.moneyEarnt = moneyEarnt;
         this.kill = kill;
      }
      
      public double getDamage() {
         return damage;
      }
      
      public double getMoneyEarnt() {
         return moneyEarnt;
      }
      
      public boolean wasKill() {
         return kill;
      }
   }
   

}
