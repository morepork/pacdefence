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

package sprites;

import gui.GameMap;
import images.ImageHelper;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

public class Pacman extends AbstractSprite {

   private static final int width = GameMap.PATH_WIDTH * 2 - 10;
   private static final BufferedImage image = ImageHelper.makeImage("sprites",
         "pacman_yellow.png");
   
   public Pacman(int hp, List<Point> path) {
      super(width, image, hp, path);
   }

}
