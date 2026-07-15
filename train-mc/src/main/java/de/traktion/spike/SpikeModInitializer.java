package de.traktion.spike;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * P0.4 Spike-ModInitializer: registriert PathEntity.
 * Beim Start wird eine Entity am Spawn gespawnt, die dem hartkodierten Pfad folgt.
 *
 * Contributors: Build-Agent (z-ai/glm-5.2), 2026-07-15. Nikinger (Operator).
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
    }
}
