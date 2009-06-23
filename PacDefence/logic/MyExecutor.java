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

package logic;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MyExecutor {

   public static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();

   private static final ExecutorService executorService =
         Executors.newFixedThreadPool(NUM_PROCESSORS);

   public static <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
      try {
         return executorService.invokeAll(tasks);
      } catch(InterruptedException e) {
         // Should never happen
         throw new RuntimeException(e);
      }
   }

}
