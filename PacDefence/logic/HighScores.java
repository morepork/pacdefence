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

package logic;

import gui.Applet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class HighScores {
   
   // Note, these don't work if running from an Applet, so I check if it's an applet, and if so
   // return a value signalling an error, or that nothing happened
   
   // The number of high scores to keep for each level
   public static final int NUM_HIGH_SCORES = 10;
   
   private static final Preferences highScoresNode;
   
   static {
      if(Applet.isApplet()) {
         highScoresNode = null;
      } else {
         highScoresNode = Preferences.userRoot().node("PacDefence/HighScores");
      }
   }
   
   public static Map<String, List<Integer>> getHighScores() throws BackingStoreException {
      // Note that this method should never be called from an applet, as the show high scores
      // button isn't there and addScore() returns false straight away
      if(Applet.isApplet()) {
         return null;
      }
      
      Map<String, List<Integer>> highScores = new HashMap<String, List<Integer>>();
      
      
      for(String mapName : highScoresNode.childrenNames()) {
         highScores.put(mapName, getHighScores(mapName));
      }
      
      return highScores;
   }
   
   public static List<Integer> getHighScores(String mapName) throws BackingStoreException {
      // Note that this method should never be called from an applet, as the show high scores
      // button isn't there and addScore() returns false straight away
      if(Applet.isApplet()) {
         return null;
      }
      
      if(!highScoresNode.nodeExists(mapName)) {
         return new ArrayList<Integer>(); // Return an empty list if the node doesn't exist
      }
      
      Preferences mapNode = highScoresNode.node(mapName);
      List<Integer> scores = new ArrayList<Integer>();
      
      int i = 1; // Start at the top high score
      int level = mapNode.getInt(String.valueOf(i), -1);
      
      while(level != -1) { // While this node exists, add the score to the list
         scores.add(level);
         
         level = mapNode.getInt(String.valueOf(++i), -1);
      }
      
      return scores;
   }
   
   /**
    * Add the score for this map to the high scores list, if it's high enough.
    * 
    * Returns true if the score was added, false if it wasn't good enough to be added.
    */
   public static boolean addScore(String mapName, int levelReached) throws BackingStoreException {
      if(Applet.isApplet()) {
         return false; // If an applet, return false so nothing special happens
      }
      
      List<Integer> currentScores = getHighScores(mapName);
      
      int numHigher = 0; // The number of scores higher than this one
      for(int i : currentScores) {
         if(i >= levelReached) {
            numHigher++;
         }
      }
      
      if(numHigher >= NUM_HIGH_SCORES) {
         return false; // If the table is full, and all the scores are higher than this, do nothing
      }
      
      currentScores.add(numHigher, levelReached); // Add this score in the correct place
      
      // Just put everything back in, overwriting the higher scores
      Preferences mapNode = highScoresNode.node(mapName);
      for(int i = 0; i < Math.min(NUM_HIGH_SCORES, currentScores.size()); i++) {
         // Remember 0th position in the list corresponds to a rank of 1, etc.
         mapNode.putInt(String.valueOf(i + 1), currentScores.get(i));
      }
      
      return true;
   }

}
