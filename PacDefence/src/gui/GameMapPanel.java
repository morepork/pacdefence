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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import sprites.Sprite;
import towers.Bullet;
import towers.Tower;


@SuppressWarnings("serial")
public class GameMapPanel extends JPanel {
   
   private final boolean debugTimes;
   private final boolean debugPath;
   
   private final BufferedImage backgroundImage;
   private int bufferIndex = 0;
   private final BufferedImage[] buffers = new BufferedImage[2];
   private final Polygon path;
   private final List<Point> pathPoints;

   private GameOver gameOver = null;
   private final TextDisplay textDisplay;
   
   private long lastPaintTime = 0;

   public GameMapPanel(int width, int height, BufferedImage background, GameMap map,
         boolean debugTimes, boolean debugPath) {
      this.debugTimes = debugTimes;
      this.debugPath = debugPath;
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
      textDisplay = new TextDisplay(width, height);
      if(debugPath) {
         printClickedCoords();
      }
   }
   
   @Override
   public synchronized void paintComponent(Graphics g) {
      long beginTime = System.nanoTime();
      g.drawImage(buffers[bufferIndex], 0, 0, null);
      lastPaintTime = System.nanoTime() - beginTime;
   }
   
   public void displayText(String... lines) {
      textDisplay.displayText(lines);
   }
   
   public void removeText() {
      textDisplay.clear();
   }
   
   public void restart() {
      gameOver = null;
      repaint();
   }
   
   public void signalGameOver() {
      if(gameOver == null) {
         gameOver = new GameOver();
      }
   }
   
   public long draw(List<Tower> towers, Tower buildingTower,
         List<Sprite> sprites, List<Bullet> bullets, long processTime, long processSpritesTime,
         long processBulletsTime, long processTowersTime, long drawTime, int numBullets) {
      if(gameOver == null) {
         // Each time draw on a different buffer so that the a half drawn buffer
         // isn't drawn on the component.
         int nextBufferIndex = bufferIndex == 0 ? 1 : 0;
         Graphics2D bufferGraphics = (Graphics2D) buffers[nextBufferIndex].getGraphics();
         drawUpdate(bufferGraphics, towers, buildingTower, sprites, bullets, processTime,
               processSpritesTime, processBulletsTime, processTowersTime, drawTime, numBullets);
         textDisplay.draw(bufferGraphics);
         bufferIndex = nextBufferIndex;
      } else {
         // Game over needs to always draw on the same buffer for its sliding effect
         gameOver.draw(buffers[bufferIndex].getGraphics());
      }
      return lastPaintTime;
   }
   
   private void drawUpdate(Graphics g, List<Tower> towers, Tower buildingTower,
         List<Sprite> sprites, List<Bullet> bullets, long processTime, long processSpritesTime,
         long processBulletsTime, long processTowersTime, long drawTime, int numBullets) {
      // This should completely cover the old image in the buffer
      g.drawImage(backgroundImage, 0, 0, null);
      for(int i = 0; i < sprites.size(); i++) {
         sprites.get(i).draw(g);
      }
      // Draw towers after sprites so the sprites aren't drawn on the range of the
      // selected tower
      for(int i = 0; i < towers.size(); i++) {
         towers.get(i).draw(g);
      }
      for(int i = 0; i < bullets.size(); i++) {
         bullets.get(i).draw(g);
      }
      drawDebug(g, processTime, processSpritesTime, processBulletsTime, processTowersTime,
            drawTime, numBullets);
      if(buildingTower != null) {
         Point p = getMousePosition();
         if (p != null) {
            buildingTower.drawShadowAt(g, p);
         }
      }
   }
   
   private void drawDebug(Graphics g, long processTime, long processSpritesTime,
         long processBulletsTime, long processTowersTime, long drawTime, int numBullets) {
      if(debugTimes) {
         g.setColor(Color.WHITE);
         g.drawString("Process time: " + processTime, 10, 15);
         g.drawString("Draw time: " + drawTime, 10, 30);
         g.drawString("Sprites time: " + processSpritesTime, 150, 15);
         g.drawString("Bullets time: " + processBulletsTime, 150, 30);
         g.drawString("Towers time: " + processTowersTime, 150, 45);
         g.drawString("Num bullets: " + numBullets, 400, 15);
      }
      if(debugPath) {
         drawPath(g);
         drawPathOutline(g);
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
         // These two lines are the cross at each point
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
      private final int startPosition;
      private int currentPosition;
      private int heightToDisplay;
      private static final int rounding = 40;
      private static final int aboveTextMargin = 5;
      private static final int sideMargin = 12;
      private static final int offset = 5;
      private final int width;
      private final int height;
      private final BufferedImage display;
      private final Graphics displayGraphics;
      private boolean isOnDisplay = false;
      private boolean clearingFlag = false;
      private boolean drawingFlag = false;
      
      public TextDisplay(int width, int height) {
         startPosition = height;
         currentPosition = startPosition;
         this.width = width - offset * 2;
         this.height = height;
         display  = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
         displayGraphics = display.getGraphics();
      }
      
      public void draw(Graphics g) {
         if (drawingFlag) {
            currentPosition--;
            if(currentPosition <= startPosition - heightToDisplay) {
               drawingFlag = false;
               isOnDisplay = true;
            }
         } else if (clearingFlag) {
            currentPosition++;
            if(currentPosition >= startPosition) {
               clearingFlag = false;
               currentPosition = startPosition;
               return;
            }
         } else if (!isOnDisplay) {
            return;
         }
         g.drawImage(display, offset, currentPosition, null);
      }
      
      public void displayText(String... lines) {
         if(isOnDisplay) {
            throw new RuntimeException("There is already text on display.");
         }
         drawImage(lines);
         drawingFlag = true;
         clearingFlag = false;
         isOnDisplay = false;
      }
      
      public void clear() {
         heightToDisplay = 0;
         clearingFlag = true;
         drawingFlag = false;
         isOnDisplay = false;
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
