package model;

/* Rappresenta un edificio commerciale.
   Produce denaro, offre posti di lavoro e richiede energia. */
public class Commercial extends EconomicBuilding
{
    private static final int HAPPINESS_INCREASE = 150;
    private static final double MAX_ECONOMY_GROWTH_RATE = 3.0;
    private static final int POLLUTION_GENERATED = 150;
    private static final int POWER_CONSUMPTION = 200;
    private static final int PLACEMENT_PRICE = -300;
    private static final int MAX_MONEY_PRODUCTION = 300;
    private static final double BASE_MONEY_PRODUCTION = 50;
    private static final int NUM_EMPLOYEE = 30;
    private static final int MAINTENANCE_COST = 30;

    // Crea un edificio commerciale con consumi e costo prestabiliti.
    public Commercial()
    {
        super(POWER_CONSUMPTION, PLACEMENT_PRICE);
    }

    // Restituisce l'inquinamento prodotto se l'edificio è alimentato.
    @Override
    public int getPollutionImpact()
    {
        if (isPowered())
        {
            return POLLUTION_GENERATED;
        }

        return 0;
    }

    // Restituisce il numero di posti di lavoro offerti.
    @Override
    public int getNumberOfEmployee()
    {
        return NUM_EMPLOYEE;
    }

    // Restituisce l'aumento della felicità se l'edificio è alimentato.
    @Override
    public int getHappinessImpact()
    {
        if (isPowered())
        {
            return HAPPINESS_INCREASE;
        }

        return 0;
    }

    // Restituisce il massimo tasso di crescita economica raggiungibile.
    @Override
    public double getMaxEconomyGrowthRate()
    {
        return MAX_ECONOMY_GROWTH_RATE;
    }

    // Restituisce la produzione economica di base dell'edificio.
    @Override
    protected double getBaseMoneyProduction()
    {
        return BASE_MONEY_PRODUCTION;
    }

    // Restituisce la produzione economica massima raggiungibile.
    @Override
    protected int getMaxMoneyProduction()
    {
        return MAX_MONEY_PRODUCTION;
    }

    // Restituisce il tipo corrispondente all'edificio commerciale.
    @Override
    public ConstructionType getType()
    {
        return ConstructionType.COMMERCIAL;
    }

    @Override
    public int getMaintenanceCost()
    {
        return MAINTENANCE_COST;
    }
}