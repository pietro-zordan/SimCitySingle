package policies;

import model.Construction;

/* Rappresenta la policy standard.
   Non modifica i costi, l'inquinamento o l'economia. */
public class StandardPolicy implements Policy
{
    // Restituisce il costo base della costruzione senza modificarlo.
    @Override
    public int calculatePlacementCost(Construction construction)
    {
        return construction.getPlacementPrice();
    }

    // Restituisce il nome della policy standard.
    @Override
    public String getName()
    {
        return "Standard policy";
    }

    // Restituisce il tipo corrispondente alla policy standard.
    @Override
    public PolicyType getType()
    {
        return PolicyType.STANDARD;
    }

    // Restituisce l'inquinamento totale senza modificarlo.
    @Override
    public int modifyPollution(int totalPollution)
    {
        return totalPollution;
    }

    // Restituisce l'economia totale senza modificarla.
    @Override
    public int modifyEconomy(int totalEconomy)
    {
        return totalEconomy;
    }
}