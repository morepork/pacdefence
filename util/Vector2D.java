/*
 * This file is part of Pac Defence.
 *
 * Pac Defence is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Pac Defence is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * (C) Liam Byrne, 2008 - 2012.
 */

package util;

import java.awt.geom.Point2D;


public class Vector2D {
   
   private static final double twoPi = 2 * Math.PI;
   private final double x, y;
   
   public Vector2D(double x, double y) {
      this.x = x;
      this.y = y;
   }

   public double getX() {
      return x;
   }
   
   public double getY() {
      return y;
   }
   
   public double getLength() {
      // Maybe precompute this?
      return Math.sqrt(x * x + y * y);
   }
   
   public double getAngle() {
      return Vector2D.angle(x, y);
   }
   
   // Static constructors _____________________________________________
   
   public static Vector2D createFromPoints(Point2D p1, Point2D p2) {
      return new Vector2D(p2.getX() - p1.getX(), p2.getY() - p1.getY());
   }
   
   public static Vector2D createFromVector(Vector2D vec, double length) {
      double mult = length / vec.getLength();
      return new Vector2D(vec.getX() * mult, vec.getY() * mult);
   }
   
   public static Vector2D createFromAngle(double angle, double length) {
      return new Vector2D(Math.sin(angle) * length, Math.cos(angle) * length);
   }
   
   // Static functions ________________________________________________
   
   public static double angle(double x, double y) {
      if (x >= 0) {
         if (y > 0) {
            return Math.atan(x / y);
         } else {
            return Math.atan(-y / x) + Math.PI / 2;
         }
      } else {
         if (y > 0) {
            return Math.atan(x / y) + twoPi;
         } else {
            return Math.atan(x / y) + Math.PI;
         }
      }
   }
   
   public static double angle(Point2D p1, Point2D p2) {
      return Vector2D.angle(p2.getX() - p1.getX(), p2.getY() - p1.getY());
   }
   
   public static Point2D add(Point2D point, Vector2D vec) {
      return new Point2D.Double(point.getX() + vec.getX(), point.getY() + vec.getY());
   }

}
