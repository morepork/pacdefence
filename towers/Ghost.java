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

import images.ImageHelper;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import creeps.Creep;


public class Ghost implements Tower {
   
   private static final int width = 40;
   private static final int halfWidth = width / 2;
   private static final BufferedImage buttonImage = ImageHelper.loadImage("buttons", "towers",
         "ghostButton.png");
   private static final BufferedImage image = ImageHelper.loadImage(width, width, "towers",
         "ghost.png");
   
   private static final int hits = 5;
   private int hitsLeft = hits;
   private final Point centre;
   private final Rectangle bounds;
   
   public Ghost(Point p) {
      centre = p;
      bounds = new Rectangle(p.x - halfWidth, p.y - halfWidth, width, width);
   }

   @Override
   public List<Bullet> tick(List<Creep> creeps, boolean levelInProgress) {
      if(hitsLeft <= 0) {
         return null;
      }
      List<Bullet> toReturn = new ArrayList<Bullet>();
      for(Creep c : creeps) {
         if(c.isAlive() && c.getPosition().distance(centre) < c.getHalfWidth() + halfWidth) {
            Creep.DamageReport d = c.hit(c.getHPLeft(), null);
            if(d != null) {
               final double moneyEarnt = d.getMoneyEarnt();
               // Gives you the money for killing this creep
               toReturn.add(new AbstractBullet() {
                  @Override
                  public void draw(Graphics2D g) {
                     // Does nothing as it isn't drawn
                  }
   
                  @Override
                  public double tick(List<Creep> creeps) {
                     return moneyEarnt;
                  }
               });
               if(--hitsLeft <= 0) {
                  return toReturn;
               }
            }
         }
      }
      return toReturn;
   }

   @Override
   public void draw(Graphics2D g) {
      Graphics2D g2D = (Graphics2D) g;
      g2D.drawImage(image, centre.x - halfWidth, centre.y - halfWidth, null);
      Font f = g2D.getFont();
      g2D.setFont(g2D.getFont().deriveFont(Font.BOLD).deriveFont(14F));
      String s = String.valueOf(hitsLeft);
      FontMetrics fm = g2D.getFontMetrics();
      LineMetrics lm = fm.getLineMetrics(s, g2D);
      Rectangle2D size = fm.getStringBounds(s, g2D);
      double width = size.getWidth();
      double height = size.getHeight() - lm.getLeading() - lm.getDescent();
      g2D.setColor(Color.BLACK);
      g2D.drawString(s, (float)(centre.getX() - width / 2), (float)(centre.getY() + height / 2));
      g2D.setFont(f);
   }

   @Override
   public void drawShadowAt(Graphics2D g, Point p, boolean validPlacement) {
      // Save the current composite to reset back to later
      Composite c = g.getComposite();
      // Makes it so what is drawn is partly transparent
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
            AbstractTower.shadowAmount));
      g.drawImage(image, (int) p.getX() - halfWidth, (int) p.getY() - halfWidth, null);
      g.setComposite(c);
      if(!validPlacement) {
         AbstractTower.drawX(g, p, halfWidth);
      }
   }
   
   @Override
   public ZCoordinate getZ() {
      return ZCoordinate.Tower;
   }
   
   @Override
   public boolean canTowerBeBuilt(List<Polygon> path) {
      for(Polygon p : path) {
         if(p.contains(bounds)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public Tower constructNew(Point p, List<Shape> pathBounds) {
      return new Ghost(p);
   }

   @Override
   public void addDamageNotifier(DamageNotifier d) {
      // Does nothing
   }

   @Override
   public void aidAttribute(Attribute a, double increaseFactor, int towerID) {
      // Does nothing
   }

   @Override
   public boolean contains(Point p) {
      // Ghosts don't contain anything
      return false;
   }

   @Override
   public boolean doesTowerClashWith(Tower t) {
      // Ghosts can be placed on top of other ghosts
      return false;
   }

   @Override
   public int getAttributeLevel(Attribute a) {
      // Attributes don't change
      return 0;
   }

   @Override
   public Shape getBounds() {
      return new Rectangle(bounds);
   }

   @Override
   public BufferedImage getButtonImage() {
      return buttonImage;
   }

   @Override
   public Point getCentre() {
      return new Point(centre);
   }

   @Override
   public ExperienceReport getExperienceReport() {
      // Ghosts don't deal any 'damage', or get upgraded
      return new ExperienceReport(0, hits - hitsLeft, 0, 0, 0);
   }

   @Override
   public String getName() {
      return "Ghost";
   }

   @Override
   public Comparator<Creep> getCreepComparator() {
      // Ghosts don't have comparators
      return null;
   }

   @Override
   public String getStat(Attribute a) {
      // Ghosts don't have stats
      return " ";
   }

   @Override
   public String getStatName(Attribute a) {
      // Ghosts don't have stats
      return a.toString();
   }

   @Override
   public void increaseDamageDealt(double damage) {
      // Ghosts don't get upgrades
   }

   @Override
   public void increaseKills(int kills) {
      // Ghosts don't get upgrades
   }

   @Override
   public void raiseAttributeLevel(Attribute a, boolean boughtUpgrade) {
      // Ghosts don't get upgrades
   }

   @Override
   public void select(boolean select) {
      // Ghosts can't be selected
   }

   @Override
   public void setCreepComparator(Comparator<Creep> c) {
      // Ghosts don't have comparators
   }
   
   @Override
   public void sell() {
      throw new RuntimeException("Ghosts can't be sold.");
   }

}
