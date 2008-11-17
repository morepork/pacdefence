package towers;

import images.ImageHelper;

import java.awt.Point;
import java.awt.image.BufferedImage;




public class BasicTower extends AbstractTower {
   
   private static final BufferedImage image = ImageHelper.makeImage("towers", "basic.png");
   
   public BasicTower() {
      this(new Point());
   }
   
   private BasicTower(Point p) {
      super(p, "Basic", 40, 100, 5, 10, 50, 25, image);
   }
   
   @Override
   public Tower constructNew() {
      return new BasicTower(getCentre());
   }

   @Override
   protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
         double damage, Point p) {
      return new BasicBullet(this, dx, dy, turretWidth, range, speed, damage, p);
   }
   
   private static class BasicBullet extends AbstractBullet {

      public BasicBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p) {
         super(shotBy, dx, dy, turretWidth, range, speed, damage, p);
      }
      
   }

}
