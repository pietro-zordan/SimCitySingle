package model;

/* Definisce le operazioni necessarie per applicare
   e rimuovere un bonus economico temporaneo. */
public interface EconomyBoostable
{
    // Applica il bonus indicato alla crescita economica.
    void applyEconomicBoost(double boost);

    // Rimuove il bonus economico temporaneo.
    void removeEconomicBoost();
}