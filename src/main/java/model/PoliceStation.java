package model;

public class PoliceStation extends Construction implements DefenceSystem{

    private static final int MAINTENANCE_COST = 120;
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
        return MAINTENANCE_COST;
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

    @Override
    public void setDamage() {
        return;
    }

    @Override
    public void resetDamage() {
        return;
    }
}
