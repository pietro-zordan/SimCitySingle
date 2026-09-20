package model;

/*
 * Rappresenta una piccola area verde generata automaticamente
 * nelle celle rimaste inutilizzabili.
 */
public class Grass extends Construction
{
    private static final int POLLUTION_REDUCTION = -10;

    public Grass()
    {
        super(0, 0, 0);
    }

    @Override
    public int getPollutionImpact()
    {
        return POLLUTION_REDUCTION;
    }

    @Override
    public ConstructionType getType()
    {
        return ConstructionType.GRASS;
    }
}
