package towers;

import images.ImageHelper;

import java.awt.Point;
import java.awt.image.BufferedImage;




public class BasicTower extends AbstractTower {
   
   private static final BufferedImage image = ImageHelper.makeImage("towers", "basic.png");
   
   public BasicTower() {
      this(new Point());
   }
   
   public BasicTower(Point p) {
      super(p, "Basic", 40, 100, 5, 10, 50, 25, image);
   }
   
   @Override
   public BufferedImage getButtonImage() {
      return null;
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
