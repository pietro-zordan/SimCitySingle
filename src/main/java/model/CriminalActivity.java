package model;

public class CriminalActivity extends Construction{

    public CriminalActivity()
    {
        super(-300, 200, 0);
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.CRIMINAL_ACTIVITY;
    }

    public int getHappinessImpact()
    {
        return -400;
    }


}
