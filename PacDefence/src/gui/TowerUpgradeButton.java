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

import javax.swing.JButton;
import javax.swing.plaf.metal.MetalButtonUI;

@SuppressWarnings("serial")
public class TowerUpgradeButton extends JButton {
   
   private final Color textColour;
   
   public TowerUpgradeButton(Color textColour, float textSize) {
      super();
      this.textColour = textColour;
      setFont(getFont().deriveFont(textSize));
      setForeground(textColour);
      setOpaque(false);
      setContentAreaFilled(false);
      setUI();     
   }
   
   private void setUI() {
      // Makes it so the disabled text colour is the same as the normal one
      // There may be a better way of doing this
      setUI(new MetalButtonUI(){
         @Override
         public Color getDisabledTextColor() {
            return textColour;
         }
      });
   }

}
