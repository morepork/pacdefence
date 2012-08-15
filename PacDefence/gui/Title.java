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
 *  (C) Liam Byrne, 2008 - 10.
 */

package gui;

import images.ImageHelper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import logic.Game;

@SuppressWarnings("serial")
public class Title extends JPanel {
   
   private static boolean firstRun = true;
   private static Skin selectedSkin;
   private JComboBox skinComboBox;
   private final BufferedImage background;
   
   public Title(int width, int height, ActionListener continueListener) {
      super(new BorderLayout());
      
      background = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
      Graphics2D g = background.createGraphics();
      g.drawImage(ImageHelper.loadImage(width, height, "other", "hoops.png"), 0, 0, null);
      g.drawImage(ImageHelper.loadImage(width, height, "other", "title.png"), 0, 0, null);
      
      add(createTopRow(), BorderLayout.NORTH);
      add(createBottomRow(continueListener), BorderLayout.SOUTH);
      
      setPreferredSize(new Dimension(width, height));
   }
   
   @Override
   public void paintComponent(Graphics g) {
      g.drawImage(background, 0, 0, null);
   }
   
   public Skin getSelectedSkin() {
      if(skinComboBox != null) {
         selectedSkin = (Skin) skinComboBox.getSelectedItem();
      }
      return selectedSkin;
   }
   
   private JComponent createTopRow() {
      JPanel topRow = new JPanel(new BorderLayout());
      topRow.setOpaque(false);
      if(firstRun) {
         firstRun = false;
         skinComboBox = new JComboBox(Skin.getSkins().toArray());
         // Only show the skin chooser on the first run, as otherwise every loaded, cached, etc.
         // image would be reloaded, which I think is way too much work
         topRow.add(createSkinSelector(), BorderLayout.WEST);
      } else {
         skinComboBox = null;
      }
      topRow.add(createLicenseInfo(), BorderLayout.EAST);
      return topRow;
   }
   
   private JComponent createBottomRow(ActionListener continueListener) {
      JPanel botRow = new JPanel(new BorderLayout());
      botRow.setOpaque(false);
      botRow.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

      if(!Applet.isApplet()) { // Don't show this button if it's an Applet, it won't work
         botRow.add(createHighScoresButton(), BorderLayout.WEST);
      }
      
      botRow.add(createContinueButton(continueListener), BorderLayout.EAST);
      
      return botRow;
   }
   
   private JComponent createContinueButton(ActionListener continueListener) {
      JButton continueButton = new OverlayButton("Continue", 50, 12, 12);
      continueButton.addActionListener(continueListener);
      
      return continueButton;
   }
   
   private JComponent createSkinSelector() {
      JLabel label = new JLabel("Skin:");
      label.setForeground(Color.YELLOW);

      JPanel panel = new JPanel();
      panel.setOpaque(false);
      
      panel.add(label);
      panel.add(skinComboBox);
      
      return panel;
   }
   
   private JComponent createHighScoresButton() {
      OverlayButton button = new OverlayButton("High Scores", 20F);
      
      final Title title = this;
      
      button.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            HighScoresDialog.showHighScoresDialog(title);
         }
      });
      
      return SwingHelper.createBorderLayedOutWrapperPanel(button, BorderLayout.SOUTH);
   }
   
   private JComponent createLicenseInfo() {
      JPanel panel = new JPanel();
      panel.setOpaque(false);
      
      MyJLabel noWarranty = new MyJLabel("This is free software with ABSOLUTELY NO WARRANTY.");
      noWarranty.setForeground(Color.YELLOW);
      noWarranty.setFontSize(16);
      
      JButton detailsButton = new OverlayButton("Details");
      
      detailsButton.addActionListener(createLicencePopUpDialogOnAction());
      
      panel.add(noWarranty);
      panel.add(detailsButton);
      return panel;
   }
   
   private ActionListener createLicencePopUpDialogOnAction() {
      final JPanel title = this;
      return new ActionListener() {
         private JDialog licenceDialog;
         @Override
         public void actionPerformed(ActionEvent e) {
            if(licenceDialog == null) {
               licenceDialog = createLicenceDialog();
            }
            if(licenceDialog.isVisible()) {  // If it's already showing, hide it
               licenceDialog.setVisible(false);
            } else {
               licenceDialog.pack();
               
               // Centre the dialog in the title
               licenceDialog.setLocation(title.getX() + (title.getWidth() - getWidth()) / 2,
                     title.getY() + (title.getHeight() - getHeight()) / 2);
               licenceDialog.setLocationRelativeTo(title);
               
               licenceDialog.setVisible(true);
            }
         }
         
         private JDialog createLicenceDialog() {
            JTextArea licence = new JTextArea(parseLicence()) {
               // Set the preferred viewport size so it fits nicely
               @Override
               public Dimension getPreferredScrollableViewportSize() {
                  Dimension d = getPreferredSize();
                  d.setSize(d.getWidth(), Game.HEIGHT - 100.0);
                  return d;
               }
            };
            int sideBorder = 5;
            licence.setBorder(BorderFactory.createEmptyBorder(1, sideBorder, 1, sideBorder));
            
            licenceDialog = new JDialog();
            licenceDialog.setTitle("Licence");
            licenceDialog.add(new JScrollPane(licence));
            
            return licenceDialog;
         }
      };
   }
   
   private String parseLicence() {
      Scanner scan = new Scanner(getClass().getResourceAsStream("/COPYING"));
      StringBuilder licence = new StringBuilder();
      while(scan.hasNextLine()) {
         licence.append(scan.nextLine());
         licence.append("\n");
      }
      return licence.toString();
   }

}
