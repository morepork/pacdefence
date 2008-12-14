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

import gui.Circle;
import gui.Formulae;
import gui.Helper;
import images.ImageHelper;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sprites.Sprite;

public abstract class AbstractTower implements Tower {

   // The color of the range of the tower when drawn
   private static final Color rangeColour = new Color(255, 255, 255, 100);
   protected static final float upgradeIncreaseFactor = 1.05F;

   private int damageLevel = 1;
   private int rangeLevel = 1;
   private int rateLevel = 1;
   private int speedLevel = 1;
   private int specialLevel = 1;

   // The top left point of this tower
   private final Point topLeft;
   // The centre of this tower
   private final Point centre;
   private final Circle bounds;
   private final Rectangle boundingRectangle = new Rectangle();
   private final String name;
   // The number of clock ticks between each shot
   private double fireRate;
   // The number of clock ticks until this tower's next shot
   protected double timeToNextShot = 0;
   private double range;
   private final double rangeUpgrade;
   private int twiceRange;
   private double bulletSpeed;
   private final double bulletSpeedUpgrade;
   private double damage;

   // The width/height of the image (as it's square)
   private final int width;
   private final int halfWidth;
   // The width of the turret from the centre of the tower
   private final int turretWidth;

   private final BufferedImage originalImage;
   private BufferedImage currentImage;
   private final BufferedImage buttonImage;
   
   private final Polygon path;

   private boolean isSelected = false;

   private long damageDealt = 0;
   private int kills = 0;
   private int killsLevel = 1;
   private int damageDealtLevel = 1;
   private long nextUpgradeDamage = Formulae.nextUpgradeDamage(killsLevel);
   private int nextUpgradeKills = Formulae.nextUpgradeKills(damageDealtLevel);
   
   private List<Bullet> bulletsToAdd = new ArrayList<Bullet>();

   public AbstractTower(Point p, Polygon path, String name, int fireRate, double range, double bulletSpeed,
         double damage, int width, int turretWidth, String imageName,
         String buttonImageName) {
      centre = new Point(p);
      this.path = path;
      // Only temporary, it gets actually set later
      topLeft = new Point(0, 0);
      this.name = name;
      this.fireRate = fireRate;
      this.range = range;
      rangeUpgrade = range * (upgradeIncreaseFactor - 1);
      twiceRange = (int)(range * 2);
      this.bulletSpeed = bulletSpeed;
      bulletSpeedUpgrade = bulletSpeed * (upgradeIncreaseFactor - 1);
      this.damage = damage;
      this.width = width;
      halfWidth = width / 2;
      bounds = new Circle(centre, halfWidth);
      this.turretWidth = turretWidth;
      setTopLeft();
      setBounds();
      originalImage = ImageHelper.makeImage(width, width, "towers", imageName);
      currentImage = originalImage;
      if(buttonImageName == null) {
         // TODO Remove this when I'm finished
         buttonImage = null;
      } else {
         buttonImage = ImageHelper.makeImage("buttons", "towers", buttonImageName);
      }
   }

   @Override
   public List<Bullet> tick(List<Sprite> sprites) {
      // Decrements here so it's on every tick, not just when it is able to shoot
      timeToNextShot--;
      List<Bullet> fired = fireBullets(sprites);
      if (timeToNextShot <= 0 && fired.size() > 0) {
         timeToNextShot = fireRate;
         bulletsToAdd.addAll(fired);
      }
      List<Bullet> bullets = bulletsToAdd;
      bulletsToAdd = new ArrayList<Bullet>();
      return bullets;
   }

   @Override
   public void draw(Graphics g) {
      if (!isSelected) {
         g.drawImage(currentImage, (int) topLeft.getX(), (int) topLeft.getY(), null);
      }
   }

   @Override
   public void drawSelected(Graphics g) {
      if (!isSelected) {
         throw new RuntimeException("Tower that isn't selected is being drawn as if it was");
      }
      drawRange(g);
      g.drawImage(currentImage, (int) topLeft.getX(), (int) topLeft.getY(), width, width, null);
   }

   @Override
   public void drawShadow(Graphics g) {
      Graphics2D g2D = (Graphics2D) g;
      drawRange(g2D);
      // Save the current composite to reset back to later
      Composite c = g2D.getComposite();
      // Makes it so what is drawn is partly transparent
      g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));  
      g2D.drawImage(currentImage, (int) topLeft.getX(), (int) topLeft.getY(), width, width, null);
      g2D.setComposite(c);
   }

   @Override
   public boolean towerClash(Tower t) {
      Shape s = t.getBounds();
      if (s instanceof Circle) {
         Circle c = (Circle) s;
         double distance = Point.distance(centre.getX(), centre.getY(), t.getCentre().getX(), t
               .getCentre().getY());
         return distance < bounds.getRadius() + c.getRadius();
      } else {
         return bounds.intersects(s.getBounds2D());
      }
   }

   @Override
   public boolean contains(Point p) {
      if (boundingRectangle.contains(p)) {
         int x = (int) (p.getX() - centre.getX() + halfWidth);
         int y = (int) (p.getY() - centre.getY() + halfWidth);
         // RGB of zero means a completely alpha i.e. transparent pixel
         return currentImage.getRGB(x, y) != 0;
      }
      return false;
   }

   @Override
   public Shape getBounds() {
      return bounds;
   }

   @Override
   public Rectangle getBoundingRectangle() {
      return boundingRectangle;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public int getRange() {
      return (int)range;
   }

   @Override
   public Point getCentre() {
      return centre;
   }

   @Override
   public void setCentre(Point p) {
      centre.setLocation(p);
      setTopLeft();
      setBounds();
   }

   @Override
   public int getAttributeLevel(Attribute a) {
      if (a == Tower.Attribute.Damage) {
         return damageLevel;
      } else if (a == Tower.Attribute.Range) {
         return rangeLevel;
      } else if (a == Tower.Attribute.Rate) {
         return rateLevel;
      } else if (a == Tower.Attribute.Speed) {
         return speedLevel;
      } else if (a == Tower.Attribute.Special) {
         return specialLevel;
      } else {
         throw new RuntimeException("Extra attribute has been added without changing "
               + "getAttributeLevel in AbstractTower");
      }
   }

   @Override
   public void raiseAttributeLevel(Attribute a, boolean boughtUpgrade) {
      if (a == Tower.Attribute.Damage) {
         if(boughtUpgrade) {
            damageLevel++;
         }
         upgradeDamage();
      } else if (a == Tower.Attribute.Range) {
         if(boughtUpgrade) {
            rangeLevel++;
         }
         upgradeRange();
      } else if (a == Tower.Attribute.Rate) {
         if(boughtUpgrade) {
            rateLevel++;
         }
         upgradeFireRate();
      } else if (a == Tower.Attribute.Speed) {
         if(boughtUpgrade) {
            speedLevel++;
         }
         upgradeBulletSpeed();
      } else if (a == Tower.Attribute.Special) {
         if(boughtUpgrade) {
            specialLevel++;
         }
         upgradeSpecial();
      } else {
         throw new RuntimeException("Extra attribute has been added without changing "
               + "raiseAttributeLevel in AbstractTower");
      }
   }

   @Override
   public double getDamage() {
      return damage;
   }

   @Override
   public int getFireRate() {
      return (int)fireRate;
   }

   @Override
   public double getBulletSpeed() {
      return bulletSpeed;
   }

   @Override
   public abstract String getSpecial();
   
   @Override
   public abstract String getSpecialName();

   @Override
   public void select(boolean select) {
      isSelected = select;
   }

   @Override
   public Tower constructNew(Polygon path) {
      return constructNew(centre, path);
   }
   
   @Override
   public Tower constructNew(Point p, Polygon path) {
      try {
         Constructor<? extends Tower> c = this.getClass().getConstructor(Point.class,
               Polygon.class);
         return c.newInstance(p, path);
      } catch(Exception e) {
         // No exception should be thrown if the superclass is reasonably behaved
         throw new RuntimeException("\nSuperclass of AbstractTower is not well behaved.\n" + e +
               " was thrown.\n" + "Either fix this, or override constructNew()");
      }
   }

   @Override
   public void increaseDamageDealt(double damage) {
      assert damage > 0 : "Damage given was negative or zero.";
      damageDealt += damage;
      if(damageDealt >= nextUpgradeDamage) {
         damageDealtLevel++;
         nextUpgradeDamage = Formulae.nextUpgradeDamage(damageDealtLevel);
         upgradeAllStats();
      }
   }

   @Override
   public void increaseKills(int kills) {
      assert kills > 0 : "Kills given was less than or equal to zero";
      this.kills += kills;
      if (this.kills >= nextUpgradeKills) {
         killsLevel++;
         nextUpgradeKills = Formulae.nextUpgradeKills(killsLevel);
         upgradeAllStats();
      }
   }

   @Override
   public long getDamageDealt() {
      return damageDealt;
   }
   
   @Override
   public long getDamageDealtForUpgrade() {
      return nextUpgradeDamage;
   }

   @Override
   public int getKills() {
      return kills;
   }
   
   @Override
   public int getKillsForUpgrade() {
      return nextUpgradeKills;
   }

   @Override
   public BufferedImage getButtonImage() {
      return buttonImage;
   }
   
   @Override
   public void addExtraBullets(Bullet... bullets) {
      for(Bullet b : bullets) {
         bulletsToAdd.add(b);
      }
   }
   
   @Override
   public int getExperienceLevel() {
      // -1 so it starts at level 1
      return killsLevel + damageDealtLevel - 1;
   }
   
   protected abstract Bullet makeBullet(double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, Sprite s, Polygon path);
   
   protected List<Bullet> makeBullets(double dx, double dy, int turretWidth, int range,
            double speed, double damage, Point p, Sprite s, Polygon path) {
      return Helper.makeListContaining(makeBullet(dx, dy, turretWidth, range, speed,
            damage, p, s, path));
   }
   
   protected List<Bullet> fireBullets(List<Sprite> sprites) {
      // Do this for loop even if tower can't shoot so tower rotates to track sprites
      for (Sprite s : sprites) {
         if (checkDistance(s)) {
            return fireBulletsAt(s, true);
         }
      }
      return Collections.emptyList();
   }
   
   protected boolean checkDistance(Sprite s) {
      return checkDistance(s, centre, range);
   }
   
   /**
    * Checks if the distance from the sprite to a point is less than the range.
    * @param s
    * @param p
    * @param range
    * @return
    */
   protected boolean checkDistance(Sprite s, Point p, double range) {
      if (!s.isAlive()) {
         return false;
      }
      double distance = Helper.distance(s.getPosition(), p);
      return distance < range + s.getHalfWidth();
   }
   
   protected List<Bullet> fireBulletsAt(Sprite s, boolean rotateTurret) {
      return fireBulletsAt(s, centre, rotateTurret, turretWidth, range, bulletSpeed, damage);
   }
   
   protected List<Bullet> fireBulletsAt(Sprite s, Point p, boolean rotateTurret,
         int turretWidth, double range, double bulletSpeed, double damage) {
      double dx = s.getPosition().getX() - p.getX();
      double dy = s.getPosition().getY() - p.getY();
      if(rotateTurret) {
         currentImage = ImageHelper.rotateImage(originalImage, dx, -dy);
      }
      return makeBullets(dx, dy, turretWidth, (int)range, bulletSpeed, damage, p, s, path);
   }
   
   private void drawRange(Graphics g) {
      int topLeftRangeX = (int)(centre.getX() - range);
      int topLeftRangeY = (int)(centre.getY() - range);
      g.setColor(rangeColour);
      g.fillOval(topLeftRangeX, topLeftRangeY, twiceRange, twiceRange);
      g.setColor(Color.BLACK);
      g.drawOval(topLeftRangeX, topLeftRangeY, twiceRange, twiceRange);
   }

   private void setTopLeft() {
      topLeft.setLocation((int) centre.getX() - halfWidth, (int) centre.getY() - halfWidth);
   }

   private void setBounds() {
      boundingRectangle.setBounds((int) topLeft.getX(), (int) topLeft.getY(), width, width);
      bounds.setCentre(centre);
   }

   private void upgradeDamage() {
      damage *= upgradeIncreaseFactor;
   }

   private void upgradeRange() {
      range += rangeUpgrade;
      twiceRange = (int)(range * 2);
   }

   private void upgradeFireRate() {
      fireRate /= upgradeIncreaseFactor;
   }

   private void upgradeBulletSpeed() {
      bulletSpeed += bulletSpeedUpgrade;
   }

   protected abstract void upgradeSpecial();
   
   private void upgradeAllStats() {
      upgradeDamage();
      upgradeRange();
      upgradeFireRate();
      upgradeBulletSpeed();
      upgradeSpecial();
   }

}
