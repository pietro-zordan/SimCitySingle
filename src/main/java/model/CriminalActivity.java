package model;

public class CriminalActivity extends Construction{

    public CriminalActivity()
    {
        super(-200, 200, 0);
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.CRIMINAL_ACTIVITY;
    }

    public int getHappinessImpact()
    {
        return -300;
    }


}
