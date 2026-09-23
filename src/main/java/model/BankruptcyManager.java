package model;

/*
 * Controlla il fallimento economico della città.
 * La partita viene considerata persa quando per 15 tick consecutivi
 * il budget è negativo e la produzione economica è inferiore
 * ai costi di manutenzione.
 */
public class BankruptcyManager
{
    private static final int BANKRUPTCY_TICKS = 15;

    private int criticalTicks = 0;

    // Aggiorna il contatore in base alla situazione economica corrente.
    public void update(
            int budget,
            int economy,
            int maintenance)
    {
        if (budget < 0
                && economy < maintenance)
        {
            criticalTicks++;
        }
        else
        {
            criticalTicks = 0;
        }
    }

    // Comunica se la città ha raggiunto la condizione di fallimento.
    public boolean isBankrupt()
    {
        return criticalTicks >= BANKRUPTCY_TICKS;
    }

    // Restituisce da quanti tick consecutivi la città è in situazione critica.
    public int getCriticalTicks()
    {
        return criticalTicks;
    }

    public void restoreCriticalTicks(int criticalTicks)
    {
        if (criticalTicks < 0)
        {
            throw new IllegalArgumentException(
                    "Critical ticks cannot be negative"
            );
        }

        this.criticalTicks = criticalTicks;
    }
}
