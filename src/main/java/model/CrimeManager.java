package model;

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

        Construction criminalActivity =
                ConstructionFactory.create(
                        ConstructionType.CRIMINAL_ACTIVITY
                );

        grid.placeConstruction(
                criminalActivity,
                randomCell.getRow(),
                randomCell.getColumn()
        );

        return true;
    }
}