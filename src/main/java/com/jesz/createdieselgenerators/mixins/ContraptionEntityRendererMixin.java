package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.contraption.PumpjackHeadMovementBehaviour;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds CDG movement actors to Minecraft 26.2's submit renderer without coupling Create to this addon. */
@Mixin(ContraptionEntityRenderer.class)
public abstract class ContraptionEntityRendererMixin {
    @Inject(method = "submitActors", at = @At("TAIL"), remap = false)
    private static void createdieselgenerators$submitPumpjackRope(AbstractContraptionEntity entity,
                                                                  VirtualRenderWorld renderWorld,
                                                                  Contraption contraption,
                                                                  PoseStack ms,
                                                                  SubmitNodeCollector collector,
                                                                  float partialTicks,
                                                                  CallbackInfo ci) {
        Level level = entity.level();
        for (Pair<StructureTemplate.StructureBlockInfo, MovementContext> actor : contraption.getActors()) {
            MovementContext context = actor.getRight();
            if (context == null)
                continue;
            if (context.world == null)
                context.world = level;

            StructureTemplate.StructureBlockInfo blockInfo = actor.getLeft();
            if (contraption.isHiddenInPortal(blockInfo.pos()))
                continue;

            MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(blockInfo.state());
            if (!(behaviour instanceof PumpjackHeadMovementBehaviour pumpjack))
                continue;

            BlockPos lightPos = BlockPos.containing(
                    entity.toGlobalVector(Vec3.atCenterOf(blockInfo.pos()), partialTicks));
            int light = LightCoordsUtil.pack(level.getBrightness(LightLayer.BLOCK, lightPos),
                    level.getBrightness(LightLayer.SKY, lightPos));

            ms.pushPose();
            ms.translate(blockInfo.pos().getX(), blockInfo.pos().getY(), blockInfo.pos().getZ());
            pumpjack.submitRopeInContraption(context, ms, collector, light, partialTicks);
            ms.popPose();
        }
    }
}
