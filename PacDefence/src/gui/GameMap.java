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

package gui;

import images.ImageHelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import sprites.AbstractSprite;
import sprites.Pacman;
import sprites.Sprite;
import towers.Bullet;
import towers.Tower;


@SuppressWarnings("serial")
public class GameMap extends JPanel {


   // Time clock takes to update in ms
   public static final int CLOCK_TICK = 50;
   
   public static final int PATH_WIDTH = 35;
   
   private final BufferedImage backgroundImage = ImageHelper.makeImage("maps", "mosaic_path.jpg");
   private BufferedImage resizedBackgroundImage = backgroundImage;
   private int lastWidth = getWidth();
   private int lastHeight = getHeight();
   private final Polygon path;
   private final List<Point> pathPoints;
   private final List<Sprite> sprites = new ArrayList<Sprite>();
   private final List<Tower> towers = new ArrayList<Tower>();
   private final List<Bullet> bullets = new ArrayList<Bullet>();
   private final Clock clockRunnable;
   private final Thread clock;
   private ControlPanel cp;
   private long processTime = 0;
   private long drawTime = 0;
   private Tower shadowingTower = null;
   private Tower selectedTower = null;
   private int spritesToAdd;
   private int levelHP;
   private boolean levelInProgress = false;
   private GameOver gameOver = null;

   public GameMap(int width, int height) {
      setPreferredSize(new Dimension(width, height));
      pathPoints = makePathPoints();
      path = makePath();
      //printClickedCoords();
      addMouseListeners();
      clockRunnable = new Clock();
      clock = new Thread(clockRunnable);
      clock.start();
   }
   
   @Override
   public void paint(Graphics g) {
      if(gameOver != null) {
         gameOver.draw(g);
         return;
      }
      long beginTime = System.nanoTime();
      // First buffers onto this image then redraws it onto screen
      BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D biGraphics = bi.createGraphics();
      paintComponent(biGraphics);
      // Don't use for each loops here to avoid concurrent modification exceptions
      for(int i = 0; i < towers.size(); i++) {
         Tower t = towers.get(i);
         t.draw(biGraphics);
      }
      for(int i = 0; i < sprites.size(); i++) {
         Sprite s = sprites.get(i);
         s.draw(biGraphics);
      }
      for(int i = 0; i < bullets.size(); i++) {
         Bullet b = bullets.get(i);
         b.draw(biGraphics);
      }
      if(selectedTower != null) {
         selectedTower.drawSelected(biGraphics);
      }
      biGraphics.setColor(Color.WHITE);
      biGraphics.drawString("Process time: " + Long.toString(processTime), 10, 15);
      biGraphics.drawString("Draw time: " + Long.toString(drawTime), 10, 30);
      drawPath(biGraphics);
      drawPathOutline(biGraphics);
      if(shadowingTower != null) {
         Point p = getMousePosition();
         if (p != null) {
            shadowingTower.setCentre(p);
            shadowingTower.drawShadow(biGraphics);
         }
      }
      g.drawImage(bi, 0, 0, null);
      // Divides by a million to convert to ms
      long elapsedTime = (System.nanoTime() - beginTime) / 1000000;
      //System.out.println(elapsedTime);
      clockRunnable.calculateDrawTimeTaken(elapsedTime);
   }

   @Override
   public void paintComponent(Graphics g) {
      if(lastWidth != getWidth() || lastHeight != getHeight()) {
         //System.out.println("Resizing background image");
         lastWidth = getWidth();
         lastHeight = getHeight();
         resizedBackgroundImage = ImageHelper.resize(backgroundImage, lastWidth, lastHeight);
      }
      g.drawImage(resizedBackgroundImage, 0, 0, null);
   }
   
   @Override
   public void update(Graphics g) {
      // Normally this would clear the screen before calling paint
      paint(g);
   }
   
   /**
    * Notifies the map that a tower button has been pressed.
    * @param t
    * @return
    *        true if a tower is to be shadowed, false if none
    */
   public boolean towerButtonPressed(Tower t) {
      if(shadowingTower == null || !shadowingTower.getClass().equals(t.getClass())) {
         deselectTower();
         shadowingTower = t;
         return true;
      } else {
         shadowingTower = null;
         return false;
      }
   }
   
   public boolean start(int level){
      if(sprites.isEmpty()) {
         levelInProgress = true;
         spritesToAdd = Formulae.numSprites(level);
         levelHP = Formulae.hp(level);
         if(clockRunnable.ticksBetweenAddSprite > 1) {
            clockRunnable.ticksBetweenAddSprite--;
         }
         return true;
      } else {
         return false;
      }
   }
   
   public void setControlPanel(ControlPanel cp) {
      this.cp = cp;
   }
   
   private void addMouseListeners() {
      addMouseListener(new MouseAdapter(){
         @Override
         public void mouseReleased(MouseEvent e) {
            deselectTower();
            if(e.getButton() == MouseEvent.BUTTON3) {
               // Stop everything if it's the right mouse button
               shadowingTower = null;
               return;
            }
            Point p = e.getPoint();
            for(Tower t : towers) {
               // Select a tower if one is clicked on
               if(t.contains(p)) {
                  selectTower(t);
                  return;
               }
            }
            tryToBuildTower(p);            
         }
      });
   }
   
   private void tryToBuildTower(Point p) {
      if(shadowingTower == null) {
         return;
      }
      shadowingTower.setCentre(p);
      for(Tower t : towers) {
         // Checks that the point doesn't clash with another tower
         if(t.towerClash(shadowingTower)) {
            return;
         }
      }
      // Checks that the point isn't on the path
      Shape bounds = shadowingTower.getBounds();
      if(bounds instanceof Circle) {
         if(((Circle) bounds).intersects(path)) {
            return;
         }
      } else {
         if(path.intersects(bounds.getBounds())) {
            return;
         }
      }
      if(cp.canBuildTower()) {
         towers.add(shadowingTower.constructNew());
         if(!cp.buildTower()) {
            // If this returns false another tower can't be afforded
            shadowingTower = null;
         }
      }
   }
   
   private List<Point> makePathPoints(){
      List<Point> list = new ArrayList<Point>();
      list.add(new Point(0, 135));
      list.add(new Point(235, 135));
      list.add(new Point(235, 335));
      list.add(new Point(100, 335));
      list.add(new Point(100, 470));
      list.add(new Point(600, 470));
      return Collections.unmodifiableList(list);
   }
   
   private Polygon makePath() {
      // These need to be worked out depending on the image.
      int[] xPoints = new int[]{0, 274, 274, 139, 139, 600, 600, 66, 66, 202, 202, 0};
      int[] yPoints = new int[]{105, 105, 375, 375, 437, 437, 504, 504, 304, 304, 168, 168};
      return new Polygon(xPoints, yPoints, xPoints.length);
   }
   
   private void selectTower(Tower t) {
      cp.selectTower(t);
      t.select(true);
      selectedTower = t;
      shadowingTower = null;
   }
   
   private void deselectTower() {
      cp.deselectTower();
      if(selectedTower != null) {
         selectedTower.select(false);
         selectedTower = null;
      }
   }
   
   private void signalGameOver() {
      if(gameOver == null) {
         gameOver = new GameOver();
      }
   }
   
   /**
    * Test method that draws in each point on the path and links them up.
    */
   private void drawPath(Graphics g) {
      g.setColor(Color.BLACK);
      Point lastPoint = null;
      for(Point p : pathPoints) {
         int x = p.x;
         int y = p.y;
         g.drawLine(x - 10, y - 10, x + 10, y + 10);
         g.drawLine(x - 10, y + 10, x + 10, y - 10);
         if (lastPoint != null) {
            g.drawLine(lastPoint.x, lastPoint.y, x, y);
         }
         lastPoint = p;
      }      
   }
   
   /**
    * Test method that draws an outline of the path.
    */
   private void drawPathOutline(Graphics g) {
      g.setColor(Color.BLACK);
      g.drawPolygon(path);
   }
   
   /**
    * Test method that prints the coords of a mouse click.
    */
   @SuppressWarnings("unused")
   private void printClickedCoords() {
      addMouseListener(new MouseAdapter(){
         @Override
         public void mouseClicked(MouseEvent e) {
            System.out.println("Mouse clicked on map at (" + e.getX() + "," + e.getY() + ")");
         }
      });
   }
   
   private class Clock implements Runnable {
      
      private int ticksBetweenAddSprite = 20;
      private int addSpriteIn = 0;
      private int processTimesPos = 0;
      private int drawTimesPos = 0;
      private final int timesLength = 50;
      private final long[] processTimes = new long[timesLength];
      private final long[] drawTimes = new long[timesLength];
            
      public void run() {
         while(true) {
            // Used nanoTime as many OS, notably windows, don't record ms times less than 10ms
            long beginTime = System.nanoTime();
            if(gameOver == null) {
               // I don't want to sort the actual list of sprites as that would
               // affect the order they're drawn which looks weird.
               List<Sprite> sortedSprites = Helper.cloneList(sprites);
               Collections.sort(sortedSprites,
                     AbstractSprite.getTotalDistanceTravelledComparator());
               List<Sprite> unmodifiableSprites = Collections.unmodifiableList(sortedSprites);
               if(cp != null) {
                  if(cp.decrementLives(doSprites())) {
                     signalGameOver();
                  }
                  doTowers(unmodifiableSprites);
                  cp.incrementMoney(doBullets(unmodifiableSprites));
               }
            }
            repaint();
            // Divides by a million to convert to ms
            long elapsedTime = (System.nanoTime() - beginTime) / 1000000;
            //System.out.println(elapsedTime);
            calculateProcessTimeTaken(elapsedTime);            
            if(elapsedTime < CLOCK_TICK) {
               try {
                  Thread.sleep(CLOCK_TICK - elapsedTime);
               } catch(InterruptedException e) {
                  // The sleep should never be interrupted
               }
            }
         }
      }
      
      private void calculateProcessTimeTaken(long elapsedTime) {
         processTimes[processTimesPos] = elapsedTime;
         processTimesPos = (processTimesPos + 1) % timesLength;
         //System.out.println(elapsedTimesPos);
         long sum = 0;
         for(long a : processTimes) {
            sum += a;
         }
         processTime = sum / timesLength;
      }
      
      private void calculateDrawTimeTaken(long elapsedTime) {
         // Basically a duplicate of the above. Should fix.
         drawTimes[drawTimesPos] = elapsedTime;
         drawTimesPos = (drawTimesPos + 1) % timesLength;
         //System.out.println(elapsedTimesPos);
         long sum = 0;
         for(long a : drawTimes) {
            sum += a;
         }
         drawTime = sum / timesLength;
      }
      
      private int doSprites() {
         int livesLost = 0;
         if(levelInProgress && sprites.isEmpty() && spritesToAdd <= 0) {
            System.out.println("Level finished");
            cp.endLevel();
            levelInProgress = false;
         }
         if(spritesToAdd > 0) {
            if(addSpriteIn < 1) {
               sprites.add(new Pacman(levelHP, Collections.unmodifiableList(pathPoints)));
               addSpriteIn = ticksBetweenAddSprite;
               spritesToAdd--;
            } else {
               addSpriteIn--;
            }
         }
         List<Sprite> toRemove = new ArrayList<Sprite>();
         for(Sprite s : sprites) {
            if(s.tick()) {
               toRemove.add(s);
               if(s.isAlive()) {
                  // If the sprite is being removed and is still alive, it went
                  // off the edge of the screen
                  livesLost++;
               }
            }
         }
         sprites.removeAll(toRemove);
         return livesLost;
      }
      
      private void doTowers(List<Sprite> unmodifiableSprites) {
         // Don't use for each loop here as a new tower can be built
         for(int i = 0; i < towers.size(); i++ ) {
            Tower t = towers.get(i);
            Bullet b = t.tick(unmodifiableSprites);
            if(b != null) {
               bullets.add(b);
            }
         }
      }
      
      private int doBullets(List<Sprite> unmodifiableSprites) {
         double moneyEarnt = 0;
         List<Bullet> toRemove = new ArrayList<Bullet>();
         for(Bullet b : bullets) {
            double money = b.tick(unmodifiableSprites);
            if(money >= 0) {
               moneyEarnt += money;
               toRemove.add(b);
            }
         }
         bullets.removeAll(toRemove);
         return (int)moneyEarnt;
      }
      
   }
   
   /**
    * Draws game over screen for a loss
    * 
    * @author Liam Byrne
    *
    */
   private class GameOver {
      
      private int deltaY, deltaX;
      private BufferedImage img;
      private int currentX, currentY, i, numTimes;
      
      private GameOver() {
         deltaY = 1;
         img = ImageHelper.makeImage("other", "GameOver.png");
         img = ImageHelper.resize(img, (int)(getWidth()*0.75), (int)(getHeight()*0.2));
         currentY = img.getHeight();
         numTimes = ((int)(getHeight()*0.75) - img.getHeight() - currentY)/deltaY;
         currentX = getWidth()/2 - img.getWidth()/2;
         repaint();
      }
      
      private void draw(Graphics g) {
         if(i > numTimes) {
            return;
         }
         currentY += deltaY;
         currentX += deltaX;
         i++;         
         g.drawImage(img, currentX, currentY, null);
      }
   }

   public static void main(String... args) {
   }

}
