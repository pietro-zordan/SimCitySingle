package model;

public class TerroristicGroup extends Construction {

    public TerroristicGroup()
    {
        super(-100, 50, 0);
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.TERRORISTIC_GROUP;
    }


}
