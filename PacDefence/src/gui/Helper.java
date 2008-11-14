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

}
