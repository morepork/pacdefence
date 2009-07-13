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
 *  (C) Liam Byrne, 2008 - 09.
 */

package website;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Creates web pages based on master.html and *.part.html.
 * 
 * The new file will have the same name as the x.part.html except will be called x.html, and will
 * have the contents of the master file and the contents of the particular file.
 * 
 * This is handy for putting common code into multiple pages without having to copy and paste it all
 * and will be useful for when I make changes.
 * 
 * @author Liam Byrne
 *
 */
public class WebPageCreator {
   
   // This assumes the webpages have nothing that needs to be there besides the head and the body
   
   public static void main(String... args) {
      try {
         Scanner master = new Scanner(WebPageCreator.class.getResourceAsStream("master.html"));
         List<String> head = parseHead(master);
         List<String> body = parseBody(master);
         master.close();
         
         // Gets the directory this class is in
         File dir = new File(WebPageCreator.class.getResource(".").toURI());
         for(String s : dir.list()) {
            if(s.contains(".part.")) {
               Scanner part = new Scanner(WebPageCreator.class.getResourceAsStream(s));
               List<String> partHead = parseHead(part);
               partHead.addAll(0, head);
               List<String> partBody = parseBody(part);
               partBody.addAll(0, body);
               part.close();
               
               String newFileName = s.replace(".part.", ".");
               
               boldNavbarLinkToCurrentPage(newFileName, partBody);
               
               PrintStream ps = new PrintStream(new File(dir, newFileName));
               writeHTML(ps, partHead, partBody);
               ps.close();
               System.out.println("Wrote " + newFileName);
            }
         }
      } catch(URISyntaxException e) { // These shouldn't happen so just re-throw them
         throw new RuntimeException(e);
      } catch(FileNotFoundException e) {
         throw new RuntimeException(e);
      }
   }
   
   private static void boldNavbarLinkToCurrentPage(String fileName, List<String> body) {
      for(int i = 0; i < body.size(); i++) {
         String line = body.get(i);
         if(line.contains("<li><a href=\"" + fileName + "\">")) {
            // Put the opening <b> tag after the end of the link
            line = line.replace(".html\">", ".html\"><b>");
            // and put the closing </b> tag just before the end of the link
            line = line.replace("</a>", "</b></a>");
            // Then remove the old line, and add the new one in its place
            body.remove(i);
            body.add(i, line);
            return;
         }
      }
   }
   
   private static void writeHTML(PrintStream ps, List<String> head, List<String> body) {
      ps.println("<!DOCTYPE html>");
      ps.println("<html>");
      ps.println();
      ps.println("<head>");
      for(String s : head) {
         ps.println(s);
      }
      ps.println("</head>");
      ps.println();
      ps.println("<body>");
      for(String s : body) {
         ps.println(s);
      }
      ps.println("</body>");
      ps.println();
      ps.println("</html>");
      ps.println();
   }
   
   private static List<String> parseHead(Scanner scan) {
      return parseSection(scan, "<head>", "</head>");
   }
   
   private static List<String> parseBody(Scanner scan) {
      return parseSection(scan, "<body>", "</body>");
   }
   
   private static List<String> parseSection(Scanner scan, String start, String end) {
      List<String> strings = new ArrayList<String>();
      String line = scan.nextLine();
      // This assumes the tag will be on its own line, so make sure you do that...
      while(!line.trim().equalsIgnoreCase(start)) {
         line = scan.nextLine();
      }
      // Skip the start string
      line = scan.nextLine();
      while(!line.trim().equalsIgnoreCase(end)) {
         strings.add(line);
         line = scan.nextLine();
      }
      return strings;
   }

}
