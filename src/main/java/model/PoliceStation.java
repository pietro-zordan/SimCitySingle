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
    public ConstructionType getType() {
        return null;
    }
}
