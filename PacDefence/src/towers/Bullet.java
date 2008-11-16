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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

import sprites.Sprite;
import sprites.Sprite.DamageReport;

public class Bullet {

   // The direction of the bullet, first is dx, second is dy. Should be normalised
   // then multiplied by the speed.
   private final double[] dir = new double[2];
   private final double speed;
   private final double distancePerTick;
   private double distanceTravelled = 0;
   private final Point2D lastPosition;
   private final Point2D position;
   private final int range;
   private final int radius = 3;
   private final int twiceRadius = radius * 2;
   private final double damage;
   private boolean alive = true;
   private final Tower shotBy;
   
   public Bullet(Tower shotBy, double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p) {
      this.shotBy = shotBy;
      this.range = range - turretWidth;
      this.speed = speed;
      this.damage = damage;
      double divisor = Math.sqrt(dx * dx + dy * dy);
      dir[0] = speed * dx / divisor;
      dir[1] = speed * dy / divisor;
      distancePerTick = Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1]);
      //System.out.println(distancePerTick);
      //System.out.println(dir[0] + " " + dir[1]);
      position = new Point2D.Double(p.getX() + turretWidth * dx / divisor,
            p.getY() + turretWidth * dy / divisor);
      lastPosition = new Point2D.Double(position.getX(), position.getY());
   }

   /**
    * 
    * @param sprites
    * @return How much money, if any, the bullet earnt. A negative return
    *         value means the bullet is still going.
    */
   public double tick(List<Sprite> sprites) {
      if(distanceTravelled >= range) {
         //System.out.println("Bullet reached edge of range");
         return 0;
      }
      distanceTravelled += distancePerTick;
      //System.out.println(distanceTravelled);
      lastPosition.setLocation(position);
      if(distanceTravelled > range) {
         // This deals with a bullet nearing the edge of its range
         double extraDistance = distanceTravelled - range;
         if(extraDistance < 1) {
            // If the extra distance is this small it's too trivial to redraw
            return 0;
         }
         double fraction = extraDistance / distancePerTick;
         position.setLocation(position.getX() + fraction * dir[0],
               position.getY() + fraction * dir[1]);
      } else {
         position.setLocation(position.getX() + dir[0], position.getY() + dir[1]);
      }
      Line2D line = new Line2D.Double(lastPosition, position);
      for(Sprite s : sprites) {
         if(s.intersects(line)) {
            DamageReport d = s.hitBy(this);
            if(d != null) {
               if(d.wasKill()) {
                  shotBy.increaseKills(1);
               }
               shotBy.increaseDamageDealt(d.getDamage());
               return d.getMoneyEarnt();
            }
         }
      }
      return -1;
   }

   public void draw(Graphics g) {
      if(!alive) {
         return;
      }
      g.setColor(Color.BLACK);
      g.fillOval((int) position.getX() - radius, (int) position.getY() - radius, twiceRadius,
            twiceRadius);
   }

   
   public double getDamage() {
      return damage;
   }

}
