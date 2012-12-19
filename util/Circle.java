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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;



public class Circle implements Shape {
   
   private double x, y;
   private double radius;
   private double radiusSq;
   private Ellipse2D bounds;
   
   public Circle() {
      this(0, 0, 0);
   }
   
   public Circle(double x, double y, double radius) {
      this.x = x;
      this.y = y;
      setRadius(radius);
      setBounds();
   }
   
   public Circle(Point2D centre, double radius){
      this(centre.getX(), centre.getY(), radius);
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
      double dx = x - this.x;
      double dy = y - this.y;
      return dx*dx + dy*dy < radiusSq;
   }

   @Override
   public boolean contains(double x, double y, double w, double h) {
      // The four corners of the rectangle
      return contains(x, y) && contains(x + w, y) && contains (x, y + h) &&
            contains(x + w, y + h);
   }
   
   public Point2D getCentre() {
      return new Point2D.Double(x, y);
   }
   
   public double getRadius() {
      return radius;
   }
   
   public double getRadiusSq() {
      return radiusSq;
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
      this.x = x;
      this.y = y;
      setBounds();
   }
   
   public void setRadius(double r) {
      if(r < 0) {
         throw new IllegalArgumentException("Radius cannot be negative");
      }
      radius = r;
      radiusSq = r * r;
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
      if(p.contains(x, y)) {
         return true;
      }
      List<Line2D> outline = Helper.getPolygonOutline(p);
      for(Line2D line : outline) {
         if(intersects(line)) {
            return true;
         }
      }
      return false;
   }
   
   public boolean intersects(Circle c) {
      double combinedRadii = radius + c.radius;
      return Point2D.distanceSq(x, y, c.x, c.y) < combinedRadii * combinedRadii;
   }
   
   public boolean intersects(Line2D line) {
      return line.ptSegDistSq(x, y) < radiusSq;
   }
   
   public boolean intersects(Arc2D a) {
      if(a.getHeight() != a.getWidth()) {
         throw new IllegalArgumentException("This only works with arcs that have a square " +
               "framing rectangle.");
      }
      // These are the easy but necessary checks (latter two for open arcs)
      if(a.contains(x, y) || contains(a.getStartPoint()) || contains(a.getEndPoint())) {
         return true;
      }
      List<Line2D> lines = new ArrayList<Line2D>();
      Point2D arcCentre = new Point2D.Double(a.getCenterX(), a.getCenterY());
      if(a.getArcType() == Arc2D.PIE) {
         lines.add(new Line2D.Double(arcCentre, a.getStartPoint()));
         lines.add(new Line2D.Double(arcCentre, a.getEndPoint()));
      } else if(a.getArcType() == Arc2D.CHORD) {
         lines.add(new Line2D.Double(a.getStartPoint(), a.getEndPoint()));
      }
      for(Line2D line : lines) {
         if(intersects(line)) {
            return true;
         }
      }
      double distance = Point2D.distance(x, y, arcCentre.getX(), arcCentre.getY());
      // Need to halve the height to get the radius of the arc
      double arcRadius = a.getHeight() / 2;
      if(distance < arcRadius + radius && distance > arcRadius - radius) {
         // The distance from the centre is less than the radius of the arc
         // plus the radius of the circle
         double angleBetween = Vector2D.angle(new Point2D.Double(x, y), arcCentre);
         double angleToStart = Vector2D.angle(a.getStartPoint(), arcCentre);
         double extentAngle = Math.toRadians(a.getAngleExtent());
         if(extentAngle > 0 ?
               angleBetween > angleToStart && angleBetween < angleToStart + extentAngle :
               angleBetween < angleToStart && angleBetween < angleToStart + extentAngle) {
            // The centre is between the lines from arcCentre to the start/end
            // of the arc, stretched to infinity so the circle intersects the
            // end of the arc
            return true;
         }
      }
      return false;
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
      } else if(s instanceof Arc2D) {
         return intersects((Arc2D) s);
      } else {
         return intersects(s.getBounds2D());
      }
   }
   
   @Override
   public Circle clone() {
      return new Circle(x, y, radius);
   }
   
   public double calculateCircumference() {
      return 2 * Math.PI * radius;
   }
   
   public Point2D getPointAt(double theta) {
      return new Point2D.Double(x + radius * Math.sin(theta), y + radius * Math.cos(theta));
   }
   
   private void setBounds() {
      bounds = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
   }

}
