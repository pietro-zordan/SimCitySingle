package model;

/* La loggia compare automaticamente dopo l'adesione alla massoneria. */
public final class MasonicLodge extends Construction
{
    public MasonicLodge()
    {
        super(0, 20, 0);
    }

    @Override
    public ConstructionType getType()
    {
        return ConstructionType.MASONIC_LODGE;
    }
}
