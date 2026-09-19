package model;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/* Gestisce le celle della griglia e le operazioni sulle costruzioni.
   Controlla piazzamento, rimozione, collegamenti e aggiornamento dei tick. */
public class Grid
{
    private static final int N_ROW = 20;
    private static final int N_COL = 20;

    // Spostamenti per raggiungere le quattro celle adiacenti.
    private static final int[][] ORTHOGONAL_DIRECTIONS =
            {
                    {-1, 0}, // Sopra
                    {1, 0},  // Sotto
                    {0, -1}, // Sinistra
                    {0, 1}   // Destra
            };

    private final Cell[][] cells;

    // Crea e inizializza tutte le celle della griglia.
    public Grid()
    {
        cells = new Cell[N_ROW][N_COL];

        for (int row = 0; row < N_ROW; row++)
        {
            for (int column = 0;
                 column < N_COL;
                 column++)
            {
                cells[row][column] =
                        new Cell(row, column);
            }
        }
    }

    // Verifica se la posizione indicata si trova nella griglia.
    public boolean isInside(int row, int column)
    {
        return row >= 0
                && row < N_ROW
                && column >= 0
                && column < N_COL;
    }

    // Restituisce la cella presente nella posizione indicata.
    public Cell getCell(int row, int column)
    {
        if (!isInside(row, column))
        {
            throw new IllegalArgumentException(
                    "Position outside the grid: row="
                            + row
                            + ", column="
                            + column
            );
        }

        return cells[row][column];
    }

    /* Posiziona una costruzione dopo aver controllato la posizione,
       la disponibilità della cella e il collegamento stradale. */
    public void placeConstruction(
            Construction construction,
            int row,
            int column)
    {
        if (construction == null)
        {
            throw new IllegalArgumentException(
                    "model.Construction cannot be null"
            );
        }

        if (!isInside(row, column))
        {
            throw new IllegalArgumentException(
                    "Position outside the grid"
            );
        }

        Cell cell = getCell(row, column);

        if (!cell.isEmpty())
        {
            throw new IllegalStateException(
                    "The cell is already occupied"
            );
        }

        boolean placingRoad =
                construction.getType()
                        == ConstructionType.ROAD;

        if (!placingRoad
                && countBuildableCells() == 1)
        {
            throw new IllegalStateException(
                    "Only a Road can be placed when just one buildable cell remains"
            );
        }

        if (placingRoad)
        {
            if (hasAnyRoad()
                    && !hasAdjacentRoad(cell))
            {
                throw new IllegalStateException(
                        "A Road must be adjacent to another Road"
                );
            }
        }
        else
        {
            if (!hasAdjacentRoad(cell))
            {
                throw new IllegalStateException(
                        "The construction must be adjacent to a Road"
                );
            }
        }

        cell.placeConstruction(construction);

        construction.initializeAfterPlacement(
                this,
                row,
                column
        );

        if (placingRoad)
        {
            updateRoadConnections();
        }
        else
        {
            construction.setRoadConnected(true);
        }

        updatePowerConnections();

    }

    // Rimuove la costruzione dopo aver verificato che sia eliminabile.
    public void removeConstruction(
            int row,
            int column)
    {
        if (!isInside(row, column))
        {
            throw new IllegalArgumentException(
                    "Position outside the grid"
            );
        }

        Cell cell = getCell(row, column);

        if (cell.isEmpty())
        {
            return;
        }

        Construction construction =
                cell.getConstruction();

        if (!construction.canBeRemoved())
        {
            throw new IllegalStateException(
                    "This construction cannot be removed"
            );
        }

        construction.prepareForRemoval();
        cell.removeConstruction();
    }

    // Verifica se una delle quattro celle adiacenti contiene una strada.
    public boolean hasAdjacentRoad(Cell cell)
    {
        int row = cell.getRow();
        int column = cell.getColumn();

        for (int[] direction
                : ORTHOGONAL_DIRECTIONS)
        {
            int newRow = row + direction[0];
            int newColumn = column + direction[1];

            if (isInside(newRow, newColumn))
            {
                Cell neighbor =
                        getCell(newRow, newColumn);

                if (!neighbor.isEmpty()
                        && neighbor.getConstruction().getType()
                        == ConstructionType.ROAD)
                {
                    return true;
                }
            }
        }

        return false;
    }

    /* Verifica se nella griglia è già presente una strada.
       Serve per permettere il piazzamento libero della prima strada. */
    public boolean hasAnyRoad()
    {
        for (int row = 0; row < N_ROW; row++)
        {
            for (int column = 0;
                 column < N_COL;
                 column++)
            {
                Construction construction =
                        cells[row][column]
                                .getConstruction();

                if (construction != null
                        && construction.getType()
                        == ConstructionType.ROAD)
                {
                    return true;
                }
            }
        }

        return false;
    }

    public int getNumberOfPoweredCC()
    {
        int count = 0;

        for (Construction construction : getConstructions())
        {
            if (construction.getType() == ConstructionType.CONSTRUCTION_COMPANY
            && construction.isPowered())
            {
                count++;
            }
        }

        return count;
    }

    public int getNumberOfBanks()
    {
        int numberOfBanks = 0;

        for (Construction construction : getConstructions())
        {
            if (construction.getType() == ConstructionType.BANK)
            {
                numberOfBanks++;
            }
        }

        return numberOfBanks;
    }

    public int getNumberOfPoweredBanks()
    {
        int numberOfPoweredBanks = 0;

        for (Construction construction : getConstructions())
        {
            if (construction.getType() == ConstructionType.BANK
                    && construction.isPowered())
            {
                numberOfPoweredBanks++;
            }
        }

        return numberOfPoweredBanks;
    }

    // Ricalcola il collegamento stradale di tutte le costruzioni.
    public void updateRoadConnections()
    {
        for (int row = 0; row < N_ROW; row++)
        {
            for (int column = 0;
                 column < N_COL;
                 column++)
            {
                Cell cell = cells[row][column];

                if (!cell.isEmpty())
                {
                    Construction construction =
                            cell.getConstruction();

                    construction.setRoadConnected(
                            hasAdjacentRoad(cell)
                    );
                }
            }
        }
    }

    /* Aggiorna prima le centrali e poi le altre costruzioni.
       Rimuove quelle che non possono più rimanere nella griglia. */
    public void updateOfOneTick()
    {
        // Prima fase: aggiorna tutte le centrali elettriche.
        for (int row = 0; row < N_ROW; row++)
        {
            for (int column = 0;
                 column < N_COL;
                 column++)
            {
                Cell cell = cells[row][column];

                if (!cell.isEmpty()
                        && cell.getConstruction()
                        instanceof PowerPlant)
                {
                    boolean mustRemove =
                            cell.updateOfOneTick();

                    if (mustRemove)
                    {
                        removeConstruction(
                                row,
                                column
                        );
                    }
                }
            }
        }

        // Seconda fase: aggiorna tutte le altre costruzioni.
        for (int row = 0; row < N_ROW; row++)
        {
            for (int column = 0;
                 column < N_COL;
                 column++)
            {
                Cell cell = cells[row][column];

                if (!cell.isEmpty()
                        && !(cell.getConstruction()
                        instanceof PowerPlant))
                {
                    boolean mustRemove =
                            cell.updateOfOneTick();

                    if (mustRemove)
                    {
                        removeConstruction(
                                row,
                                column
                        );
                    }
                }
            }
        }
    }

    // Restituisce tutte le centrali presenti nella griglia.
    public List<PowerPlant> getAllPowerPlants()
    {
        List<PowerPlant> powerPlants =
                new ArrayList<>();

        for (Construction construction
                : getConstructions())
        {
            if (construction
                    instanceof PowerPlant powerPlant)
            {
                powerPlants.add(powerPlant);
            }
        }

        return powerPlants;
    }

    // Restituisce il numero di righe della griglia.
    public int getNumberOfRows()
    {
        return N_ROW;
    }

    // Restituisce il numero di colonne della griglia.
    public int getNumberOfColumns()
    {
        return N_COL;
    }

    // Conta le celle vuote adiacenti ad almeno una strada.
    public int countBuildableCells()
    {
        Set<Cell> buildable =
                new HashSet<>();

        for (int row = 0; row < N_ROW; row++)
        {
            for (int column = 0;
                 column < N_COL;
                 column++)
            {
                Cell cell = cells[row][column];

                if (!cell.isEmpty()
                        && cell.getConstruction().getType()
                        == ConstructionType.ROAD)
                {
                    for (int[] direction
                            : ORTHOGONAL_DIRECTIONS)
                    {
                        int newRow =
                                row + direction[0];

                        int newColumn =
                                column + direction[1];

                        if (isInside(
                                newRow,
                                newColumn))
                        {
                            Cell neighbor =
                                    getCell(
                                            newRow,
                                            newColumn
                                    );

                            if (neighbor.isEmpty())
                            {
                                buildable.add(neighbor);
                            }
                        }
                    }
                }
            }
        }

        return buildable.size();
    }

    /* Inserisce una costruzione salvata senza applicare nuovamente
       i normali vincoli di piazzamento. */
    public void restoreConstruction(
            Construction construction,
            int row,
            int column)
    {
        if (construction == null)
        {
            throw new IllegalArgumentException(
                    "The construction cannot be null"
            );
        }

        if (!isInside(row, column))
        {
            throw new IllegalArgumentException(
                    "Saved position outside the grid"
            );
        }

        Cell cell = getCell(row, column);

        if (!cell.isEmpty())
        {
            throw new IllegalStateException(
                    "Two saved constructions occupy the same cell"
            );
        }

        cell.placeConstruction(construction);

        construction.initializeAfterPlacement(
                this,
                row,
                column
        );
    }

    /* Dopo il caricamento ricalcola i collegamenti stradali
       e ricollega le costruzioni alle centrali. */
    public void rebuildConnectionsAfterLoad()
    {
        updateRoadConnections();

        List<PowerPlant> powerPlants =
                getAllPowerPlants();

        for (PowerPlant powerPlant
                : powerPlants)
        {
            powerPlant.serveConstruction();
        }
    }

    // Restituisce tutte le costruzioni presenti nella griglia.
    public List<Construction> getConstructions()
    {
        List<Construction> constructions =
                new ArrayList<>();

        for (int row = 0; row < N_ROW; row++)
        {
            for (int column = 0;
                 column < N_COL;
                 column++)
            {
                Cell cell = cells[row][column];

                if (!cell.isEmpty())
                {
                    constructions.add(
                            cell.getConstruction()
                    );
                }
            }
        }

        return constructions;
    }

    private void updatePowerConnections()
    {
        for (PowerPlant powerPlant : getAllPowerPlants())
        {
            if (powerPlant.isActive())
            {
                powerPlant.serveConstruction();
                powerPlant.reconnect();
            }
        }
    }

}