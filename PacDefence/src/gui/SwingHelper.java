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

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;


public class SwingHelper {
   
   public static JPanel createJPanel() {
      JPanel panel = new JPanel();
      panel.setOpaque(false);   
      return panel;
   }

   public static JPanel createBorderLayedOutJPanel() {
      JPanel panel = createJPanel();
      panel.setLayout(new BorderLayout());
      return panel;
   }

   public static JPanel createBorderLayedOutWrapperPanel(Component comp, String pos) {
      JPanel panel = SwingHelper.createBorderLayedOutJPanel();
      panel.add(comp, pos);
      return panel;
   }
   
   public static JPanel createLeftRightPanel(Component left, Component right) {
      JPanel panel = createBorderLayedOutJPanel();
      panel.add(left, BorderLayout.WEST);
      panel.add(right, BorderLayout.EAST);
      return panel;
   }

   /**
    * Creates an empty border with specified width.
    */
   public static Border createEmptyBorder(int width) {
      return BorderFactory.createEmptyBorder(width, width, width, width);
   }
   
   public static JPanel createWrapperPanel(JComponent c) {
      JPanel panel = new JPanel();
      panel.add(c);
      panel.setOpaque(false);
      return panel;
   }

   /**
    * Creates a panel that wraps this component and has a empty border around
    * it.
    */
   public static JPanel createWrapperPanel(JComponent c, int borderWidth) {
      JPanel panel = new JPanel();
      panel.add(c);
      panel.setOpaque(false);
      panel.setBorder(createEmptyBorder(borderWidth));
      return panel;
   }

}
