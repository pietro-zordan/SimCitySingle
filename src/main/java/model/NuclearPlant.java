package model;

public class NuclearPlant extends Construction
{

    public NuclearPlant()
    {
        super(200,0,3000);
    }

    private static final int POWER_GENERATED = 10000;

    @Override
    public int getPollutionImpact()
    {
        return 900;
    }

    @Override
    public int getHappinessImpact()
    {
        return 400;
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.NUCLEAR_PLANT;
    }


}
