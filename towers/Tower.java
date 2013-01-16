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

package towers;

import gui.Drawable;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;

import creeps.Creep;


public interface Tower extends Drawable, Cloneable {
   
   public enum Attribute{
      
      // The order of these is the same as they are listed from top to bottom
      // on the right side for upgrades and showing the current stat.
      
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
      Rate{
         @Override
         public String toString() {
            return "Fire Rate";
         }
      },
      
      /**
       * The speed that a Tower's bullets travel at
       */
      Speed{
         @Override
         public String toString() {
            return "Bullet Speed";
         }
      },
      
      /**
       * The special ability of a Tower
       */
      Special;
      
   };
   
   public void draw(Graphics g);
   public void drawShadowAt(Graphics g, Point p, boolean validPlacement);
   /**
    * 
    * @param creeps
    * @param levelInProgress
    * @return The bullets shot by this tower after the last tick and
    *         up to this one null if the tower is to be removed
    */
   public List<Bullet> tick(List<Creep> creeps, boolean levelInProgress);
   public boolean doesTowerClashWith(Tower t);
   public boolean canTowerBeBuilt(List<Polygon> path);
   public boolean contains(Point p);
   public Point getCentre();
   public String getName();
   public Shape getBounds();
   public int getAttributeLevel(Attribute a);
   public void raiseAttributeLevel(Attribute a, boolean boughtUpgrade);
   
   /**
    * Aids the specified attribute by increaseFactor.
    * 
    * Towers should only be aided by the highest increaseFactor they are currently
    * receiving in each attribute. An increaseFactor of 1 (which means no increase)
    * signals that the aiding tower has been sold.
    */
   public void aidAttribute(Attribute a, double increaseFactor, int towerID);
   public String getStat(Attribute a);
   public String getStatName(Attribute a);
   public Tower constructNew(Point p, List<Shape> pathBounds);
   public void select(boolean select);
   public void increaseDamageDealt(double damage);
   public void increaseKills(int kills);
   public void addDamageNotifier(DamageNotifier d);
   public ExperienceReport getExperienceReport();
   public BufferedImage getButtonImage();
   public void setCreepComparator(Comparator<Creep> c);
   public Comparator<Creep> getCreepComparator();
   public void sell();

}
