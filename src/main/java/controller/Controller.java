package controller;

import Events.EventType;
import model.*;
import policies.Policy;
import policies.PolicyFactory;
import policies.PolicyType;
import policies.StandardPolicy;
import progress.Progress;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;


/* Collega la GUI al modello e coordina città, griglia e simulazione.
   Notifica inoltre gli osservatori quando lo stato del gioco cambia. */
public final class Controller
{
    private final Grid grid;
    private final City city;
    private final Simulation simulation;
    private final AchievementManager achievementManager;
    private final List<GameObserver> observers = new ArrayList<>();

    /*
     * Informazioni sufficienti alla GUI per disegnare una cella,
     * senza permetterle di modificare direttamente model.Cell o model.Grid.
     */
    public record CellState(
            boolean empty,
            ConstructionType type,
            boolean onFire,
            boolean powered,
            boolean boosted)
    {
    }

    // Crea un controller con una nuova griglia e la policy standard.
    public Controller()
    {
        this(new Grid(), new StandardPolicy());
    }

    /*
     * Questo costruttore è utile anche nei test:
     * puoi passare una griglia o una policy differente.
     */
    public Controller(Grid grid, Policy initialPolicy)
    {
        this(grid, initialPolicy, new AchievementManager());
    }

    // Crea un controller ripristinando i dati di una partita salvata.
    public Controller(
            Grid grid,
            Policy initialPolicy,
            int budget,
            int currentTick,
            int lastPolicyChangeTick)
    {
        this(
                grid,
                initialPolicy,
                budget,
                currentTick,
                lastPolicyChangeTick,
                false
        );
    }

    public Controller(
            Grid grid,
            Policy initialPolicy,
            int budget,
            int currentTick,
            int lastPolicyChangeTick,
            boolean tsunamiInsuranceActive)
    {
        this(
                grid,
                initialPolicy,
                budget,
                currentTick,
                lastPolicyChangeTick,
                tsunamiInsuranceActive,
                new AchievementManager()
        );
    }

    public Controller(Grid grid, Policy initialPolicy,
                      AchievementManager achievementManager)
    {
        this.grid = Objects.requireNonNull(
                grid,
                "model.Grid cannot be null"
        );

        Objects.requireNonNull(
                initialPolicy,
                "Initial policy cannot be null"
        );

        this.achievementManager =
                Objects.requireNonNull(
                        achievementManager,
                        "Achievement manager cannot be null"
                );

        this.city = new City(grid, initialPolicy);
        this.simulation = new Simulation(city, grid);
    }

    public Controller(
            Grid grid,
            Policy initialPolicy,
            int budget,
            int currentTick,
            int lastPolicyChangeTick,
            boolean tsunamiInsuranceActive,
            AchievementManager achievementManager)
    {
        this.grid = Objects.requireNonNull(
                grid,
                "model.Grid cannot be null"
        );

        Objects.requireNonNull(
                initialPolicy,
                "Initial policy cannot be null"
        );

        this.achievementManager =
                Objects.requireNonNull(
                        achievementManager,
                        "Achievement manager cannot be null"
                );

        this.city = new City(
                grid,
                initialPolicy,
                budget
        );

        this.simulation = new Simulation(
                city,
                grid,
                currentTick,
                lastPolicyChangeTick,
                tsunamiInsuranceActive
        );
    }

    public Controller(AchievementManager achievementManager)
    {
        this(new Grid(), new StandardPolicy(), achievementManager);
    }

    // Registra un osservatore che verrà aggiornato quando cambia il gioco.
    public void addObserver(GameObserver observer)
    {
        if (observer == null)
        {
            throw new NullPointerException(
                    "Game observer cannot be null"
            );
        }

        if (!observers.contains(observer))
        {
            observers.add(observer);
        }
    }

    // Rimuove un osservatore dall'elenco degli osservatori registrati.
    public void removeObserver(GameObserver gameObserver) {
        observers.remove(gameObserver);
    }

    // Richiede a tutti gli osservatori di aggiornare la schermata di gioco.
    private void notifyObservers() {
        for (GameObserver gameObserver : observers) {
            gameObserver.refreshGameView();
        }
    }

    /*
     * Unico metodo che la GUI deve usare per costruire.
     * La GUI non deve più chiamare model.Cell.placeConstruction().
     */
    public CellState placeConstruction(
            ConstructionType type,
            int row,
            int column)
    {
        if (type == null)
        {
            throw new IllegalArgumentException(
                    "Construction type cannot be null"
            );
        }

        Construction construction =
                ConstructionFactory.create(type);

        if (simulation.getCurrentTick()
                < construction.getUnlockTick())
        {
            throw new IllegalStateException(
                    "This construction is available from tick "
                            + construction.getUnlockTick()
            );
        }

        city.placeConstruction(
                construction,
                row,
                column
        );

        achievementManager.onConstructionPlaced(type);
        notifyObservers();

        return getCellState(
                row,
                column
        );
    }

    /*
     * Unico metodo che la GUI e gli eventi devono usare
     * per rimuovere una costruzione.
     */
    public void removeConstruction(int row, int column)
    {
        grid.removeConstruction(row, column);
        city.refreshStatistics();
        notifyObservers();
    }

    public void removeConstructionByPlayer(int row, int column)
    {
        requireValidPosition(row, column);
        CellState state = getCellState(row, column);

        if (state.empty())
        {
            throw new IllegalStateException(
                    "There is no construction to remove"
            );
        }

        if (!simulation.canRemoveConstruction())
        {
            throw new IllegalStateException(
                    "No demolitions available"
            );
        }

        grid.removeConstruction(row, column);

        simulation.registerRemoval();

        city.refreshStatistics();
        notifyObservers();
    }

    // Fa avanzare l'intero modello di un tick.
    public boolean updateOfOneTick()
    {
        boolean criminalActivityCreated =
                simulation.updateOfOneTick();

        notifyObservers();

        return criminalActivityCreated;
    }

    public boolean isFreemasonryInvitationPending()
    {
        return simulation.isFreemasonryInvitationPending();
    }

    public FreemasonryChoice getFreemasonryChoice()
    {
        return simulation.getFreemasonryChoice();
    }

    public boolean chooseFreemasonry(FreemasonryChoice choice)
    {
        boolean chosen = simulation.chooseFreemasonry(choice);

        if (chosen)
        {
            notifyObservers();
        }

        return chosen;
    }

    public void restoreFreemasonryChoice(
            FreemasonryChoice choice)
    {
        simulation.restoreFreemasonryChoice(choice);
    }

    // Verifica se è possibile cambiare la policy attiva.
    public boolean canChangePolicy()
    {
        return simulation.canChangePolicy();
    }

    // Crea la policy richiesta e prova ad applicarla alla simulazione.
    public boolean changePolicy(PolicyType policyType)
    {
        Policy newPolicy = PolicyFactory.create(policyType);

        return changePolicy(newPolicy);
    }

    // Applica la nuova policy e aggiorna gli osservatori se il cambio riesce.
    private boolean changePolicy(Policy newPolicy)
    {
        boolean changed = simulation.changePolicy(newPolicy);
        if (changed) {
            notifyObservers();
        }
        return changed;
    }

    // Restituisce quanti tick mancano prima di poter cambiare la policy.
    public int getTicksToPolicyChange(){
        return simulation.getTicksToPolicyChange();
    }

    /*
     * Restituisce una fotografia della cella.
     * Non restituisce direttamente model.Cell, impedendo alla GUI
     * di bypassare le regole del dominio.
     */
    public CellState getCellState(int row, int column)
    {
        requireValidPosition(row, column);

        boolean onFire = simulation.isCellOnFire(row, column);

        Construction construction =
                grid.getCell(row, column).getConstruction();

        if (construction == null)
        {
            return new CellState(
                    true,
                    null,
                    false,
                    false,
                    false
            );
        }

        boolean powered = construction.isPowered();
        boolean boosted = simulation.isConstructionBoosted(construction);

        return new CellState(
                false,
                construction.getType(),
                onFire,
                powered,
                boosted
        );
    }

    public boolean canPlaceConstructionCompany()
    {
        return simulation.canPlaceConstructionCompany();
    }

    public boolean canPlaceBank()
    {
        return simulation.canPlaceBank();
    }

    public boolean canRequestLoan()
    {
        return simulation.canRequestLoan();
    }

    public boolean requestLoan(int amount)
    {
        boolean requested =
                simulation.requestLoan(amount);

        if (requested)
        {
            notifyObservers();
        }

        return requested;
    }

    public int getMaxLoanAmount()
    {
        return simulation.getMaxLoanAmount();
    }

    public void restoreBankState(
            int lastLoanTick,
            boolean loanActive,
            int loanAmount,
            int loanStartTick,
            int remainingDebt)
    {
        simulation.restoreBankState(
                lastLoanTick,
                loanActive,
                loanAmount,
                loanStartTick,
                remainingDebt
        );
    }

    public boolean hasBanks()
    {
        return simulation.hasBanks();
    }

    public boolean canBuyTsunamiInsurance()
    {
        return simulation.canBuyTsunamiInsurance();
    }

    public boolean buyTsunamiInsurance()
    {
        boolean bought =
                simulation.buyTsunamiInsurance();

        if (bought)
        {
            notifyObservers();
        }

        return bought;
    }

    public int getTsunamiInsuranceCost()
    {
        return simulation.getTsunamiInsuranceCost();
    }

    public int getTsunamiInsuranceBuildingCount()
    {
        return simulation
                .getTsunamiInsuranceBuildingCount();
    }

    public int getTsunamiInsuranceDiscountPercentage()
    {
        return simulation
                .getTsunamiInsuranceDiscountPercentage();
    }

    public boolean isTsunamiInsuranceActive()
    {
        return simulation
                .isTsunamiInsuranceActive();
    }

    public boolean canBuyNuclearFireProtection()
    {
        return simulation.canBuyNuclearFireProtection();
    }

    public int getNuclearFireProtectionCost()
    {
        return simulation.getNuclearFireProtectionCost();
    }

    public int getNuclearPlantsNeedingFireProtectionCount()
    {
        return simulation.getNuclearPlantsNeedingFireProtectionCount();
    }

    public int getNuclearFireProtectionDiscountPercentage()
    {
        return simulation.getNuclearFireProtectionDiscountPercentage();
    }

    public boolean buyNuclearFireProtection()
    {
        boolean bought = simulation.buyNuclearFireProtection();

        if (bought)
        {
            notifyObservers();
        }

        return bought;
    }

    public void startTsunamiReconstruction()
    {
        simulation.startTsunamiReconstruction();
    }

    public void restoreTsunamiReconstruction(
            List<ReconstructionEntry> entries,
            String direction)
    {
        simulation.restoreTsunamiReconstruction(
                entries,
                direction
        );
    }

    // Restituisce alla GUI le esplosioni non ancora mostrate e le rimuove dalla coda della griglia.
    public List<ExplosionInfo> consumeExplosions()
    {
        return grid.consumeExplosions();
    }

    public Achievement consumeNewlyUnlockedAchievement()
    {
        return achievementManager
                .consumeNewlyUnlockedAchievement();
    }

    // Restituisce il numero di righe della griglia.
    public int getNumberOfRows()
    {
        return grid.getNumberOfRows();
    }

    // Restituisce il numero di colonne della griglia.
    public int getNumberOfColumns()
    {
        return grid.getNumberOfColumns();
    }

    // Restituisce il budget disponibile.
    public int getBudget()
    {
        return city.getBudget();
    }

    // Restituisce la popolazione totale della città.
    public int getPopulation()
    {
        return city.getGlobalPopulation();
    }

    // Restituisce l'inquinamento totale della città.
    public int getPollution()
    {
        return city.getGlobalPollution();
    }

    // Restituisce l'economia totale della città.
    public int getEconomy()
    {
        return city.getGlobalEconomy();
    }

    // Restituisce la felicità totale della città.
    public int getHappiness()
    {
        return city.getGlobalHappiness();
    }

    // Restituisce il numero di cittadini disoccupati.
    public int getUnemployed(){return city.getGlobalUnemployed(); }

    // Restituisce l'energia elettrica realmente consumata dalla città.
    public int getEnergyConsumed()
    {
        return city.getEnergyConsumed();
    }

    // Restituisce la potenza totale disponibile dalle centrali attive.
    public int getEnergyAvailable()
    {
        return city.getEnergyAvailable();
    }

    // Restituisce il consumo energetico totale richiesto da tutte le costruzioni.
    public int getTotalEnergyDemand()
    {
        return city.getTotalEnergyDemand();
    }

    // Restituisce il limite massimo di energia servibile dalla simulazione.
    public int getMaxEnergyServed()
    {
        return simulation.getMaxEnergyServed();
    }

    // Indica alla GUI quando deve comparire il pulsante della centrale nucleare.
    public boolean shouldShowNuclearPlant()
    {
        return grid.shouldShowNuclearPlant();
    }

    // Restituisce l'energia utilizzata dalla centrale presente nella cella indicata.
    public int getPowerPlantEnergyConsumed(int row, int column)
    {
        requireValidPosition(row, column);

        Construction construction =
                grid.getCell(row, column).getConstruction();

        if (construction instanceof PowerPlant powerPlant)
        {
            return grid.getPowerPlantUsedPower(
                    powerPlant
            );
        }

        return 0;
    }

    // Restituisce la capacità massima della centrale presente nella cella indicata.
    public int getPowerPlantEnergyCapacity(int row, int column)
    {
        requireValidPosition(row, column);

        Construction construction =
                grid.getCell(row, column).getConstruction();

        if (construction instanceof PowerPlant powerPlant)
        {
            return powerPlant.getPowerCapacity();
        }

        return 0;
    }

    // Restituisce il nome della policy attualmente attiva.
    public String getCurrentPolicyName()
    {
        return city.getCurrentPolicy().getName();
    }

    // Restituisce il numero del tick corrente.
    public int getCurrentTick() {return simulation.getCurrentTick();}

    /* Calcola il costo di una costruzione usando la policy attiva,
       senza piazzarla realmente nella griglia. */
    public int getPlacementCost(ConstructionType type)
    {
        // Crea una costruzione temporanea usata soltanto per calcolare il costo.
        Construction construction = ConstructionFactory.create(type);
        return city.calculatePlacementCost(construction);
    }

    // Verifica che la posizione indicata si trovi all'interno della griglia.
    private void requireValidPosition(int row, int column)
    {
        if (!grid.isInside(row, column))
        {
            throw new IllegalArgumentException(
                    "Position outside the grid: row="
                            + row
                            + ", column="
                            + column
            );
        }
    }

    // Restituisce la direzione dello tsunami attivo.
    public String getActiveTsunamiDirection()
    {
        return simulation.getActiveTsunamiDirection();
    }

    // Restituisce l'avanzamento dello tsunami attivo.
    public int getActiveTsunamiAdvancementLength()
    {
        return simulation.getActiveTsunamiAdvancementLength();
    }

    // Crea i dati necessari per salvare lo stato attuale della partita.
    public Progress createProgress()
    {
        return Progress.fromGame(
                grid,
                city,
                simulation
        );
    }

    public int getActiveMissileTargetRow()
    {
        return simulation.getActiveMissileTargetRow();
    }

    public int getActiveMissileTargetColumn()
    {
        return simulation.getActiveMissileTargetColumn();
    }

    public boolean isActiveMissileIntercepted()
    {
        return simulation.isActiveMissileIntercepted();
    }

    public boolean isMissileDefenseAvailable()
    {
        return simulation.isMissileDefenseAvailable();
    }

    public boolean canBuyMissileDefense()
    {
        return simulation.canBuyMissileDefense();
    }

    public boolean buyMissileDefense()
    {
        boolean bought =
                simulation.buyMissileDefense();

        if (bought)
        {
            notifyObservers();
        }

        return bought;
    }

    public boolean canRepairMissileDefense()
    {
        return simulation.canRepairMissileDefense();
    }

    public boolean repairMissileDefense()
    {
        boolean repaired =
                simulation.repairMissileDefense();

        if (repaired)
        {
            notifyObservers();
        }

        return repaired;
    }

    public boolean repairMissileDefenseFully()
    {
        boolean repaired =
                simulation.repairMissileDefenseFully();

        if (repaired)
        {
            notifyObservers();
        }

        return repaired;
    }

    public boolean isMissileDefensePurchased()
    {
        return simulation.isMissileDefensePurchased();
    }

    public boolean isMissileDefenseActive()
    {
        return simulation.isMissileDefenseActive();
    }

    public int getMissileDefenseHitsRemaining()
    {
        return simulation.getMissileDefenseHitsRemaining();
    }

    public int getMissileDefensePurchaseCost()
    {
        return simulation.getMissileDefensePurchaseCost();
    }

    public int getMissileDefensePurchaseDiscountPercentage()
    {
        return simulation
                .getMissileDefensePurchaseDiscountPercentage();
    }

    public int getMissileDefenseRepairCost()
    {
        return simulation.getMissileDefenseRepairCost();
    }

    public int getMissileDefenseFullRepairCost()
    {
        return simulation.getMissileDefenseFullRepairCost();
    }

    public void restoreMissileDefenseState(
            boolean purchased,
            int hitsRemaining)
    {
        simulation.restoreMissileDefenseState(
                purchased,
                hitsRemaining
        );
    }

    public boolean isAchievementUnlocked(
            Achievement achievement)
    {
        return achievementManager.isUnlocked(
                achievement
        );
    }

    // Restituisce il tipo dell'evento attualmente attivo.
    public EventType getActiveEventType()
    {
        return simulation.getActiveEventType();
    }

    public boolean isGameOver()
    {
        return simulation.isGameOver();
    }

    public void restoreBankruptcyState(int criticalTicks)
    {
        simulation.restoreBankruptcyState(criticalTicks);
    }

    public boolean isConstructionUnlocked(
            ConstructionType type)
    {
        Construction construction =
                ConstructionFactory.create(type);

        return simulation.getCurrentTick()
                >= construction.getUnlockTick();
    }

    public int getRemovedCriminalActivities()
    {
        return simulation.getRemovedCriminalActivities();
    }

    public int getRemovedTerroristicGroups()
    {
        return simulation.getRemovedTerroristicGroups();
    }

    public int getRemovedTerroristicGroupsByMilitary()
    {
        return simulation.getRemovedTerroristicGroupsByMilitary();
    }

    public boolean wasTerroristicGroupCreated()
    {
        return simulation.wasTerroristicGroupCreated();
    }

}
