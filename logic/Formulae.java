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
 *  (C) Liam Byrne, 2008 - 2012.
 */

package logic;

import towers.Tower;
import towers.Tower.Attribute;

public class Formulae {

  public static int numCreeps(int level) {
    return 20 + 4 * (level - 1);
  }

  public static int ticksBetweenAddCreep(int level) {
    int ticks = 40;
    for (int i = 1; i < level; i++) {
      ticks *= 0.95;
    }
    return ticks > 1 ? ticks : 1;
  }

  public static long hp(int level) {
    return (long) (Math.pow(1.5, level - 1) * 10);
  }

  public static int levelEndBonus(int level) {
    return 1000 + 100 * (level - 1);
  }

  public static int noEnemiesThroughBonus(int level) {
    return 100 * level;
  }

  public static long upgradeCost(int currentLevel) {
    return (long) (Math.pow(1.25, currentLevel - 1) * 100);
  }

  public static double damageDollars(double hpLost, double hpFactor, int level) {
    return hpFactor * hpLost / getMoneyDivisor(level);
  }

  public static double killBonus(long levelHP, int level) {
    return levelHP / getMoneyDivisor(level);
  }

  private static double getMoneyDivisor(int level) {
    return Math.pow(1.15, level - 1);
  }

  public static int nextUpgradeKills(int currentLevel) {
    return currentLevel * currentLevel * 10;
  }

  public static long nextUpgradeDamage(int currentLevel) {
    return (long) (Math.pow(2, currentLevel - 1)) * 250;
  }

  public static long towerCost(int numTowers, int numOfThisType) {
    return (long) (Math.pow(1.05, numTowers) * Math.pow(1.1, numOfThisType) * 1000);
  }

  public static long sellValue(Tower t, int numTowers, int numOfThisType) {
    double value = towerCost(numTowers - 1, numOfThisType - 1);
    for (int i = 1; i < t.getExperienceReport().level; i++) {
      value *= 1.1;
    }
    for (Attribute a : Attribute.values()) {
      for (int i = 1; i < t.getAttributeLevel(a); i++) {
        value += upgradeCost(i);
      }
    }
    return (long) (value * 0.9);
  }
}
