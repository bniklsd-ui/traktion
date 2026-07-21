package de.traktion.traincore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fixed-dt-Substep-Simulator (Z3, T-D13, T-D24).
 *
 * <p><b>T-D13:</b> Sub-Tick, fixed dt, deterministisch. {@code dt = TICK_SECONDS / N_SUBSTEPS}
 * (Default 4), semi-implizites Euler. Keine Wall-Clock, keine variable Schrittweite, jemals.
 * Geordnete Collections in der Physikschleife, gesäter Zufall.
 *
 * <p><b>Regel 2:</b> der Simulator ruft {@link Physics#requiredPowerW} auf — er implementiert
 * die Formel NICHT selbst. Ein Duplikat wäre ein Regel-2-Verstoß (§9).
 *
 * <p><b>Z3:</b> Token bewegt sich A→B. Bei Unterversorgung (PowerGrid liefert weniger als der
 * Bedarf) bremst der Zug.
 *
 * <p><b>Physik-Modell (P1-Durchstich):</b>
 * <ol>
 *   <li>Bedarf: {@code reqW = Physics.requiredPowerW(consist, v, gradient)} (Regel 2)</li>
 *   <li>Angebot: {@code availW = powerGrid.availableW(maxPowerW, distanceOnEdge, dt)}
 *       (mit Spannungsabfall — der Token fragt sein Leistungsbudget an, nicht den Bedarf, damit
 *       es Überschuss für Beschleunigung gibt)</li>
 *   <li>Überschuss: {@code excessW = availW - reqW} (negativ bei Unterversorgung)</li>
 *   <li>Kraft: {@code F = excessW / max(v, EPS_V)} (bei v=0: Anfahren mit EPS_V als Divisor)</li>
 *   <li>Beschleunigung: {@code a = F / mass}</li>
 *   <li>Semi-implizites Euler: {@code v = max(0, v + a*dt); x = x + v*dt}</li>
 * </ol>
 *
 * <p><b>Determinismus (Regel 8, T-D24):</b> {@code Random} mit festem Seed (Constructor), keine
 * Wall-Clock. Geordnete Iteration über Tokens ({@link ArrayList}, nicht {@code HashSet}).
 * In P1 gibt es keine echten Zufallsquellen in der Physik — der Seed etabliert die API für
 * P3/P5 (property-based Tests, mehrere Tokens in zufälliger Reihenfolge).
 */
public final class Simulator {

    /** Minecraft-Tick in Sekunden (1/20 s). */
    public static final double TICK_SECONDS = 0.05;

    /** Anzahl der Substeps pro Tick (T-D13 Default). */
    public static final int DEFAULT_N_SUBSTEPS = 4;

    /** Kleinste Geschwindigkeit für Division (vermeide Division durch null beim Anfahren). */
    private static final double EPS_V = 0.01;

    private final PowerGrid powerGrid;
    private final int nSubsteps;
    private final double dt;
    private final long seed;
    private final Random rng;
    private final List<Token> tokens;

    /**
     * @param powerGrid  Stromnetz (liefert availableW mit Spannungsabfall)
     * @param nSubsteps  Substeps pro Tick (> 0)
     * @param seed       Determinismus-Seed (T-D24)
     */
    public Simulator(PowerGrid powerGrid, int nSubsteps, long seed) {
        if (powerGrid == null) {
            throw new IllegalArgumentException("powerGrid must not be null");
        }
        if (nSubsteps < 1) {
            throw new IllegalArgumentException("nSubsteps must be >= 1: " + nSubsteps);
        }
        this.powerGrid = powerGrid;
        this.nSubsteps = nSubsteps;
        this.dt = TICK_SECONDS / nSubsteps;
        this.seed = seed;
        this.rng = new Random(seed);
        this.tokens = new ArrayList<>();
    }

    /** Default-Substeps (4). */
    public Simulator(PowerGrid powerGrid, long seed) {
        this(powerGrid, DEFAULT_N_SUBSTEPS, seed);
    }

    /** Fügt einen Token hinzu (geordnet — ArrayList, nicht HashSet, Regel 8). */
    public void addToken(Token token) {
        if (token == null) {
            throw new IllegalArgumentException("token must not be null");
        }
        tokens.add(token);
    }

    /** Unveränderliche Sicht auf alle Tokens (Iteration in Einfügereihenfolge). */
    public List<Token> tokens() {
        return List.copyOf(tokens);
    }

    /** Tick-Länge in Sekunden. */
    public double tickSeconds() { return TICK_SECONDS; }

    /** Substep-Anzahl. */
    public int nSubsteps() { return nSubsteps; }

    /** Substep-Länge in Sekunden (dt = TICK_SECONDS / nSubsteps). */
    public double dt() { return dt; }

    /** Seed (T-D24). */
    public long seed() { return seed; }

    /**
     * Führt einen Tick aus: {@code nSubsteps} Substeps, jeder Token pro Substep bewegt.
     * Geordnete Iteration über Tokens (ArrayList, Regel 8). Keine Wall-Clock.
     */
    public void tick() {
        for (int sub = 0; sub < nSubsteps; sub++) {
            for (Token token : tokens) {
                stepToken(token, dt);
            }
        }
    }

    /**
     * Führt {@code nTicks} Ticks aus.
     */
    public void run(int nTicks) {
        if (nTicks < 0) {
            throw new IllegalArgumentException("nTicks must be >= 0: " + nTicks);
        }
        for (int t = 0; t < nTicks; t++) {
            tick();
        }
    }

    private void stepToken(Token token, double dt) {
        Consist consist = token.consist();
        double mass = consist.totalMassKg();
        if (mass == 0.0) {
            return; // kein Zug, keine Bewegung
        }

        double v = token.speedMps();
        Edge edge = token.edge();
        double gradient = edge.gradient();
        double distanceOnEdge = token.distanceOnEdgeMeters();

        // Regel 2: Bedarf aus der EINEN Physikfunktion (kein Duplikat)
        double reqW = Physics.requiredPowerW(consist, v, gradient);

        // Angebot: Token fragt sein Leistungsbudget an (mit Spannungsabfall)
        double availW = powerGrid.availableW(token.maxPowerW(), distanceOnEdge, dt);

        // Überschuss (negativ bei Unterversorgung → Bremsung, Z3)
        double excessW = availW - reqW;

        // Kraft aus Überschuss (bei v=0: Anfahren mit EPS_V als Divisor)
        double effectiveV = Math.max(v, EPS_V);
        double force = excessW / effectiveV;

        // Beschleunigung
        double a = force / mass;

        // Semi-implizites Euler: v zuerst, dann x mit neuem v
        double newV = v + a * dt;
        if (newV < 0) {
            newV = 0.0; // Zug hält, fährt nicht rückwärts
        }
        double newX = token.progressMeters() + newV * dt;

        token.setSpeedMps(newV);
        token.setProgressMeters(newX);
    }
}
