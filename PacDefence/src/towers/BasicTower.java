package towers;

import java.awt.Point;




public class BasicTower extends AbstractTower {
   
   public BasicTower() {
      this(new Point());
   }
   
   public BasicTower(Point p) {
      super(p, "Basic", 40, 100, 5, 10, 50, 25, "basic.png", null);
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p) {
      return new AbstractBullet(this, dx, dy, turretWidth, range, speed, damage, p){};
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
