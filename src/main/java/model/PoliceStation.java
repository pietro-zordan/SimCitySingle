package model;

public class PoliceStation extends Construction{

    public PoliceStation()
    {
        super(0, 200, 1200);
    }

    @Override
    public int getPollutionImpact()
    {
        return 110;
    }

    @Override
    public int getMaintenanceCost()
    {
        return 200;
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.POLICE_STATION;
    }
}
