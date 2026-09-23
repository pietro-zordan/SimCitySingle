package model;

public class WasteTreatmentPlant extends Construction{

    public WasteTreatmentPlant()
    {
        super(0,-400, -1600);
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.WASTE_TREATMENT_PLANT;
    }

    @Override
    public int getPollutionImpact() {
        return -1200;
    }

    @Override
    public int getMaintenanceCost() {
        return 160;
    }

    @Override
    public int getUnlockTick()
    {
        return 60;
    }


}
