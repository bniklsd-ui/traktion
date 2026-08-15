package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für {@link ManualGenerator} (T-D30, Port 1 zweite Produktions-Implementierung).
 */
class ManualGeneratorTest {

    // --- Konstruktor ---

    @Test
    void constructor_defaultValues() {
        ManualGenerator g = new ManualGenerator();
        assertEquals(100_000.0, g.maxOutputW());
        assertEquals(1_000.0, g.fuelMj());
    }

    @Test
    void constructor_explicitValues() {
        ManualGenerator g = new ManualGenerator(50_000.0, 500.0);
        assertEquals(50_000.0, g.maxOutputW());
        assertEquals(500.0, g.fuelMj());
    }

    @Test
    void constructor_zeroMaxOutput_throws() {
        assertThrows(IllegalArgumentException.class, () -> new ManualGenerator(0.0, 100.0));
    }

    @Test
    void constructor_negativeMaxOutput_throws() {
        assertThrows(IllegalArgumentException.class, () -> new ManualGenerator(-1.0, 100.0));
    }

    @Test
    void constructor_negativeFuel_throws() {
        assertThrows(IllegalArgumentException.class, () -> new ManualGenerator(100_000.0, -1.0));
    }

    @Test
    void constructor_nanFuel_throws() {
        assertThrows(IllegalArgumentException.class, () -> new ManualGenerator(100_000.0, Double.NaN));
    }

    @Test
    void constructor_infiniteFuel_throws() {
        assertThrows(IllegalArgumentException.class, () -> new ManualGenerator(100_000.0, Double.POSITIVE_INFINITY));
    }

    @Test
    void constructor_infiniteMaxOutput_throws() {
        assertThrows(IllegalArgumentException.class, () -> new ManualGenerator(Double.POSITIVE_INFINITY, 100.0));
    }

    // --- supply() Vertrag (PowerSupply) ---

    @Test
    void supply_requestedZero_returnsZero() {
        ManualGenerator g = new ManualGenerator();
        assertEquals(0.0, g.supply(0.0, 1.0));
    }

    @Test
    void supply_negativeRequest_returnsZero() {
        ManualGenerator g = new ManualGenerator();
        assertEquals(0.0, g.supply(-100.0, 1.0));
    }

    @Test
    void supply_nanRequest_returnsZero() {
        ManualGenerator g = new ManualGenerator();
        assertEquals(0.0, g.supply(Double.NaN, 1.0));
    }

    @Test
    void supply_infiniteRequest_returnsZero() {
        ManualGenerator g = new ManualGenerator();
        assertEquals(0.0, g.supply(Double.POSITIVE_INFINITY, 1.0));
    }

    @Test
    void supply_zeroDt_returnsZero() {
        ManualGenerator g = new ManualGenerator();
        assertEquals(0.0, g.supply(1000.0, 0.0));
    }

    @Test
    void supply_negativeDt_returnsZero() {
        ManualGenerator g = new ManualGenerator();
        assertEquals(0.0, g.supply(1000.0, -0.1));
    }

    @Test
    void supply_nanDt_returnsZero() {
        ManualGenerator g = new ManualGenerator();
        assertEquals(0.0, g.supply(1000.0, Double.NaN));
    }

    // --- supply() Lieferung ---

    @Test
    void supply_withinHardwareAndFuelLimit_deliversRequested() {
        // 100 kW max, 1000 MJ fuel, anfordern 50 kW für 1s → sollte 50 kW liefern
        ManualGenerator g = new ManualGenerator(100_000.0, 1000.0);
        double delivered = g.supply(50_000.0, 1.0);
        assertEquals(50_000.0, delivered, 1e-9);
    }

    @Test
    void supply_atHardwareLimit_deliversMaxOutput() {
        // 100 kW max, anfordern 200 kW → sollte 100 kW liefern (Hardware-Limit)
        ManualGenerator g = new ManualGenerator(100_000.0, 1000.0);
        double delivered = g.supply(200_000.0, 1.0);
        assertEquals(100_000.0, delivered, 1e-9);
    }

    @Test
    void supply_shortDuration_stillDeliversFullPower() {
        // supply() liefert Leistung (Watt), nicht Energie. Bei 100 kW max und 100 kW anfordern
        // liefert er 100 kW — die Dauer beeinflusst nur den Brennstoff-Verbrauch.
        ManualGenerator g = new ManualGenerator(100_000.0, 1000.0);
        double delivered = g.supply(100_000.0, 0.5);
        assertEquals(100_000.0, delivered, 1e-9);
        // Aber der Brennstoff-Verbrauch ist halbiert: 100 kW * 0.5s = 50 kJ = 0.05 MJ
        assertEquals(999.95, g.fuelMj(), 1e-9);
    }

    @Test
    void supply_emptyFuel_returnsZero() {
        ManualGenerator g = new ManualGenerator(100_000.0, 0.0);
        assertEquals(0.0, g.supply(50_000.0, 1.0));
    }

    @Test
    void supply_exhaustedFuel_returnsZero() {
        // Minimaler Brennstoff: 0.1 MJ = 100 kJ
        // Verbrauch für 100 kW für 1s = 100 kJ
        ManualGenerator g = new ManualGenerator(100_000.0, 0.05); // nur 50 kJ
        double delivered = g.supply(100_000.0, 1.0); // würde 100 kW für 1s wollen, braucht 100 kJ
        // Lieferung wird durch fuel begrenzt: fuel * 1e6 / dt = 0.05 * 1e6 / 1 = 50.000 W
        assertEquals(50_000.0, delivered, 1e-9);
        // Jetzt tank leer
        assertEquals(0.0, g.supply(50_000.0, 1.0), 1e-9);
    }

    // --- Brennstoff-Verbrauch ---

    @Test
    void supply_consumesFuel_proportionalToDelivery() {
        ManualGenerator g = new ManualGenerator(100_000.0, 1000.0);
        // Liefern 50 kW für 1s → 50 kJ = 0.05 MJ verbraucht
        g.supply(50_000.0, 1.0);
        assertEquals(999.95, g.fuelMj(), 1e-9);
    }

    @Test
    void supply_fractionalFuelConsumption() {
        ManualGenerator g = new ManualGenerator(100_000.0, 1000.0);
        // 10 kW für 1s → 10 kJ = 0.01 MJ
        g.supply(10_000.0, 1.0);
        assertEquals(999.99, g.fuelMj(), 1e-9);
    }

    @Test
    void supply_fuelNotDepletedBelowZero() {
        ManualGenerator g = new ManualGenerator(100_000.0, 0.001); // 1 kJ
        g.supply(10_000.0, 1.0); // würde 10 kJ wollen, aber nur 1 kJ verfügbar
        assertEquals(0.0, g.fuelMj(), 1e-9);
    }

    @Test
    void supply_veryShortDuration_smallFuelConsumption() {
        ManualGenerator g = new ManualGenerator(100_000.0, 1000.0);
        // 100 kW für 0.001s (1ms) → 100 J = 0.0001 MJ
        g.supply(100_000.0, 0.001);
        assertEquals(999.9999, g.fuelMj(), 1e-6);
    }

    // --- refuel ---

    @Test
    void refuel_increasesFuel() {
        ManualGenerator g = new ManualGenerator(100_000.0, 100.0);
        g.refuel(50.0);
        assertEquals(150.0, g.fuelMj(), 1e-9);
    }

    @Test
    void refuel_zero_doesNothing() {
        ManualGenerator g = new ManualGenerator(100_000.0, 100.0);
        g.refuel(0.0);
        assertEquals(100.0, g.fuelMj());
    }

    @Test
    void refuel_negative_throws() {
        ManualGenerator g = new ManualGenerator();
        assertThrows(IllegalArgumentException.class, () -> g.refuel(-1.0));
    }

    @Test
    void refuel_nan_throws() {
        ManualGenerator g = new ManualGenerator();
        assertThrows(IllegalArgumentException.class, () -> g.refuel(Double.NaN));
    }

    @Test
    void refuel_infinite_throws() {
        ManualGenerator g = new ManualGenerator();
        assertThrows(IllegalArgumentException.class, () -> g.refuel(Double.POSITIVE_INFINITY));
    }

    // --- Integration mit PowerGrid ---

    @Test
    void integration_withPowerGrid_works() {
        // ManualGenerator als Supply für PowerGrid
        ManualGenerator gen = new ManualGenerator(100_000.0, 1000.0);
        PowerGrid grid = new PowerGrid(gen, 500.0);

        // Verfügbare Leistung am Unterwerk (Distanz 0)
        double avail = grid.availableW(50_000.0, 0.0, 1.0, 0.05);
        assertEquals(50_000.0, avail, 1e-9);
        assertTrue(gen.fuelMj() < 1000.0); // Brennstoff verbraucht
    }

    // --- Monotonie ---

    @Test
    void monotonie_largerRequestNeverDeliversLess() {
        ManualGenerator g = new ManualGenerator(100_000.0, 1000.0);
        double d1 = g.supply(30_000.0, 1.0);
        double d2 = g.supply(50_000.0, 1.0);
        assertTrue(d2 >= d1, "larger request should deliver >= smaller request");
    }

    @Test
    void monotonie_moreFuelNeverDeliversLess() {
        ManualGenerator g1 = new ManualGenerator(100_000.0, 100.0);
        ManualGenerator g2 = new ManualGenerator(100_000.0, 1000.0);
        double d1 = g1.supply(100_000.0, 1.0);
        double d2 = g2.supply(100_000.0, 1.0);
        // Same maxOutput, g2 just has more fuel (but can't deliver more than maxOutput anyway)
        assertEquals(d1, d2, 1e-9);
    }
}
