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
   
   public Vector2D(Point2D p1, Point2D p2) {
      this(p2.getX() - p1.getX(), p2.getY() - p1.getY());
   }
   
   public Vector2D(Vector2D vec, double length) {
      this(length * vec.getX() / vec.getLength(), length * vec.getY() / vec.getLength());
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
   
   public double getAngle() {
      return Vector2D.angle(x, y);
   }
   
   public Point2D addToPoint(Point2D point) {
      return new Point2D.Double(point.getX() + x, point.getY() + y);
   }

}
