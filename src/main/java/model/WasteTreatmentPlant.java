package model;

public class WasteTreatmentPlant extends Construction{

    public WasteTreatmentPlant()
    {
        super(0,0, -1600);
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.WASTE_TREATMENT_PLANT;
    }

    @Override
    public int getPollutionImpact() {
        return -1200;
    }
}
