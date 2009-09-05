/*
 * This file is part of Pac Defence.
 * 
 * Pac Defence is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Pac Defence is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Pac Defence. If not,
 * see <http://www.gnu.org/licenses/>.
 * 
 * (C) Liam Byrne, 2008 - 09.
 */

package gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import logic.HighScores;


public class HighScoresDialog {
   
   private HighScoresDialog() {} // This class shouldn't be instantiated

   public static void showHighScoresDialog(JComponent c) {
      try {
         Map<String, List<Integer>> highScores = HighScores.getHighScores();

         JDialog dialog = createDialog(highScores);

         dialog.pack();

         // Centre the dialog in the title
         dialog.setLocation(c.getX() + (c.getWidth() - dialog.getWidth()) / 2, c.getY() +
               (c.getHeight() - dialog.getHeight()) / 2);
         dialog.setLocationRelativeTo(c);

         dialog.setVisible(true);
         
      } catch(BackingStoreException e) {
         // Print the stack trace, and pop up an error, then continue
         e.printStackTrace();
         JOptionPane.showMessageDialog(null,
               "There was an error when trying to save/load the high scores",
               "Error", JOptionPane.ERROR_MESSAGE);
      }
   }

   private static JDialog createDialog(Map<String, List<Integer>> highScores) {
      JDialog dialog = new JDialog();
      dialog.setTitle("High Scores");

      JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);

      // Need to sort the entries so they show up in a nice order
      List<String> maps = new ArrayList<String>(highScores.keySet());
      Collections.sort(maps, new LevelOrderComparator());

      for(String s : maps) {
         tabbedPane.addTab(s, createTab(highScores.get(s)));
      }

      dialog.add(tabbedPane);

      return dialog;
   }

   private static JComponent createTab(List<Integer> scores) {
      Box tab = Box.createVerticalBox();

      tab.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

      Box header = Box.createHorizontalBox();

      header.add(new JLabel("Rank"));
      header.add(Box.createHorizontalGlue()); // So the headers are always at the edges
      header.add(Box.createHorizontalStrut(50)); // So there's always a decent enough gap
      header.add(new JLabel("Level"));

      tab.add(header);
      tab.add(Box.createVerticalStrut(2)); // Have a bit of an extra gap after the header

      for(int i = 0; i < HighScores.NUM_HIGH_SCORES; i++) {
         tab.add(Box.createVerticalStrut(3)); // Have a gap between this one and the previous one
         
         String level = i < scores.size() ? String.valueOf(scores.get(i)) : " ";

         tab.add(SwingHelper.createLeftRightPanel(new MyJLabel(i + 1), new JLabel(level)));
      }

      return tab;
   }

   private static class LevelOrderComparator implements Comparator<String> {

      @Override
      public int compare(String s1, String s2) {
         String s1Start = s1.substring(0, s1.lastIndexOf(" "));
         String s1End = s1.substring(s1.lastIndexOf(" ") + 1);

         String s2Start = s2.substring(0, s2.lastIndexOf(" "));
         String s2End = s2.substring(s2.lastIndexOf(" ") + 1);

         // The ends should be one of "Easy" "Medium" or "Hard"
         if(s1Start.equals(s2Start)) {
            if(s1End.equals("Easy")) {
               return -1;
            } else if(s2End.equals("Easy")) {
               return 1;
            } else if(s1End.equals("Medium")) {
               return -1;
            } else if(s2End.equals("Medium")) {
               return 1;
            }
         }
         
         return s1.compareTo(s2);
      }

   }

}
