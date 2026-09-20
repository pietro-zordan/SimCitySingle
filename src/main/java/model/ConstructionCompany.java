package model;

public class ConstructionCompany extends Construction
{
    private static final int MAINTENANCE_COST = 40;

    public ConstructionCompany()
    {
        super(0, 200, -1000);
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.CONSTRUCTION_COMPANY;
    }

    @Override
    public int getPollutionImpact() {
        return 150;
    }

    @Override
    public int getMaintenanceCost()
    {
        return MAINTENANCE_COST;
    }

    @Override
    public int getUnlockTick()
    {
        return 40;
    }
}
