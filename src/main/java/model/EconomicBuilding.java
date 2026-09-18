package model;

public abstract class EconomicBuilding extends Construction
                                        implements EconomyBoostable
{
    private static final double INITIAL_ECONOMY_GROWTH_RATE = 1.0;
    private static final double ECONOMY_GROWTH_INCREMENT = 0.1;

    private double economyGrowthRate;
    private double economicBoost;

    /* Inizializza l'edificio con il tasso di crescita economica di base
       e senza alcun bonus economico temporaneo. */

    protected EconomicBuilding(
            int powerConsumption,
            int placementPrice)
    {
        super(0, powerConsumption, placementPrice);

        economyGrowthRate = INITIAL_ECONOMY_GROWTH_RATE;
        economicBoost=0;
    }
    // Restituisce il tasso attuale di crescita economica.
    @Override
    public double getEconomyGrowthRate()
    {
        return economyGrowthRate;
    }



    /* Aggiorna la produzione di denaro in base all'alimentazione
   e aumenta gradualmente il tasso di crescita fino al limite massimo. */

    @Override
    public void updateOfOneTick()
    {
        if (isPowered())
        {
            // Ricalcola sempre la produzione.
            moneyProduction = (int)
                    (getBaseMoneyProduction()
                            * getEffectiveEconomyGrowthRate());

            // Impedisce alla produzione di superare il massimo.
            moneyProduction = Math.min(
                    moneyProduction,
                    getMaxMoneyProduction()
            );

            // Aumenta il tasso per il tick successivo.
            if (economyGrowthRate
                    < getMaxEconomyGrowthRate())
            {
                economyGrowthRate +=
                        ECONOMY_GROWTH_INCREMENT;

                // Impedisce al growth rate di superare il massimo.
                economyGrowthRate = Math.min(
                        economyGrowthRate,
                        getMaxEconomyGrowthRate()
                );
            }
        }
        else
        {
            // Azzera la produzione quando l'edificio non è alimentato.
            moneyProduction = 0;
        }
    }

    // Calcola il tasso economico includendo il bonus senza superare il massimo.
    private double getEffectiveEconomyGrowthRate()
    {
        return Math.min(
                economyGrowthRate + economicBoost,
                getMaxEconomyGrowthRate()
        );
    }

    // Applica il bonus economico temporaneo ricevuto da un evento.
    @Override
    public void applyEconomicBoost(double boost)
    {
        if (boost < 0)
        {
            throw new IllegalArgumentException(
                    "Economic boost cannot be negative"
            );
        }

        economicBoost = boost;
    }

    // Rimuove il bonus economico temporaneo precedentemente applicato.
    @Override
    public void removeEconomicBoost()
    {
        economicBoost = 0;
    }


    // Imposta il tasso di crescita se il valore rispetta i limiti consentiti.
    public void setEconomyGrowthRate(double growthRate)
    {
        if (growthRate >= 0
                && growthRate <= getMaxEconomyGrowthRate())
        {
            economyGrowthRate = growthRate;
        }
    }

    // Restituisce il massimo tasso di crescita previsto dalla sottoclasse.
    public abstract double getMaxEconomyGrowthRate();

    // Restituisce la produzione economica di base prevista dalla sottoclasse.
    protected abstract double getBaseMoneyProduction();

    // Restituisce la produzione economica massima prevista dalla sottoclasse.
    protected abstract int getMaxMoneyProduction();


    /* Ripristina la produzione di denaro e il tasso di crescita economica
    utilizzando i valori recuperati dal salvataggio. */
    @Override
    public void restoreState(
            int population,
            int populationGrowthRate,
            int populationDecreaseRate,
            double economyGrowthRate,
            int moneyProduction)
    {
        super.restoreState(
                population,
                populationGrowthRate,
                populationDecreaseRate,
                economyGrowthRate,
                moneyProduction
        );

        setEconomyGrowthRate(economyGrowthRate);
    }
}