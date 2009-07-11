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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Skin {
   
   // The first one in this array is the default
   private static final Skin[] skins = new Skin[]{
         new Skin("New", "new"),
         new Skin("Original", null)
   };
   
   private final String name, directory;

   public Skin(String name, String directory) {
      this.name = name;
      this.directory = directory;
   }

   public String getDirectory() {
      return directory;
   }
   
   @Override
   public String toString() {
      return name;
   }
   
   public static List<Skin> getSkins() {
     return Collections.unmodifiableList(Arrays.asList(skins));
   }

}
