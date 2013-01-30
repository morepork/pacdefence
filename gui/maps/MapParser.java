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

package gui.maps;

import images.ImageHelper;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import logic.Constants;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;


public class MapParser {
   
   private static final DocumentBuilder domBuilder = createDocumentBuilder();
   
   // This class should never be instantiated
   private MapParser(){}
   
   public static GameMap parse(String fileName) {
      try {
         InputStream stream = MapParser.class.getResourceAsStream(fileName);
         Document document;
         // DocumentBuilder.parse isn't thread-safe, so synchronise it
         synchronized(MapParser.class) {
            document = domBuilder.parse(stream);
         }
         stream.close();
         removeWhitespaceNodes(document);
         // The map should be the only node in the document
         return parseMap(document.getChildNodes().item(0));
      } catch (SAXException e) { // These should hopefully never be thrown
         System.err.println(e.getMessage());
         throw new RuntimeException(e);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
   
   private static GameMap parseMap(Node map) {
      NodeList children = map.getChildNodes();
      String description = null;
      BufferedImage image = null;
      List<Point> pathPoints = null;
      List<Polygon> path = null;
      List<Shape> pathBounds = null;
      for(int i = 0; i < children.getLength(); i++) {
         Node n = children.item(i);
         String name = n.getNodeName();
         if(name.equals("description")) {
            description = parseDescription(n);
         } else if(name.equals("images")) {
            image = parseImages(n);
         } else if(name.equals("pathPoints")) {
            pathPoints = parsePathPoints(n);
         } else if(name.equals("path")) {
            path = parsePath(n);
         } else if(name.equals("pathBounds")) {
            pathBounds = parsePathBounds(n);
         }
      }
      return new GameMap(description, pathPoints, path, pathBounds, image);
   }
   
   private static String parseDescription(Node n) {
      return n.getChildNodes().item(0).getNodeValue();
   }
   
   private static BufferedImage parseImages(Node n) {
      NodeList children = n.getChildNodes();
      // Make a blank image of this type so all colours can be shown
      BufferedImage image = new BufferedImage(Constants.MAP_WIDTH, Constants.MAP_HEIGHT,
            BufferedImage.TYPE_INT_ARGB_PRE);
      Graphics2D g = image.createGraphics();
      for(int i = 0; i < children.getLength(); i++) {
         // Then draw each succesive image over the top
         g.drawImage(ImageHelper.loadImage("maps", children.item(i).getFirstChild().getNodeValue()),
               0, 0, null);
      }
      g.dispose();
      return image;
   }
   
   private static List<Point> parsePathPoints(Node n) {
      NodeList children = n.getChildNodes();
      List<Point> points = new ArrayList<Point>();
      for(int i = 0; i < children.getLength(); i++) {
         points.add(parsePoint(children.item(i)));
      }
      return points;
   }
   
   private static List<Polygon> parsePath(Node n) {
      NodeList children = n.getChildNodes();
      List<Polygon> polygons = new ArrayList<Polygon>();
      for(int i = 0; i < children.getLength(); i++) {
         polygons.add(parsePolygon(children.item(i)));
      }
      return polygons;
   }
   
   private static List<Shape> parsePathBounds(Node n) {
      NodeList children = n.getChildNodes();
      List<Shape> shapes = new ArrayList<Shape>();
      for(int i = 0; i < children.getLength(); i++) {
         Node child = children.item(i);
         if(child.getNodeName().equals("rectangle")) {
            shapes.add(parseRectangle(child));
         } else if(child.getNodeName().equals("polygon")) {
            shapes.add(parsePolygon(child));
         }
      }
      return shapes;
   }
   
   private static Point parsePoint(Node n) {
      NamedNodeMap map = n.getAttributes();
      int x = Integer.valueOf(map.getNamedItem("x").getNodeValue());
      int y = Integer.valueOf(map.getNamedItem("y").getNodeValue());
      return new Point(x, y);
   }
   
   private static Rectangle parseRectangle(Node n) {
      NamedNodeMap map = n.getAttributes();
      int x = Integer.valueOf(map.getNamedItem("x").getNodeValue());
      int y = Integer.valueOf(map.getNamedItem("y").getNodeValue());
      int width = Integer.valueOf(map.getNamedItem("width").getNodeValue());
      int height = Integer.valueOf(map.getNamedItem("height").getNodeValue());
      return new Rectangle(x, y, width, height);
   }
   
   private static Polygon parsePolygon(Node n) {
      NodeList children = n.getChildNodes();
      List<Point> points = new ArrayList<Point>();
      for(int i = 0; i < children.getLength(); i++) {
         points.add(parsePoint(children.item(i)));
      }
      int[] x = new int[points.size()], y = new int[points.size()];
      int i = 0;
      for(Point p : points) {
         x[i] = p.x;
         y[i++] = p.y;
      }
      return new Polygon(x, y, points.size());
   }
   
   private static DocumentBuilder createDocumentBuilder() {
      DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
      domBuilderFactory.setNamespaceAware(true);
      domBuilderFactory.setIgnoringComments(true);
      try {
         Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
               .newSchema(MapParser.class.getResource("map.xsd"));
         domBuilderFactory.setSchema(schema);
         DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
         return domBuilder;
      } catch (SAXException e) { // These Exceptions should hopefully never be thrown
         throw new RuntimeException(e);
      } catch (ParserConfigurationException e) {
         throw new RuntimeException(e);
      }
   }
   
   private static void removeWhitespaceNodes(Node node) {
      NodeList children = node.getChildNodes();
      // Count down as nodes are being removed, so all nodes are covered
      for (int i = children.getLength() - 1; i >= 0 ; i--) {
         Node child = children.item(i);
         if (child instanceof Text && ((Text) child).getData().trim().length() == 0) {
            node.removeChild(child);
         } else if (child instanceof Node) {
            removeWhitespaceNodes(child);
         }
      }
   }
   
   public static class GameMap {

      private final String description;
      private final List<Point> pathPoints;
      private final List<Polygon> path;
      private final List<Shape> pathBounds;
      private final BufferedImage image;
      
      public GameMap(String description, List<Point> pathPoints, List<Polygon> path,
            List<Shape> pathBounds, BufferedImage image) {
         this.description = description;
         this.pathPoints = Collections.unmodifiableList(pathPoints);
         this.path = Collections.unmodifiableList(path);
         this.pathBounds = Collections.unmodifiableList(pathBounds);
         this.image = image;
      }
      
      public String getDescription() {
         return description;
      }
      
      public List<Point> getPathPoints() {
         return pathPoints;
      }
      
      public List<Polygon> getPath() {
         return path;
      }
      
      public List<Shape> getPathBounds() {
         return pathBounds;
      }
      
      public BufferedImage getImage() {
         return image;
      }
      
   }
}
