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
 * (C) Liam Byrne, 2008 - 10.
 */

package logic;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MyExecutor {

   private static int numThreads;
   
   private static ExecutorService executorService;
   
   public static void setNumThreads(int num) {
      if(num <= 0) {
         throw new IllegalArgumentException("Number of threads must be >= 0.");
      }
      if(numThreads != 0) {
         throw new IllegalStateException("The number of threads has already been set.");
      }
      numThreads = num;
   }
   
   public static void initialiseExecutor() {
      if(executorService != null) {
         throw new IllegalStateException("The ExecutorService has already been initialised.");
      }
      // If it hasn't been explicitly set, use the number of processors
      if(numThreads == 0) {
         numThreads = Runtime.getRuntime().availableProcessors();
      }
      // Only make the ExecutorService if more than one thread is being used
      if(numThreads > 1) {
         System.out.println("Using " + numThreads + " threads.");
         executorService = Executors.newFixedThreadPool(numThreads);
      }
   }
   
   public static boolean singleThreaded() {
      return numThreads == 1;
   }
   
   public static int getNumThreads() {
      return numThreads;
   }
   
   public static void terminateExecutor() {
      if(executorService == null || executorService.isTerminated()) { // Nothing to terminate
         return;
      }
      try {
         executorService.shutdown();
         try {
            // Half a second should be long enough
            executorService.awaitTermination(500, TimeUnit.MILLISECONDS);
         } catch(InterruptedException e) { }
         // If it hasn't terminated yet, brutally shut it down
         if(!executorService.isTerminated()) {
            executorService.shutdownNow();
         }
      } catch(SecurityException e) {
         // This gets thrown when run from an applet (possibly at other times too)
         e.printStackTrace();
      }
      // If a security exception was thrown, lets just hope that its current tasks finish and
      // the gc is able to take care of it...
      executorService = null;
   }
   
   public static <T> Future<T> submit(Callable<T> task) {
      return executorService.submit(task);
   }

   public static <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
      try {
         return executorService.invokeAll(tasks);
      } catch(InterruptedException e) {
         // Should never happen
         throw new RuntimeException(e);
      }
   }

}
