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

package towers;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;

import sprites.Sprite;


public interface Tower extends Cloneable {
   
   public enum Attribute{
      
      /**
       * The current damage per shot of a Tower
       */
      Damage,
      
      /**
       * The current range of a Tower
       */
      Range,
      
      /**
       * The rate at which a Tower fires
       */
      Rate,
      
      /**
       * The speed that a Tower's bullets travel at
       */
      Speed,
      
      /**
       * The special ability of a Tower
       */
      Special;
      
      public static Attribute fromString(String s) {
         if(s.equalsIgnoreCase("Damage")) {
            return Damage;
         } else if (s.equalsIgnoreCase("Range")) {
            return Range;
         } else if (s.equalsIgnoreCase("Rate")) {
            return Rate;
         } else if (s.equalsIgnoreCase("Speed")) {
            return Speed;
         } else if (s.equalsIgnoreCase("Special")) {
            return Special;
         } else {
            throw new IllegalArgumentException("No attribute is associated to: " + s);
         }
      }
   };
   
   public void draw(Graphics g);   
   public void drawSelected(Graphics g);   
   public void drawShadow(Graphics g);   
   public Bullet tick(List<Sprite> sprites);   
   public boolean towerClash(Tower t);   
   public boolean contains(Point p);
   public Point getCentre();
   public void setCentre(Point p);   
   public String getName();   
   public Rectangle getBoundingRectangle();
   public Shape getBounds();
   public int getAttributeLevel(Attribute a);
   public void raiseAttributeLevel(Attribute a);
   public double getDamage();
   public int getRange();   
   public int getFireRate();
   public double getBulletSpeed();
   public String getSpecial();
   public Tower constructNew();
   public void select(boolean select);

}
