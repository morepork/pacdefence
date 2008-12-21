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
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;

import sprites.Sprite;


public interface Tower extends Cloneable {
   
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
   public void drawSelected(Graphics g);   
   public void drawShadow(Graphics g);
   /**
    * 
    * @param sprites
    * @return The bullets shot by this tower after the last tick and
    *         up to this one
    */
   public List<Bullet> tick(List<Sprite> sprites);  
   public boolean doesTowerClashWith(Tower t);   
   public boolean contains(Point p);
   public Point getCentre();
   public String getName();
   public Shape getBounds();
   public int getAttributeLevel(Attribute a);
   public void raiseAttributeLevel(Attribute a, boolean boughtUpgrade);
   public void aidAttribute(Attribute a, double increaseFactor, int towerID);
   public String getStat(Attribute a);
   public String getStatName(Attribute a);
   public Tower constructNew(Point p, Rectangle2D pathBounds);
   public void select(boolean select);
   public void increaseDamageDealt(double damage);
   public void increaseKills(int kills);
   public void addDamageNotifier(DamageNotifier d);
   public long getDamageDealt();
   public long getDamageDealtForUpgrade();
   public int getKills();
   public int getKillsForUpgrade();
   public int getExperienceLevel();
   public BufferedImage getButtonImage();
   public void setSpriteComparator(Comparator<Sprite> c);
   public Comparator<Sprite> getSpriteComparator();

}
