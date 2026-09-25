package progress;

import controller.Controller;
import model.AchievementManager;
import model.City;
import model.Construction;
import model.ConstructionType;
import model.Grid;
import model.FreemasonryChoice;
import model.ReconstructionEntry;
import model.Simulation;
import policies.Policy;
import policies.PolicyFactory;
import policies.PolicyType;

import java.util.List;
import java.util.ArrayList;

/* Rappresenta lo stato complessivo di una partita salvata,
comprendendo tick, budget, policy e costruzioni. */
public class Progress
{
    private int currentTick;
    private int lastPolicyChangeTick;
    private PolicyType policyType;
    private int budget;
    // Nei vecchi salvataggi il campo manca e Gson usa zero.
    private int criticalTicks;
    // Le vecchie partite prive del campo restano in attesa della scelta.
    private FreemasonryChoice freemasonryChoice;
    // Nei vecchi salvataggi il valore predefinito 0 non blocca i tick successivi.
    private int masonicLodgeRemovedTick;
    // Nei vecchi salvataggi il flag manca: la quota riparte dalla finestra corrente.
    private boolean demolitionStatePresent;
    private int usedRemovals;
    private int lastRemovalResetTick;
    private boolean tsunamiInsuranceActive;

    // Stato dei prestiti. bankStatePresent mantiene compatibili i vecchi salvataggi.
    private boolean bankStatePresent;
    private int lastLoanTick;
    private boolean loanActive;
    private int loanAmount;
    private int loanStartTick;
    private int remainingDebt;

    // Stato della difesa missilistica. Il flag mantiene compatibili i vecchi salvataggi.
    private boolean missileDefenseStatePresent;
    private boolean missileDefensePurchased;
    private int missileDefenseHitsRemaining;

    private List<ConstructionProgress> constructions;
    private List<ConstructionProgress>
            pendingTsunamiReconstructions;
    private String tsunamiReconstructionDirection;

    public Progress(
            int currentTick,
            int lastPolicyChangeTick,
            int budget,
            PolicyType policyType,
            List<ConstructionProgress> constructions)
    {
        this(
                currentTick,
                lastPolicyChangeTick,
                budget,
                policyType,
                constructions,
                false
        );
    }

    public Progress(
            int currentTick,
            int lastPolicyChangeTick,
            int budget,
            PolicyType policyType,
            List<ConstructionProgress> constructions,
            boolean tsunamiInsuranceActive)
    {
        this(
                currentTick,
                lastPolicyChangeTick,
                budget,
                policyType,
                constructions,
                tsunamiInsuranceActive,
                null,
                null
        );
    }

    public Progress(
            int currentTick,
            int lastPolicyChangeTick,
            int budget,
            PolicyType policyType,
            List<ConstructionProgress> constructions,
            boolean tsunamiInsuranceActive,
            List<ConstructionProgress> pendingTsunamiReconstructions,
            String tsunamiReconstructionDirection)
    {
        this.currentTick = currentTick;
        this.lastPolicyChangeTick = lastPolicyChangeTick;
        this.budget = budget;
        this.policyType = policyType;
        this.constructions = constructions;
        this.tsunamiInsuranceActive =
                tsunamiInsuranceActive;
        this.pendingTsunamiReconstructions =
                pendingTsunamiReconstructions;
        this.tsunamiReconstructionDirection =
                tsunamiReconstructionDirection;
    }

    /* Crea lo stato di salvataggio della partita
   raccogliendo i dati della simulazione e della griglia. */
    public static Progress fromGame(
            Grid grid,
            City city,
            Simulation simulation)
    {
        List<ConstructionProgress> savedConstructions =
                new ArrayList<>();

        for (int row = 0; row < grid.getNumberOfRows(); row++)
        {
            for (int column = 0;
                 column < grid.getNumberOfColumns();
                 column++)
            {
                Construction construction =
                        grid.getCell(row, column).getConstruction();

                if (construction != null
                        && construction.getType() != ConstructionType.GRASS)
                {
                    ConstructionProgress constructionProgress =
                            ConstructionProgress.fromConstruction(
                                    construction,
                                    row,
                                    column
                            );

                    savedConstructions.add(constructionProgress);
                }
            }
        }

        List<ConstructionProgress>
                savedPendingReconstructions =
                new ArrayList<>();

        List<ReconstructionEntry>
                pendingReconstructions =
                simulation
                        .getPendingTsunamiReconstructions();

        if (pendingReconstructions != null)
        {
            for (ReconstructionEntry entry
                    : pendingReconstructions)
            {
                savedPendingReconstructions.add(
                        ConstructionProgress
                                .fromConstruction(
                                        entry.construction(),
                                        entry.row(),
                                        entry.column()
                                )
                );
            }
        }

        Progress progress = new Progress(
                simulation.getCurrentTick(),
                simulation.getLastPolicyChangeTick(),
                city.getBudget(),
                city.getCurrentPolicy().getType(),
                savedConstructions,
                simulation.isTsunamiInsuranceActive(),
                savedPendingReconstructions,
                simulation
                        .getTsunamiReconstructionDirection()
        );

        progress.bankStatePresent = true;
        progress.lastLoanTick =
                simulation.getLastLoanTick();
        progress.loanActive =
                simulation.isLoanActive();
        progress.loanAmount =
                simulation.getLoanAmount();
        progress.loanStartTick =
                simulation.getLoanStartTick();
        progress.remainingDebt =
                simulation.getRemainingDebt();

        progress.missileDefenseStatePresent = true;
        progress.missileDefensePurchased =
                simulation.isMissileDefensePurchased();
        progress.missileDefenseHitsRemaining =
                simulation.getMissileDefenseHitsRemaining();

        progress.freemasonryChoice =
                simulation.getFreemasonryChoice();
        progress.masonicLodgeRemovedTick =
                simulation.getMasonicLodgeRemovedTick();

        progress.demolitionStatePresent = true;
        progress.usedRemovals =
                simulation.getUsedRemovals();
        progress.lastRemovalResetTick =
                simulation.getLastRemovalResetTick();

        progress.criticalTicks =
                simulation.getCriticalTicks();

        return progress;
    }

    public int getCurrentTick()
    {
        return currentTick;
    }

    public int getLastPolicyChangeTick()
    {
        return lastPolicyChangeTick;
    }

    public int getBudget()
    {
        return budget;
    }

    public int getCriticalTicks()
    {
        return criticalTicks;
    }

    public FreemasonryChoice getFreemasonryChoice()
    {
        return freemasonryChoice == null
                ? FreemasonryChoice.PENDING
                : freemasonryChoice;
    }

    public PolicyType getPolicyType()
    {
        return policyType;
    }

    public List<ConstructionProgress> getConstructions()
    {
        return constructions;
    }

    public boolean isTsunamiInsuranceActive()
    {
        return tsunamiInsuranceActive;
    }

    public List<ConstructionProgress>
    getPendingTsunamiReconstructions()
    {
        return pendingTsunamiReconstructions;
    }

    public String getTsunamiReconstructionDirection()
    {
        return tsunamiReconstructionDirection;
    }

    /* Ricostruisce la griglia utilizzando le costruzioni salvate
   e ripristina le connessioni stradali ed elettriche. */
    public Grid restoreGrid()
    {
        if (constructions == null)
        {
            throw new IllegalStateException(
                    "The saved construction list cannot be null"
            );
        }

        Grid restoredGrid = new Grid();

        for (ConstructionProgress constructionProgress : constructions)
        {
            // I vecchi salvataggi possono ancora contenere erba: viene ignorata perché la generazione è disattivata.
            if (constructionProgress.getType() == ConstructionType.GRASS)
            {
                continue;
            }

            Construction construction =
                    constructionProgress.toConstruction();

            restoredGrid.restoreConstruction(
                    construction,
                    constructionProgress.getRow(),
                    constructionProgress.getColumn()
            );
        }

        restoredGrid.rebuildConnectionsAfterLoad();

        return restoredGrid;
    }

    // Ricrea la policy che era attiva al momento del salvataggio.
    public Policy restorePolicy()
    {
        return PolicyFactory.create(policyType);
    }

    /* Ricostruisce il controller completo collegando la griglia,
   la policy, il budget e i tick precedentemente salvati. */
    public Controller restoreController()
    {
        return restoreController(
                new AchievementManager()
        );
    }

    /* Variante usata dall'applicazione per mantenere gli stessi
       achievement anche quando viene caricata un'altra partita. */
    public Controller restoreController(
            AchievementManager achievementManager)
    {
        Grid restoredGrid = restoreGrid();
        Policy restoredPolicy = restorePolicy();

        Controller controller =
                new Controller(
                        restoredGrid,
                        restoredPolicy,
                        budget,
                        currentTick,
                        lastPolicyChangeTick,
                        tsunamiInsuranceActive,
                        achievementManager
                );

        controller.restoreBankruptcyState(criticalTicks);

        if (demolitionStatePresent)
        {
            controller.restoreRemovalState(
                    usedRemovals,
                    lastRemovalResetTick
            );
        }

        if (bankStatePresent)
        {
            controller.restoreBankState(
                    lastLoanTick,
                    loanActive,
                    loanAmount,
                    loanStartTick,
                    remainingDebt
            );
        }

        if (missileDefenseStatePresent)
        {
            controller.restoreMissileDefenseState(
                    missileDefensePurchased,
                    missileDefenseHitsRemaining
            );
        }

        if (pendingTsunamiReconstructions != null
                && !pendingTsunamiReconstructions.isEmpty())
        {
            List<ReconstructionEntry> entries =
                    new ArrayList<>();

            for (ConstructionProgress savedEntry
                    : pendingTsunamiReconstructions)
            {
                entries.add(
                        new ReconstructionEntry(
                                savedEntry
                                        .toConstruction(),
                                savedEntry.getRow(),
                                savedEntry.getColumn()
                        )
                );
            }

            controller
                    .restoreTsunamiReconstruction(
                            entries,
                            tsunamiReconstructionDirection
                    );
        }

        controller.restoreMasonicLodgeRemovedTick(
                masonicLodgeRemovedTick
        );
        controller.restoreFreemasonryChoice(
                getFreemasonryChoice()
        );

        return controller;
    }

}
