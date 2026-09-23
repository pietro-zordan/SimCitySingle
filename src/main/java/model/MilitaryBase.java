package model;

public class MilitaryBase extends Construction implements DefenceSystem{

    private static final int TERRORIST_REMOVAL_COOLDOWN = 12;
    private int lastRemovalTick = -1;

    public MilitaryBase()
    {
        super(0, 500, -2250);
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.MILITARY_BASE;
    }

    @Override
    public int getPollutionImpact() {
        return 300;
    }

    @Override
    public int getUnlockTick()
    {
        return 80;
    }

    public boolean canRemoveCriminalActivity(
            int currentTick)
    {
        return lastRemovalTick == -1
                || currentTick - lastRemovalTick
                >= TERRORIST_REMOVAL_COOLDOWN;
    }

    public void registerCriminalActivityRemoval(int currentTick)
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
