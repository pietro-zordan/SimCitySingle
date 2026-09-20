package model;

public class CriminalActivity extends Construction
{
    private static final int MIN_LIFETIME = 3;

    private int creationTick;

    public CriminalActivity()
    {
        super(-300, 200, 0);
    }

    @Override
    public ConstructionType getType()
    {
        return ConstructionType.CRIMINAL_ACTIVITY;
    }

    @Override
    public int getHappinessImpact()
    {
        return -400;
    }

    public void setCreationTick(
            int creationTick)
    {
        this.creationTick = creationTick;
    }

    public boolean canBeRemovedByPolice(
            int currentTick)
    {
        return currentTick - creationTick
                >= MIN_LIFETIME;
    }
}