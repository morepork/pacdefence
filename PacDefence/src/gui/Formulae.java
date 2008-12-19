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

import java.util.ArrayList;
import java.util.List;


public class Formulae {
   
   private final static List<Double> moneyDivisors = new ArrayList<Double>();
   
   public static int numSprites(int level) {
      return 20 + 2 * (level - 1);
   }
   
   public static long hp(int level) {
      return (long)(Math.pow(1.5, level - 1) * 10);
   }
   
   public static int levelEndBonus(int level) {
      return 1000 + 100 * (level - 1);
   }
   
   public static int noEnemiesThroughBonus(int level) {
      return level * 100;
   }
   
   public static long upgradeCost(int currentLevel) {
      return (long)(Math.pow(1.25, currentLevel - 1) * 100);
   }
   
   public static double damageDollars(double hpLost, double hpFactor, int level) {
      return hpFactor * hpLost / getMoneyDivisor(level);
   }
   
   public static double killBonus(long levelHP, int level) {
      return levelHP / getMoneyDivisor(level);
   }
   
   private static double getMoneyDivisor(int level) {
      // Keep a list of these as this is called every time a bullet hits.
      level--;
      if(level >= moneyDivisors.size()) {
         moneyDivisors.add(Math.pow(1.15, level));
      }
      return moneyDivisors.get(level);
   }
   
   public static int nextUpgradeKills(int currentLevel) {
      return currentLevel * currentLevel * 10;
   }
   
   public static long nextUpgradeDamage(int currentLevel) {
      return (long)(Math.pow(2, currentLevel - 1)) * 250;
   }
   
   public static int towerCost(int numTowers, int numOfThatType) {
      return (int)(Math.pow(1.05, numTowers) * Math.pow(1.1, numOfThatType) * 1000);
   }

}
