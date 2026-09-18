package model;

/* Rappresenta un parco della città.
   Riduce l'inquinamento e aumenta la felicità. */
public class Park extends Construction {
    private static final int POLLUTION_REDUCTION=-400;
    private static final int HAPPINESS_INCREASE=400;
    private static final int PLACEMENT_PRICE=-150;
    private static final int MAINTENANCE_COST = 10;


    /* Rappresenta un parco della città.
   Riduce l'inquinamento e aumenta la felicità. */
    public Park(){

        super(0,0,PLACEMENT_PRICE);
    }

    // Restituisce la riduzione dell'inquinamento prodotta dal parco.
    @Override
    public int getPollutionImpact()
    {
        return POLLUTION_REDUCTION;
    }

    // Restituisce l'aumento della felicità prodotto dal parco.
    @Override
    public int getHappinessImpact()
    {
        return HAPPINESS_INCREASE;
    }

    // Restituisce il tipo corrispondente al parco.
    @Override
    public ConstructionType getType()
    {
        return ConstructionType.PARK;
    }

    @Override
    public int getMaintenanceCost()
    {
        return MAINTENANCE_COST;
    }
}
