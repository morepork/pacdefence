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

package gui;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;


public class Helper {
   
   public static <T> List<T> cloneList(List<T> list) {
      ArrayList<T> newList = new ArrayList<T>(list.size());
      for(T t : list) {
         newList.add(t);
      }
      return newList;
   }
   
   public static double distance(Point2D p1, Point2D p2) {
      return Point2D.distance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
   }
   
   public static double distanceSq(Point2D p1, Point2D p2) {
      return Point2D.distanceSq(p1.getX(), p1.getY(), p2.getX(), p2.getY());
   }
   
   public static List<Point2D> getPointsOnLine(Line2D line) {
      List<Point2D> points = new ArrayList<Point2D>();
      Point2D p1 = line.getP1();
      Point2D p2 = line.getP2();
      double length = Math.floor(distance(p1, p2));
      double xLength = p2.getX() - p1.getX();
      double yLength = p2.getY() - p1.getY();
      double xStep = xLength / length;
      double yStep = yLength / length;
      points.add((Point2D) p1.clone());
      Point2D lastPoint = p1;
      for(int i = 1; i <= length; i++) {
         lastPoint = new Point2D.Double(lastPoint.getX() + xStep, lastPoint.getY() + yStep);
         points.add(lastPoint);
      }
      points.add((Point2D) p2.clone());
      return points;
   }

}
