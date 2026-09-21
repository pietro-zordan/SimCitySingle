package model;

/* Rappresenta una centrale elettrica.
   Mantiene soltanto le caratteristiche proprie della centrale,
   mentre la distribuzione dell'energia è gestita da EnergyManager. */
public class PowerPlant extends Construction {
    private static final int POWER_GENERATED = 6000;
    private static final int POLLUTION_GENERATED = 200;
    private static final int PLACEMENT_PRICE = -800;

    private boolean suspended;

    // Crea una centrale elettrica inizialmente attiva.
    public PowerPlant() {
        super(0, 0, PLACEMENT_PRICE);
    }

    // Restituisce l'inquinamento prodotto quando la centrale è attiva.
    @Override
    public int getPollutionImpact() {
        if (isActive()) {
            return POLLUTION_GENERATED;
        }

        return 0;
    }

    // Restituisce la potenza generata quando la centrale è attiva.
    public int getPowerGenerated() {
        if (isActive()) {
            return POWER_GENERATED;
        }

        return 0;
    }

    // Restituisce la capacità massima della centrale.
    public int getPowerCapacity() {
        return POWER_GENERATED;
    }

    // Disattiva temporaneamente la centrale.
    public void suspend() {
        suspended = true;
    }

    // Riattiva la centrale.
    public void resume() {
        suspended = false;
    }

    // Comunica se la centrale è attiva.
    public boolean isActive() {
        return !suspended;
    }

    // Una centrale è considerata alimentata quando è attiva.
    @Override
    public boolean isPowered() {
        return isActive();
    }

    @Override
    public int getMaintenanceCost()
    {
        return 10;
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.POWER_PLANT;
    }
}
