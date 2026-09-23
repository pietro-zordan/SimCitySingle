package model;

public class TerroristicGroup extends Construction {

    private static final int POLICE_MIN_LIFETIME = 5;
    private static final int MILITARY_BASE_MIN_LIFETIME = 2;

    private int lifetimeTicks;

    public TerroristicGroup()
    {
        super(-100, 50, 0);
    }

    // Aumenta di uno il numero di tick per cui il gruppo è rimasto attivo.
    @Override
    public void updateOfOneTick()
    {
        lifetimeTicks++;
    }

    // La polizia può rimuovere il gruppo solo dopo almeno 5 tick di vita.
    public boolean canBeRemovedByPolice()
    {
        return lifetimeTicks >= POLICE_MIN_LIFETIME;
    }

    public boolean canBeRemovedByMilitaryBase()
    {
        return lifetimeTicks >= MILITARY_BASE_MIN_LIFETIME;
    }

    public int getLifetimeTicks()
    {
        return lifetimeTicks;
    }

    public void setLifetimeTicks(int lifetimeTicks)
    {
        this.lifetimeTicks = Math.max(0, lifetimeTicks);
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.TERRORISTIC_GROUP;
    }
}
