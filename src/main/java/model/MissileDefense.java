package model;

/*
 * Rappresenta il sistema di difesa missilistica globale della città.
 * Non occupa una cella della griglia: viene acquistato come potenziamento
 * e può assorbire fino a tre impatti prima di richiedere una riparazione.
 */
public class MissileDefense
{
    public static final int UNLOCK_TICK = 198;
    public static final int PURCHASE_COST = 20000;
    public static final int REPAIR_COST = 300;
    public static final int MAX_HITS = 3;

    private boolean purchased;
    private int hitsRemaining = MAX_HITS;

    public boolean isPurchased()
    {
        return purchased;
    }

    public int getHitsRemaining()
    {
        return hitsRemaining;
    }

    public boolean isActive()
    {
        return purchased && hitsRemaining > 0;
    }

    public boolean purchase()
    {
        if (purchased)
        {
            return false;
        }

        purchased = true;
        hitsRemaining = MAX_HITS;
        return true;
    }

    // Restituisce true soltanto quando il missile viene realmente intercettato.
    public boolean absorbMissile()
    {
        if (!isActive())
        {
            return false;
        }

        hitsRemaining--;
        return true;
    }

    // Ripara un singolo punto di resistenza: anche da 0 il sistema torna operativo.
    public boolean repairOneHit()
    {
        if (!purchased
                || hitsRemaining >= MAX_HITS)
        {
            return false;
        }

        hitsRemaining++;
        return true;
    }

    public void restoreState(
            boolean purchased,
            int hitsRemaining)
    {
        this.purchased = purchased;

        if (!purchased)
        {
            this.hitsRemaining = MAX_HITS;
            return;
        }

        this.hitsRemaining =
                Math.max(
                        0,
                        Math.min(
                                MAX_HITS,
                                hitsRemaining
                        )
                );
    }
}
