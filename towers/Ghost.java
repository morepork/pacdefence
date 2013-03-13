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
import images.ImageHelper;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import creeps.Creep;


public class Ghost implements Drawable, Buildable {
   
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

   /**
    * Tick this ghost, returns the amount of money earned by killing creeps,
    * or a negative value if it should be removed. 
    */
   public double tick(List<Creep> creeps) {
      if(hitsLeft <= 0) {
         return -1;
      }
      double moneyEarned = 0;
      for(Creep c : creeps) {
         if(c.isAlive() && c.getPosition().distance(centre) < c.getHalfWidth() + halfWidth) {
            Creep.DamageReport d = c.hit(c.getHPLeft(), null);
            if(d != null) {
               // Gives you the money for killing this creep
               moneyEarned += d.getMoneyEarned();
               if(--hitsLeft <= 0) {
                  break;
               }
            }
         }
      }
      return moneyEarned;
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
      // Makes it so what is drawn is partly transparent
      Graphics2D gCopy = (Graphics2D) g.create();
      gCopy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
            AbstractTower.shadowAmount));
      gCopy.drawImage(image, (int) p.getX() - halfWidth, (int) p.getY() - halfWidth, null);
      gCopy.dispose();
      
      if(!validPlacement) {
         AbstractTower.drawX(g, p, halfWidth);
      }
   }
   
   @Override
   public ZCoordinate getZ() {
      return ZCoordinate.Ghost;
   }
   
   @Override
   public boolean canBuild(List<Polygon> path) {
      for(Polygon p : path) {
         if(p.contains(bounds)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public Buildable constructNew(Point p) {
      return new Ghost(p);
   }

   @Override
   public BufferedImage getButtonImage() {
      return buttonImage;
   }

   @Override
   public String getName() {
      return "Ghost";
   }

}
