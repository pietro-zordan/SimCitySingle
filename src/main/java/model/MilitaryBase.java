package model;

public class MilitaryBase extends Construction{

    public MilitaryBase()
    {
        super(0, 500, 2250);
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.MILITARY_BASE;
    }

    @Override
    public int getPollutionImpact() {
        return 300;
    }

}
