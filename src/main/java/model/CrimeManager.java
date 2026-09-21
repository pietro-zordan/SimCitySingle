package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CrimeManager
{
    private static final double CRIME_ECONOMY_FACTOR = 0.00008;
    private static final double MAX_CRIME_PROBABILITY = 0.25;

    private final Random random = new Random();
    private final City city;
    private final Grid grid;


    public CrimeManager(City city, Grid grid)
    {
        if (city == null)
        {
            throw new IllegalArgumentException(
                    "The city cannot be null"
            );
        }

        if (grid == null)
        {
            throw new IllegalArgumentException(
                    "The grid cannot be null"
            );
        }

        this.city = city;
        this.grid = grid;
    }

    public boolean tryToCreateCriminalActivity(
            int currentTick)
    {
        double probability =
                city.getGlobalEconomy()
                        * CRIME_ECONOMY_FACTOR;

        probability = Math.min(
                probability,
                MAX_CRIME_PROBABILITY
        );

        if (random.nextDouble() < probability)
        {
            return placeCriminalActivity(
                    currentTick
            );
        }

        return false;
    }

    public boolean placeCriminalActivity(
            int currentTick)
    {
        List<Cell> buildableCells =
                grid.getBuildableCells();

        // L'ultima cella costruibile viene riservata alle strade da Grid.
        // Evita quindi di tentare un piazzamento che Grid rifiuterebbe.
        if (buildableCells.size() <= 1)
        {
            return false;
        }

        int randomIndex =
                random.nextInt(
                        buildableCells.size()
                );

        Cell randomCell =
                buildableCells.get(
                        randomIndex
                );

        CriminalActivity criminalActivity =
                (CriminalActivity)
                        ConstructionFactory.create(
                                ConstructionType.CRIMINAL_ACTIVITY
                        );

        criminalActivity.setCreationTick(
                currentTick
        );

        grid.placeConstruction(
                criminalActivity,
                randomCell.getRow(),
                randomCell.getColumn()
        );

        city.refreshStatistics();

        return true;
    }

    public int getNumOfPoliceStation()
    {
        int numOfPoliceStation = 0;

        for (Construction construction
                : grid.getConstructions())
        {
            if (construction instanceof PoliceStation
                    && construction.isPowered())
            {
                numOfPoliceStation++;
            }
        }

        return numOfPoliceStation;
    }

    public int getMaxRemoval()
    {
        return getNumOfPoliceStation();
    }

    private List<Cell> getCriminalActivities(
            int currentTick)
    {
        List<Cell> criminalActivities =
                new ArrayList<>();

        for (int row = 0;
             row < grid.getNumberOfRows();
             row++)
        {
            for (int column = 0;
                 column < grid.getNumberOfColumns();
                 column++)
            {
                Cell cell =
                        grid.getCell(
                                row,
                                column
                        );

                if (!cell.isEmpty()
                        && cell.getConstruction()
                        instanceof CriminalActivity)
                {
                    CriminalActivity criminalActivity =
                            (CriminalActivity)
                                    cell.getConstruction();

                    if (criminalActivity
                            .canBeRemovedByPolice(
                                    currentTick
                            ))
                    {
                        criminalActivities.add(
                                cell
                        );
                    }
                }
            }
        }

        return criminalActivities;
    }

    // Riunisce le minacce che una stazione di polizia può rimuovere.
    // Le attività criminali rispettano la loro vita minima,
    // mentre i gruppi terroristici sono rimovibili subito.
    private List<Cell> getPoliceTargets(
            int currentTick)
    {
        List<Cell> policeTargets =
                new ArrayList<>(
                        getCriminalActivities(
                                currentTick
                        )
                );

        for (int row = 0;
             row < grid.getNumberOfRows();
             row++)
        {
            for (int column = 0;
                 column < grid.getNumberOfColumns();
                 column++)
            {
                Cell cell =
                        grid.getCell(
                                row,
                                column
                        );

                if (!cell.isEmpty()
                        && cell.getConstruction()
                        instanceof TerroristicGroup)
                {
                    policeTargets.add(cell);
                }
            }
        }

        return policeTargets;
    }

    public boolean destroyCriminalActivity(
            int currentTick)
    {
        List<Cell> criminalActivities =
                getCriminalActivities(
                        currentTick
                );

        if (criminalActivities.isEmpty())
        {
            return false;
        }

        int randomIndex =
                random.nextInt(
                        criminalActivities.size()
                );

        Cell criminalCell =
                criminalActivities.get(
                        randomIndex
                );

        grid.removeConstruction(
                criminalCell.getRow(),
                criminalCell.getColumn()
        );

        return true;
    }

    // Rimuove una sola minaccia scegliendo tra criminalità e terrorismo.
    private boolean destroyPoliceTarget(
            int currentTick)
    {
        List<Cell> policeTargets =
                getPoliceTargets(
                        currentTick
                );

        if (policeTargets.isEmpty())
        {
            return false;
        }

        int randomIndex =
                random.nextInt(
                        policeTargets.size()
                );

        Cell targetCell =
                policeTargets.get(
                        randomIndex
                );

        grid.removeConstruction(
                targetCell.getRow(),
                targetCell.getColumn()
        );

        return true;
    }

    public int tryToDestroyCriminalActivities(
            int currentTick)
    {
        int removed = 0;

        for (Construction construction
                : grid.getConstructions())
        {
            if (construction instanceof PoliceStation)
            {
                PoliceStation policeStation =
                        (PoliceStation) construction;

                if (policeStation.isPowered()
                        && policeStation
                        .canRemoveCriminalActivity(
                                currentTick
                        ))
                {
                    // Una stazione esegue una sola rimozione totale:
                    // attività criminale oppure gruppo terroristico.
                    if (destroyPoliceTarget(
                            currentTick))
                    {
                        policeStation
                                .registerCriminalActivityRemoval(
                                        currentTick
                                );

                        removed++;
                    }
                }
            }
        }

        return removed;
    }
}