package de.traktion.spike;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.syncher.SynchedEntityData;

/**
 * P0.4 Spike-Entity: faehrt einem hartkodierten rechteckigen Pfad folgt.
 * Speichert Pfad-Fortschritt in ValueOutput/ValueInput (26.x API).
 * Bei Chunk-Unload despawnt sie (Vanilla), bei Chunk-Load rekonstruiert sie
 * mit erhaltenem Zustand — das testet T-D3.
 *
 * Contributors: Build-Agent (z-ai/glm-5.2), 2026-07-15. Nikinger (Operator).
 */
public class PathEntity extends Entity {

    // Hartkodierter Pfad: Quadrat mit Seitenlaenge 10 Bloecke um (0,0)
    private static final double PATH_RADIUS = 10.0;
    private static final double SPEED = 0.2; // Bloecke pro Tick

    // Pfad-Fortschritt: 0.0 bis 4.0 (4 Seiten des Quadrats)
    private double pathProgress = 0.0;

    public PathEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true; // ignoriert Kollisionen fuer einfachen Pfad
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return; // Logik nur serverseitig
        }

        // Pfad-Fortschritt aktualisieren
        pathProgress += SPEED / PATH_RADIUS;
        if (pathProgress >= 4.0) {
            pathProgress = 0.0; // Schleife
        }

        // Position auf dem Quadrat berechnen (Zentrum 0,0)
        double side = pathProgress;
        double x, z;

        if (side < 1.0) {
            x = -PATH_RADIUS + (2 * PATH_RADIUS * side);
            z = -PATH_RADIUS;
        } else if (side < 2.0) {
            x = PATH_RADIUS;
            z = -PATH_RADIUS + (2 * PATH_RADIUS * (side - 1.0));
        } else if (side < 3.0) {
            x = PATH_RADIUS - (2 * PATH_RADIUS * (side - 2.0));
            z = PATH_RADIUS;
        } else {
            x = -PATH_RADIUS;
            z = PATH_RADIUS - (2 * PATH_RADIUS * (side - 3.0));
        }

        this.setPos(x, 1.0, z);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putDouble("path_progress", this.pathProgress);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.pathProgress = valueInput.getDoubleOr("path_progress", 0.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Keine synched Daten fuer den Spike
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount) {
        return false; // Spike-Entity nimmt keinen Schaden
    }
}
