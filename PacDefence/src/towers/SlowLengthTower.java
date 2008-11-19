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

import images.ImageHelper;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import sprites.Sprite;


public class SlowLengthTower extends AbstractTower {
   
   private static final BufferedImage image = ImageHelper.makeImage("towers", "slowLength.png");
   private static final BufferedImage buttonImage = ImageHelper.makeImage("buttons",
         "SlowLengthTower.png");
   private int slowTicks = 10;
   private final double slowFactor = 0.75;
   
   public SlowLengthTower() {
      this(new Point());
   }
   
   public SlowLengthTower(Point p) {
      super(p, "Slow (length)", 40, 100, 5, 1, 50, 23, image);
   }

   @Override
   public BufferedImage getButtonImage() {
      return buttonImage;
   }

   @Override
   public String getSpecial() {
      return Integer.toString(slowTicks);
   }

   @Override
   public String getSpecialName() {
      return "Slow ticks";
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p) {
      return new AbstractBullet(this, dx, dy, turretWidth, range, speed, damage, p) {
         @Override
         public void specialOnHit(Point2D p, Sprite s) {
            s.slow(slowFactor, slowTicks);
         }         
      };
   }

   @Override
   protected void upgradeSpecial() {
      slowTicks *= upgradeIncreaseFactor;
   }

}
