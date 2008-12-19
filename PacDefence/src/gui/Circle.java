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
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

// Not fully implemented

public class Circle implements Shape {
   
   private final Point2D centre;
   private double radius;
   private Ellipse2D bounds;
   
   public Circle() {
      this(new Point2D.Double(), 0);
   }
   
   public Circle(Point2D centre, double radius){
      this.centre = new Point2D.Double(centre.getX(), centre.getY());
      if(radius < 0) {
         throw new IllegalArgumentException("Radius cannot be less than zero.");
      }
      this.radius = radius;
      setBounds();
   }
   
   public Circle(double x, double y, double radius) {
      this(new Point2D.Double(x, y), radius);
   }
   
   public void draw(Graphics g) {
      ((Graphics2D) g).draw(bounds);
   }
   
   public void fill(Graphics g) {
      ((Graphics2D) g).fill(bounds);
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
      return bounds.getBounds();
   }

   @Override
   public Rectangle2D getBounds2D() {
      return bounds.getBounds2D();
   }

   @Override
   public PathIterator getPathIterator(AffineTransform at) {
      return bounds.getPathIterator(at);
   }

   @Override
   public PathIterator getPathIterator(AffineTransform at, double flatness) {
      return bounds.getPathIterator(at, flatness);
   }
   
   public void setCentre(Point2D centre) {
      setCentre(centre.getX(), centre.getY());
   }
   
   public void setCentre(double x, double y) {
      centre.setLocation(x, y);
      setBounds();
   }
   
   public void setRadius(double r) {
      radius = r;
      setBounds();
   }

   @Override
   public boolean intersects(Rectangle2D r) {
      return bounds.intersects(r);
   }

   @Override
   public boolean intersects(double x, double y, double w, double h) {
      return bounds.intersects(x, y, w, h);
   }
   
   public boolean intersects(Polygon p) {
      if(p.contains(centre)) {
         return true;
      }
      List<Line2D> outline = makePolygonOutline(p);
      for(Line2D line : outline) {
         if(intersects(line)) {
            return true;
         }
      }
      return false;
   }
   
   public boolean intersects(Circle c) {
      return centre.distance(c.getCentre()) < radius + c.getRadius();
   }
   
   public boolean intersects(Line2D line) {
      return line.ptSegDist(centre) < radius;
   }
   
   public boolean intersects(Shape s) {
      if(s instanceof Circle) {
         return intersects((Circle) s);
      } else if(s instanceof Rectangle2D) {
         return intersects((Rectangle2D) s);
      } else if(s instanceof Polygon) {
         return intersects((Polygon) s);
      } else if(s instanceof Line2D) {
         return intersects((Line2D) s);
      } else {
         return intersects(s.getBounds2D());
      }
   }
   
   @Override
   public Circle clone() {
      return new Circle(centre, radius);
   }
   
   public double calculateCircumference() {
      return 2 * Math.PI * radius;
   }
   
   public Point2D getPointAt(double theta) {
      return new Point2D.Double(centre.getX() + radius * Math.sin(theta),
            centre.getY() + radius * Math.cos(theta));
   }
   
   private void setBounds() {
      bounds = new Ellipse2D.Double(centre.getX() - radius, centre.getY() - radius, radius * 2,
            radius * 2);
   }
   
   private List<Line2D> makePolygonOutline(Polygon p) {
      List<Line2D> outline = new ArrayList<Line2D>();
      int[] xPoints = p.xpoints;
      int[] yPoints = p.ypoints;
      int length = xPoints.length;
      outline.add(new Line2D.Double(xPoints[length - 1], yPoints[length - 1], xPoints[0],
            yPoints[0]));
      for(int i = 1; i < length; i++) {
         outline.add(new Line2D.Double(xPoints[i - 1], yPoints[i - 1], xPoints[i], yPoints[i]));
      }
      return outline;
   }

}
