package Events;

import model.Cell;
import model.City;
import model.Construction;
import model.Grid;

/* Rappresenta una crisi energetica della durata di 5 tick.
   Ogni edificio alimentato che consuma energia causa un costo aggiuntivo. */
public class EnergyCrisis extends Event
{
    private static final int TICK_DURATION = 5;
    private static final int EXTRA_COST_PER_BUILDING = 35;

    private final Grid grid;

    // Inizializza la crisi energetica associandola alla città e alla griglia.
    public EnergyCrisis(City city, Grid grid)
    {
        super(TICK_DURATION, city, 0);
        launchNumber = 35; //35
        this.grid = grid;
    }

    // Calcola e sottrae dal budget il costo aggiuntivo del tick.
    @Override
    public void updateOfOneTick()
    {
        int totalExtraCost =
                calculateTotalExtraCost(grid);

        applyExtraCostToCity(totalExtraCost);
    }

    /* Calcola il costo totale considerando tutti gli edifici
       alimentati che consumano energia. */
    private int calculateTotalExtraCost(Grid grid)
    {
        int extraCost = 0;

        for (int riga = 0;
             riga < grid.getNumberOfRows();
             riga++)
        {
            for (int colonna = 0;
                 colonna < grid.getNumberOfColumns();
                 colonna++)
            {
                Cell cell = grid.getCell(riga, colonna);

                if (!cell.isEmpty())
                {
                    Construction construction =
                            cell.getConstruction();

                    if (construction.getPowerConsumption() > 0
                            && construction.isPowered())
                    {
                        extraCost += EXTRA_COST_PER_BUILDING;
                    }
                }
            }
        }

        return extraCost;
    }

    // Sottrae l'intero costo della crisi, permettendo al budget di diventare negativo.
    private void applyExtraCostToCity(int extraCost)
    {
        if (extraCost > 0)
        {
            city.updateBudgetAllowNegative(
                    -extraCost
            );
        }
    }

    /* Permette l'avvio dell'evento solo se esiste almeno
       un edificio alimentato che consuma energia. */
    @Override
    public boolean canStart()
    {
        return super.canStart()
                && calculateTotalExtraCost(grid) > 0;
    }

    // Restituisce il tipo corrispondente alla crisi energetica.
    @Override
    public EventType getType()
    {
        return EventType.ENERGY_CRISIS;
    }
}