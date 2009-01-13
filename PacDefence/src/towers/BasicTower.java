package towers;

import java.awt.Point;
import java.awt.Shape;
import java.util.List;

import sprites.Sprite;




public class BasicTower extends AbstractTower {
   
   public BasicTower(Point p, List<Shape> pathBounds) {
      super(p, pathBounds, "Basic", 40, 100, 5, 13, 50, 25, true);
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, List<Shape> pathBounds) {
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
