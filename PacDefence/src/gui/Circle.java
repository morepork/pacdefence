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

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// Not fully implemented

public class Circle implements Shape {
   
   private final Point2D centre;
   private final double radius;
   private final Ellipse2D bounds;
   
   public Circle(Point2D centre, double radius){
      this.centre = centre;
      this.radius = radius;
      double twiceRadius = radius * 2;
      bounds = new Ellipse2D.Double(centre.getX() - radius, centre.getY() - radius, twiceRadius,
            twiceRadius);
   }
   
   public Circle(double x, double y, double radius) {
      this(new Point2D.Double(x, y), radius);
   }
   
   public void draw(Graphics g) {
      g.drawOval((int)(centre.getX() - radius), (int)(centre.getY() - radius), (int)(radius*2),
            (int)(radius*2));
   }

   @Override
   public boolean contains(Point2D p) {
      return contains(p.getX(), p.getY());
   }

   @Override
   public boolean contains(Rectangle2D r) {
      return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
   }

   @Override
   public boolean contains(double x, double y) {
      double dx = x - centre.getX();
      double dy = y - centre.getY();
      double distance = Math.sqrt(dx*dx + dy*dy);
      return distance < radius;
   }

   @Override
   public boolean contains(double x, double y, double w, double h) {
      Point2D topLeft = new Point2D.Double(x, y);
      Point2D topRight = new Point2D.Double(x + w, y);
      Point2D bottomLeft = new Point2D.Double(x, y + h);
      Point2D bottomRight = new Point2D.Double(x + w, y + h);
      return contains(topLeft) && contains(topRight) && contains (bottomLeft) &&
            contains(bottomRight);
   }
   
   public Point2D getCentre() {
      return (Point2D) centre.clone();
   }
   
   public double getRadius() {
      return radius;
   }

   @Override
   public Rectangle getBounds() {
      return getBounds2D().getBounds();
   }

   @Override
   public Rectangle2D getBounds2D() {
      return new Rectangle2D.Double(centre.getX() - radius, centre.getY() - radius, radius*2,
            radius*2);
   }

   @Override
   public PathIterator getPathIterator(AffineTransform at) {
      return bounds.getPathIterator(at);
   }

   @Override
   public PathIterator getPathIterator(AffineTransform at, double flatness) {
      return bounds.getPathIterator(at, flatness);
   }

   @Override
   public boolean intersects(Rectangle2D r) {
      return bounds.intersects(r);
   }

   @Override
   public boolean intersects(double x, double y, double w, double h) {
      return bounds.intersects(x, y, w, h);
   }

}
