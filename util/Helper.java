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

package util;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Helper {

   private static final Map<Integer, DecimalFormat> formats =
         new HashMap<Integer, DecimalFormat>();

   private static final double scientificFormatThreshold = 1e10;
   private static final DecimalFormat scientificFormat = new DecimalFormat("0.000E0");
   
   public static List<Point2D> getPointsOnLine(Line2D line) {
      return getPointsOnLine(line.getP1(), line.getP2());
   }
   
   public static List<Point2D> getPointsOnLine(Point2D p1, Point2D p2) {
      return getPointsOnLine(p1, p2, null);
   }
   
   public static List<Point2D> getPointsOnLine(Point2D p1, Point2D p2, List<Shape> containedIn) {
      Vector2D dir = Vector2D.createFromPoints(p1, p2);
      // The maximum length in either the x or y directions to divide the line
      // into points a maximum of one pixel apart in either the x or y directions
      double absDx = Math.abs(dir.getX());
      double absDy = Math.abs(dir.getY());
      double length = (absDx > absDy) ? absDx : absDy;
      Vector2D step = Vector2D.createFromVector(dir, length);
      List<Point2D> points = new ArrayList<Point2D>((int) length + 2);
      if(containedIn == null || containedInAShape(p1, containedIn)) {
         points.add((Point2D) p1.clone());
      }
      Point2D lastPoint = p1;
      for(int i = 1; i <= length; i++) {
         lastPoint = Vector2D.add(lastPoint, step);
         if(containedIn == null || containedInAShape(lastPoint, containedIn)) {
            points.add(lastPoint);
         }
      }
      if(containedIn == null || containedInAShape(p2, containedIn)) {
         points.add((Point2D) p2.clone());
      }
      return points;
   }
   
   public static boolean containedInAShape(Point2D p, List<Shape> shapes) {
      for(Shape s : shapes) {
         if(s.contains(p)) {
            return true;
         }
      }
      return false;
   }
   
   @SafeVarargs
   public static <T> List<T> makeListContaining(T... ts) {
      return new ArrayList<T>(Arrays.asList(ts));
   }
   
   public static void removeAll(List<?> list, List<Integer> positions) {
      // Assumes the list of Integers is sorted smallest to largest
      
      // Removes from last to first as it is faster in an array backed list
      // which is what I usually use.
      for(int i = positions.size() - 1; i >= 0; i--) {
         // Convert to int otherwise it calls list.remove(Object o)
         list.remove(positions.get(i).intValue());
      }
   }

   public static String format(double d) {
      return format(d, 0);
   }
   
   public static String format(double d, int decimalPlaces) {
      assert decimalPlaces >= 0 : "decimalPlaces must be >= 0";
      if (d > scientificFormatThreshold) {
         return scientificFormat.format(d);
      }
      if(!formats.containsKey(decimalPlaces)) {
         formats.put(decimalPlaces, makeFormat(decimalPlaces));
      }
      return formats.get(decimalPlaces).format(d);
   }

   private static DecimalFormat makeFormat(int decimalPlaces) {
      assert decimalPlaces >= 0 : "decimalPlaces must be >= 0";
      StringBuilder pattern = new StringBuilder("###,##0");
      if(decimalPlaces > 0) {
         pattern.append(".");
         for(int i = 0; i < decimalPlaces; i++) {
            pattern.append("0");
         }
      }
      return new DecimalFormat(pattern.toString());
   }
   
   public static List<Line2D> getPolygonOutline(Polygon p) {
      int[] xPoints = p.xpoints;
      int[] yPoints = p.ypoints;
      int length = p.npoints;
      List<Line2D> outline = new ArrayList<Line2D>(length);
      outline.add(new Line2D.Double(xPoints[length - 1], yPoints[length - 1], xPoints[0],
            yPoints[0]));
      for(int i = 1; i < length; i++) {
         outline.add(new Line2D.Double(xPoints[i - 1], yPoints[i - 1], xPoints[i], yPoints[i]));
      }
      return outline;
   }
   
   public static String getNumberSuffix(int n) {
      if (n > 9 && n < 20) { // The teens (eleventh, ...) all end in th
         return "th";
      }
      // Otherwise it's based on the last digit
      switch(n % 10) {
      case 1:
         return "st";
      case 2:
         return "nd";
      case 3:
         return "rd";
      default:
         return "th";
      }
   }

}
