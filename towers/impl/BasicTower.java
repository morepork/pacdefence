package towers.impl;

import java.awt.Point;

import towers.AbstractTower;




public class BasicTower extends AbstractTower {
   
   public BasicTower(Point p) {
      super(p, "Basic", 40, 100, 5, 13, 50, 25, true);
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
