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

package creeps;

import gui.Drawable;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Comparator;

import towers.DamageNotifier;
import towers.Tower;


/**
 * Is a creep, one of the things that is meant to be killed
 * 
 * @author Byrne
 * 
 */
public interface Creep extends Comparable<Creep>, Drawable {
   
   /**
    * 
    * @return true if this Creep has finished or has been killed, false otherwise
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
   public boolean isFinished();
   /**
    * 
    * @param b
    * @return null if the creep is already dead
    */
   public DamageReport hit(double damage, Class<? extends Tower> towerClass);
   public boolean intersects(Point2D p);
   public Point2D intersects(Line2D line);
   public boolean intersects(Arc2D a);
   
   public void slow(double factor, int numTicks);
   public void setDamageMultiplier(DamageNotifier dn, double multiplier, int numTicks);
   public void poison(int numTicks);
   
   public enum CreepEffect {
      SLOW,
      WEAK,
      POISON;
   }
   
   public abstract class AbstractCreepComparator implements Comparator<Creep> {
      @Override
      public boolean equals(Object o) {
         return getClass() == o.getClass();
      }
      
      @Override
      public abstract String toString();
   }

   public class FirstComparator extends AbstractCreepComparator {
      @Override
      public int compare(Creep c1, Creep c2) {
         return Double.compare(c2.getTotalDistanceTravelled(), c1.getTotalDistanceTravelled());
      }
      
      @Override
      public String toString() {
         return "First";
      }
   }
   
   public class LastComparator extends FirstComparator {
      @Override
      public int compare(Creep c1, Creep c2) {
         return -super.compare(c1, c2);
      }
      
      @Override
      public String toString() {
         return "Last";
      }
   }
   
   public class FastestComparator extends AbstractCreepComparator {
      @Override
      public int compare(Creep c1, Creep c2) {
         return Double.compare(c2.getSpeed(), c1.getSpeed());
      }
      
      @Override
      public String toString() {
         return "Fastest";
      }
   }
   
   public class SlowestComparator extends FastestComparator {
      @Override
      public int compare(Creep c1, Creep c2) {
         return -super.compare(c1, c2);
      }
      
      @Override
      public String toString() {
         return "Slowest";
      }
   }
   
   public class MostHPComparator extends AbstractCreepComparator {
      @Override
      public int compare(Creep c1, Creep c2) {
         return Double.compare(c2.getHPLeft(), c1.getHPLeft());
      }
      
      @Override
      public String toString() {
         return "Most HP";
      }
   }
   
   public class LeastHPComparator extends MostHPComparator {
      @Override
      public int compare(Creep c1, Creep c2) {
         return -super.compare(c1, c2);
      }
      
      @Override
      public String toString() {
         return "Least HP";
      }
   }
   
   public class RandomComparator extends AbstractCreepComparator {
      @Override
      public int compare(Creep c1, Creep c2) {
         throw new RuntimeException("Not implemented");
      }
      
      @Override
      public String toString() {
         return "Random";
      }
   }
   
   // Note, as the compare method is slow, this could cause some lag when there are lots of creeps
   public class DistanceComparator extends AbstractCreepComparator {
      private final Point2D towerLocation = new Point2D.Double();
      private final boolean closestFirst;
      
      public DistanceComparator(Point2D p, boolean closestFirst) {
         this.towerLocation.setLocation(p);
         this.closestFirst = closestFirst;
      }
      
      private double distanceSq(Point2D p1, Point2D p2) {
         double dx = p2.getX() - p1.getX();
         double dy = p2.getY() - p1.getY();
         return dx * dx + dy * dy;
      }

      @Override
      public int compare(Creep c1, Creep c2) {
         // Find the distance (squared) to each creep. Using the square as it's not worth the extra
         // operation to find the square root to get the actual distance when for the comparison it doesn't
         // matter.
         double d1 = distanceSq(c1.getPosition(), towerLocation);
         double d2 = distanceSq(c2.getPosition(), towerLocation);
         int val = Double.compare(d1, d2);
         return closestFirst ? val : -val;
      }
      
      @Override
      public boolean equals(Object o) {
         return getClass() == o.getClass() && closestFirst == ((DistanceComparator) o).closestFirst;
      }
      
      @Override
      public String toString() {
         return closestFirst ? "Closest" : "Farthest";
      }
      
      public boolean isClosestFirst() {
         return closestFirst;
      }
      
   }
   
   public class DamageReport {
      
      private final double damage;
      private final double moneyEarned;
      private final boolean kill;
      
      public DamageReport(double damage, double moneyEarned, boolean kill) {
         this.damage = damage;
         this.moneyEarned = moneyEarned;
         this.kill = kill;
      }
      
      public double getDamage() {
         return damage;
      }
      
      public double getMoneyEarned() {
         return moneyEarned;
      }
      
      /**
       * Returns whether this damage was a kill directly.
       * If the damage was increased because the creep was taking extra damage due to the effect
       * of another tower, and this caused the creep to be killed, this will be false even though
       * the tower was killed.
       */
      public boolean wasKill() {
         return kill;
      }
   }
   

}
