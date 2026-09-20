package Events;

import model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* Rappresenta uno tsunami che arriva da un lato casuale della griglia.
   Distrugge gli edifici nelle quattro righe o colonne vicine al lato scelto,
   senza rimuovere strade ed erba. */
public class Tsunami extends Event{

    public static final int ADVANCEMENT_LENGTH = 4;
    private final static int TICK_DURATION=3;
    private final static int HAPPINESS_DECREASE=50;
    private final Grid grid;
    private String direction;
    private final List<ReconstructionEntry> destroyedBuildings =
            new ArrayList<>();

    // Crea uno tsunami della durata di tre tick e imposta la sua probabilità di avvio.
    public Tsunami(City city, Grid grid)
    {
        super(TICK_DURATION, city);
        this.grid = grid;
        launchNumber = 20;
    }

    /* Sceglie casualmente il lato da cui arriva lo tsunami
       e distrugge gli edifici presenti nella zona colpita. */
    public void getBuildingsToDestroy(Grid grid)
    {
        destroyedBuildings.clear();

        String[] directions = {"UP", "DOWN", "LEFT", "RIGHT"};
        Random random = new Random();
        direction = directions[random.nextInt(directions.length)];

        // Attraversa le righe o le colonne vicine al lato scelto.
        switch (direction)
        {
            case "UP"->
            {
                for(int i=0; i<ADVANCEMENT_LENGTH; i++)
                    for(int j=0; j< grid.getNumberOfColumns(); j++)
                    {
                        destroyBuildingIfNeeded(
                                grid,
                                i,
                                j
                        );
                    }
            }
            case "DOWN"->
            {
                for(int i= grid.getNumberOfRows()-1;
                    i >= grid.getNumberOfRows() - ADVANCEMENT_LENGTH; i--)
                    for(int j=0; j< grid.getNumberOfColumns(); j++)
                    {
                        destroyBuildingIfNeeded(
                                grid,
                                i,
                                j
                        );
                    }
            }
            case "LEFT"->
            {
                for(int i=0; i< grid.getNumberOfRows(); i++)
                    for(int j=0; j<ADVANCEMENT_LENGTH; j++)
                    {
                        destroyBuildingIfNeeded(
                                grid,
                                i,
                                j
                        );
                    }
            }
            case "RIGHT"->
            {
                for (int i = 0; i < grid.getNumberOfRows(); i++)
                    for(int j= grid.getNumberOfColumns()-1;
                        j >= grid.getNumberOfColumns() - ADVANCEMENT_LENGTH; j--)
                    {
                        destroyBuildingIfNeeded(
                                grid,
                                i,
                                j
                        );
                    }
            }
        }
    }

    private void destroyBuildingIfNeeded(
            Grid targetGrid,
            int row,
            int column)
    {
        Cell cell =
                targetGrid.getCell(
                        row,
                        column
                );

        Construction construction =
                cell.getConstruction();

        if (construction != null
                && !(construction instanceof Road)
                && !(construction instanceof Grass))
        {
            destroyedBuildings.add(
                    new ReconstructionEntry(
                            construction,
                            row,
                            column
                    )
            );

            targetGrid.removeConstruction(
                    row,
                    column
            );
        }
    }

    public List<ReconstructionEntry> getDestroyedBuildings()
    {
        return new ArrayList<>(
                destroyedBuildings
        );
    }

    // Distrugge gli edifici colpiti e aggiorna subito le statistiche della città.
    @Override
    public void start()
    {
        grid.setGrassGenerationSuspended(
                true
        );

        getBuildingsToDestroy(grid);
        city.refreshStatistics();
    }

    @Override
    public void end()
    {
        grid.setGrassGenerationSuspended(
                false
        );
    }

    // Riduce la felicità della città a ogni tick dello tsunami.
    @Override
    public void updateOfOneTick()
    {
        city.decreaseGlobalHappiness(HAPPINESS_DECREASE);
    }

    // Restituisce il lato da cui arriva lo tsunami.
    public String getDirection()
    {
        return direction;
    }

    // Restituisce il numero di righe o colonne colpite dallo tsunami.
    public int getAdvancementLength()
    {
        return ADVANCEMENT_LENGTH;
    }

    // Identifica l'evento come tsunami.
    @Override
    public EventType getType()
    {
        return EventType.TSUNAMI;
    }
}