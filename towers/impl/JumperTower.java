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

package towers.impl;

import creeps.Creep;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;
import logic.CreepGrid;
import towers.AbstractTower;
import towers.BasicBullet;
import towers.Bullet;
import towers.Tower;
import util.Circle;
import util.Vector2D;

public class JumperTower extends AbstractTower {

  private static final int jumpRange = 50;

  private int jumps = 1;

  public JumperTower(Point p) {
    super(p, "Jumper", 40, 100, 5, 5, 50, 20, true);
  }

  @Override
  public String getSpecial() {
    return String.valueOf(jumps);
  }

  @Override
  public String getSpecialName() {
    return "Jumps";
  }

  @Override
  protected Bullet makeBullet(
      Vector2D dir, int turretWidth, int range, double speed, double damage, Point p, Creep c) {
    return new JumpingBullet(this, dir, turretWidth, range, speed, damage, p, jumps);
  }

  @Override
  protected void upgradeSpecial() {
    jumps++;
  }

  private class JumpingBullet extends BasicBullet {

    private Creep lastHit;
    private int jumpsLeft;
    private int moneyEarned = 0;

    public JumpingBullet(
        Tower shotBy,
        Vector2D dir,
        int turretWidth,
        int range,
        double speed,
        double damage,
        Point p,
        int jumps) {
      super(shotBy, dir, turretWidth, range, speed, damage, p);
      jumpsLeft = jumps;
    }

    @Override
    protected void specialOnHit(Point2D p, Creep hitCreep, CreepGrid creeps) {
      // If there are jumps left, target the closest creep, so the bullet jumps to it
      if (jumpsLeft > 0) {
        Creep closest = null;
        double closestDistanceSq = Double.MAX_VALUE;
        // Include the creep hit on the previous jump, so it can be targeted again
        if (lastHit != null && lastHit.isAlive()) {
          closest = lastHit;
          closestDistanceSq = p.distanceSq(lastHit.getPosition());
        }
        Circle jumpBounds = new Circle(p, jumpRange);
        for (Creep c : creeps.filter(jumpBounds)) {
          if (c.equals(hitCreep)) { // Can't target the creep that was just hit
            continue;
          }
          double distanceSq = p.distanceSq(c.getPosition());
          if (closest == null || distanceSq < closestDistanceSq) {
            closest = c;
            closestDistanceSq = distanceSq;
          }
        }
        if (closest != null && checkDistance(closest, p, jumpRange)) {
          super.setDirection(Vector2D.createFromPoints(p, closest.getPosition()));
          distanceTravelled = range - jumpRange;
        } else {
          // If no creeps in range, finish up
          jumpsLeft = 0;
        }
      }
      lastHit = hitCreep;
      jumpsLeft--;
    }

    @Override
    protected double doTick(CreepGrid creeps) {
      // Remove the last hit creep so that it won't get hit again
      double result = super.doTick(creeps.excluding(Arrays.asList(lastHit)));
      if (result < 0) {
        // Bullet didn't hit anything
        return result;
      } else if (result == 0) {
        // Bullet has reached the edge of its range
        return moneyEarned;
      } else {
        // Bullet hit something
        moneyEarned += result;
        // (jumpsLeft == 0) means the bullet has just done its final jump,
        // so wait until it hits something before finishing it
        if (jumpsLeft < 0) {
          return moneyEarned;
        } else {
          return -1;
        }
      }
    }
  }
}
