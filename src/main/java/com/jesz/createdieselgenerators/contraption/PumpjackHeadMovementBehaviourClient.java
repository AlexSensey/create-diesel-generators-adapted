package com.jesz.createdieselgenerators.contraption;

import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlockEntity;
import com.jesz.createdieselgenerators.foundation.FluidCompatibility;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.jesz.createdieselgenerators.CDGPartialModels.PUMPJACK_ROPE;

/** Client-only rope renderer for the pumpjack movement actor. */
public final class PumpjackHeadMovementBehaviourClient {
    private PumpjackHeadMovementBehaviourClient() {}

    public static void submitRope(MovementContext context, PoseStack poseStack,
                                  SubmitNodeCollector collector, int light, float partialTicks) {
        BlockPos hole = findHole(context);
        if (hole == null || context.position == null
                || !(context.contraption instanceof BearingContraption bearing)
                || !(context.contraption.entity instanceof ControlledContraptionEntity entity))
            return;

        BlockStateModelPart rope = PUMPJACK_ROPE.get();
        if (rope == null)
            return;

        Vec3 previousPosition = context.position.subtract(context.motion);
        Direction.Axis bearingAxis = bearing.getFacing().getOpposite().getAxis();
        poseStack.pushPose();
        poseStack.translate(.5, 7 / 16f, .5);
        if (bearingAxis == Direction.Axis.X) {
            double zDistance = Mth.lerp(partialTicks, previousPosition.z, context.position.z) - hole.getZ() - .5;
            double yDistance = Mth.lerp(partialTicks, previousPosition.y, context.position.y) - hole.getY() - .8;
            float length = (float) Math.sqrt(zDistance * zDistance + yDistance * yDistance);
            float angle = (float) (-entity.getAngle(partialTicks)
                    - Math.toDegrees(Math.atan2(yDistance, zDistance)) + 90);
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle));
            poseStack.scale(1, length, 1);
        } else {
            double xDistance = Mth.lerp(partialTicks, previousPosition.x, context.position.x) - hole.getX() - .5;
            double yDistance = Mth.lerp(partialTicks, previousPosition.y, context.position.y) - hole.getY() - .8;
            float length = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);
            float angle = (float) (-entity.getAngle(partialTicks)
                    + Math.toDegrees(Math.atan2(yDistance, xDistance)) - 90);
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
            poseStack.scale(1, length, 1);
        }
        collector.submitBlockModel(poseStack, RenderTypes.cutoutMovingBlock(), List.of(rope),
                BlockModelRenderState.EMPTY_TINTS, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static BlockPos findHole(MovementContext context) {
        BlockPos saved = FluidCompatibility.readBlockPos(context.data, "HolePos");
        if (saved != null && context.world.getBlockEntity(saved) instanceof PumpjackHoleBlockEntity)
            return saved;

        BlockPos localColumn = context.contraption.anchor.offset(context.localPos);
        for (int depth = 0; depth <= 32; depth++) {
            BlockPos candidate = localColumn.below(depth);
            if (context.world.getBlockEntity(candidate) instanceof PumpjackHoleBlockEntity)
                return candidate;
        }

        if (context.position != null) {
            BlockPos animated = BlockPos.containing(context.position);
            for (int depth = 0; depth <= 32; depth++)
                for (int x = -2; x <= 2; x++)
                    for (int z = -2; z <= 2; z++) {
                        BlockPos candidate = animated.offset(x, -depth, z);
                        if (context.world.getBlockEntity(candidate) instanceof PumpjackHoleBlockEntity)
                            return candidate;
                    }
        }
        return null;
    }
}
