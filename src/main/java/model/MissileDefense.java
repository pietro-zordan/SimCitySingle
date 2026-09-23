package model;

public class MissileDefense extends Construction implements DefenceSystem{

    private int damageCounter = 3;
    private int repairCost = 300;

    private static final int  MAX_DAMAGE_COUNTER = 3;

    public MissileDefense()
    {
        super(0, 0, 20000);
    }

    @Override
    public boolean canRemoveCriminalActivity(int currentTick) {
        return false;
    }

    @Override
    public void registerCriminalActivityRemoval(int currentTick) {
        return;
    }

    @Override
    public void setDamage() {
        damageCounter--;
    }

    //riporta il numero massimo di missili sopportabili a 3;
    @Override
    public void resetDamage() {
        damageCounter = MAX_DAMAGE_COUNTER;
    }

    //dice se il sistema missilistico funziona
    public boolean isActive()
    {
        return damageCounter >= 1;
    }






}
