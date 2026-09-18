package model;

public class Bank extends Construction
{

    private final static int PLACEMENT_PRICE= -800;

    public Bank()
    {
        super(0, 100, PLACEMENT_PRICE);
    }

    @Override
    public ConstructionType getType()
    {
        return ConstructionType.BANK;
    }

    public void applyLoan()
    {
        moneyProduction=1000;
    }

    @Override
    public void updateOfOneTick()
    {

    }


}
