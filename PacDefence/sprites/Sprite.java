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

package sprites;

import gui.Drawable;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.Random;

import towers.Tower;


/**
 * Is a sprite, one of the things that is meant to be killed
 * 
 * @author Byrne
 * 
 */
public interface Sprite extends Comparable<Sprite>, Drawable {
   
   public void draw(Graphics g);
   /**
    * 
    * @return true if this Sprite has finished or has been killed, false otherwise
    */
   public boolean tick();
   public int getHalfWidth();
   public Point2D getPosition();
   public double getCurrentAngle();
   public double getSpeed();
   public double getHPLeft();
   public double getTotalDistanceTravelled();
   public Shape getBounds();
   public boolean isAlive();
   /**
    * 
    * @param b
    * @return null if the sprite is already dead
    */
   public DamageReport hit(double damage, Class<? extends Tower> towerClass);
   public boolean intersects(Point2D p);
   public Point2D intersects(Line2D line);
   public boolean intersects(Arc2D a);
   
   /**
    * Returns a double representing initial baseHP / totalHP
    */
   public double getHPFactor();
   public void slow(double factor, int numTicks);
   public void setDamageMultiplier(double multiplier, int numTicks);
   
   public abstract class AbstractSpriteComparator implements Comparator<Sprite> {
      @Override
      public boolean equals(Object o) {
         return getClass() == o.getClass();
      }
      
      @Override
      public abstract String toString();
   }

   public class FirstComparator extends AbstractSpriteComparator {
      @Override
      public int compare(Sprite s1, Sprite s2) {
         return (int) (s2.getTotalDistanceTravelled() - s1.getTotalDistanceTravelled());
      }
      
      @Override
      public String toString() {
         return "First";
      }
   }
   
   public class LastComparator extends FirstComparator {
      @Override
      public int compare(Sprite s1, Sprite s2) {
         return -super.compare(s1, s2);
      }
      
      @Override
      public String toString() {
         return "Last";
      }
   }
   
   public class FastestComparator extends AbstractSpriteComparator {
      @Override
      public int compare(Sprite s1, Sprite s2) {
         return (int)((s2.getSpeed() - s1.getSpeed()) * 100);
      }
      
      @Override
      public String toString() {
         return "Fastest";
      }
   }
   
   public class SlowestComparator extends FastestComparator {
      @Override
      public int compare(Sprite s1, Sprite s2) {
         return -super.compare(s1, s2);
      }
      
      @Override
      public String toString() {
         return "Slowest";
      }
   }
   
   public class MostHPComparator extends AbstractSpriteComparator {
      @Override
      public int compare(Sprite s1, Sprite s2) {
         int sign = s2.getHPLeft() > s1.getHPLeft() ? 1 : -1;
         // Take the log here as the hp difference can get larger than an int
         return sign * (int)Math.log(Math.abs(s2.getHPLeft() - s1.getHPLeft()) + 1);
      }
      
      @Override
      public String toString() {
         return "Most HP";
      }
   }
   
   public class LeastHPComparator extends MostHPComparator {
      @Override
      public int compare(Sprite s1, Sprite s2) {
         return -super.compare(s1, s2);
      }
      
      @Override
      public String toString() {
         return "Least HP";
      }
   }
   
   public class RandomComparator extends AbstractSpriteComparator {
      private static final Random rand = new Random();
      @Override
      public int compare(Sprite s1, Sprite s2) {
         return rand.nextInt();
      }
      
      @Override
      public String toString() {
         return "Random";
      }
   }
   
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
