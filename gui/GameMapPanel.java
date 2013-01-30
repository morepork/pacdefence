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

package gui;

import gui.maps.MapParser.GameMap;
import images.ImageHelper;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;

import logic.Constants;
import util.Helper;


@SuppressWarnings("serial")
public class GameMapPanel extends JPanel {
   
   private final boolean debugTimes;
   private final boolean debugPath;
   
   private final GameMap gameMap;
   
   private final BufferedImage backgroundImage;
   //Have two precreated buffers as recreating them at each step is much slower
   private BufferedImage buffer;
   
   private boolean drawingFlag = false;
   
   private List<Drawable> drawablesToDraw;
   private DebugStats debugStatsToDraw;
   
   private GameOver gameOver = null;
   private final TextDisplay textDisplay;
   
   private long lastPaintTime = 0;

   public GameMapPanel(GameMap map, boolean debugTimes, boolean debugPath) {
      int width = Constants.MAP_WIDTH;
      int height = Constants.MAP_HEIGHT;
      this.debugTimes = debugTimes;
      this.debugPath = debugPath;
      
      // This class does its own double buffering
      setDoubleBuffered(false);
      // Repaints are scheduled by the clock every few ms, so we don't need the normal ones
      setIgnoreRepaint(true);
      
      setPreferredSize(new Dimension(width, height));
      
      GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
            getDefaultScreenDevice().getDefaultConfiguration();
      backgroundImage = gc.createCompatibleImage(width, height, Transparency.OPAQUE);
      buffer = gc.createCompatibleImage(width, height, Transparency.OPAQUE);

      // Draws the actual map (maze) onto the background image
      Graphics2D g = backgroundImage.createGraphics();
      g.drawImage(map.getImage(), 0, 0, width, height, null);
      g.dispose();
      
      this.gameMap = map;

      textDisplay = new TextDisplay(width, height);
      
      if(debugPath) {
         printClickedCoords();
      }
   }
   
   @Override
   public void paintComponent(Graphics g) {
      // If this is called again while it is drawing, just do nothing, an update will be soon enough
      // and dropping frames is probably the best way to handle the drawing if it starts to lag
      if(!drawingFlag) {
         long beginTime = System.nanoTime();
         
         drawingFlag = true;
         
         if(drawablesToDraw != null) {
            // First set the fields to null, so this particular update isn't drawn multiple times,
            // but if an update is scheduled while it's redrawing, that it will get drawn
            List<Drawable> drawables = drawablesToDraw;
            drawablesToDraw = null;
            DebugStats debugStats = debugStatsToDraw;
            debugStatsToDraw = null;
            drawUpdate(drawables, debugStats);
         }
         
         g.drawImage(buffer, 0, 0, null); // Always draw the buffer on screen
         
         drawingFlag = false;
         
         lastPaintTime = System.nanoTime() - beginTime;
      }
   }
   
   public void displayText(String... lines) {
      textDisplay.displayText(lines);
   }
   
   public void removeText() {
      textDisplay.clear();
   }
   
   public void restart() {
      gameOver = null;
      removeText();
      repaint();
   }
   
   public void signalGameOver(int highScorePosition) {
      if(gameOver == null) {
         gameOver = new GameOver(highScorePosition);
      }
   }
   
   public long redraw(List<Drawable> drawables, DebugStats debugStats) {
      if(gameOver == null) {
         Collections.sort(drawables, new Comparator<Drawable>() {
            @Override
            public int compare(Drawable d1, Drawable d2) {
               return d1.getZ().ordinal() - d2.getZ().ordinal();
            }
         });
         this.drawablesToDraw = drawables;
         this.debugStatsToDraw = debugStats;
      } else {
         // Game over needs to always draw on the same buffer for its sliding effect
         Graphics2D g = buffer.createGraphics();
         gameOver.draw(g);
         g.dispose();
      }
      repaint();
      return lastPaintTime;
   }
   
   private void drawUpdate(List<Drawable> drawables, DebugStats debugStats) {
      Graphics2D g = buffer.createGraphics();
      // The default value for alpha interpolation causes significant lag
      g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
            RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
      
      // This should completely cover the old image in the buffer
      g.drawImage(backgroundImage, 0, 0, null);
      for(Drawable d : drawables) {
         // These should never be null, but occasionally, I think due to threading, they are
         if(d != null) {
            d.draw(g);
         }
      }
      drawDebug(g, debugStats);
      textDisplay.draw(g);
      
      g.dispose();
   }
   
   private void drawDebug(Graphics2D g, DebugStats debugStats) {
      if(debugTimes) {
         g.setColor(Color.WHITE);
         g.drawString("Process time: " + debugStats.processTime, 10, 15);
         g.drawString("Draw time: " + debugStats.drawTime, 10, 30);
         g.drawString("Creeps time: " + debugStats.processCreepsTime, 150, 15);
         g.drawString("Bullets time: " + debugStats.processBulletsTime, 150, 30);
         g.drawString("Towers time: " + debugStats.processTowersTime, 150, 45);
         g.drawString("Num bullets: " + debugStats.numBullets, 400, 15);
      }
      if(debugPath) {
         drawPath(g);
         drawPathOutline(g);
         drawPathBounds(g);
      }
   }
   
   /**
    * Test method that draws in each point on the path and links them up.
    */
   private void drawPath(Graphics2D g) {
      g.setColor(Color.BLACK);
      Point lastPoint = null;
      for(Point p : gameMap.getPathPoints()) {
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
   private void drawPathOutline(Graphics2D g) {
      for(Polygon p : gameMap.getPath()) {
         g.setColor(Color.RED);
         g.drawPolygon(p);
         g.setColor(new Color(0, 0, 0, 90));
         g.fillPolygon(p);
      }
   }
   
   private void drawPathBounds(Graphics2D g) {
      g.setColor(new Color(0, 0, 255, 30));
      for(Shape s : gameMap.getPathBounds()) {
         g.fill(s);
      }
      g.setColor(new Color(0, 0, 255, 100));
      for(Shape s : gameMap.getPathBounds()) {
         g.draw(s);
      }
   }
   
   /**
    * Test method that prints the coords of a mouse click.
    */
   private void printClickedCoords() {
      addMouseListener(new MouseAdapter(){
         @Override
         public void mouseClicked(MouseEvent e) {
            System.out.println("<point x=\"" + e.getX() + "\" y=\"" + e.getY() + "\" />");
         }
      });
   }
   
   public static class DebugStats {
      final long processTime, processCreepsTime, processBulletsTime, processTowersTime, drawTime;
      final int numBullets;
      
      public DebugStats(long processTime, long processCreepsTime, long processBulletsTime,
            long processTowersTime, long drawTime, int numBullets) {
         this.processTime = processTime;
         this.processCreepsTime = processCreepsTime;
         this.processBulletsTime = processBulletsTime;
         this.processTowersTime = processTowersTime;
         this.drawTime = drawTime;
         this.numBullets = numBullets;
      }
      
   }
   
   /**
    * Draws game over screen for a loss
    * 
    * @author Liam Byrne
    *
    */
   private class GameOver {
      
      private final Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
      private final Font font = new Font(Font.SANS_SERIF, Font.BOLD, 30);
      private final int deltaY;
      private final BufferedImage img;
      private int currentX, currentY, iterations, numTimes;
      private int highScorePosition;
      
      private GameOver(int highScorePosition) {
         this.highScorePosition = highScorePosition;
         deltaY = 1;
         img = ImageHelper.loadImage((int)(getWidth()*0.75), (int)(getHeight()*0.2), "other",
               "GameOver.png");
         double mult = 1.2;
         currentY = (int)(img.getHeight() * mult);
         numTimes = (int)(((getHeight() * 0.75) - img.getHeight() * mult - currentY)/deltaY);
         currentX = getWidth()/2 - img.getWidth()/2;
      }
      
      private void draw(Graphics2D g) {
         if(iterations > numTimes) {
            return;
         }
         currentY += deltaY;
         iterations++;
         
         drawHighScore(g, highScorePosition);
         
         // Makes it so what is drawn is partly transparent
         g.setComposite(comp);
         g.drawImage(img, currentX, currentY, null);
      }
      
      private void drawHighScore(Graphics2D g, int highScorePosition) {
         if (highScorePosition <= 0) {
            return;
         }
         
         String text;
         if(highScorePosition == 1) {
            text = "Top High Score!";
         }
         else {
            text = highScorePosition + Helper.getNumberSuffix(highScorePosition) + " Highest Score";
         }
         
         g.setColor(Color.ORANGE);
         g.setFont(font);
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         g.drawString(text, 50, 50);
         
         highScorePosition = 0; // Only draw this once
      }
   }

   private static class TextDisplay {
      
      private static final Color backgroundColour = Color.WHITE;
      private static final Color textColour = Color.BLACK;
      private static final Font font = new Font(Font.SANS_SERIF, Font.BOLD, 14);
      private static final Composite composite =
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5F);
      private final int startPosition;
      private int currentPosition;
      private int heightToDisplay;
      // The amount of rounding on the corners
      private static final int rounding = 40;
      private static final int aboveTextMargin = 7;
      private static final int belowTextMargin = 10;
      // The margin on the side from the left of the TextDisplay (not the screen)
      private static final int sideMargin = 12;
      // The offset from the side of the screen for the TextDisplay
      private static final int offset = 3;
      // The height of the screen
      private final int screenHeight;
      private final BufferedImage image;
      private boolean isOnDisplay = false;
      private boolean goingUp;
      
      /**
       * Creates a new TextDisplay for a screen with the specified width and height
       * 
       * @param width
       * @param height
       */
      public TextDisplay(int width, int height) {
         // It starts at the bottom of the screen
         startPosition = height;
         currentPosition = startPosition;
         this.screenHeight = height;
         image = new BufferedImage(width - offset * 2, height, BufferedImage.TYPE_INT_ARGB_PRE);
      }
      
      public void draw(Graphics2D g) {
         if(!isOnDisplay) {
            return;
         }
         adjustImagePosition();
         // Only draw if it's still on display
         if(isOnDisplay) {
            // Use a composite to make it translucent, save the previous composite to restore later
            Composite c = g.getComposite();
            g.setComposite(composite);
            g.drawImage(image, offset, currentPosition, null);
            g.setComposite(c);
         }
      }
      
      public void displayText(String... lines) {
         if(isOnDisplay) { // If it's already (partly) showing, make it start again
            currentPosition = startPosition;
         }
         drawImage(lines);
         goingUp = true;
         isOnDisplay = true;
      }
      
      public void clear() {
         heightToDisplay = 0;
         goingUp = false;
      }
      
      private void adjustImagePosition() {
         // Adjust the position of the display
         if(goingUp) {
            // Decrement the current position until it reaches the full height on display
            if(currentPosition > startPosition - heightToDisplay) {
               currentPosition--;
            }
         } else {
            currentPosition++;
            // When it has gone off screen, reset currentPosition and set it so it's not on display
            if(currentPosition >= startPosition) {
               currentPosition = startPosition;
               isOnDisplay = false;
            }
         }
      }
      
      private void drawImage(String[] lines) {
         ImageHelper.clearImage(image);
         Graphics2D g = image.createGraphics();
         
         // Draw the background
         g.setColor(backgroundColour);
         g.fillRoundRect(0, 0, image.getWidth(), screenHeight, rounding, rounding);
         
         // Use the same font, just change the font size
         g.setFont(font);
         // Draw the text on top
         int lineHeight = g.getFontMetrics().getHeight() + aboveTextMargin;
         g.setColor(textColour);
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         for(String s : lines) {
            heightToDisplay += lineHeight;
            g.drawString(s, sideMargin, heightToDisplay);
         }
         
         g.dispose();
         
         heightToDisplay += belowTextMargin;
      }
      
   }

}
