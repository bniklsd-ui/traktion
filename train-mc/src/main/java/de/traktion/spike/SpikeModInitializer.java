package de.traktion.spike;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * P0.4 Spike-ModInitializer: registriert PathEntity und spawnt eine beim Serverstart.
 * Die Entity folgt einem hartkodierten Pfad — das testet T-D3 (zustandserhaltende
 * Rekonstruktion nach Chunk-Unload).
 *
 * API verifiziert gegen dekompilierte MC 26.2 + Fabric-API 0.154.0+26.2 JARs:
 * - ServerLifecycleEvents.SERVER_STARTED (Callback: MinecraftServer)
 * - MinecraftServer.overworld() -> ServerLevel
 * - EntityType.spawn(ServerLevel, BlockPos, EntitySpawnReason)
 *
 * Contributors: Build-Agent (z-ai/glm-5.2), 2026-07-15/16. Nikinger (Operator).
 */
public class SpikeModInitializer implements ModInitializer {

    public static final String MOD_ID = "traktion";
    public static final Logger LOGGER = LoggerFactory.getLogger("traktion-spike");

    public static final EntityType<PathEntity> PATH_ENTITY = register(
        "path_entity",
        EntityType.Builder.<PathEntity>of(PathEntity::new, MobCategory.MISC)
            .sized(0.6f, 0.6f)
    );

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(MOD_ID, name)
        );
        return Registry.register(
            BuiltInRegistries.ENTITY_TYPE, key, builder.build(key)
        );
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Traktion P0.4 Spike: PathEntity registriert. T-D3 wird geprueft.");

        // Spawn eine PathEntity beim Serverstart in der Overworld am Welt-Spawn.
        // Vorheriger Code registrierte nur — die Entity erschien nie (Nikingers Smoke-Test-Befund).
        ServerLifecycleEvents.SERVER_STARTED.register(this::spawnPathEntityOnStart);
    }

    private void spawnPathEntityOnStart(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        BlockPos spawnPos = new BlockPos(0, 1, 0);

        PathEntity entity = PATH_ENTITY.spawn(overworld, spawnPos, EntitySpawnReason.COMMAND);
        if (entity != null) {
            LOGGER.info("Traktion P0.4 Spike: PathEntity gespawnt bei {}", spawnPos);
        } else {
            LOGGER.error("Traktion P0.4 Spike: PathEntity-Spawn fehlgeschlagen bei {}", spawnPos);
        }
    }
}
