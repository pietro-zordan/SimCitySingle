package policies;

import model.Construction;

/* Definisce le operazioni che ogni policy della città deve offrire.
   Le diverse implementazioni modificano costi e statistiche globali. */
public interface Policy
{
    // Calcola il costo di piazzamento secondo le regole della policy.
    int calculatePlacementCost(Construction construction);

    // Restituisce il tipo della policy.
    PolicyType getType();

    // Restituisce il nome della policy.
    String getName();

    // Modifica l'inquinamento totale della città.
    int modifyPollution(int totalPollution);

    // Modifica l'economia totale della città.
    int modifyEconomy(int totalEconomy);
}