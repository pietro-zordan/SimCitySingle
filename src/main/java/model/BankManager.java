package model;

/*
 * Gestisce la logica finanziaria legata alle banche:
 * prestiti, cooldown, debiti e restituzione automatica.
 */
public class BankManager
{
    private static final int BANK_UNLOCK_TICK = 30;
    private static final int LOAN_DURATION = 3;
    private static final double LOAN_MULTIPLIER = 1.5;
    private static final int LOAN_INTERVAL = 15;
    private static final int BASE_LOAN_AMOUNT = 1000;

    private final City city;
    private final Grid grid;

    private int lastLoanTick = -LOAN_INTERVAL;
    private boolean loanActive;
    private int loanAmount;
    private int loanStartTick;
    private int remainingDebt;

    public BankManager(City city, Grid grid)
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

        this.city = city;
        this.grid = grid;
    }

    // Aggiorna lo stato dei prestiti e prova a ripagare il debito.
    public void update(int currentTick)
    {
        checkLoanRepayment(currentTick);
    }

    public boolean canPlaceBank(int currentTick)
    {
        return currentTick >= BANK_UNLOCK_TICK;
    }

    public boolean canRequestLoan(int currentTick)
    {
        int passedTicks = currentTick - lastLoanTick;

        return canPlaceBank(currentTick)
                && grid.getNumberOfPoweredBanks() > 0
                && passedTicks >= LOAN_INTERVAL;
    }

    public boolean requestLoan(
            int amount,
            int currentTick)
    {
        if (!canRequestLoan(currentTick)
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

    public int getMaxLoanAmount()
    {
        return BASE_LOAN_AMOUNT
                * grid.getNumberOfPoweredBanks();
    }

    private void checkLoanRepayment(int currentTick)
    {
        // Dopo tre tick il prestito diventa debito da restituire.
        if (loanActive
                && currentTick - loanStartTick >= LOAN_DURATION)
        {
            remainingDebt +=
                    (int) Math.round(
                            loanAmount
                                    * LOAN_MULTIPLIER
                    );

            loanActive = false;
            loanAmount = 0;
        }

        // Se esiste un debito, usa il budget disponibile senza renderlo negativo.
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
}
