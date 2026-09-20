package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TerrorManager {

    private final City city;
    private final Grid grid;
    List<Residential> houses = getAllResidentials();
    private int populationDecreaseRate;

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
        Random random = new Random();
        int index = random.nextInt(houses.size());
        Residential randomHouse = houses.get(index);

        randomHouse.setPopulationDecreaseRate(populationDecreaseRate);

    }

    public void killRate()
    {
        populationDecreaseRate=5*getNumOfTC();
    }

    public void place(int row, int column)
    {
        if(city.getGlobalHappiness()<500)
        {
            TerroristicGroup terroristicGroup= new TerroristicGroup();
            city.placeConstruction(terroristicGroup, row, column);
        }
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
