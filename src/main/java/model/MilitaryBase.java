package model;

public class MilitaryBase extends Construction implements DefenceSystem{

    private int lastRemovalTick = -1;

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

    public boolean canRemoveCriminalActivity(
            int currentTick)
    {
        return lastRemovalTick == -1
                || currentTick - lastRemovalTick >= 12;
    }

    public void registerCriminalActivityRemoval(int currentTick)
    {
        lastRemovalTick = currentTick;
    }


}
