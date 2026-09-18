package Events;

import model.*;

import java.util.Random;

/* Rappresenta uno tsunami che arriva da un lato casuale della griglia.
   Distrugge gli edifici nelle quattro righe o colonne vicine al lato scelto,
   senza rimuovere strade e parchi. */
public class Tsunami extends Event{

    private static final int ADVANCMENT_LENGTH=4;
    private final static int TICK_DURATION=3;
    private final static int HAPPINESS_DECREASE=50;
    private final Grid grid;
    private String direction;

    // Crea uno tsunami della durata di tre tick e imposta la sua probabilità di avvio.
    public Tsunami(City city, Grid grid)
    {
        super(TICK_DURATION, city);
        this.grid = grid;
        launchNumber = 25;
    }

    /* Sceglie casualmente il lato da cui arriva lo tsunami
       e distrugge gli edifici presenti nella zona colpita. */
    public void getBuildingsToDestroy(Grid grid)
    {
        String[] directions = {"UP", "DOWN", "LEFT", "RIGHT"};
        Random random = new Random();
        direction = directions[random.nextInt(directions.length)];

        // Attraversa le righe o le colonne vicine al lato scelto.
        switch (direction)
        {
            case "UP"->
            {
                for(int i=0; i<ADVANCMENT_LENGTH; i++)
                    for(int j=0; j< grid.getNumberOfColumns(); j++)
                    {
                        Cell cell=grid.getCell(i,j);
                        Construction c= cell.getConstruction();
                        if (c != null
                                && !(c instanceof Road)
                                && !(c instanceof Park)) {
                            grid.removeConstruction(i, j);
                        }
                    }
            }
            case "DOWN"->
            {
                for(int i= grid.getNumberOfRows()-1;
                    i >= grid.getNumberOfRows() - ADVANCMENT_LENGTH; i--)
                    for(int j=0; j< grid.getNumberOfColumns(); j++)
                    {
                        Cell cell=grid.getCell(i,j);
                        Construction c= cell.getConstruction();
                        if (c != null
                                && !(c instanceof Road)
                                && !(c instanceof Park)) {
                            grid.removeConstruction(i, j);
                        }
                    }
            }
            case "LEFT"->
            {
                for(int i=0; i< grid.getNumberOfRows(); i++)
                    for(int j=0; j<ADVANCMENT_LENGTH; j++)
                    {
                        Cell cell=grid.getCell(i,j);
                        Construction c= cell.getConstruction();
                        if (c != null
                                && !(c instanceof Road)
                                && !(c instanceof Park)) {
                            grid.removeConstruction(i, j);
                        }
                    }
            }
            case "RIGHT"->
            {
                for (int i = 0; i < grid.getNumberOfRows(); i++)
                    for(int j= grid.getNumberOfColumns()-1;
                        j >= grid.getNumberOfColumns() - ADVANCMENT_LENGTH; j--)
                    {
                        Cell cell=grid.getCell(i,j);
                        Construction c= cell.getConstruction();
                        if (c != null
                                && !(c instanceof Road)
                                && !(c instanceof Park)) {
                            grid.removeConstruction(i, j);
                        }
                    }
            }
        }
    }

    // Distrugge gli edifici colpiti e aggiorna subito le statistiche della città.
    @Override
    public void start()
    {
        getBuildingsToDestroy(grid);
        city.refreshStatistics();
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
        return ADVANCMENT_LENGTH;
    }

    // Identifica l'evento come tsunami.
    @Override
    public EventType getType()
    {
        return EventType.TSUNAMI;
    }
}