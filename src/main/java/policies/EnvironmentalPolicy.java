package policies;

import model.Construction;
import model.ConstructionType;

/* Rappresenta la policy ambientale.
   Favorisce i parchi e riduce inquinamento ed economia. */
public class EnvironmentalPolicy implements Policy
{
    private static final int INDUSTRIAL_COST_INCREASE = 100;
    private static final int PARK_COST_REDUCTION = 50;

    /* Aumenta il costo delle industrie, riduce quello dei parchi
       e lascia invariato il costo delle altre costruzioni. */
    @Override
    public int calculatePlacementCost(Construction construction)
    {
        int baseCost = construction.getPlacementPrice();
        ConstructionType type = construction.getType();

        if (type == ConstructionType.INDUSTRIAL)
        {
            return baseCost - INDUSTRIAL_COST_INCREASE;
        }

        if (type == ConstructionType.PARK)
        {
            return Math.min(
                    0,
                    baseCost + PARK_COST_REDUCTION
            );
        }

        return baseCost;
    }

    // Restituisce il nome della policy ambientale.
    @Override
    public String getName()
    {
        return "Environmental policy";
    }

    // Restituisce il tipo corrispondente alla policy ambientale.
    @Override
    public PolicyType getType()
    {
        return PolicyType.ENVIRONMENTAL;
    }

    // Riduce l'inquinamento totale del 25%.
    @Override
    public int modifyPollution(int totalPollution)
    {
        if (totalPollution >= 0)
        {
            return (int) (totalPollution * 0.75);
        }

        return (int) (totalPollution * 1.25);
    }

    // Riduce l'economia totale del 25%.
    @Override
    public int modifyEconomy(int totalEconomy)
    {
        return (int) (totalEconomy * 0.75);
    }
}