package towers;

import java.awt.Point;
import java.awt.Polygon;

import sprites.Sprite;




public class BasicTower extends AbstractTower {
   
   public BasicTower() {
      this(new Point(), null);
   }
   
   public BasicTower(Point p, Polygon path) {
      super(p, path, "Basic", 40, 100, 5, 10, 50, 25, "basic.png", "BasicTower.png");
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p, Sprite s, Polygon path) {
      return new BasicBullet(this, dx, dy, turretWidth, range, speed, damage, p, path);
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
