package policies;

import model.Construction;
import model.ConstructionType;

/* Rappresenta la policy industriale.
   Favorisce le industrie e aumenta economia e inquinamento. */
public class IndustrialPolicy implements Policy
{
    private static final int INDUSTRIAL_COST_REDUCTION = 100;
    private static final int PARK_COST_INCREASE = 50;

    /* Riduce il costo delle industrie, aumenta quello dei parchi
       e lascia invariato il costo delle altre costruzioni. */
    @Override
    public int calculatePlacementCost(Construction construction)
    {
        int baseCost = construction.getPlacementPrice();
        ConstructionType type = construction.getType();

        if (type == ConstructionType.INDUSTRIAL)
        {
            return Math.min(
                    0,
                    baseCost + INDUSTRIAL_COST_REDUCTION
            );
        }

        if (type == ConstructionType.PARK)
        {
            return baseCost - PARK_COST_INCREASE;
        }

        return baseCost;
    }

    // Restituisce il nome della policy industriale.
    @Override
    public String getName()
    {
        return "Industrial policy";
    }

    // Restituisce il tipo corrispondente alla policy industriale.
    @Override
    public PolicyType getType()
    {
        return PolicyType.INDUSTRIAL;
    }

    // Aumenta l'inquinamento totale del 25%.
    @Override
    public int modifyPollution(int totalPollution)
    {
        if (totalPollution >= 0)
        {
            return (int) (totalPollution * 1.25);
        }

        return (int) (totalPollution * 0.75);
    }

    // Aumenta l'economia totale del 25%.
    @Override
    public int modifyEconomy(int totalEconomy)
    {
        return (int) (totalEconomy * 1.25);
    }
}