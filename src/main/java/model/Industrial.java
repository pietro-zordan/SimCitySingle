package model;

public class Industrial extends EconomicBuilding{

    private static final int HAPPINESS_DECREASE=-200;
    private static final double MAX_ECONOMY_GROWTH_RATE=3.5; //VALORE DA DECIDERE
    private static final int POLLUTION_GENERATED=600;
    private static final int POWER_CONSUMPTION=1000;
    private static final int PLACEMENT_PRICE = -600;
    private static final int MAX_MONEY_PRODUCTION=1500;
    private static final double BASE_MONEY_PRODUCTION = 120;
    private static final int NUM_EMPLOYEE = 100;
    private static final int MAX_POLLUTION = 2000;

    // Crea un edificio industriale con consumi e costo di piazzamento prestabiliti.
    public Industrial (){
        super(POWER_CONSUMPTION, PLACEMENT_PRICE);
    }

    // Restituisce l'inquinamento prodotto, purché l'edificio sia alimentato.
    @Override
    public int getPollutionImpact(){
        if (isPowered()){
            return POLLUTION_GENERATED;
        }
        return 0;
    }


    @Override
    public int getNumberOfEmployee(){
        return NUM_EMPLOYEE;
    }

    // Restituisce la diminuzione di felicità causata, purché l'edificio sia alimentato.
    @Override
    public int getHappinessImpact(){
        if (isPowered()){
            return HAPPINESS_DECREASE;
        }
        return 0;
    }

    // Restituisce il massimo tasso di crescita economica raggiungibile.
    @Override
    public double getMaxEconomyGrowthRate() {
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

    @Override
    public ConstructionType getType()
    {
        return ConstructionType.INDUSTRIAL;
    }

    private static final int MAINTENANCE_COST = 150;

    @Override
    public int getMaintenanceCost()
    {
        return MAINTENANCE_COST;
    }

    @Override
    public String getPlacementErrorMessage(
            int pollution,
            int unemployed)
    {
        if (unemployed <= 0)
        {
            return super.getPlacementErrorMessage(
                    pollution,
                    unemployed
            );
        }

        return "Pollution is too high cannot place industrial";
    }
}

