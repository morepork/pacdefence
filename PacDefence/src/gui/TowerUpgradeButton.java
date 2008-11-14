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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

@SuppressWarnings("serial")
public class TowerUpgradeButton extends JButton {
   
   public TowerUpgradeButton(String label, Color textColour) {
      super(label);
      setForeground(textColour);
      setOpaque(false);
      setContentAreaFilled(false);
      setEnabled(false);
      setUI();
   }
   
   @Override
   public void setEnabled(boolean b) {
      super.setEnabled(b);
      setBorderPainted(b);
   }
   
   private void setUI() {
      // Poor, but working means of setting the disabled text colour to be the
      // same as the enabled text colour.
      setUI(new BasicButtonUI(){
         @Override
         public void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
            boolean wasEnabled = b.isEnabled();
            b.setEnabled(true);
            super.paintText(g, b, textRect, text);
            b.setEnabled(wasEnabled);
         }
      });
   }

}
