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
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreepGrid {

  // The distance over the edge of the map that cells are created.
  private static final int overflow = 100;

  private static final int nDivisions = 5;
  // Width (horizontal and vertical) of each cell in the grid.
  private static final int cellWidth = (Constants.MAP_WIDTH + 2 * overflow) / nDivisions;

  private final List<Creep> creeps;
  private final List<Cell> cells;

  public CreepGrid(List<Creep> creeps) {
    this.creeps = Collections.unmodifiableList(creeps);

    this.cells = new ArrayList<>();

    for (int i = 0; i < nDivisions; i++) {
      int x = -overflow + i * cellWidth;
      for (int j = 0; j < nDivisions; j++) {
        int y = -overflow + j * cellWidth;
        Cell cell = new Cell(x, y, creeps);
        if (!cell.creeps.isEmpty()) {
          this.cells.add(cell);
        }
        //System.out.println(i + "," + j + " : " + cell.creeps.size());
      }
    }
  }

  private CreepGrid(CreepGrid base, Collection<Creep> excluding) {
    List<Creep> creeps = new ArrayList<>(base.creeps);
    creeps.removeAll(excluding);
    this.creeps = Collections.unmodifiableList(creeps);

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

  public Collection<Creep> filterCreeps(Line2D line) {
    // In most cases the line will only intersect one cell
    List<Cell> intersectingCells = new ArrayList<>();
    for (Cell cell : this.cells) {
      if (cell.bounds.intersectsLine(line)) {
        intersectingCells.add(cell);
      }
    }
    if (intersectingCells.size() == 1) {
      return intersectingCells.get(0).creeps;
    } else if (intersectingCells.isEmpty()) {
      return Collections.emptyList();
    } else {
      Set<Creep> creeps = new HashSet<Creep>();
      for (Cell cell : intersectingCells) {
        creeps.addAll(cell.creeps);
      }
      return creeps;
    }
  }

  private static final class Cell {

    private final Rectangle2D bounds;
    private final List<Creep> creeps;

    private Cell(int x, int y, List<Creep> creeps) {
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
      this.bounds = base.bounds;
      List<Creep> creeps = new ArrayList<>(base.creeps);
      creeps.removeAll(excluding);
      this.creeps = Collections.unmodifiableList(creeps);
    }
  }
}
