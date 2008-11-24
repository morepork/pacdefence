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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
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
import towers.Tower.Attribute;


@SuppressWarnings("serial")
public class GameMap extends JPanel {


   // Time clock takes to update in ms
   public static final int CLOCK_TICK = 30;
   
   private final boolean debugTimes = true;
   private final boolean debugPath = false;
   
   private final BufferedImage backgroundImage = ImageHelper.makeImage(OuterPanel.MAP_WIDTH,
         OuterPanel.MAP_HEIGHT, "maps", "mosaic_path.jpg");
   private BufferedImage buffer = new BufferedImage(OuterPanel.MAP_WIDTH, OuterPanel.MAP_HEIGHT,
         BufferedImage.TYPE_INT_RGB);
   private Graphics2D bufferGraphics = buffer.createGraphics();
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
   private Tower buildingTower = null;
   private Tower selectedTower = null;
   private int spritesToAdd;
   private int levelHP;
   private boolean levelInProgress = false;
   private boolean needsRepaint = false;
   private GameOver gameOver = null;
   private final TextDisplay textDisplay = new TextDisplay(); 

   public GameMap(int width, int height) {
      setDoubleBuffered(false);
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
   public synchronized void paintComponent(Graphics g) {
      long beginTime = System.nanoTime();
      if(needsRepaint) {
         needsRepaint = false;
         // First buffers onto the buffer, then draws it onto screen later
         if(gameOver == null) {
            drawUpdate(bufferGraphics);
            textDisplay.draw(bufferGraphics);
         } else {
            gameOver.draw(bufferGraphics);
         }
      }
      g.drawImage(buffer, 0, 0, null);
      // Divides by a million to convert to ms
      long elapsedTime = (System.nanoTime() - beginTime) / 1000000;
      //System.out.println(elapsedTime);
      clockRunnable.calculateDrawTimeTaken(elapsedTime);
   }
   
   /**
    * Notifies the map that a tower button has been pressed.
    * @param t
    * @return
    *        true if a tower is to be shadowed, false if none
    */
   public boolean towerButtonPressed(Tower t) {
      if(buildingTower == null || !buildingTower.getClass().equals(t.getClass())) {
         deselectTower();
         buildingTower = t;
         return true;
      } else {
         clearBuildingTower();
         return false;
      }
   }
   
   public boolean start(int level){
      if(sprites.isEmpty()) {
         levelInProgress = true;
         spritesToAdd = Formulae.numSprites(level);
         levelHP = Formulae.hp(level);
         if(clockRunnable.ticksBetweenAddSprite > 1) {
            // As the level increases, the sprites start entering closer together
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
   
   public void displayText(String... lines) {
      textDisplay.displayText(lines);
   }
   
   public void removeText() {
      textDisplay.clear();
   }
   
   public void upgradeAll(Attribute a) {
      for(Tower t : towers) {
         t.raiseAttributeLevel(a, false);
      }
   }
   
   public List<Tower> getTowers() {
      return Collections.unmodifiableList(towers);
   }
   
   private void drawUpdate(Graphics g) {
      // This should completely cover the old image in the buffer
      g.drawImage(backgroundImage, 0, 0, null);
      // Don't use for each loops here to avoid concurrent modification exceptions
      for(int i = 0; i < towers.size(); i++) {
         Tower t = towers.get(i);
         t.draw(g);
      }
      for(int i = 0; i < sprites.size(); i++) {
         Sprite s = sprites.get(i);
         s.draw(g);
      }
      if(selectedTower != null) {
         selectedTower.drawSelected(g);
      }
      for(int i = 0; i < bullets.size(); i++) {
         Bullet b = bullets.get(i);
         b.draw(g);
      }
      drawDebug(g);
      if(buildingTower != null) {
         Point p = getMousePosition();
         if (p != null) {
            buildingTower.setCentre(p);
            buildingTower.drawShadow(g);
         }
      }
   }
   
   private void drawDebug(Graphics g) {
      if(debugTimes) {
         bufferGraphics.setColor(Color.WHITE);
         bufferGraphics.drawString("Process time: " + Long.toString(processTime), 10, 15);
         bufferGraphics.drawString("Draw time: " + Long.toString(drawTime), 10, 30);
      }
      if(debugPath) {
         drawPath(bufferGraphics);
         drawPathOutline(bufferGraphics);
      }
   }
   
   private void addMouseListeners() {
      addMouseListener(new MouseAdapter(){
         @Override
         public void mouseReleased(MouseEvent e) {
            deselectTower();
            if(e.getButton() == MouseEvent.BUTTON3) {
               // Stop everything if it's the right mouse button
               clearBuildingTower();
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
      if(buildingTower == null) {
         return;
      }
      buildingTower.setCentre(p);
      for(Tower t : towers) {
         // Checks that the point doesn't clash with another tower
         if(t.towerClash(buildingTower)) {
            return;
         }
      }
      // Checks that the point isn't on the path
      Shape bounds = buildingTower.getBounds();
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
         cp.buildTower();
         towers.add(buildingTower.constructNew());
         if(!cp.canBuildTower()) {
            clearBuildingTower();
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
      int[] xPoints = new int[]{0, 273, 273, 137, 137, 600, 600, 67, 67, 203, 203, 0};
      int[] yPoints = new int[]{106, 106, 372, 372, 438, 438, 503, 503, 305, 305, 166, 166};
      return new Polygon(xPoints, yPoints, xPoints.length);
   }
   
   private void selectTower(Tower t) {
      cp.selectTower(t);
      t.select(true);
      selectedTower = t;
      clearBuildingTower();
   }
   
   private void deselectTower() {
      cp.deselectTower();
      if(selectedTower != null) {
         selectedTower.select(false);
         selectedTower = null;
      }
   }
   
   private void clearBuildingTower() {
      buildingTower = null;
      cp.clearBuildingTower();
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
      
      private int ticksBetweenAddSprite = 40;
      private int addSpriteIn = 0;
      private int processTimesPos = 0;
      private int drawTimesPos = 0;
      private final int timesLength = 50;
      private final long[] processTimes = new long[timesLength];
      private final long[] drawTimes = new long[timesLength];
            
      public synchronized void run() {
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
                  cp.increaseMoney(doBullets(unmodifiableSprites));
               }
            }
            // Divides by a million to convert to ms
            long elapsedTime = (System.nanoTime() - beginTime) / 1000000;
            //System.out.println(elapsedTime);
            needsRepaint = true;
            repaint();
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
            cp.endLevel();
            levelInProgress = false;
         }
         if(spritesToAdd > 0) {
            if(addSpriteIn < 1) {
               sprites.add(new Pacman(levelHP, clonePathPoints()));
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
      
      private List<Point> clonePathPoints() {
         List<Point> clone = new ArrayList<Point>(pathPoints.size());
         for(Point p : pathPoints) {
            clone.add(new Point(p));
         }
         return clone;
      }
      
      private void doTowers(List<Sprite> unmodifiableSprites) {
         // Don't use for each loop here as a new tower can be built
         for(int i = 0; i < towers.size(); i++ ) {
            Tower t = towers.get(i);
            List<Bullet> b = t.tick(unmodifiableSprites);
            if(b != null) {
               bullets.addAll(b);
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
      
      private final int framesBetweenRedraw = 1;
      private int framesUntilRedraw = 0;
      private int deltaY, deltaX;
      private BufferedImage img;
      private int currentX, currentY, iterations, numTimes;
      
      private GameOver() {
         deltaY = 1;
         img = ImageHelper.makeImage((int)(getWidth()*0.75), (int)(getHeight()*0.2), "other",
               "GameOver.png");
         double mult = 1.2;
         currentY = (int)(img.getHeight() * mult);
         numTimes = (int)(((getHeight() * 0.75) - img.getHeight() * mult - currentY)/deltaY);
         currentX = getWidth()/2 - img.getWidth()/2;
      }
      
      private void draw(Graphics g) {
         if(iterations > numTimes) {
            return;
         }
         if(framesUntilRedraw > 0) {
            framesUntilRedraw--;
         } else {
            framesUntilRedraw = framesBetweenRedraw;
            currentY += deltaY;
            currentX += deltaX;
            iterations++;
         }
         Graphics2D g2D = (Graphics2D)g;
         // Save the current composite to reset back to later
         Composite c = g2D.getComposite();
         // Makes it so what is drawn is partly transparent
         g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
               0.7f/(framesBetweenRedraw + 1)));
         g.drawImage(img, currentX, currentY, null);
         g2D.setComposite(c);
      }
   }

   private static class TextDisplay {
      
      private static final Color backgroundColour = new Color(255, 255, 255, 120);
      private static final Color textColour = Color.BLACK;
      private final int startPosition = OuterPanel.MAP_HEIGHT;
      private int currentPosition = startPosition;
      private int heightToDisplay;
      private static final int rounding = 40;
      private static final int aboveTextMargin = 5;
      private static final int sideMargin = 12;
      private static final int offset = 5;
      private static final int width = OuterPanel.MAP_WIDTH - offset * 2;
      private static final int height = OuterPanel.MAP_HEIGHT;
      private final BufferedImage display = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_ARGB_PRE);
      private final Graphics displayGraphics = display.getGraphics();
      private boolean displayed = false;
      private boolean clearingFlag = false;
      private boolean drawingFlag = false;
      
      public void draw(Graphics g) {
         if (drawingFlag) {
            currentPosition--;
            if(currentPosition <= startPosition - heightToDisplay) {
               drawingFlag = false;
               displayed = true;
            }
         } else if (clearingFlag) {
            currentPosition++;
            if(currentPosition >= startPosition) {
               clearingFlag = false;
               currentPosition = startPosition;
               return;
            }
         } else if (!displayed) {
            return;
         }
         g.drawImage(display, offset, currentPosition, null);
      }
      
      public void displayText(String... lines) {
         if(displayed) {
            throw new RuntimeException("There is already text on display.");
         }
         drawImage(lines);
         drawingFlag = true;
         clearingFlag = false;
         displayed = false;
      }
      
      public void clear() {
         heightToDisplay = 0;
         clearingFlag = true;
         drawingFlag = false;
         displayed = false;
      }
      
      private void drawImage(String[] lines) {
         clearImage();
         int lineHeight = displayGraphics.getFontMetrics().getHeight() + aboveTextMargin;
         displayGraphics.setColor(textColour);
         for(String s : lines) {
            heightToDisplay += lineHeight;
            displayGraphics.drawString(s, sideMargin, heightToDisplay);
         }
         heightToDisplay += aboveTextMargin * 2;
      }
      
      private void clearImage() {
         ImageHelper.clearImage(display);
         displayGraphics.setColor(backgroundColour);
         displayGraphics.fillRoundRect(0, 0, width, height, rounding, rounding);
      }
      
   }

}
