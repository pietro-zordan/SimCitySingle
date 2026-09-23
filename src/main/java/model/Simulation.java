package model;

import Events.*;
import policies.Policy;

import java.util.List;
import java.util.Random;

/* Gestisce lo scorrere dei tick della partita.
   Aggiorna la città e gli eventi e controlla quando è possibile cambiare policy. */
public class Simulation{

    private static final int POLICY_CHANGE_INTERVAL = 12;
    private static final int CC_UNLOCK_TICK = 40;
    private static final int CC_INTERVAL = 10;
    private static final int FREEMASONRY_INVITATION_TICK = 500;

    private final Random random = new Random();
    private int currentTick;
    private int lastPolicyChangeTick;
    private FreemasonryChoice freemasonryChoice =
            FreemasonryChoice.PENDING;
    private final City city;
    private Event activeEvent;
    private int eventTicksPassed;
    private final Grid grid;
    private int usedRemovals = 0;
    private int lastRemovalResetTick = 0;
    private final BankruptcyManager bankruptcyManager;
    private final BankManager bankManager;
    private final InsuranceManager insuranceManager;
    private final MissileDefense missileDefense;
    private final TerrorManager terrorManager;
    private boolean terroristicGroupCreated;

    private final CrimeManager crimeManager;
    private int removedCriminalActivities;
    private int removedTerroristicGroups;

    // Crea una nuova simulazione partendo dal tick zero.
    public Simulation(City city, Grid grid)
    {
        this(
                city,
                grid,
                0,
                0,
                false
        );
    }

    // Crea una simulazione usando i tick recuperati da un salvataggio.
    public Simulation(
            City city,
            Grid grid,
            int currentTick,
            int lastPolicyChangeTick)
    {
        this(
                city,
                grid,
                currentTick,
                lastPolicyChangeTick,
                false
        );
    }

    public Simulation(
            City city,
            Grid grid,
            int currentTick,
            int lastPolicyChangeTick,
            boolean tsunamiInsuranceActive)
    {
        if (city == null)
        {
            throw new IllegalArgumentException(
                    "The city cannot be null"
            );
        }

        if (grid == null)
        {
            throw new IllegalArgumentException(
                    "The grid cannot be null"
            );
        }

        if (currentTick < 0)
        {
            throw new IllegalArgumentException(
                    "Current tick cannot be negative"
            );
        }

        if (lastPolicyChangeTick < 0
                || lastPolicyChangeTick > currentTick)
        {
            throw new IllegalArgumentException(
                    "Invalid last policy change tick"
            );
        }

        this.city = city;
        this.grid = grid;
        this.currentTick = currentTick;
        this.lastPolicyChangeTick = lastPolicyChangeTick;
        this.bankruptcyManager = new BankruptcyManager();
        this.bankManager = new BankManager(city, grid);
        this.insuranceManager = new InsuranceManager(city, grid, tsunamiInsuranceActive);
        this.missileDefense = new MissileDefense();
        this.terrorManager = new TerrorManager(city, grid);
        this.crimeManager = new CrimeManager(city, grid);
    }

    // Controlla se sono trascorsi almeno dodici tick dall'ultimo cambio di policy.
    public boolean canChangePolicy()
    {
        int passedTicks = currentTick - lastPolicyChangeTick;

        return passedTicks >= POLICY_CHANGE_INTERVAL;
    }

    public FreemasonryChoice getFreemasonryChoice()
    {
        return freemasonryChoice;
    }

    public boolean isFreemasonryInvitationPending()
    {
        return currentTick >= FREEMASONRY_INVITATION_TICK
                && freemasonryChoice == FreemasonryChoice.PENDING;
    }

    public boolean chooseFreemasonry(FreemasonryChoice choice)
    {
        if (!isFreemasonryInvitationPending()
                || choice == null
                || choice == FreemasonryChoice.PENDING)
        {
            return false;
        }

        freemasonryChoice = choice;
        return true;
    }

    public void restoreFreemasonryChoice(
            FreemasonryChoice choice)
    {
        freemasonryChoice = choice == null
                ? FreemasonryChoice.PENDING
                : choice;
    }

    // Cambia la policy se è valida e sono trascorsi abbastanza tick.
    public boolean changePolicy(Policy newPolicy)
    {
        if (newPolicy == null)
        {
            return false;
        }

        if (!canChangePolicy())
        {
            return false;
        }

        city.setPolicy(newPolicy);
        city.refreshStatistics();
        lastPolicyChangeTick = currentTick;

        return true;
    }

    // Restituisce quanti tick mancano prima di poter cambiare nuovamente policy.
    public int getTicksToPolicyChange() {
        int passedTicks = currentTick - lastPolicyChangeTick;
        int remaining = POLICY_CHANGE_INTERVAL - passedTicks;
        return Math.max(0, remaining);
    }

    public boolean canPlaceBank()
    {
        return bankManager.canPlaceBank(currentTick);
    }

    public boolean canPlaceConstructionCompany()
    {
        return currentTick >= CC_UNLOCK_TICK;
    }

    public boolean isMissileDefenseAvailable()
    {
        return currentTick >= MissileDefense.UNLOCK_TICK
                && grid.hasMilitaryBase();
    }

    public boolean canBuyMissileDefense()
    {
        return isMissileDefenseAvailable()
                && !missileDefense.isPurchased();
    }

    public boolean buyMissileDefense()
    {
        int cost = getMissileDefensePurchaseCost();

        if (!canBuyMissileDefense()
                || city.getBudget()
                < cost)
        {
            return false;
        }

        city.updateBudget(-cost);

        return missileDefense.purchase();
    }

    public boolean canRepairMissileDefense()
    {
        return missileDefense.isPurchased()
                && missileDefense.getHitsRemaining()
                < MissileDefense.MAX_HITS;
    }

    public boolean repairMissileDefense()
    {
        if (!canRepairMissileDefense()
                || city.getBudget()
                < MissileDefense.REPAIR_COST)
        {
            return false;
        }

        city.updateBudget(
                -MissileDefense.REPAIR_COST
        );

        return missileDefense.repairOneHit();
    }

    public boolean repairMissileDefenseFully()
    {
        int cost = getMissileDefenseFullRepairCost();

        if (!canRepairMissileDefense()
                || city.getBudget()
                < cost)
        {
            return false;
        }

        city.updateBudget(-cost);

        return missileDefense.repairAllHits();
    }

    public boolean isMissileDefensePurchased()
    {
        return missileDefense.isPurchased();
    }

    public boolean isMissileDefenseActive()
    {
        return missileDefense.isActive();
    }

    public int getMissileDefenseHitsRemaining()
    {
        return missileDefense.getHitsRemaining();
    }

    public int getMissileDefensePurchaseCost()
    {
        return MissileDefense.PURCHASE_COST
                * (100 - getMissileDefensePurchaseDiscountPercentage())
                / 100;
    }

    public int getMissileDefensePurchaseDiscountPercentage()
    {
        return Math.min(grid.getNumberOfMilitaryBases(), 6) * 5;
    }

    public int getMissileDefenseRepairCost()
    {
        return MissileDefense.REPAIR_COST;
    }

    public int getMissileDefenseFullRepairCost()
    {
        return (MissileDefense.MAX_HITS
                - missileDefense.getHitsRemaining())
                * MissileDefense.REPAIR_COST;
    }

    public void restoreMissileDefenseState(
            boolean purchased,
            int hitsRemaining)
    {
        missileDefense.restoreState(
                purchased,
                hitsRemaining
        );
    }

    // Crea casualmente uno degli eventi disponibili.
    private Event createRandomEvent()
    {
        Event[] possibleEvents = {
                new HackerAttack(city, grid),
                new EnergyCrisis(city, grid),
                new Fire(city, grid),
                new Tsunami(city, grid),
                new EconomicBoom(city, grid),
                new MissileAttack(
                        city,
                        grid,
                        missileDefense
                )
        };

        int selectedEvent = random.nextInt(possibleEvents.length);

        return possibleEvents[selectedEvent];
    }

    /* Aggiorna la città e l'evento attivo.
       Se non ci sono eventi, prova ad avviarne uno casuale e infine incrementa il tick. */
    public boolean updateOfOneTick()
    {
        if (isGameOver())
        {
            throw new IllegalStateException(
                    "The game is over"
            );
        }

        if (isFreemasonryInvitationPending())
        {
            throw new IllegalStateException(
                    "Choose an answer to the invitation before continuing"
            );
        }
        city.updateOfOneTick();

        if (isEventActive())
        {
            activeEvent.updateOfOneTick();
            eventTicksPassed++;

            if (activeEvent.isFinished(eventTicksPassed))
            {
                activeEvent.end();
                activeEvent = null;
                eventTicksPassed = 0;
            }
        }
        else
        {
            startEvent(createRandomEvent());
        }

        insuranceManager.updateReconstruction();

        // Prima agiscono le stazioni di polizia.
        int removedThreats =
                crimeManager
                        .tryToDestroyCriminalActivities(
                                currentTick
                        );

        removedCriminalActivities =
                crimeManager.getRemovedCriminalActivities();

        removedTerroristicGroups =
                crimeManager.getRemovedTerroristicGroups();

        if (removedThreats > 0)
        {
            city.refreshStatistics();
        }

        // Solo dopo può comparire una nuova attività criminale
        boolean criminalActivityCreated =
                crimeManager
                        .tryToCreateCriminalActivity(
                                currentTick
                        );

        currentTick++;

        if (currentTick
                - lastRemovalResetTick
                >= CC_INTERVAL)
        {
            usedRemovals = 0;
            lastRemovalResetTick = currentTick;
        }

        bankManager.update(currentTick);

        bankruptcyManager.update(
                city.getBudget(),
                city.getGlobalEconomy(),
                city.getMaintenance()
        );

        terroristicGroupCreated = terrorManager.updateOfOneTick();

        return criminalActivityCreated;
    }

    public void registerRemoval()
    {
        usedRemovals++;
    }

    public int getMaxLoanAmount()
    {
        return bankManager.getMaxLoanAmount();
    }

    public int getLastLoanTick()
    {
        return bankManager.getLastLoanTick();
    }

    public boolean isLoanActive()
    {
        return bankManager.isLoanActive();
    }

    public int getLoanAmount()
    {
        return bankManager.getLoanAmount();
    }

    public int getLoanStartTick()
    {
        return bankManager.getLoanStartTick();
    }

    public int getRemainingDebt()
    {
        return bankManager.getRemainingDebt();
    }

    public void restoreBankState(
            int lastLoanTick,
            boolean loanActive,
            int loanAmount,
            int loanStartTick,
            int remainingDebt)
    {
        bankManager.restoreState(
                lastLoanTick,
                loanActive,
                loanAmount,
                loanStartTick,
                remainingDebt
        );
    }

    public int getMaxEnergyServed()
    {
        return grid.getMaxEnergyServed();
    }

    // Controlla se è già presente un evento attivo.
    public boolean isEventActive()
    {
        return activeEvent!=null;
    }

    // Avvia l'evento ricevuto se non ci sono altri eventi attivi e l'estrazione ha successo.
    public void startEvent(Event event)
    {
        if (event == null || isEventActive())
            return;

        if (!event.canBeChosen(currentTick))
            return;

        event.randomProbability();

        if(event.canStart()){
            activeEvent = event;
            eventTicksPassed = 0;
            activeEvent.start();

            if (activeEvent instanceof Tsunami)
            {
                Tsunami tsunami =
                        (Tsunami) activeEvent;

                insuranceManager.registerTsunamiDamage(
                        tsunami.getDestroyedBuildings(),
                        tsunami.getDirection()
                );
            }
        }
    }

    // Controlla se una cella è coinvolta nell'incendio attivo.
    public boolean isCellOnFire(int row, int column)
    {
        if (activeEvent instanceof Fire)
        {
            return ((Fire) activeEvent).isCellOnFire(row, column);
        }

        return false;
    }

    public boolean requestLoan(int amount)
    {
        return bankManager.requestLoan(
                amount,
                currentTick
        );
    }

    public boolean canRequestLoan()
    {
        return bankManager.canRequestLoan(currentTick);
    }



    public boolean hasBanks()
    {
        return insuranceManager.hasBanks();
    }

    public boolean canBuyTsunamiInsurance()
    {
        return insuranceManager
                .canBuyTsunamiInsurance();
    }

    public boolean buyTsunamiInsurance()
    {
        return insuranceManager
                .buyTsunamiInsurance();
    }

    public int getTsunamiInsuranceCost()
    {
        return insuranceManager
                .getTsunamiInsuranceCost();
    }

    public int getTsunamiInsuranceBuildingCount()
    {
        return insuranceManager
                .getTsunamiInsuranceBuildingCount();
    }

    public int getTsunamiInsuranceDiscountPercentage()
    {
        return insuranceManager
                .getBankDiscountPercentage();
    }

    public boolean isTsunamiInsuranceActive()
    {
        return insuranceManager
                .isTsunamiInsuranceActive();
    }

    public boolean canBuyNuclearFireProtection()
    {
        return insuranceManager.canBuyNuclearFireProtection();
    }

    public int getNuclearFireProtectionCost()
    {
        return insuranceManager.getNuclearFireProtectionCost();
    }

    public int getNuclearPlantsNeedingFireProtectionCount()
    {
        return insuranceManager.getNuclearPlantsNeedingFireProtectionCount();
    }

    public int getNuclearFireProtectionDiscountPercentage()
    {
        return insuranceManager.getBankDiscountPercentage();
    }

    public boolean buyNuclearFireProtection()
    {
        return insuranceManager.buyNuclearFireProtection();
    }

    public void startTsunamiReconstruction()
    {
        insuranceManager
                .startTsunamiReconstruction();
    }

    public List<ReconstructionEntry>
    getPendingTsunamiReconstructions()
    {
        return insuranceManager
                .getPendingReconstructions();
    }

    public String getTsunamiReconstructionDirection()
    {
        return insuranceManager
                .getReconstructionDirection();
    }

    public void restoreTsunamiReconstruction(
            List<ReconstructionEntry> entries,
            String direction)
    {
        insuranceManager
                .restorePendingReconstruction(
                        entries,
                        direction
                );
    }

    // Controlla se una costruzione ha ricevuto il bonus del boom economico attivo.
    public boolean isConstructionBoosted(Construction construction) {
        if (activeEvent == null || construction == null) {
            return false;
        }

        if (activeEvent instanceof EconomicBoom)
        {
            return ((EconomicBoom)activeEvent).isBoosted(construction);
        }

        return false;
    }

    public int getAvailableRemovals()
    {
        return Math.max(
                0,
                grid.getNumberOfPoweredCC() - usedRemovals
        );
    }

    public boolean canRemoveConstruction()
    {
        return getAvailableRemovals() > 0;
    }


    // Restituisce il tick attuale della simulazione.
    public int getCurrentTick()
    {
        return currentTick;
    }

    // Restituisce il lato da cui arriva lo tsunami attivo.
    public String getActiveTsunamiDirection()
    {
        if (activeEvent instanceof Tsunami)
        {
            Tsunami tsunami = (Tsunami) activeEvent;
            return tsunami.getDirection();
        }

        return null;
    }

    // Restituisce il numero di righe o colonne colpite dallo tsunami attivo.
    public int getActiveTsunamiAdvancementLength()
    {
        if (activeEvent instanceof Tsunami)
        {
            Tsunami tsunami = (Tsunami) activeEvent;
            return tsunami.getAdvancementLength();
        }

        return 0;
    }

    public int getActiveMissileTargetRow()
    {
        if (activeEvent instanceof MissileAttack)
        {
            MissileAttack missileAttack =
                    (MissileAttack) activeEvent;

            return missileAttack.getTargetRow();
        }

        return -1;
    }

    public int getActiveMissileTargetColumn()
    {
        if (activeEvent instanceof MissileAttack)
        {
            MissileAttack missileAttack =
                    (MissileAttack) activeEvent;

            return missileAttack.getTargetColumn();
        }

        return -1;
    }

    public boolean isActiveMissileIntercepted()
    {
        if (activeEvent instanceof MissileAttack)
        {
            MissileAttack missileAttack =
                    (MissileAttack) activeEvent;

            return missileAttack.isIntercepted();
        }

        return false;
    }

    // Restituisce il tick in cui è stata cambiata l'ultima policy.
    public int getLastPolicyChangeTick()
    {
        return lastPolicyChangeTick;
    }

    // Restituisce il tipo dell'evento attivo oppure null se non ce ne sono.
    public EventType getActiveEventType()
    {
        if (isEventActive())
        {
            return activeEvent.getType();
        }

        return null;
    }

    public boolean isGameOver()
    {
        return bankruptcyManager.isBankrupt();
    }

    public int getCriticalTicks()
    {
        return bankruptcyManager.getCriticalTicks();
    }

    public void restoreBankruptcyState(int criticalTicks)
    {
        bankruptcyManager.restoreCriticalTicks(criticalTicks);
    }

    public int getRemovedCriminalActivities()
    {
        return removedCriminalActivities;
    }

    public int getRemovedTerroristicGroups()
    {
        return removedTerroristicGroups;
    }

    public int getRemovedTerroristicGroupsByMilitary()
    {
        return crimeManager.getRemovedTerroristicGroupsByMilitary();
    }

    public boolean wasTerroristicGroupCreated()
    {
        return terroristicGroupCreated;
    }

}
