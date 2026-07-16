package de.traktion.spike;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * P0.4 Spike-Client-Initializer: registriert den PathEntityRenderer.
 *
 * Ohne registrierten Renderer crasht der Client mit NPE, wenn die Entity
 * gerendert werden soll (EntityRenderDispatcher.shouldRender -> renderer ist null).
 *
 * API verifiziert gegen dekompilierte fabric-rendering-v1 JAR:
 * - EntityRendererRegistry.register(EntityType, EntityRendererProvider)
 *
 * Contributors: Build-Agent (z-ai/glm-5.2), 2026-07-16. Nikinger (Operator).
 */
public class SpikeClientInitializer implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
            SpikeModInitializer.PATH_ENTITY,
            PathEntityRenderer::new
        );

        SpikeModInitializer.LOGGER.info("Traktion P0.4 Spike: PathEntityRenderer registriert.");
    }
}
