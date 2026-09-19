package model;

public class CriminalActivity extends Construction{

    double probability = 0.1 ;

    public CriminalActivity()
    {
        super(-200, 200, 0);
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.CRIMINAL_ACTIVITY;
    }

    public void setProbability()
    {
        probability += 0.1;
    }

    public int getHappinessImpact()
    {
        return -300;
    }


}
