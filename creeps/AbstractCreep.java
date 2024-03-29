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
 *  (C) Liam Byrne, 2008 - 2012.
 */

package creeps;

import images.ImageHelper;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import logic.Formulae;
import towers.DamageNotifier;
import towers.Tower;
import util.Circle;
import util.Helper;
import util.Vector2D;

public abstract class AbstractCreep implements Creep, Comparable<Creep> {
   
   private static final double baseSpeed = 2;
   private static final double maxMult = 2;
   private static final Random rand = new Random();
   private static final double multiTowerBonusPerTower = 1.1;
   
   // The amount the image of the creep shrinks by per tick after it has been killed
   private static final int dieImageWidthShrinkAmount = 4;

   private int width;
   private int halfWidth;
   
   private final Circle bounds = new Circle();

   // Cache the rotated images so every time a creep rounds a corner the original images do not
   // need to be re-rotated, but can be retrieved from here.
   // The list of images if filed under the specific class of AbstractCreep and a LooseFloat which
   // is the angle the creep is facing.
   private static final Map<Class<? extends AbstractCreep>,
         Map<LooseFloat, List<BufferedImage>>> rotatedImages = new HashMap<>();
   
   private final List<BufferedImage> originalImages;
   private List<BufferedImage> currentImages;
   private BufferedImage currentImage;
   private int currentImageIndex = 0;
   private double currentAngle;

   private final int currentLevel;
   private final double speed;
   private final long levelHP;
   private double hp;
   private final double hpFactor;
   private final List<Point> path;
   private Point nextPoint;
   private int nextGoalPointIndex;
   
   private final Point2D centre = new Point2D.Double();
   private Vector2D step;
   private double totalDistanceTravelled = 0;
   // The distance in pixels to the next point from the previous one
   private double distance;
   // The steps taken so far (in pixels) in the current direction
   private double steps;
   // Whether the creep is still alive
   private boolean alive = true;
   // Whether the creep has finished, i.e. got to the end of the map
   private boolean finished = false;
   
   private double speedFactor = 1;
   private int adjustedSpeedTicksLeft = 0;
   
   private double damageMultiplier = 1;
   private int adjustedDamageTicksLeft = 0;
   private DamageNotifier adjustedDamageNotifier;
   
   private int poisonTicksLeft = 0;
   
   private final Set<CreepEffect> currentEffects = EnumSet.noneOf(CreepEffect.class);
   private static final Map<CreepEffect, Color> effectsColours = createEffectsColours();
   private static final Composite effectsComposite =
         AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25F);
   
   private final List<Class<? extends Tower>> hits = new ArrayList<>();

   public AbstractCreep(List<BufferedImage> images, int currentLevel, long hp, List<Point> path){
      this.currentLevel = currentLevel;
      width = images.get(0).getWidth();
      halfWidth = width / 2;
      centre.setLocation(path.get(0));
      bounds.setRadius(halfWidth);
      setBounds();
      // Use two clones here so that currentImages can be edited without
      // affecting originalImages
      originalImages = Collections.unmodifiableList(images);
      currentImages = new ArrayList<BufferedImage>(images);
      speed = calculateSpeed(hp);
      levelHP = hp;
      hpFactor = levelHP / this.hp;
      this.path = path;
      nextPoint = calculateFirstPoint();
      nextGoalPointIndex = 0;
      calculateNextMove();
   }

   @Override
   public void draw(Graphics2D g) {
      // Save these as the processing might change them in another thread, causing strange errors
      int currentHalfWidth = halfWidth;
      int currentWidth = width;
      
      if(currentHalfWidth < 0) { // Creep is dead and so small it's not showing
         return;
      }
      
      g.drawImage(currentImage, (int) centre.getX() - currentHalfWidth,
            (int) centre.getY() - currentHalfWidth, currentWidth, currentWidth, null);
      
      if(!currentEffects.isEmpty()) {
         drawCurrentEffects(g, currentHalfWidth);
      }
   }
   
   @Override
   public ZCoordinate getZ() {
      return ZCoordinate.Creep;
   }

   @Override
   public boolean tick() {
      if(alive) {
         // Set the currentImage to the next one in the list, wrapping at the end
         currentImageIndex++;
         currentImageIndex %= currentImages.size();
         currentImage = currentImages.get(currentImageIndex);
         if(!move()) { // If this returns false; the creep has finished
            finished = true;
            return true;
         }
         decreaseEffectsTicksLeft();
      } else {
         // If the creep is dead, reduce the size it is drawn at
         width -= dieImageWidthShrinkAmount;
         halfWidth = width / 2;
         // When it gets too small, stop showing it - the creep is gone from the game now
         if(halfWidth < 0) {
            return true;
         }
      }
      return false;
   }

   @Override
   public double getTotalDistanceTravelled() {
      return totalDistanceTravelled;
   }

   @Override
   public Point2D getPosition() {
      return new Point2D.Double(centre.getX(), centre.getY());
   }
   
   @Override
   public double getCurrentAngle() {
      return currentAngle;
   }
   
   @Override
   public double getSpeed() {
      return speedFactor * speed;
   }
   
   @Override
   public double getHPLeft() {
      return hp;
   }
   
   @Override
   public Shape getBounds() {
      return bounds.clone();
   }

   @Override
   public boolean isAlive() {
      return alive;
   }

   @Override
   public boolean intersects(Point2D p) {
      if(alive) {
         return fastIntersects(p);
      } else {
         // If the creep is dead or dying it can't be hit
         return false;
      }
   }
   
   @Override
   public boolean isFinished() {
      return finished;
   }
   
   @Override
   public Point2D intersects(Line2D line) {
      if(alive && bounds.intersects(line)) {
         for(Point2D p : Helper.getPointsOnLine(line)) {
             if(fastIntersects(p)) {
                return p;
             }
          }
      }
      return null;
   }
   
   @Override
   public boolean intersects(Arc2D a) {
      return bounds.intersects(a);
   }

   @Override
   public synchronized DamageReport hit(double damage, Class<? extends Tower> towerClass) {
      if(!alive) {
         return null;
      }
      damage *= calculateMultiTowerBonus(towerClass);
      double adjustedDamage = damage;
      if(adjustedDamageTicksLeft > 0) {
         adjustedDamage *= damageMultiplier;
      }
      if (hp - adjustedDamage <= 0) {
         // This hit killed the creep, so the damage dealt is the number of hp the creep had left,
         // not the raw damage of the hit
         alive = false;
         double damageToReport = hp;
         boolean wasKill = true;
         if(adjustedDamageNotifier != null && adjustedDamage > damage && hp > damage) {
            // Notify the tower that caused the extra damage of the damage it caused
            // Note that it only caused extra damage if the original shot would not have killed this
            // creep.
            adjustedDamageNotifier.notifyOfDamage(hp - damage);
            adjustedDamageNotifier.notifyOfKills(1);
            
            damageToReport = damage;
            // Don't count it as a kill on the damage report
            wasKill = false;
         }
         double moneyEarned = Formulae.damageDollars(hp, hpFactor, currentLevel) +
               Formulae.killBonus(levelHP, currentLevel);
         return new DamageReport(damageToReport, moneyEarned, wasKill);
      } else {
         hp -= adjustedDamage;
         if(adjustedDamageNotifier != null && adjustedDamage > damage) {
            // Notify the tower that caused the extra damage of the damage it caused
            adjustedDamageNotifier.notifyOfDamage(adjustedDamage / damageMultiplier);
         }
         double moneyEarned = Formulae.damageDollars(adjustedDamage, hpFactor, currentLevel);
         // Only report the damage actually from this shot, not the extra
         return new DamageReport(damage, moneyEarned, false);
      }
   }

   @Override
   public int getHalfWidth() {
      return halfWidth;
   }
   
   @Override
   public void slow(double factor, int numTicks) {
      if(factor >= 1) {
         throw new IllegalArgumentException("Factor must be less than 1 in order to slow.");
      }
      currentEffects.add(CreepEffect.SLOW);
      if(factor < speedFactor) {
         // New speed is slower, so it is better, even if only for a short
         // time. Or so I think.
         speedFactor = factor;
         adjustedSpeedTicksLeft = numTicks;
      } else if(Math.abs(factor - speedFactor) < 0.01) {
         // The new slow speed factor is basically the same as the current
         // speed, so if it is for longer, lengthen it
         if(numTicks > adjustedSpeedTicksLeft) {
            speedFactor = factor;
            adjustedSpeedTicksLeft = numTicks;
         }
      }
      // Otherwise ignore it as it would increase the creep's speed
   }
   
   @Override
   public void setDamageMultiplier(DamageNotifier dn, double multiplier, int numTicks) {
      assert multiplier > 1 : "Multiplier must be greater than 1";
      currentEffects.add(CreepEffect.WEAK);
      if(multiplier > damageMultiplier) { // If this would weaken the creep by more
         damageMultiplier = multiplier;
         adjustedDamageTicksLeft = numTicks;
         adjustedDamageNotifier = dn;
      } else if(Math.abs(multiplier - damageMultiplier) < 0.01) {
         // Otherwise only apply it if it's approx. the same and would apply for longer
         if(numTicks > adjustedDamageTicksLeft) {
            damageMultiplier = multiplier;
            adjustedDamageTicksLeft = numTicks;
            adjustedDamageNotifier = dn;
         }
      }
   }
   
   @Override
   public void poison(int numTicks) {
      assert numTicks > 0 : "Can't be poisoned for 0 or fewer ticks";
      currentEffects.add(CreepEffect.POISON);
      if(numTicks > poisonTicksLeft) {
         poisonTicksLeft = numTicks;
      }
   }
   
   @Override
   public int compareTo(Creep c) {
      return Double.compare(c.getSpeed(), this.getSpeed());
   }
   
   /**
    * Moves the creep.
    * 
    * @return
    *        true if it is still on screen, false if it has finished and has gone off screen
    */
   private boolean move() {
      centre.setLocation(centre.getX() + step.getX() * speedFactor,
            centre.getY() + step.getY() * speedFactor);
      setBounds();
      totalDistanceTravelled += step.getLength() * speedFactor;
      steps += speedFactor;
      if (steps + 1 > distance) {
         return calculateNextMove();
      }
      return true;
   }
   
   private void setBounds() {
      bounds.setCentre(centre);
   }

   private Point calculateFirstPoint() {
      Point p1 = path.get(0);
      Point p2 = path.get(1);
      Vector2D vec = Vector2D.createFromPoints(p1, p2);
      // Now make it so the creep starts fully off screen, then comes on screen
      double mult = (halfWidth + 1) / vec.getLength();
      int x = (int) (p1.getX() - mult * vec.getX());
      int y = (int) (p1.getY() - mult * vec.getY());
      return new Point(x, y);
   }

   /**
    * Calculates and sets the next point on this creep's path
    * 
    * @return
    *        true if it is still on screen, false if it has finished and has gone off screen
    */
   private boolean calculateNextMove() {
      if (nextGoalPointIndex < path.size()) {
         // There is still another point to head to
         headTowardsNextPoint();
      } else {
         // There are no more points to head towards
         if (nextPoint != null) {
            // Need to add enough distance so the creep will go off the screen
            // Flag that we've done this so we don't do it again
            nextPoint = null;
            // One pixel is added to halfWidth just to be sure
            distance += (halfWidth + 1) / step.getLength();
         } else {
            // Creep is finished
            return false;
         }
      }
      return true;
   }
   
   private void headTowardsNextPoint() {
      centre.setLocation(nextPoint);
      setBounds();
      nextPoint.setLocation(path.get(nextGoalPointIndex));
      nextGoalPointIndex++;
      // The next line the creep has to travel down
      Vector2D nextLine = Vector2D.createFromPoints(centre, nextPoint);
      step = Vector2D.createFromVector(nextLine, speed);
      // The number of steps the creep will take to reach nextPoint
      distance = nextLine.getLength() / step.getLength();

      steps = 0;
      // Invert yStep here as y coord goes down as it increases, rather than up as in a
      // conventional coordinate system.
      rotateImages(Vector2D.angle(step.getX(), -step.getY()));
   }
   
   private void rotateImages(double angle) {
      currentAngle = angle;
      if(!rotatedImages.containsKey(getClass())) {
         rotatedImages.put(getClass(), new HashMap<LooseFloat, List<BufferedImage>>());
      }
      Map<LooseFloat, List<BufferedImage>> m = rotatedImages.get(getClass());
      // Use LooseFloat to reduce precision so rotated images are less likely to be duplicated
      LooseFloat f = new AbstractCreepLooseFloat(angle);
      if(!m.containsKey(f)) {
         List<BufferedImage> images = new ArrayList<BufferedImage>(originalImages.size());
         for(BufferedImage i : originalImages) {
            images.add(ImageHelper.rotateImage(i, angle));
         }
         m.put(f, Collections.unmodifiableList(images));
      }
      // Need to copy this as when the creep dies the images are changed
      currentImages = new ArrayList<BufferedImage>(m.get(f));
   }
   
   /**
    * Calculates the speed and also sets the hp based on this
    * @param hp
    * @return
    */
   private double calculateSpeed(long hp) {
      // A random multiplier between one and maxMult
      double mult = (maxMult - 1) * rand.nextDouble() + 1;
      // Now randomly pick whether to decrease hp/increase speed, or vice versa
      if(rand.nextBoolean()) {
         this.hp = hp / mult;
         return baseSpeed * mult;
      } else {
         this.hp = hp * mult;
         return baseSpeed / mult;
      }
   }
   
   /**
    * A faster version of the intersect method - doesn't check if Creep is alive
    * @param p
    * @return
    */
   private boolean fastIntersects(Point2D p) {
      // Need to check this first otherwise the image check won't work, as it'll look outside the
      // bounds of the image
      if (bounds.contains(p)) {
         int x = (int) (p.getX() - centre.getX() + halfWidth);
         int y = (int) (p.getY() - centre.getY() + halfWidth);
         // If this is a completely transparent pixel, it's not a hit
         return !ImageHelper.isCompletelyTransparent(currentImage, x, y);
      }
      return false;
   }
   
   private void decreaseEffectsTicksLeft() {
      if(adjustedSpeedTicksLeft > 0) {
         adjustedSpeedTicksLeft--;
         if(adjustedSpeedTicksLeft <= 0) {
            currentEffects.remove(CreepEffect.SLOW);
            speedFactor = 1;
         }
      }
      if(adjustedDamageTicksLeft > 0) {
         adjustedDamageTicksLeft--;
         if(adjustedDamageTicksLeft <= 0) {
            currentEffects.remove(CreepEffect.WEAK);
            damageMultiplier = 1;
            adjustedDamageNotifier = null;
         }
      }
      if(poisonTicksLeft > 0) {
         poisonTicksLeft--;
         if(poisonTicksLeft <= 0) {
            currentEffects.remove(CreepEffect.POISON);
         }
      }
   }
   
   private double calculateMultiTowerBonus(Class<? extends Tower> towerClass) {
      if(towerClass == null) {
         return 1;
      }
      int index = hits.lastIndexOf(towerClass);
      int numOtherTowers;
      if(index >= 0) {
         // If the tower is in the list, the number of other towers is the
         // number between the index and the end
         numOtherTowers = hits.size() - index - 1;
         // Remove this hit so that the list doesn't keep on growing. A new hit will be added at
         // the end.
         hits.remove(index);
      } else {
         // If this tower isn't in the list, the number of other towers is the
         // size of the list
         numOtherTowers = hits.size();
      }
      hits.add(towerClass);
      double mult = 1;
      while(numOtherTowers > 0) {
         mult *= multiTowerBonusPerTower;
         numOtherTowers--;
      }
      return mult;
   }
   
   private void drawCurrentEffects(Graphics2D g, int radius) {
      g = (Graphics2D) g.create();
      
      g.setComposite(effectsComposite);
      
      // If the creep is dying, only have the circle as big as its current size, which is why a new
      // circle is created, rather than using bounds
      // Increase the radius to ensure it fully covers the creep picture
      Circle fillArea = new Circle(centre, radius + 1);
      
      for(CreepEffect se : currentEffects) {
         g.setColor(effectsColours.get(se));
         g.fill(fillArea);
      }
      
      g.dispose();
   }
   
   private static Map<CreepEffect, Color> createEffectsColours() {
      Map<CreepEffect, Color> map = new EnumMap<CreepEffect, Color>(CreepEffect.class);
      map.put(CreepEffect.SLOW, Color.BLUE);
      map.put(CreepEffect.WEAK, Color.MAGENTA);
      map.put(CreepEffect.POISON, Color.GREEN);
      return Collections.unmodifiableMap(map);
   }
   
   private class AbstractCreepLooseFloat extends LooseFloat {
      
      public AbstractCreepLooseFloat(float f) {
         super(f);
      }
      
      public AbstractCreepLooseFloat(double d) {
         super(d);
      }

      @Override
      protected float getPrecision() {
         // Watch out with decreasing this, while it may improve the quality, because images are
         // cached it can increase the memory use significantly
         return 0.08F;
      }
   }

}
