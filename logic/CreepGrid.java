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
 *  (C) Liam Byrne, 2008 - 2026.
 */

package logic;

import creeps.Creep;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import util.Circle;
import util.Helper;

public class CreepGrid {

  // The distance over the edge of the map that cells are created.
  private static final int overflow = 100;

  private static final int nDivisions = 5;
  private static final int totalCells = nDivisions * nDivisions;
  // Width (horizontal and vertical) of each cell in the grid.
  private static final int cellWidth = (Constants.MAP_WIDTH + 2 * overflow) / nDivisions;

  private final List<Creep> creeps;
  private final List<Cell> cells;
  private final Map<BitSet, Collection<Creep>> combinedCellsCache = new HashMap<>();

  public CreepGrid(List<Creep> creeps) {
    this.creeps = Collections.unmodifiableList(creeps);

    this.cells = new ArrayList<>();

    for (int i = 0; i < nDivisions; i++) {
      for (int j = 0; j < nDivisions; j++) {
        Cell cell = new Cell(i, j, creeps);
        if (!cell.creeps.isEmpty()) {
          this.cells.add(cell);
        }
        // System.out.println(i + "," + j + " : " + cell.creeps.size());
      }
    }
  }

  private CreepGrid(CreepGrid base, Collection<Creep> excluding) {
    this.creeps = Collections.unmodifiableList(Helper.filter(base.creeps, excluding));

    this.cells = new ArrayList<>(base.cells.size());
    for (Cell cell : base.cells) {
      this.cells.add(new Cell(cell, excluding));
    }
  }

  public CreepGrid excluding(Collection<Creep> excluding) {
    return new CreepGrid(this, excluding);
  }

  public List<Creep> allCreeps() {
    return this.creeps;
  }

  public Collection<Creep> filter(Line2D line) {
    // In most cases the line will only intersect one cell
    List<Cell> intersectingCells = new ArrayList<>(this.cells.size());
    for (Cell cell : this.cells) {
      if (cell.bounds.intersectsLine(line)) {
        intersectingCells.add(cell);
      }
    }
    return combineIntersectingCells(intersectingCells);
  }

  public Collection<Creep> filter(Arc2D arc) {
    Rectangle2D arcBounds = arc.getBounds2D();
    List<Cell> intersectingCells = new ArrayList<>(this.cells.size());
    for (Cell cell : this.cells) {
      if (cell.bounds.intersects(arcBounds)) {
        intersectingCells.add(cell);
      }
    }
    return combineIntersectingCells(intersectingCells);
  }

  public Collection<Creep> filter(Circle circle) {
    List<Cell> intersectingCells = new ArrayList<>(this.cells.size());
    for (Cell cell : this.cells) {
      if (circle.intersects(cell.bounds)) {
        intersectingCells.add(cell);
      }
    }
    return combineIntersectingCells(intersectingCells);
  }

  public Collection<Creep> filter(List<Point2D> points) {
    List<Cell> intersectingCells = new ArrayList<>(this.cells.size());
    for (Cell cell : this.cells) {
      for (Point2D p : points) {
        if (cell.bounds.contains(p)) {
          intersectingCells.add(cell);
          break;
        }
      }
    }
    return combineIntersectingCells(intersectingCells);
  }

  private Collection<Creep> combineIntersectingCells(List<Cell> cells) {
    int size = cells.size();
    switch (cells.size()) {
      case 0:
        return Collections.emptyList();
      case 1:
        return cells.get(0).creeps;
      case totalCells:
        return this.creeps;
      default:
        BitSet bs = new BitSet(totalCells);
        for (Cell c : cells) {
          bs.set(c.i * nDivisions + c.j);
        }
        return this.combinedCellsCache.computeIfAbsent(
            bs,
            k -> {
              // In the tight loop, resizing the hash set is expensive, so lets get it close. This
              // will usually be an overestimate as some creeps are cross multiple cells, but there
              // are never that many creeps, max ~200, so the array is always tiny.
              int expectedCreeps = 0;
              for (Cell cell : cells) {
                expectedCreeps += cell.creeps.size();
              }
              Set<Creep> creeps = HashSet.newHashSet(expectedCreeps);
              for (Cell cell : cells) {
                creeps.addAll(cell.creeps);
              }
              return creeps;
            });
    }
  }

  private static final class Cell {

    private final int i, j;
    private final Rectangle2D bounds;
    private final List<Creep> creeps;

    private Cell(int i, int j, List<Creep> creeps) {
      this.i = i;
      this.j = j;
      int x = -overflow + i * cellWidth;
      int y = -overflow + j * cellWidth;
      this.bounds = new Rectangle2D.Float(x, y, cellWidth, cellWidth);
      List<Creep> containedCreeps = new ArrayList<>();
      for (Creep c : creeps) {
        if (c.getBounds().intersects(this.bounds)) {
          containedCreeps.add(c);
        }
      }
      this.creeps = Collections.unmodifiableList(containedCreeps);
    }

    private Cell(Cell base, Collection<Creep> excluding) {
      this.i = base.i;
      this.j = base.j;
      this.bounds = base.bounds;
      this.creeps = Collections.unmodifiableList(Helper.filter(base.creeps, excluding));
    }
  }
}
