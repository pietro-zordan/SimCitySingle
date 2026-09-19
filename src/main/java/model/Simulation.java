package model;

import Events.*;
import policies.Policy;

import java.util.Random;

/* Gestisce lo scorrere dei tick della partita.
   Aggiorna la città e gli eventi e controlla quando è possibile cambiare policy. */
public class Simulation{

    private static final int POLICY_CHANGE_INTERVAL = 12;
    private static final int BANK_UNLOCK_TICK = 30;
    private static final int LOAN_DURATION = 3;
    private static final int LOAN_MULTIPLIER = 3;
    private static final int LOAN_INTERVAL = 15;
    private static final int BASE_LOAN_AMOUNT = 1000;
    private int lastLoanTick = -LOAN_INTERVAL;

    private boolean loanActive;
    private int loanAmount;
    private int loanStartTick;
    private final Random random = new Random();
    private int currentTick;
    private int lastPolicyChangeTick;
    private final City city;
    private Event activeEvent;
    private int eventTicksPassed;
    private final Grid grid;
    private int remainingDebt;

    // Crea una nuova simulazione partendo dal tick zero.
    public Simulation(City city, Grid grid)
    {
        this(city, grid, 0, 0);
    }

    // Crea una simulazione usando i tick recuperati da un salvataggio.
    public Simulation(
            City city,
            Grid grid,
            int currentTick,
            int lastPolicyChangeTick)
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
    }

    // Controlla se sono trascorsi almeno dodici tick dall'ultimo cambio di policy.
    public boolean canChangePolicy()
    {
        int passedTicks = currentTick - lastPolicyChangeTick;

        return passedTicks >= POLICY_CHANGE_INTERVAL;
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
        return currentTick >= BANK_UNLOCK_TICK;
    }

    // Crea casualmente uno degli eventi disponibili.
    private Event createRandomEvent()
    {
        Event[] possibleEvents = {
                new HackerAttack(city, grid),
                new EnergyCrisis(city, grid),
                new Fire(city, grid),
                new Tsunami(city, grid),
                new EconomicBoom(city, grid)
        };

        int selectedEvent = random.nextInt(possibleEvents.length);

        return possibleEvents[selectedEvent];
    }

    /* Aggiorna la città e l'evento attivo.
       Se non ci sono eventi, prova ad avviarne uno casuale e infine incrementa il tick. */
    public void updateOfOneTick()
    {
        city.updateOfOneTick();
        if(isEventActive())
        {
            activeEvent.updateOfOneTick();
            eventTicksPassed++;
            if(activeEvent.isFinished(eventTicksPassed) ){
                activeEvent.end();
                activeEvent = null;
                eventTicksPassed = 0;
            }
        }
        else startEvent(createRandomEvent());
        currentTick++;
        checkLoanRepayment();
    }

    private void checkLoanRepayment()
    {
        // Dopo 3 tick il prestito diventa debito da restituire
        if (loanActive
                && currentTick - loanStartTick >= LOAN_DURATION)
        {
            remainingDebt +=
                    loanAmount * LOAN_MULTIPLIER;

            loanActive = false;
            loanAmount = 0;
        }

        // Se esiste un debito, usa tutto il budget disponibile
        // senza permettere al budget di diventare negativo
        if (remainingDebt > 0
                && city.getBudget() > 0)
        {
            int payment = Math.min(
                    city.getBudget(),
                    remainingDebt
            );

            city.updateBudget(-payment);

            remainingDebt -= payment;
        }
    }

    public int getMaxLoanAmount()
    {
        return BASE_LOAN_AMOUNT
                * grid.getNumberOfPoweredBanks();
    }

    // Controlla se è già presente un evento attivo.
    public boolean isEventActive()
    {
        return activeEvent!=null;
    }

    // Avvia l'evento ricevuto se non ci sono altri eventi attivi e l'estrazione ha successo.
    public void startEvent(Event event)
    {
        if(event == null || isEventActive())
            return;
        event.randomProbability();
        if(event.canStart()){
            activeEvent = event;
            eventTicksPassed = 0;
            activeEvent.start();
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
        if (!canRequestLoan()
                || amount <= 0
                || amount > getMaxLoanAmount())
        {
            return false;
        }

        city.updateBudget(amount);

        loanAmount = amount;
        loanStartTick = currentTick;
        lastLoanTick = currentTick;
        loanActive = true;

        return true;
    }

    public boolean canRequestLoan()
    {
        int passedTicks = currentTick - lastLoanTick;

        return canPlaceBank()
                && grid.getNumberOfPoweredBanks() > 0
                && passedTicks >= LOAN_INTERVAL;
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
}