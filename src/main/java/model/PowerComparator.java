package model;

import java.util.Comparator;

public class PowerComparator implements Comparator<Construction> {

    @Override
    public int compare(Construction c1, Construction c2) {
        return Integer.compare(c1.getPowerConsumption(), c2.getPowerConsumption());
    }
}
