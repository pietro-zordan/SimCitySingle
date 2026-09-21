package Events;

import model.City;
import model.Grid;
import model.PowerPlant;

import java.util.List;
import java.util.Random;

/* Rappresenta un attacco hacker della durata di 5 tick.
   Sospende una centrale e interrompe l'alimentazione delle sue costruzioni. */
public class HackerAttack extends Event
{
    private PowerPlant suspendedPowerPlant = null;
    private static final int TICK_DURATION = 5;

    private final Grid grid;

    // Inizializza l'attacco hacker associandolo alla città e alla griglia.
    public HackerAttack(City city, Grid grid)
    {
        super(TICK_DURATION, city);
        this.grid = grid;
        launchNumber = 26;
    }

    // Seleziona casualmente una delle centrali presenti nella griglia.
    private PowerPlant selectRandomPowerPlant(Grid grid)
    {
        List<PowerPlant> powerPlants =
                grid.getAllPowerPlants();

        if (powerPlants.isEmpty())
        {
            return null;
        }

        Random dice = new Random();
        int numero = dice.nextInt(
                powerPlants.size()
        );

        return powerPlants.get(numero);
    }

    // Sospende la centrale selezionata, se presente.
    private void setSuspended()
    {
        if (suspendedPowerPlant != null)
        {
            suspendedPowerPlant.suspend();
        }
    }

    // Permette l'avvio dell'evento solo se esiste almeno una centrale.
    @Override
    public boolean canStart()
    {
        return super.canStart()
                && !grid.getAllPowerPlants().isEmpty();
    }

    // Seleziona una centrale casuale e la sospende.
    @Override
    public void start()
    {
        suspendedPowerPlant =
                selectRandomPowerPlant(grid);

        setSuspended();
    }

    // Riattiva la centrale sospesa al termine dell'evento.
    @Override
    public void end()
    {
        if (suspendedPowerPlant != null)
        {
            suspendedPowerPlant.resume();
        }
    }

    // Restituisce il tipo corrispondente all'attacco hacker.
    @Override
    public EventType getType()
    {
        return EventType.HACKER_ATTACK;
    }
}