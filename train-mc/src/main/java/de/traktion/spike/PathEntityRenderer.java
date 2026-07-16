package de.traktion.spike;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * P0.4 Spike-Renderer: rendert nichts. Verhindert den NPE-Crash, der auftritt,
 * wenn eine Entity ohne registrierten Renderer gerendert werden soll.
 *
 * Der Spike testet T-D3 (zustandserhaltende Rekonstruktion), nicht Rendering.
 * Die Entity ist unsichtbar — sichtbar nur über F3+B (Hitbox) oder F3-Entity-Count.
 *
 * API verifiziert gegen dekompilierte MC 26.2 JAR:
 * - EntityRenderer<T, S> ist abstrakt, createRenderState() ist abstrakt
 * - EntityRenderState ist eine konkrete Klasse (keine Ableitung nötig)
 * - submit() kann leer bleiben (nichts rendern)
 *
 * Contributors: Build-Agent (z-ai/glm-5.2), 2026-07-16. Nikinger (Operator).
 */
public class PathEntityRenderer extends EntityRenderer<PathEntity, EntityRenderState> {

    public PathEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void extractRenderState(PathEntity entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        // Minimal: nur die Basis-Extraktion. Kein Rendering, keine Modelle.
    }

    @Override
    public void submit(EntityRenderState state,
                       com.mojang.blaze3d.vertex.PoseStack poseStack,
                       net.minecraft.client.renderer.SubmitNodeCollector nodeCollector,
                       net.minecraft.client.renderer.state.level.CameraRenderState cameraState) {
        // Leer: nichts rendern. Der Spike braucht keine visuelle Darstellung.
    }
}
