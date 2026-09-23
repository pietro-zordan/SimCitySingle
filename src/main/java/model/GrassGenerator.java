package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Individua le celle che non sono più utilizzabili e le riempie d'erba.
final class GrassGenerator
{
    private static final int[][] ORTHOGONAL_DIRECTIONS =
            {
                    {-1, 0},
                    {1, 0},
                    {0, -1},
                    {0, 1}
            };

    private final Grid grid;

    GrassGenerator(Grid grid)
    {
        this.grid = grid;
    }

    /*
     * Riempie con erba le celle rimaste inutilizzabili:
     * sia i piccoli vicoli ciechi, sia le zone vuote completamente isolate
     * dalla rete stradale.
     */
    void fillTrappedCellsWithGrass()
    {
        Set<Cell> cellsToFill =
                new HashSet<>();

        boolean[][] visited =
                new boolean[grid.getNumberOfRows()][grid.getNumberOfColumns()];

        for (int row = 0; row < grid.getNumberOfRows(); row++)
        {
            for (int column = 0; column < grid.getNumberOfColumns(); column++)
            {
                Cell cell = grid.getCell(row, column);

                if (cell.isEmpty()
                        && !grid.isCellReservedForReconstruction(
                                row,
                                column
                        )
                        && !grid.hasAdjacentRoad(cell)
                        && countBlockedSides(row, column) >= 3)
                {
                    cellsToFill.add(cell);
                }

                if (visited[row][column]
                        || !isOpenForGrassReachability(cell)
                        || grid.isCellReservedForReconstruction(
                                row,
                                column
                        ))
                {
                    continue;
                }

                List<Cell> emptyArea =
                        new ArrayList<>();

                ArrayDeque<Cell> pendingCells =
                        new ArrayDeque<>();

                pendingCells.add(cell);
                visited[row][column] = true;

                boolean reachableFromRoad = false;

                while (!pendingCells.isEmpty())
                {
                    Cell currentCell =
                            pendingCells.removeFirst();

                    if (currentCell.isEmpty())
                    {
                        emptyArea.add(currentCell);
                    }

                    if (grid.hasAdjacentRoad(currentCell))
                    {
                        reachableFromRoad = true;
                    }

                    for (int[] direction
                            : ORTHOGONAL_DIRECTIONS)
                    {
                        int newRow =
                                currentCell.getRow()
                                        + direction[0];

                        int newColumn =
                                currentCell.getColumn()
                                        + direction[1];

                        if (grid.isInside(newRow, newColumn)
                                && !visited[newRow][newColumn]
                                && isOpenForGrassReachability(
                                        grid.getCell(newRow, newColumn)
                                )
                                && !grid.isCellReservedForReconstruction(
                                        newRow,
                                        newColumn
                                ))
                        {
                            visited[newRow][newColumn] = true;

                            pendingCells.addLast(
                                    grid.getCell(newRow, newColumn)
                            );
                        }
                    }
                }

                if (!reachableFromRoad)
                {
                    cellsToFill.addAll(emptyArea);
                }
            }
        }

        for (Cell cell : cellsToFill)
        {
            if (cell.isEmpty())
            {
                cell.placeConstruction(
                        new Grass()
                );
            }
        }
    }

    // Le minacce temporanee non interrompono il percorso verso una strada.
    private boolean isOpenForGrassReachability(
            Cell cell)
    {
        if (cell.isEmpty())
        {
            return true;
        }

        ConstructionType type = cell.getConstruction().getType();

        return type == ConstructionType.CRIMINAL_ACTIVITY
                || type == ConstructionType.TERRORISTIC_GROUP;
    }

    // L'erba e le minacce temporanee non contano come lati bloccati.
    private int countBlockedSides(int row, int column)
    {
        int blockedSides = 0;

        for (int[] direction : ORTHOGONAL_DIRECTIONS)
        {
            int newRow = row + direction[0];
            int newColumn = column + direction[1];

            if (!grid.isInside(newRow, newColumn))
            {
                blockedSides++;
                continue;
            }

            Construction construction =
                    grid.getCell(newRow, newColumn)
                            .getConstruction();

            if (construction != null
                    && construction.getType() != ConstructionType.GRASS
                    && construction.getType() != ConstructionType.CRIMINAL_ACTIVITY
                    && construction.getType() != ConstructionType.TERRORISTIC_GROUP)
            {
                blockedSides++;
            }
        }

        return blockedSides;
    }
}
