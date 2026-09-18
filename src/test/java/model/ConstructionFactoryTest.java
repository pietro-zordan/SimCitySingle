package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
//si assicura che construction funzioni correttamente per qualsiasi tipo di costruzione
class ConstructionFactoryTest {

    @Test void createRoad() {
        Construction construction = ConstructionFactory.create(ConstructionType.ROAD);
        assertInstanceOf(Road.class, construction);
    }
    @Test void createPark() {
        Construction construction = ConstructionFactory.create(ConstructionType.PARK);
        assertInstanceOf(Park.class, construction);
    }
    @Test void createResidential() {
        Construction construction = ConstructionFactory.create(ConstructionType.RESIDENTIAL);
        assertInstanceOf(Residential.class, construction);
    }
    @Test void createCommercial() {
        Construction construction = ConstructionFactory.create(ConstructionType.COMMERCIAL);
        assertInstanceOf(Commercial.class, construction);
    }
    @Test void createIndustrial() {
        Construction construction = ConstructionFactory.create(ConstructionType.INDUSTRIAL);
        assertInstanceOf(Industrial.class, construction);
    }
    @Test void createPowerPlant() {
        Construction construction = ConstructionFactory.create(ConstructionType.POWER_PLANT);
        assertInstanceOf(PowerPlant.class, construction);
    }
    @Test void createWithNullThrowsException() {
        assertThrows( NullPointerException.class, () -> ConstructionFactory.create(null) ); }
}