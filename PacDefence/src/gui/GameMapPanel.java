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
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import javax.swing.JPanel;

import sprites.Pacman;
import sprites.Sprite;
import sprites.Sprite.FirstComparator;
import towers.AidTower;
import towers.Bullet;
import towers.Tower;
import towers.Tower.Attribute;


@SuppressWarnings("serial")
public class GameMapPanel extends JPanel {


   // Time clock takes to update in ms
   public static final int CLOCK_TICK = 30;
   public static final double CLOCK_TICKS_PER_SECOND = (double)1000 / CLOCK_TICK;
   
   private final boolean debugTimes = true;
   private final boolean debugPath = false;
   
   private final BufferedImage backgroundImage;
   private int bufferIndex = 0;
   private final BufferedImage[] buffers = new BufferedImage[2];
   private final Polygon path;
   private final List<Point> pathPoints;
   private final List<Sprite> sprites = Collections.synchronizedList(new ArrayList<Sprite>());
   private final List<Tower> towers = Collections.synchronizedList(new ArrayList<Tower>());
   private final List<Bullet> bullets = Collections.synchronizedList(new ArrayList<Bullet>());
   private Clock clock;
   private ControlPanel cp;
   private long processTime = 0;
   private long processSpritesTime = 0;
   private long processBulletsTime = 0;
   private long processTowersTime = 0;
   private long drawTime = 0;
   private Tower buildingTower = null;
   private Tower selectedTower = null;
   private Tower hoverOverTower = null;
   private int spritesToAdd;
   private long levelHP;
   private boolean levelInProgress = false;
   private GameOver gameOver = null;
   private final TextDisplay textDisplay = new TextDisplay(); 

   public GameMapPanel(int width, int height, BufferedImage background, GameMap map) {
      backgroundImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics g = backgroundImage.getGraphics();
      g.drawImage(background, 0, 0, width, height, null);
      g.drawImage(map.getImage(), 0, 0, width, height, null);
      buffers[0] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      buffers[1] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      setDoubleBuffered(false);
      setPreferredSize(new Dimension(width, height));
      pathPoints = map.getPathPoints();
      path = map.getPath();
      if(debugPath) {
         printClickedCoords();
      }
      addMouseListeners();
      clock = new Clock();
      clock.start();
   }
   
   @Override
   public synchronized void paintComponent(Graphics g) {
      long beginTime = System.nanoTime();
      g.drawImage(buffers[bufferIndex], 0, 0, null);
      clock.setLastDrawTime(beginTime);
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
         clock.ticksBetweenAddSprite = Formulae.ticksBetweenAddSprite(level);
         //System.out.println(clockRunnable.ticksBetweenAddSprite);
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
   
   public void removeTower(Tower t) {
      if(!towers.remove(t)) {
         throw new RuntimeException("Tower that wasn't on screen sold.");
      }
      if(t == selectedTower) {
         selectedTower = null;
      }
   }
   
   public void restart() {
      clock.end();
      sprites.clear();
      towers.clear();
      bullets.clear();
      spritesToAdd = 0;
      gameOver = null;
      buildingTower = null;
      selectedTower = null;
      hoverOverTower = null;
      levelInProgress = false;
      clock = new Clock();
      clock.start();
      repaint();
   }
   
   private void addMouseListeners() {
      addMouseListener(new MouseAdapter(){
         @Override
         public void mouseReleased(MouseEvent e) {
            processMouseReleased(e);
         }
      });
      addMouseMotionListener(new MouseMotionAdapter(){
         @Override
         public void mouseMoved(MouseEvent e) {
            processMouseMoved(e);
         }
      });
   }
   
   private void processMouseReleased(MouseEvent e) {
      deselectTower();
      if(e.getButton() == MouseEvent.BUTTON3) {
         // Stop everything if it's the right mouse button
         clearBuildingTower();
         return;
      }
      Point p = e.getPoint();
      Tower t = containedInTower(p);
      if(t == null) {
         tryToBuildTower(p);
      } else {
         // Select a tower if one is clicked on
         selectTower(t);
      }    
   }
   
   private void processMouseMoved(MouseEvent e) {
      if(selectedTower == null && buildingTower == null) {
         Tower t = containedInTower(e.getPoint());
         if(hoverOverTower == null && t == null) {
            return;
         }
         if(hoverOverTower != t) {
            cp.hoverOverTower(t);
         }
         hoverOverTower = t;
      }
   }
   
   private Tower containedInTower(Point p) {
      for(Tower t : towers) {
         if(t.contains(p)) {
            return t;
         }
      }
      return null;
   }
   
   private void tryToBuildTower(Point p) {
      if(buildingTower == null) {
         return;
      }
      Tower toBuild = buildingTower.constructNew(p, path.getBounds2D());
      for(Tower t : towers) {
         // Checks that the point doesn't clash with another tower
         if(t.doesTowerClashWith(toBuild)) {
            return;
         }
      }
      // Checks that the point isn't on the path
      Shape bounds = toBuild.getBounds();
      if(bounds instanceof Circle) {
         if(((Circle) bounds).intersects(path)) {
            return;
         }
      } else {
         if(path.intersects(bounds.getBounds())) {
            return;
         }
      }
      if(cp.canBuildTower(toBuild.getClass())) {
         cp.buildTower(toBuild);
         // Have to add after telling the control panel otherwise
         // the price will be wrong
         towers.add(toBuild);
         if(toBuild instanceof AidTower) {
            ((AidTower) toBuild).setTowers(Collections.unmodifiableList(towers));
         }
         if(!cp.canBuildTower(buildingTower.getClass())) {
            clearBuildingTower();
         }
      }
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
   
   public static class GameMap {

      private final String description;
      private final List<Point> pathPoints;
      private final Polygon path;
      private final BufferedImage image;
      
      public GameMap(String description, List<Point> pathPoints, Polygon path,
            BufferedImage image) {
         this.description = description;
         this.pathPoints = pathPoints;
         this.path = path;
         this.image = image;
      }
      
      public String getDescription() {
         return description;
      }
      
      public List<Point> getPathPoints() {
         return pathPoints;
      }
      
      public Polygon getPath() {
         return path;
      }
      
      public BufferedImage getImage() {
         return image;
      }
   }
   
   private class Clock extends Thread {
      
      private int ticksBetweenAddSprite;
      private int addSpriteIn = 0;
      private final int timesLength = (int)(CLOCK_TICKS_PER_SECOND / 2);
      private int timesPos = 0;
      // In each of these the last position is used to store the last time
      // and is not used for calculating the average
      private final long[] processTimes = new long[timesLength + 1];
      private final long[] processSpritesTimes = new long[timesLength + 1];
      private final long[] processBulletsTimes = new long[timesLength + 1];
      private final long[] processTowersTimes = new long[timesLength + 1];
      private final long[] drawTimes = new long[timesLength + 1];
      private boolean keepRunning = true;
      private boolean isWaiting;
      private final int numThreads;
      private final BulletTickThread[] bulletTickThreads;
      private final boolean[] isThreadRunning;
      private final List<Integer> toRemove = new ArrayList<Integer>();
      private double moneyEarnt = 0;
      
      public Clock() {
         numThreads = Runtime.getRuntime().availableProcessors();
         if(numThreads > 1) {
            bulletTickThreads = new BulletTickThread[numThreads];
            isThreadRunning = new boolean[numThreads];
            for(int i = 0; i < numThreads; i++) {
               bulletTickThreads[i] = new BulletTickThread(i);
               bulletTickThreads[i].start();
               isThreadRunning[i] = false;
            }
         } else {
            // If there is only one core, use the original single threaded version.
            bulletTickThreads = null;
            isThreadRunning = null;
         }
      }
            
      @Override
      public void run() {
         while(keepRunning) {
            try {
               // Used nanoTime as many OS, notably windows, don't record ms times less than 10ms
               long beginTime = System.nanoTime();
               if(gameOver == null) {
                  if(debugTimes) {
                     calculateTimesTaken();
                  }
                  tick();
               }
               if(debugTimes) {
                  processTimes[timesLength] = calculateElapsedTime(beginTime);
               }
               draw();
               repaint();
               long elapsedTime = calculateElapsedTime(beginTime);
               if(elapsedTime < CLOCK_TICK) {
                  try {
                     Thread.sleep(CLOCK_TICK - elapsedTime);
                  } catch(InterruptedException e) {
                     e.printStackTrace();
                     // The sleep should never be interrupted
                  }
               }
            } catch(Exception e) {
               e.printStackTrace();
               // This means that the game will continue even if there is some strange bug
            }
         }
      }
      
      public void end() {
         keepRunning = false;
      }
      
      private void tick() {
         // I don't want to sort the actual list of sprites as that would affect
         // the order they're drawn which looks weird, and the order can change
         // tick by tick so it's easiest to sort them once each time.
         List<Sprite> sortedSprites = new ArrayList<Sprite>(sprites);
         Collections.sort(sortedSprites, new FirstComparator());
         List<Sprite> unmodifiableSprites = Collections.unmodifiableList(sortedSprites);
         if(cp != null) {
            long beginTime;
            if(debugTimes) {
               beginTime = System.nanoTime();
            }
            if(cp.decrementLives(doSpriteTicks())) {
               signalGameOver();
            }
            if(debugTimes) {
               processSpritesTimes[timesLength] = calculateElapsedTime(beginTime);
               beginTime = System.nanoTime();
            }
            cp.increaseMoney(doBulletTicks(unmodifiableSprites));
            if(debugTimes) {
               processBulletsTimes[timesLength] = calculateElapsedTime(beginTime);
               beginTime = System.nanoTime();
            }
            doTowerTicks(unmodifiableSprites);
            if(debugTimes) {
               processTowersTimes[timesLength] = calculateElapsedTime(beginTime);
            }
         }
      }
      
      private long calculateElapsedTime(long beginTime) {
         // Returns in ms, though the beginTime is in ns
         return (System.nanoTime() - beginTime) / 1000000;
      }
      
      private void calculateTimesTaken() {
         processTime = insertAndReturnSum(processTimes);
         processSpritesTime = insertAndReturnSum(processSpritesTimes);
         processBulletsTime = insertAndReturnSum(processBulletsTimes);
         processTowersTime = insertAndReturnSum(processTowersTimes);
         drawTime = insertAndReturnSum(drawTimes);
         timesPos = (timesPos + 1) % timesLength;
      }
      
      private long insertAndReturnSum(long[] array) {
         array[timesPos] = array[timesLength];
         array[timesLength] = 0;
         long sum = 0;
         for(int i = 0; i < timesLength; i++) {
            sum += array[i];
         }
         return sum / timesLength;
      }
      
      private void setLastDrawTime(long beginTime) {
         // += as there are two stages to the drawing, firstly to the offscreen buffer
         // then drawing that buffer to screen.
         drawTimes[timesLength] += calculateElapsedTime(beginTime);
      }
      
      private void draw() {
         long beginTime = System.nanoTime();
         if(gameOver == null) {
            // Each time draw on a different buffer so that the a half drawn buffer
            // isn't drawn on the component.
            int nextBufferIndex = bufferIndex == 0 ? 1 : 0;
            Graphics2D bufferGraphics = (Graphics2D) buffers[nextBufferIndex].getGraphics();
            drawUpdate(bufferGraphics);
            textDisplay.draw(bufferGraphics);
            bufferIndex = nextBufferIndex;
         } else {
            // Game over needs to always draw on the same buffer for its sliding effect
            gameOver.draw(buffers[bufferIndex].getGraphics());
         }
         if(debugTimes) {
            setLastDrawTime(beginTime);
         }
      }
      
      private void drawUpdate(Graphics g) {
         // This should completely cover the old image in the buffer
         g.drawImage(backgroundImage, 0, 0, null);
         // Don't use for each loops here to avoid concurrent modification exceptions
         for(int i = 0; i < towers.size(); i++) {
            towers.get(i).draw(g);
         }
         for(int i = 0; i < sprites.size(); i++) {
            sprites.get(i).draw(g);
         }
         if(selectedTower != null) {
            selectedTower.drawSelected(g);
         }
         for(int i = 0; i < bullets.size(); i++) {
            bullets.get(i).draw(g);
         }
         drawDebug(g);
         if(buildingTower != null) {
            Point p = getMousePosition();
            if (p != null) {
               buildingTower = buildingTower.constructNew(p, path.getBounds2D());
               buildingTower.drawShadow(g);
            }
         }
      }
      
      private void drawDebug(Graphics g) {
         if(debugTimes) {
            g.setColor(Color.WHITE);
            g.drawString("Process time: " + processTime, 10, 15);
            g.drawString("Draw time: " + drawTime, 10, 30);
            g.drawString("Sprites time: " + processSpritesTime, 150, 15);
            g.drawString("Bullets time: " + processBulletsTime, 150, 30);
            g.drawString("Towers time: " + processTowersTime, 150, 45);
            g.drawString("Num bullets: " + bullets.size(), 400, 15);
         }
         if(debugPath) {
            drawPath(g);
            drawPathOutline(g);
         }
      }
      
      private int doSpriteTicks() {
         int livesLost = 0;
         if(levelInProgress && sprites.isEmpty() && spritesToAdd <= 0) {
            cp.endLevel();
            levelInProgress = false;
         }
         if(spritesToAdd > 0) {
            if(addSpriteIn < 1) {
               sprites.add(new Pacman(cp.getLevel(), levelHP, clonePathPoints()));
               // Adds a sprite in somewhere between 0 and twice the designated time
               addSpriteIn = (int)(Math.random() * (ticksBetweenAddSprite * 2 + 1));
               spritesToAdd--;
               cp.setNumberLeft(spritesToAdd);
            } else {
               addSpriteIn--;
            }
         }
         List<Integer> toRemove = new ArrayList<Integer>();
         for(int i = 0; i < sprites.size(); i++) {
            Sprite s = sprites.get(i);
            if(s.tick()) {
               toRemove.add(i);
               if(s.isAlive()) {
                  // If the sprite is being removed and is still alive, it went
                  // off the edge of the screen
                  livesLost++;
               }
            }
         }
         Helper.removeAll(sprites, toRemove);
         return livesLost;
      }
      
      private List<Point> clonePathPoints() {
         List<Point> clone = new ArrayList<Point>(pathPoints.size());
         for(Point p : pathPoints) {
            clone.add(new Point(p));
         }
         return clone;
      }
      
      private void doTowerTicks(List<Sprite> unmodifiableSprites) {
         // Only do this if there's a level going, to decrease load and
         // so towers such as charge towers don't charge between levels
         if(levelInProgress) {
            // Don't use for each loop here as a new tower can be built
            for(int i = 0; i < towers.size(); i++ ) {
               bullets.addAll(towers.get(i).tick(unmodifiableSprites));
            }
         }
      }
      
      private long doBulletTicks(List<Sprite> unmodifiableSprites) {
         if(numThreads == 1 || bullets.size() <= 1) {
            // Use single thread version if only one processor or 1 or fewer bullets
            // as it will be faster.
            return doBulletTicksSingleThread(unmodifiableSprites);
         }
         isWaiting = true;
         int bulletsPerThread = bullets.size() / numThreads;
         int remainder = bullets.size() - bulletsPerThread * numThreads;
         Arrays.fill(isThreadRunning, true);
         int firstPos, lastPos = 0;
         for(int i = 0; i < numThreads; i++) {
            firstPos = lastPos;
            lastPos = firstPos + bulletsPerThread + (i < remainder ? 1 : 0);
            // Copying the list should reduce the lag of each thread trying to access
            // the same list
            bulletTickThreads[i].tickBullets(firstPos, lastPos, bullets,
                  new ArrayList<Sprite>(unmodifiableSprites));
         }
         while(isWaiting) {
            LockSupport.park();
         }
         Collections.sort(toRemove);
         Helper.removeAll(bullets, toRemove);
         toRemove.clear();
         double toReturn = moneyEarnt;
         moneyEarnt = 0;
         return (long)toReturn;
      }
      
      private synchronized void informFinished(int threadNumber, double moneyEarnt,
            List<Integer> toRemove) {
         this.moneyEarnt += moneyEarnt;
         this.toRemove.addAll(toRemove);
         isThreadRunning[threadNumber] = false;
         for(boolean b : isThreadRunning) {
            if(b) {
               return;
            }
         }
         isWaiting = false;
         LockSupport.unpark(this);
      }
      
      private long doBulletTicksSingleThread(List<Sprite> unmodifiableSprites) {
         double moneyEarnt = 0;
         List<Integer> toRemove = new ArrayList<Integer>();
         for(int i = 0; i < bullets.size(); i++) {
            double money = bullets.get(i).tick(unmodifiableSprites);
            if(money >= 0) {
               moneyEarnt += money;
               toRemove.add(i);
            }
         }
         Helper.removeAll(bullets, toRemove);
         return (long)moneyEarnt;
      }
      
      private class BulletTickThread extends Thread {
         
         private final int threadNumber;
         private int firstPos, lastPos;
         private List<Bullet> bulletsToTick;
         private List<Sprite> sprites;
         private boolean doTick;
         
         public BulletTickThread(int number) {
            super();
            this.threadNumber = number;
         }
         
         public void tickBullets(int firstPos, int lastPos, List<Bullet> bulletsToTick,
               List<Sprite> sprites) {
            this.firstPos = firstPos;
            this.lastPos = lastPos;
            this.bulletsToTick = bulletsToTick;
            this.sprites = sprites;
            doTick = true;
            LockSupport.unpark(this);
         }
         
         @Override
         public void run() {
            while(true) {
               if(doTick) {
                  double moneyEarnt = 0;
                  List<Integer> toRemove = new ArrayList<Integer>();
                  for(int i = firstPos; i < lastPos; i++) {
                     double money = bulletsToTick.get(i).tick(sprites);
                     if(money >= 0) {
                        moneyEarnt += money;
                        toRemove.add(i);
                     }
                  }
                  doTick = false;
                  informFinished(threadNumber, moneyEarnt, toRemove);
               }
               LockSupport.park();
            }
         }
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
