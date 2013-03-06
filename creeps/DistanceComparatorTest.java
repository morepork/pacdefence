package creeps;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import logic.Constants;

import org.junit.Test;

import creeps.Creep.DistanceComparator;

public class DistanceComparatorTest {
   
   private final Random rand = new Random();
   
   private Point getMapPoint() {
      return new Point(rand.nextInt(Constants.MAP_WIDTH), rand.nextInt(Constants.MAP_HEIGHT));
   }
   
   private Point2D getMapPoint2D() {
      return new Point2D.Double(rand.nextDouble() * Constants.MAP_WIDTH,
            rand.nextDouble() * Constants.MAP_HEIGHT);
   }

   @Test
   public void test() {
      int numCreeps = 1000000;
      List<Creep> creeps = new ArrayList<>(numCreeps);
      
      for (int i = 0; i < numCreeps; i++) {
         creeps.add(new Pacman(1, 1000, Arrays.asList(getMapPoint(), getMapPoint())));
      }
      
      for (int i = 0; i < 100; i++) {
         System.out.println(i);
         // Ensure this doesn't generate the following exception due to
         // representing numbers as doubles:
         // java.lang.IllegalArgumentException: Comparison method violates its general contract!
         Collections.sort(creeps, new DistanceComparator(getMapPoint2D(), true));
      }
   }

}
