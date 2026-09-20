package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TerrorManager {

    private final City city;
    private final Grid grid;
    private static final int KILLS_PER_GROUP = 5;
    private static final int HAPPINESS_THRESHOLD = -500;
    private int sadnessTicks = 0;
    private static final int NUM_OF_SADNESS_TICKS = 5;

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

    public void updateOfOneTick()
    {
        if (city.getGlobalHappiness() < HAPPINESS_THRESHOLD)
            sadnessTicks++;
        else
            sadnessTicks = 0;

        if (sadnessTicks >= NUM_OF_SADNESS_TICKS)
        {
            placeTG();
            sadnessTicks = 0;
        }

        if (getNumOfTC() > 0)
            killPeople();
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

        int numberOfGroups = getNumOfTC();
        int peopleToKill = KILLS_PER_GROUP * numberOfGroups;

        randomHouse.decreasePopulationBy(peopleToKill);

        city.refreshStatistics();

    }

    public void placeTG()
    {
        List<Cell> cells = grid.getBuildableCells();

        if (cells.isEmpty())
            return;

        Random random = new Random();
        Cell cell = cells.get(random.nextInt(cells.size()));

        TerroristicGroup group = new TerroristicGroup();
        city.placeConstruction(group, cell.getRow(), cell.getColumn());
    }

    public int getNumOfTC()
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






}
