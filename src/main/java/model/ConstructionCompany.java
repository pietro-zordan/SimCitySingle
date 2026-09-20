package model;

public class ConstructionCompany extends Construction
{
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
    public int getUnlockTick()
    {
        return 40;
    }
}
