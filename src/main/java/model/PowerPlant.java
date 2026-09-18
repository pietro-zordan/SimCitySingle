package model;

import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Queue;

/* Gestisce la produzione e la distribuzione dell'energia elettrica.
   Collega, scollega e ricollega le costruzioni presenti nell'area servita. */

public class PowerPlant extends Construction
{

    private final static int POWER_GENERATED= 10000;
    private final static int POLLUTION_GENERATED = 200;
    private final static int PLACEMENT_PRICE= -800;
    private boolean suspended;

    private Cell[] nearbyCells= new Cell[48];
    private int numberOfNearbyCells;
    private Queue<Construction> servedConstructions= new ArrayDeque<>();
    private PriorityQueue<Construction> candidatesForReconnection = new PriorityQueue<>(new PowerComparator());

    // Crea una centrale elettrica inizialmente attiva e senza costruzioni collegate.
    public PowerPlant()
    {
        super(0, 0, PLACEMENT_PRICE);

    }

    // Memorizza le celle comprese nell'area di alimentazione della centrale
    public void findNearbyCells(Grid grid, int plantRow, int plantColumn)
    {
        nearbyCells=new Cell[48];
        numberOfNearbyCells = 0;

        for(int i=plantRow-3; i<=plantRow+3; i++)
        {
            for(int j=plantColumn-3; j<=plantColumn+3; j++)
            {
                if (grid.isInside(i,j) && (i != plantRow || j != plantColumn))
                {
                    nearbyCells[numberOfNearbyCells] = grid.getCell(i, j);
                    numberOfNearbyCells++;
                }
            }
        }
    }

    /*controlla se una costruzione candidata al riaggancio si trova
    nelle celle della centrale poichè nel frattempo potrebbe essere stata
    rimossa dalla griglia
     */
    private boolean isStillNearby(Construction construction)
    {
        for (int i = 0; i < numberOfNearbyCells; i++)
        {
            if (!nearbyCells[i].isEmpty()
                    && nearbyCells[i].getConstruction() == construction)
            {
                return true;
            }
        }

        return false;
    }

    // Collega le costruzioni circostanti che richiedono energia.
    public void serveConstruction()
    {
        for(int i=0; i<numberOfNearbyCells; i++)
        {

            if (!nearbyCells[i].isEmpty())
            {
                Construction construction =
                        nearbyCells[i].getConstruction();

                if (construction.requiresPower()
                        && !construction.isPowerPlantConnected()
                        && !servedConstructions.contains(construction)
                        && !candidatesForReconnection.contains(construction))
                {
                    construction.connectToPowerPlant(this);
                    servedConstructions.offer(construction);
                }
            }
        }

        while (calculateUsedPower() > POWER_GENERATED && !servedConstructions.isEmpty())
        {
            unplug();
        }
    }

    //calcola la potenza utilizzata da tutte le costruzioni
    private int calculateUsedPower()
    {
        int usedPower=0;
        for (Construction construction : servedConstructions)
        {
            usedPower = usedPower + construction.getPowerConsumption();
        }

        return usedPower;
    }

    //scollega la costruzione collegata da più tempo alla centrale elettrica
    private void unplug()
    {
        Construction unpluggedConstruction = servedConstructions.poll();

        if (unpluggedConstruction != null)
        {
            unpluggedConstruction.disconnectFromPowerPlant();
            candidatesForReconnection.add(unpluggedConstruction);
        }
    }


    // se la central ha abbastanza energia sceglie quale cella ricollegare
    public void reconnect()
    {
        int availablePower = POWER_GENERATED - calculateUsedPower();

        while (!candidatesForReconnection.isEmpty())
        {
            Construction candidate = candidatesForReconnection.peek();

            /* se nel frattempo è stata rimossa per qualche motivo
            la tolgo dalla lista dei candidati
             */
            if (!isStillNearby(candidate))
            {
                candidatesForReconnection.remove();
            }

            /*questo serve se una centrale viene messa successivamente
            alla rimozione dall'altra centrale e quindi viene "rubata"
            dalla centrale nuova
             */
            else if (candidate.isPowerPlantConnected())
            {
                candidatesForReconnection.remove();
            }

            else if (candidate.getPowerConsumption() <= availablePower)
            {

                candidatesForReconnection.remove();
                candidate.connectToPowerPlant(this);
                servedConstructions.add(candidate);

                availablePower=availablePower-candidate.getPowerConsumption();
            }
            else return;

        }
    }

    // Restituisce l'inquinamento prodotto quando la centrale è attiva.
    @Override
    public int getPollutionImpact()
    {

        if (isActive())
        {
            return POLLUTION_GENERATED;
        }

        return 0;
    }

    // Restituisce la potenza generata quando la centrale è attiva.
    public int getPowerGenerated()
    {
        if (isActive())
        {
            return POWER_GENERATED;
        }

        return 0;
    }

    // Scollega tutte le costruzioni servite e svuota le code della centrale.
    public void disconnectAll()
    {
        for (Construction construction : servedConstructions)
        {
            if (construction.getConnectedPowerPlant() == this)
            {
                construction.disconnectFromPowerPlant();
            }
        }

        servedConstructions.clear();
        candidatesForReconnection.clear();
    }

    // Elimina una costruzione dalle code e la scollega dalla centrale.
    public void removeConstruction(Construction construction)
    {
        servedConstructions.remove(construction);
        candidatesForReconnection.remove(construction);

        if (construction.getConnectedPowerPlant() == this)
        {
            construction.disconnectFromPowerPlant();
        }
    }

    //disattiva la centrale senza perdere le costruzioni collegate
    public void suspend()
    {
        suspended=true;
    }

    //ri-attiva la centrale dopo la sospensione
    public void resume()
    {
        suspended=false;
    }

    //comunica se una centrale è sospesa oppure no
    public boolean isActive()
    {
        return !suspended;
    }

    // Se la centrale è attiva, aggiorna i collegamenti e tenta le riconnessioni
    @Override
    public void updateOfOneTick()
    {
        if(isActive())
        {
            serveConstruction();
            reconnect();
        }
    }

    // Una centrale è considerata alimentata quando è attiva.
    @Override
    public boolean isPowered()
    {
        return isActive();
    }

    @Override
    public ConstructionType getType()
    {
        return ConstructionType.POWER_PLANT;
    }

    // Individua l'area servita dalla centrale dopo il suo piazzamento.
    @Override
    public void initializeAfterPlacement(
            Grid grid,
            int row,
            int column)
    {
        findNearbyCells(grid, row, column);
    }

    // Scollega tutte le costruzioni prima di rimuovere la centrale.
    @Override
    public void prepareForRemoval()
    {
        disconnectAll();
    }

}
