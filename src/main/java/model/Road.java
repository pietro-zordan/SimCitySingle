package model;

/* Rappresenta una strada della città.
   Permette alle altre costruzioni di essere collegate alla rete stradale. */
public class Road extends Construction {
    private static final int MONEY_PRODUCTION = 0;
    private static final int POWER = 0;
    private static final int PLACEMENT_PRICE = -50;

    // Inizializza la strada con i suoi valori prestabiliti.
    public Road() {
        super(MONEY_PRODUCTION, POWER, PLACEMENT_PRICE);
    }

    // Restituisce il tipo corrispondente alla strada.
    @Override
    public ConstructionType getType()
    {
        return ConstructionType.ROAD;
    }

    // Impedisce la rimozione manuale della strada. (regole del gioco)
    @Override
    public boolean canBeRemoved()
    {
        return false;
    }
}
