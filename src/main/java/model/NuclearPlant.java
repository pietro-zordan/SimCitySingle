package model;

public class NuclearPlant extends Construction {
    private static final int NETWORK_CAPACITY_INCREASE = 10000;
    private boolean fireProtected;

    // Crea una centrale nucleare che estende la capacità massima della rete di 10000.
    public NuclearPlant() {
        super(200, 0, -3000);
    }

    // Restituisce di quanto una centrale nucleare aumenta la capacità massima della rete.
    public static int getNetworkCapacityIncrease() {
        return NETWORK_CAPACITY_INCREASE;
    }

    // Indica se la centrale possiede la protezione avanzata contro gli incendi.
    public boolean isFireProtected() {
        return fireProtected;
    }

    // Registra la protezione avanzata contro gli incendi.
    public void setFireProtected(boolean fireProtected) {
        this.fireProtected = fireProtected;
    }

    @Override
    public int getPollutionImpact() {
        return 900;
    }

    @Override
    public int getHappinessImpact() {
        return -1500;
    }

    public int getMaintenanceCost()
    {
        return 100;
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.NUCLEAR_PLANT;
    }
}
