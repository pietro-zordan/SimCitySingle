package model;

import policies.Policy;

/* Gestisce le statistiche, il budget e la policy della città.
   Calcola i valori globali usando le costruzioni presenti nella griglia. */
public class City
{
    private static final int INITIAL_BUDGET = 2500;
    private Policy currentPolicy;

    private int population;
    private int pollution;
    private int economy;
    private int budget;
    private int happiness;
    private int unemployed;
    private int maintenance;

    private final Grid grid;

    // Crea una nuova città con il budget iniziale prestabilito.
    public City(Grid grid, Policy initialPolicy)
    {
        this(grid, initialPolicy, INITIAL_BUDGET);
    }

    // Crea una città usando il budget recuperato da un salvataggio.
    public City(
            Grid grid,
            Policy initialPolicy,
            int initialBudget)
    {
        if (grid == null)
        {
            throw new IllegalArgumentException(
                    "The grid cannot be null"
            );
        }

        if (initialPolicy == null)
        {
            throw new IllegalArgumentException(
                    "The initial policy cannot be null"
            );
        }

        if (initialBudget < 0)
        {
            throw new IllegalArgumentException(
                    "The initial budget cannot be negative"
            );
        }

        this.grid = grid;
        this.currentPolicy = initialPolicy;
        this.budget = initialBudget;

        recalculateStatistics();
    }

    /* Aggiorna le costruzioni, ricalcola le statistiche
       e aggiunge al budget la produzione economica del tick. */
    public void updateOfOneTick()
    {
        grid.updateOfOneTick();
        recalculateStatistics();
        budget += economy - maintenance;
    }

    /* Ricalcola le statistiche usando tutte le costruzioni presenti.
       Applica poi gli effetti della policy e calcola i disoccupati. */
    private void recalculateStatistics()
    {
        population = 0;
        pollution = 0;
        economy = 0;
        happiness = 0;
        maintenance = 0;

        int totalEmployeeSlots = 0;

        for (Construction construction
                : grid.getConstructions())
        {
            population += construction.getPopulation();
            pollution += construction.getPollutionImpact();
            economy += construction.getMoneyProduction();
            happiness += construction.getHappinessImpact();
            maintenance += construction.getMaintenanceCost();

            totalEmployeeSlots +=
                    construction.getNumberOfEmployee();
        }

        pollution = currentPolicy.modifyPollution(pollution);
        economy = currentPolicy.modifyEconomy(economy);

        unemployed = population - totalEmployeeSlots;
    }

    // Restituisce il numero di cittadini senza un posto di lavoro.
    public int getGlobalUnemployed()
    {
        return unemployed;
    }

    // Calcola il costo di una costruzione usando la policy attiva.
    public int calculatePlacementCost(Construction construction)
    {
        return currentPolicy.calculatePlacementCost(construction);
    }

    // Sostituisce la policy attiva se quella ricevuta non è null.
    public void setPolicy(Policy newPolicy)
    {
        if (newPolicy != null )
        {
            currentPolicy = newPolicy;
        }
    }

    // Modifica il budget impedendo che diventi negativo.
    public void updateBudget(int amount)
    {
        if(budget + amount < 0) {
            throw new IllegalArgumentException(
                    "budget is insufficient for this construction"
            );
        } else {
            budget += amount;
        }
    }

    // Restituisce la policy attualmente applicata alla città.
    public Policy getCurrentPolicy()
    {
        return currentPolicy;
    }

    // Restituisce la popolazione totale.
    public int getGlobalPopulation()
    {
        return population;
    }

    // Restituisce l'inquinamento totale dopo l'applicazione della policy.
    public int getGlobalPollution()
    {
        return pollution;
    }

    // Restituisce l'economia totale dopo l'applicazione della policy.
    public int getGlobalEconomy()
    {
        return economy;
    }

    // Restituisce la felicità totale della città.
    public int getGlobalHappiness()
    {
        return happiness;
    }

    // Riduce la felicità globale della quantità indicata.
    public void decreaseGlobalHappiness(int amount)
    {
        happiness-=amount;
    }

    // Restituisce il budget disponibile.
    public int getBudget()
    {
        return budget;
    }

    /* Controlla popolazione e budget, crea la costruzione richiesta
       e delega alla griglia la verifica della posizione. */
    public void placeConstruction(ConstructionType type, int row, int column)
    {
        if (type == null)
        {
            throw new IllegalArgumentException(
                    "model.Construction type cannot be null"
            );
        }

        if ((type == ConstructionType.INDUSTRIAL ||
                type == ConstructionType.COMMERCIAL) &&
                unemployed <= 0)
        {
            throw new IllegalStateException(
                    "Cannot build Industrial or Commercial: you need a bigger population"
            );
        }

        Construction construction = ConstructionFactory.create(type);

        int cost = calculatePlacementCost(construction);

        if (!construction.isPlacementAllowed(pollution))
        {
            throw new IllegalStateException(" Pollution is too high cannot place industrial");
        }

        if (budget + cost < 0)
        {
            throw new IllegalStateException(
                    "Insufficient budget: this construction costs "
                            + (-cost) + " but only " + budget + " is available"
            );
        }

        // Grid controlla la posizione, la cella e il collegamento alla strada.
        grid.placeConstruction(construction, row, column);

        updateBudget(cost);
        recalculateStatistics();
    }


    // Forza il ricalcolo delle statistiche dopo una modifica esterna alla griglia.
    public void refreshStatistics()
    {
        recalculateStatistics();
    }


}