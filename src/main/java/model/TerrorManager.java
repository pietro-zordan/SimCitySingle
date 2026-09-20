package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TerrorManager {

    private final City city;
    private final Grid grid;
    private static final int KILLS_PER_GROUP = 5;
    private static final int HAPPINESS_THRESHOLD = -49;
    private static final int NUM_OF_SADNESS_TICKS = 1;
    private static final int SPAWN_COOLDOWN = 10;
    private static final int MAX_GROUPS = 3;

    private int sadnessTicks = 0;
    private int spawnCooldown = 0;

    public TerrorManager(City city, Grid grid)
    {
        if (city == null)
        {
            throw new IllegalArgumentException("The city cannot be null");
        }

        if (grid == null)
        {
            throw new IllegalArgumentException("The grid cannot be null");
        }

        this.city = city;
        this.grid = grid;

    }

    public boolean updateOfOneTick()
    {
        if (spawnCooldown > 0)
            spawnCooldown--;

        if (city.getGlobalHappiness() < HAPPINESS_THRESHOLD)
            sadnessTicks++;
        else
            sadnessTicks = 0;

        boolean groupCreated = false;

        if (sadnessTicks >= NUM_OF_SADNESS_TICKS && canPlaceTG())
        {
            groupCreated = placeTG();

            if (groupCreated)
            {
                spawnCooldown = SPAWN_COOLDOWN;
                sadnessTicks = 0;
            }
        }

        if (getNumOfTG() > 0)
            killPeople();

        return groupCreated;
    }

    public List<Residential> getAllResidentials()
    {
        List<Residential> residentials = new ArrayList<>();

        for (Construction construction : grid.getConstructions())
        {
            if (construction
                    instanceof Residential residential)
            {
                residentials.add(residential);
            }
        }
        return residentials;
    }

    public void killPeople()
    {
        List<Residential> houses = getAllResidentials();

        if (houses.isEmpty())
        {
            return;
        }

        Random random = new Random();
        int index = random.nextInt(houses.size());
        Residential randomHouse = houses.get(index);

        int numberOfGroups = getNumOfTG();
        int peopleToKill = KILLS_PER_GROUP * numberOfGroups;

        randomHouse.decreasePopulationBy(peopleToKill);

        city.refreshStatistics();

    }

    public boolean placeTG()
    {
        List<Cell> cells = grid.getBuildableCells();

        // Non occupa l'ultima cella costruibile: Grid la riserva alle strade.
        if (cells.size() <= 1)
            return false;

        Random random = new Random();
        Cell cell = cells.get(random.nextInt(cells.size()));

        TerroristicGroup group = new TerroristicGroup();
        city.placeConstruction(group, cell.getRow(), cell.getColumn());

        return true;
    }

    public int getNumOfTG()
    {
        int count = 0;

        for (Construction construction : grid.getConstructions())
        {
            if (construction instanceof TerroristicGroup terroristicGroup)
            {
                count++;
            }
        }

        return count;
    }


    public boolean canPlaceTG()
    {
        return spawnCooldown == 0
                && getNumOfTG() < MAX_GROUPS
                && grid.getBuildableCells().size() > 1;
    }



}
