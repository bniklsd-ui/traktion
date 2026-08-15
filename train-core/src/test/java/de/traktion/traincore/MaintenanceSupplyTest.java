package de.traktion.traincore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests für {@link MaintenanceSupply} und {@link PlayerLabor} (T-D28, T-D29).
 */
class MaintenanceSupplyTest {

    // --- MaintenanceSupply Vertrag ---

    @Test
    void withdraw_zeroRequest_returnsZero() {
        MaintenanceSupply s = new PlayerLabor(5, 20);
        assertEquals(0, s.withdraw(0));
    }

    @Test
    void withdraw_zeroRequest_doesNotChangeState() {
        PlayerLabor s = new PlayerLabor(5, 20);
        s.tick(1.0); // fill some work
        int before = s.workAvailable();
        s.withdraw(0);
        assertEquals(before, s.workAvailable());
    }

    @Test
    void withdraw_negative_throws() {
        MaintenanceSupply s = new PlayerLabor();
        assertThrows(IllegalArgumentException.class, () -> s.withdraw(-1));
    }

    // --- PlayerLabor Zeit-Akkumulator ---

    @Test
    void playerLabor_startsWithZeroWorkAvailable() {
        PlayerLabor p = new PlayerLabor();
        assertEquals(0, p.workAvailable());
    }

    @Test
    void tick_increasesWorkAvailable() {
        PlayerLabor p = new PlayerLabor(5, 20);
        // rate=5/sec, dt=1.0 → should get 5 units
        p.tick(1.0);
        assertEquals(5, p.workAvailable());
    }

    @Test
    void tick_fractionalTime_accumulatesProportionally() {
        PlayerLabor p = new PlayerLabor(10, 100);
        // rate=10/sec, dt=0.5 → should get 5 units
        p.tick(0.5);
        assertEquals(5, p.workAvailable());
    }

    @Test
    void tick_respectsMaxWork() {
        PlayerLabor p = new PlayerLabor(5, 20);
        // Fill well beyond max
        p.tick(10.0); // would be 50, capped at 20
        assertEquals(20, p.workAvailable());
    }

    @Test
    void tick_multipleTicks_accumulates() {
        PlayerLabor p = new PlayerLabor(5, 20);
        p.tick(1.0); // +5
        p.tick(1.0); // +5
        assertEquals(10, p.workAvailable());
    }

    @Test
    void tick_untilMaxWork_thenStops() {
        PlayerLabor p = new PlayerLabor(5, 20);
        p.tick(10.0); // +50, capped at 20
        assertEquals(20, p.workAvailable());
        p.tick(1.0); // already at max, no change
        assertEquals(20, p.workAvailable());
    }

    @Test
    void tick_zeroDt_doesNothing() {
        PlayerLabor p = new PlayerLabor(5, 20);
        p.tick(1.0);
        int before = p.workAvailable();
        p.tick(0.0);
        assertEquals(before, p.workAvailable());
    }

    @Test
    void tick_negativeDt_throws() {
        PlayerLabor p = new PlayerLabor();
        assertThrows(IllegalArgumentException.class, () -> p.tick(-0.1));
    }

    @Test
    void tick_nanDt_throws() {
        PlayerLabor p = new PlayerLabor();
        assertThrows(IllegalArgumentException.class, () -> p.tick(Double.NaN));
    }

    // --- withdraw reduziert Vorrat ---

    @Test
    void withdraw_reducesWorkAvailable() {
        PlayerLabor p = new PlayerLabor(5, 20);
        p.tick(2.0); // 10 units
        int withdrawn = p.withdraw(6);
        assertEquals(6, withdrawn);
        assertEquals(4, p.workAvailable());
    }

    @Test
    void withdraw_lessThanAvailable_returnsRequested() {
        PlayerLabor p = new PlayerLabor(5, 20);
        p.tick(1.0); // 5 units
        int withdrawn = p.withdraw(3);
        assertEquals(3, withdrawn);
    }

    @Test
    void withdraw_moreThanAvailable_returnsWhatWasThere() {
        PlayerLabor p = new PlayerLabor(5, 20);
        p.tick(1.0); // 5 units
        int withdrawn = p.withdraw(10);
        assertEquals(5, withdrawn); // only 5 was available
        assertEquals(0, p.workAvailable());
    }

    @Test
    void withdraw_exactAmount_drainsToZero() {
        PlayerLabor p = new PlayerLabor(5, 20);
        p.tick(1.0); // 5 units
        p.withdraw(5);
        assertEquals(0, p.workAvailable());
    }

    @Test
    void withdraw_fromEmpty_returnsZero() {
        PlayerLabor p = new PlayerLabor(5, 20);
        // never ticked, workAvailable = 0
        int withdrawn = p.withdraw(5);
        assertEquals(0, withdrawn);
        assertEquals(0, p.workAvailable());
    }

    @Test
    void withdraw_twiceWithoutTick_secondYieldsNothing() {
        PlayerLabor p = new PlayerLabor(5, 20);
        p.tick(1.0); // 5 units
        p.withdraw(5); // drains to 0
        int second = p.withdraw(5); // nothing left
        assertEquals(0, second);
    }

    @Test
    void withdraw_thenTick_accumulatesAgain() {
        PlayerLabor p = new PlayerLabor(5, 20);
        p.tick(1.0); // +5
        p.withdraw(3); // leaves 2
        p.tick(1.0); // +5 → 7 total
        assertEquals(7, p.workAvailable());
    }

    // --- Clamping Invarianten ---

    @Test
    void workAvailable_neverExceedsMaxWork() {
        PlayerLabor p = new PlayerLabor(5, 20);
        for (int i = 0; i < 100; i++) {
            p.tick(1.0);
        }
        assertTrue(p.workAvailable() <= p.maxWork());
    }

    @Test
    void workAvailable_neverNegative() {
        PlayerLabor p = new PlayerLabor(5, 20);
        p.tick(1.0);
        for (int i = 0; i < 10; i++) {
            p.withdraw(5);
        }
        assertTrue(p.workAvailable() >= 0);
    }

    // --- Defaults ---

    @Test
    void defaultRate_isFive() {
        PlayerLabor p = new PlayerLabor();
        assertEquals(5, p.ratePerSecond());
    }

    @Test
    void defaultMaxWork_isTwenty() {
        PlayerLabor p = new PlayerLabor();
        assertEquals(20, p.maxWork());
    }

    // --- Konstruktor-Validierung ---

    @Test
    void constructor_zeroRate_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerLabor(0, 20));
    }

    @Test
    void constructor_negativeRate_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerLabor(-1, 20));
    }

    @Test
    void constructor_negativeMaxWork_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerLabor(5, -1));
    }

    @Test
    void constructor_zeroMaxWork_allowed() {
        PlayerLabor p = new PlayerLabor(5, 0);
        assertEquals(0, p.maxWork());
        p.tick(1.0);
        assertEquals(0, p.workAvailable()); // never accumulates
    }

    // --- Integrität: withdraw + tick zusammen ---

    @Test
    void withdrawAndTick_maintainsInvariant() {
        PlayerLabor p = new PlayerLabor(5, 20);

        // Simulate a sequence of work + repair
        for (int i = 0; i < 10; i++) {
            p.tick(1.0); // accumulate
            int available = p.workAvailable();
            // Withdraw some
            int taken = p.withdraw(3);
            assertTrue(taken <= 3);
            assertTrue(taken <= available);
            assertTrue(p.workAvailable() >= 0);
            assertTrue(p.workAvailable() <= p.maxWork());
        }
    }
}
