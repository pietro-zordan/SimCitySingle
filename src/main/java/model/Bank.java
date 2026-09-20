package model;

public class Bank extends Construction
{

    private final static int PLACEMENT_PRICE= -800;
    private static final int MAINTENANCE_COST = 60;

    public Bank()
    {
        super(0, 100, PLACEMENT_PRICE);
    }

    @Override
    public ConstructionType getType()
    {
        return ConstructionType.BANK;
    }

    @Override
    public int getPollutionImpact() {
        return 50;
    }

    @Override
    public int getMaintenanceCost()
    {
        return MAINTENANCE_COST;
    }

    @Override
    public int getUnlockTick()
    {
        return 30;
    }

}
