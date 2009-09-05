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

package gui;

import javax.swing.JLabel;

@SuppressWarnings("serial")
public class MyJLabel extends JLabel {
   // This class just makes it easier to set the values of the text fields as I
   // can just pass a number instead of changing them to Strings all the time
   public MyJLabel() {
      super();
   }
   
   public MyJLabel(long l) {
      super(String.valueOf(l));
   }
   
   public MyJLabel(String text) {
      super(text);
   }
   
   public void setFontSize(float size) {
      setFont(getFont().deriveFont(size));
   }
   
   public void setFontStyle(int style) {
      setFont(getFont().deriveFont(style));
   }
   
   public void setText(int i) {
      setText(String.valueOf(i));
   }
   
   public void setText(long l) {
      setText(String.valueOf(l));
   }
   
   public void setText(double d){
      setText(String.valueOf(d));
   }
}
