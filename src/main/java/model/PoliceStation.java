package model;

public class PoliceStation extends Construction{

    private int lastRemovalTick = -1;

    public PoliceStation()
    {
        super(0, 200, -1200);
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

    @Override
    public int getUnlockTick()
    {
        return 50;
    }

    public boolean canRemoveCriminalActivity(
            int currentTick)
    {
        return lastRemovalTick == -1
                || currentTick - lastRemovalTick >= 15;
    }

    public void registerCriminalActivityRemoval(
            int currentTick)
    {
        lastRemovalTick = currentTick;
    }
}
