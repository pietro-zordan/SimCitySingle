package model;

import java.util.*;

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
    private final boolean[][] reconstructionReserved;
    private final EnergyManager energyManager;
    private final GrassGenerator grassGenerator;
    private final List<ExplosionInfo> explosions = new ArrayList<>();
    // Per ora la generazione automatica dell'erba è disattivata, ma il sistema resta disponibile.
    private boolean grassGenerationSuspended = true;

    // Crea e inizializza tutte le celle della griglia.
    public Grid()
    {
        cells = new Cell[N_ROW][N_COL];
        reconstructionReserved =
                new boolean[N_ROW][N_COL];
        energyManager =
                new EnergyManager(this);

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

        grassGenerator = new GrassGenerator(this);
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

        if (isCellReservedForReconstruction(
                row,
                column))
        {
            throw new IllegalStateException(
                    "Impossibile piazzare un nuovo edificio: ricostruzione in corso"
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

        energyManager.registerConstruction(
                construction,
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

        energyManager.updatePowerConnections();
        fillTrappedCellsWithGrass();

    }

    // Rimuove la costruzione e applica l'eventuale effetto di distruzione preparato dalla costruzione stessa.
    public void removeConstruction(
            int row,
            int column)
    {
        removeConstructionInternal(
                row,
                column,
                false
        );
    }

    private void removeConstructionInternal(
            int row,
            int column,
            boolean forced)
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

        if (!forced
                && !construction.canBeRemoved())
        {
            throw new IllegalStateException(
                    "This construction cannot be removed"
            );
        }

        energyManager.removeConstruction(
                construction
        );

        int destructionRadius =
                construction.prepareForRemoval();

        cell.removeConstruction();

        if (destructionRadius > 0)
        {
            explosions.add(
                    new ExplosionInfo(
                            row,
                            column,
                            destructionRadius,
                            construction.getType()
                                    == ConstructionType.NUCLEAR_PLANT
                    )
            );

            makeExplosion(
                    row,
                    column,
                    destructionRadius
            );
        }
    }

    // Trova le celle occupate nell'area quadrata dell'esplosione, escludendo centro e strade.
    private List<Cell> getExplosionCells(
            int centerRow,
            int centerColumn,
            int radius)
    {
        List<Cell> explosionCells =
                new ArrayList<>();

        for (int row = centerRow - radius; row <= centerRow + radius; row++)
        {
            for (int column = centerColumn - radius;
                 column <= centerColumn + radius;
                 column++)
            {
                if (isInside(row, column)
                        && (row != centerRow
                        || column != centerColumn))
                {
                    Cell cell = getCell(row, column);

                    if (!cell.isEmpty()
                            && !(cell.getConstruction() instanceof Road))
                    {
                        explosionCells.add(cell);
                    }
                }
            }
        }

        return explosionCells;
    }

    // Distrugge tutte le costruzioni presenti nell'area dell'esplosione.
    private void makeExplosion(
            int centerRow,
            int centerColumn,
            int radius)
    {
        List<Cell> explosionCells =
                getExplosionCells(
                        centerRow,
                        centerColumn,
                        radius
                );

        for (Cell cell : explosionCells)
        {
            if (!cell.isEmpty()
                    && !(cell.getConstruction() instanceof Road))
            {
                removeConstruction(
                        cell.getRow(),
                        cell.getColumn()
                );
            }
        }
    }

    // Restituisce le esplosioni avvenute nella griglia.
    public List<ExplosionInfo> getExplosions()
    {
        return new ArrayList<>(explosions);
    }

    // Restituisce le esplosioni non ancora mostrate dalla GUI e poi svuota la lista.
    public List<ExplosionInfo> consumeExplosions()
    {
        List<ExplosionInfo> pendingExplosions =
                new ArrayList<>(explosions);

        explosions.clear();

        return pendingExplosions;
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

    public boolean hasMilitaryBase()
    {
        for (Construction construction : getConstructions())
        {
            if (construction instanceof MilitaryBase)
            {
                return true;
            }
        }

        return false;
    }

    public int getNumberOfMilitaryBases()
    {
        int count = 0;

        for (Construction construction : getConstructions())
        {
            if (construction instanceof MilitaryBase)
            {
                count++;
            }
        }

        return count;
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

    public int getNumOfNuclearPlants()
    {
        int numberOfNuclearPlants = 0;

        for (Construction construction : getConstructions())
        {
            if (construction.getType() == ConstructionType.NUCLEAR_PLANT
                    && construction.isPowered())
            {
                numberOfNuclearPlants++;
            }
        }

        return numberOfNuclearPlants;
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
        energyManager.updatePowerConnections();

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

        fillTrappedCellsWithGrass();
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

    // Restituisce l'energia realmente utilizzata dalla centrale indicata.
    public int getPowerPlantUsedPower(
            PowerPlant powerPlant)
    {
        return energyManager.getUsedPower(
                powerPlant
        );
    }

    // Restituisce l'energia realmente consumata dalle costruzioni alimentate.
    public int getEnergyConsumed()
    {
        return energyManager.getEnergyConsumed();
    }

    // Restituisce il consumo richiesto da tutte le costruzioni.
    public int getTotalEnergyDemand()
    {
        return energyManager.getTotalEnergyDemand();
    }

    // Restituisce la potenza complessivamente disponibile dalle centrali attive.
    public int getEnergyAvailable()
    {
        return energyManager.getEnergyAvailable();
    }

    // Restituisce il limite massimo di energia servibile dalla città.
    public int getMaxEnergyServed()
    {
        return energyManager.getMaxEnergyServed();
    }

    // Verifica se la rete ha abbastanza capacità globale per sostenere anche la nuova costruzione.
    public boolean canSupportConstruction(Construction construction)
    {
        return energyManager.canSupportConstruction(construction);
    }

    // Indica se è il momento di rendere disponibile la centrale nucleare nella toolbar.
    public boolean shouldShowNuclearPlant()
    {
        return energyManager.shouldShowNuclearPlant();
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

                            if (neighbor.isEmpty()
                                    && !isCellReservedForReconstruction(
                                            newRow,
                                            newColumn
                                    ))
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

        energyManager.registerConstruction(
                construction,
                row,
                column
        );
    }

    /* Dopo il caricamento ricalcola i collegamenti stradali
       e ricollega le costruzioni alle centrali. */
    public void rebuildConnectionsAfterLoad()
    {
        updateRoadConnections();
        energyManager.rebuildConnections();
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

    public void destroyArea(
            int centerRow,
            int centerColumn,
            int radius)
    {
        if (!isInside(centerRow, centerColumn))
        {
            throw new IllegalArgumentException(
                    "Explosion center outside the grid"
            );
        }

        if (radius < 0)
        {
            throw new IllegalArgumentException(
                    "Explosion radius cannot be negative"
            );
        }

        for (int row = centerRow - radius;
             row <= centerRow + radius;
             row++)
        {
            for (int column = centerColumn - radius;
                 column <= centerColumn + radius;
                 column++)
            {
                if (isInside(row, column))
                {
                    Cell cell = getCell(row, column);

                    if (!cell.isEmpty()
                            && !(cell.getConstruction() instanceof Road))
                    {
                        removeConstructionInternal(
                                row,
                                column,
                                true
                        );
                    }
                }
            }
        }

        updateRoadConnections();
        energyManager.updatePowerConnections();
    }

    public List<Cell> getBuildableCells()
    {

        Set<Cell> buildable = new HashSet<>();

        for (int row = 0; row < N_ROW; row++)
        {
            for (int column = 0; column < N_COL; column++)
            {
                Cell cell = cells[row][column];

                if (!cell.isEmpty()
                        && cell.getConstruction().getType()
                        == ConstructionType.ROAD)
                {
                    for (int[] direction : ORTHOGONAL_DIRECTIONS)
                    {
                        int newRow = row + direction[0];
                        int newColumn = column + direction[1];

                        if (isInside(newRow, newColumn))
                        {
                            Cell neighbor =
                                    getCell(newRow, newColumn);

                            if (neighbor.isEmpty()
                                    && !isCellReservedForReconstruction(
                                            newRow,
                                            newColumn
                                    ))
                            {
                                buildable.add(neighbor);
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<>(buildable);
    }


    // La generazione rimane disabilitata finché non viene attivata esplicitamente.
    private void fillTrappedCellsWithGrass()
    {
        if (!grassGenerationSuspended)
        {
            grassGenerator.fillTrappedCellsWithGrass();
        }
    }

    public void reserveCellForReconstruction(
            int row,
            int column)
    {
        if (!isInside(row, column))
        {
            throw new IllegalArgumentException(
                    "Position outside the grid"
            );
        }

        reconstructionReserved[row][column] = true;
    }

    public void releaseCellFromReconstruction(
            int row,
            int column)
    {
        if (!isInside(row, column))
        {
            throw new IllegalArgumentException(
                    "Position outside the grid"
            );
        }

        reconstructionReserved[row][column] = false;
    }

    public boolean isCellReservedForReconstruction(
            int row,
            int column)
    {
        if (!isInside(row, column))
        {
            return false;
        }

        return reconstructionReserved[row][column];
    }


    public void releaseAllReconstructionReservations()
    {
        for (int row = 0; row < N_ROW; row++)
        {
            for (int column = 0;
                 column < N_COL;
                 column++)
            {
                reconstructionReserved[row][column] = false;
            }
        }
    }


    public void setGrassGenerationSuspended(
            boolean suspended)
    {
        grassGenerationSuspended =
                suspended;
    }

}
