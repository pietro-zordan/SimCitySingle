package model;

public class Residential extends Construction
{
    private static final int PLACEMENT_PRICE = -200;
    private static final int POWER_CONSUMPTION = 100;

    private static final int MAX_POPULATION_GROWTH_RATE = 5;
    private static final int MAX_POPULATION_DECREASE_RATE = 5;
    private static final int MAX_POPULATION = 50;
    private static final int MAINTENANCE_COST = 5;

    private int population = 10;
    private int populationGrowthRate = 1;
    private int populationDecreaseRate = 1;

    public Residential()
    {
        super(0, POWER_CONSUMPTION, PLACEMENT_PRICE);
    }

    // Aggiorna la popolazione a ogni tick in base alla disponibilità di energia.
    @Override
    public void updateOfOneTick()
    {
        if (isPowered())
        {
            increasePopulation();
        }
        else
        {
            decreasePopulation();
        }
    }

    /* Aumenta la popolazione senza superare il limite massimo
   e incrementa gradualmente il tasso di crescita. */
    private void increasePopulation()
    {
        if (population < MAX_POPULATION)
        {
            population += populationGrowthRate;

            if (population > MAX_POPULATION)
            {
                population = MAX_POPULATION;
            }

            if (populationGrowthRate < MAX_POPULATION_GROWTH_RATE)
            {
                populationGrowthRate++;
            }
        }
    }

    /* Diminuisce la popolazione senza scendere sotto lo zero
   e incrementa gradualmente il tasso di diminuzione. */
    private void decreasePopulation()
    {
        if (population > 0)
        {
            population -= populationDecreaseRate;

            if (population < 0)
            {
                population = 0;
            }

            if (populationDecreaseRate < MAX_POPULATION_DECREASE_RATE)
            {
                populationDecreaseRate++;
            }
        }
    }

    public boolean hasNoPopulation()
    {
        return population == 0;
    }

    public int getPopulation()
    {
        return population;
    }

    //Restituisce il tasso attuale di crescita della popolazione
    @Override
    public int getPopulationGrowthRate()
    {
        return populationGrowthRate;
    }

    // Restituisce il tasso attuale di diminuzione della popolazione.
    @Override
    public int getPopulationDecreaseRate()
    {
        return populationDecreaseRate;
    }

    // Indica che la residenza deve essere rimossa quando non ha più abitanti.
    @Override
    public boolean mustBeRemoved()
    {
        return hasNoPopulation();
    }

    @Override
    public ConstructionType getType()
    {
        return ConstructionType.RESIDENTIAL;
    }

    /* Ripristina la popolazione e i relativi tassi di crescita e diminuzione,
   oltre agli attributi comuni gestiti dalla superclasse. */
    @Override
    public void restoreState(
            int population,
            int populationGrowthRate,
            int populationDecreaseRate,
            double economyGrowthRate,
            int moneyProduction)
    {
        super.restoreState(
                population,
                populationGrowthRate,
                populationDecreaseRate,
                economyGrowthRate,
                moneyProduction
        );

        this.population = population;
        this.populationGrowthRate = populationGrowthRate;
        this.populationDecreaseRate = populationDecreaseRate;
    }

    @Override
    public int getMaintenanceCost()
    {
        return MAINTENANCE_COST;
    }
}