package model;

public class NuclearPlant extends Construction
{

    public NuclearPlant()
    {
        super(0,0,0);
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.NUCLEAR_PLANT;
    }


}
