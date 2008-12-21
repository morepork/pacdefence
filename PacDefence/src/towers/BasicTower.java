package towers;

import java.awt.Point;
import java.awt.geom.Rectangle2D;

import sprites.Sprite;




public class BasicTower extends AbstractTower {
   
   public BasicTower() {
      this(new Point(), null);
   }
   
   public BasicTower(Point p, Rectangle2D pathBounds) {
      super(p, pathBounds, "Basic", 40, 100, 5, 13, 50, 25, "basic.png", "BasicTower.png");
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Rectangle2D pathBounds) {
      return new BasicBullet(this, dx, dy, turretWidth, range, speed, damage, p, pathBounds);
   }

   @Override
   public String getSpecial() {
      return "none";
   }
   
   @Override
   public String getSpecialName() {
      return "Special";
   }

   @Override
   protected void upgradeSpecial() {
      // Basic tower has no special
   }   

}
