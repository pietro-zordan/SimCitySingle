package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CrimeManager
{
    private static final int CRIME_START_TICK = 15;
    private static final double CRIME_ECONOMY_FACTOR = 0.00005;
    private static final double MAX_CRIME_PROBABILITY = 0.15;

    private final Random random = new Random();
    private final City city;
    private final Grid grid;
    private int numOfPoliceStation=0;
    private int maxRemoval=0;
    private int lastPoliceRemovalTick = -1;
    private static final int POLICE_REMOVAL_INTERVAL = 15;

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
        if (currentTick < CRIME_START_TICK)
        {
            return false;
        }

        double probability =
                city.getGlobalEconomy()
                        * CRIME_ECONOMY_FACTOR;

        probability = Math.min(
                probability,
                MAX_CRIME_PROBABILITY
        );

        if (random.nextDouble() < probability)
        {
            return placeCriminalActivity();
        }

        return false;
    }

    public boolean placeCriminalActivity()
    {
        List<Cell> buildableCells =
                grid.getBuildableCells();

        if (buildableCells.isEmpty())
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
                (CriminalActivity) ConstructionFactory.create(
                        ConstructionType.CRIMINAL_ACTIVITY
                );

        grid.placeConstruction(
                criminalActivity,
                randomCell.getRow(),
                randomCell.getColumn()
        );

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

    private List<Cell> getCriminalActivities()
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
                        grid.getCell(row, column);

                if (!cell.isEmpty()
                        && cell.getConstruction()
                        instanceof CriminalActivity)
                {
                    criminalActivities.add(cell);
                }
            }
        }

        return criminalActivities;
    }

    public boolean destroyCriminalActivity()
    {
        List<Cell> criminalActivities =
                getCriminalActivities();

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

    public int tryToDestroyCriminalActivities(
            int currentTick)
    {
        int maxRemoval =
                getMaxRemoval();

        if (maxRemoval == 0)
        {
            lastPoliceRemovalTick = -1;
            return 0;
        }

        if (lastPoliceRemovalTick == -1)
        {
            lastPoliceRemovalTick =
                    currentTick;

            return 0;
        }

        if (currentTick
                - lastPoliceRemovalTick
                < POLICE_REMOVAL_INTERVAL)
        {
            return 0;
        }

        int removed = 0;

        while (removed < maxRemoval
                && destroyCriminalActivity())
        {
            removed++;
        }

        lastPoliceRemovalTick =
                currentTick;

        return removed;
    }

}