package Events;

import model.City;
import model.Construction;
import model.EconomyBoostable;
import model.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* Aumenta temporaneamente la crescita economica degli edifici compatibili.
   Il bonus dura 5 tick e viene scelto casualmente tra il 30% e l'80%. */
public class EconomicBoom extends Event
{
    private static final int TICK_DURATION = 5;
    private static final double MINIMUM_BOOST = 0.3;
    private static final double MAXIMUM_BOOST = 0.8;

    private double moneyProductionMultiplier;
    private final Grid grid;

    // Edifici che hanno già ricevuto il bonus durante l'evento.
    private final List<EconomyBoostable> boostedBuildings =
            new ArrayList<>();

    // Inizializza l'evento associandolo alla città e alla griglia.
    public EconomicBoom(City city, Grid grid)
    {
        super(TICK_DURATION, city, 0);
        this.grid = grid;
        launchNumber = 22;
    }

    // Genera il bonus casuale e lo applica agli edifici compatibili.
    @Override
    public void start()
    {
        Random dice = new Random();

        moneyProductionMultiplier =
                MINIMUM_BOOST
                        + (MAXIMUM_BOOST - MINIMUM_BOOST)
                        * dice.nextDouble();

        boostEconomyGrowth();
    }

    // Applica il bonus anche agli edifici costruiti durante l'evento.
    @Override
    public void updateOfOneTick()
    {
        boostEconomyGrowth();
    }

    // Rimuove tutti i bonus economici applicati durante l'evento.
    @Override
    public void end()
    {
        removeEconomicBoosts();
    }

    /* Applica il bonus agli edifici compatibili
       che non sono ancora stati potenziati. */
    private void boostEconomyGrowth()
    {
        for (Construction construction
                : grid.getConstructions())
        {
            if (construction
                    instanceof EconomyBoostable boostable
                    && !isBoosted(construction))
            {
                boostable.applyEconomicBoost(
                        moneyProductionMultiplier
                );

                boostedBuildings.add(boostable);
            }
        }
    }

    /* Rimuove il bonus temporaneo dagli edifici coinvolti
       senza modificare il loro tasso di crescita naturale. */
    private void removeEconomicBoosts()
    {
        for (EconomyBoostable boostedBuilding
                : boostedBuildings)
        {
            boostedBuilding.removeEconomicBoost();
        }

        boostedBuildings.clear();
    }

    // Verifica se la costruzione ha già ricevuto il bonus dell'evento.
    public boolean isBoosted(Construction construction)
    {
        return construction != null
                && boostedBuildings.contains(construction);
    }

    // Restituisce il tipo corrispondente al boom economico.
    @Override
    public EventType getType()
    {
        return EventType.ECONOMIC_BOOM;
    }
}