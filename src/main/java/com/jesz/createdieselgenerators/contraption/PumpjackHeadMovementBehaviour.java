package com.jesz.createdieselgenerators.contraption;

import com.jesz.createdieselgenerators.content.pumpjack.PumpjackBearingBBlock;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackBearingBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.nbt.NBTHelper;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.jesz.createdieselgenerators.CDGPartialModels.PUMPJACK_ROPE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class PumpjackHeadMovementBehaviour implements MovementBehaviour {
    @Nullable
    @Override
    public ItemStack canBeDisabledVia(MovementContext context) {
        return null;
    }

    @Override
    public boolean isActive(MovementContext context) {
        if (!(context.contraption instanceof BearingContraption))
            return false;
        if (((BearingContraption) context.contraption).getFacing().getAxis() == Direction.Axis.Y || context.state.getValue(PumpjackBearingBBlock.FACING).getAxis() != ((BearingContraption) context.contraption).getFacing().getClockWise().getAxis())
            return false;
        return context.world.getBlockEntity(context.contraption.anchor.relative(((BearingContraption) context.contraption).getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity;
    }

    @Override
    public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld,
                                    MovementContext movementContext) {
        // The rope stretches from a moving contraption actor to a block in the real world.
        // Render it through ContraptionEntityRenderer's 26.2 submit path; a Flywheel actor
        // visual only owns contraption-local geometry and loses this cross-world transform.
        return null;
    }

    /** Minecraft 26.2 submit-path renderer used when Flywheel visualization is disabled. */
    @OnlyIn(Dist.CLIENT)
    public void submitRopeInContraption(MovementContext context, PoseStack ms, SubmitNodeCollector collector,
                                        int light, float partialTicks) {
        BlockPos hole = findHoleForRendering(context);
        if (hole == null || context.position == null
                || !(context.contraption instanceof BearingContraption bearingContraption)
                || !(context.contraption.entity instanceof ControlledContraptionEntity entity))
            return;

        BlockStateModelPart rope = PUMPJACK_ROPE.get();
        if (rope == null)
            return;

        Vec3 previousPosition = context.position.subtract(context.motion);
        Direction.Axis bearingAxis = bearingContraption.getFacing().getOpposite().getAxis();

        ms.pushPose();
        // The rope model ends at y=0. Position its top exactly against the
        // underside of the head's 7/16-high crossbar instead of overlapping it.
        ms.translate(.5, 7 / 16f, .5);
        if (bearingAxis == Direction.Axis.X) {
            double zDistance = Mth.lerp(partialTicks, previousPosition.z, context.position.z) - hole.getZ() - .5;
            double yDistance = Mth.lerp(partialTicks, previousPosition.y, context.position.y) - hole.getY() - .8;
            float length = (float) Math.sqrt(zDistance * zDistance + yDistance * yDistance);
            float angle = (float) (-entity.getAngle(partialTicks)
                    - Math.toDegrees(Math.atan2(yDistance, zDistance)) + 90);
            ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(angle));
            ms.scale(1, length, 1);
        } else {
            double xDistance = Mth.lerp(partialTicks, previousPosition.x, context.position.x) - hole.getX() - .5;
            double yDistance = Mth.lerp(partialTicks, previousPosition.y, context.position.y) - hole.getY() - .8;
            float length = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);
            float angle = (float) (-entity.getAngle(partialTicks)
                    + Math.toDegrees(Math.atan2(yDistance, xDistance)) - 90);
            ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
            ms.scale(1, length, 1);
        }
        collector.submitBlockModel(ms, RenderTypes.cutoutMovingBlock(), List.of(rope),
                BlockModelRenderState.EMPTY_TINTS, light, OverlayTexture.NO_OVERLAY, 0);
        ms.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
        BlockPos hole = findHoleForRendering(context);
        if (hole == null)
            return;
        PumpjackBearingBlockEntity bearing = null;
        if (context.world.getBlockEntity(context.contraption.anchor.relative(((BearingContraption) context.contraption).getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity be)
            bearing = be;
        if (bearing == null)
            return;
        float partialTicks = AnimationTickHolder.getPartialTicks();

        com.jesz.createdieselgenerators.foundation.PartialBufferCompatibility.LegacyBuffer cover = com.jesz.createdieselgenerators.foundation.PartialBufferCompatibility.partial(PUMPJACK_ROPE, context.state);
        if (((BearingContraption) context.contraption).getFacing().getOpposite().getAxis() == Direction.Axis.X) {
            Vec3 prevPos = context.position.subtract(context.motion);

            double zDst = Mth.lerp(partialTicks, prevPos.z, context.position.z) - hole.getZ()-0.5f;
            double yDst = Mth.lerp(partialTicks, prevPos.y, context.position.y) - hole.getY()-0.8f;
            float distanceFromHole = (float) Math.sqrt(zDst*zDst + yDst*yDst);
            double angle = -((ControlledContraptionEntity) context.contraption.entity).getAngle(partialTicks)-(180 * Math.atan2(yDst,zDst)/Math.PI)+90;
            cover.translate(0.5, 0.5,  0.5)
                    .rotateXDegrees((float) angle)
                    .scale(1, distanceFromHole, 1)
                    .useLevelLight((net.minecraft.client.renderer.block.BlockAndTintGetter) context.world, matrices.getWorld())
                    .renderInto(matrices.getModel(), buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
            return;
        }

        Vec3 prevPos = context.position.subtract(context.motion);

        double xDst = Mth.lerp(partialTicks, prevPos.x, context.position.x) - hole.getX()-0.5;
        double yDst = Mth.lerp(partialTicks, prevPos.y, context.position.y) - hole.getY()-0.8f;

        float distanceFromHole = (float) Math.sqrt(xDst*xDst + yDst*yDst);
        double angle = -((ControlledContraptionEntity) context.contraption.entity).getAngle(partialTicks)+(180 * Math.atan2(yDst,xDst)/Math.PI)-90;
        cover.translate(0.5, 0.5,  0.5)
                .rotateZDegrees((float) angle)
                .scale(1, distanceFromHole, 1)
                .useLevelLight((net.minecraft.client.renderer.block.BlockAndTintGetter) context.world, matrices.getWorld())
                .renderInto(matrices.getModel(), buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }

    @Nullable
    static BlockPos findHoleForRendering(MovementContext context) {
        BlockPos saved = com.jesz.createdieselgenerators.foundation.FluidCompatibility.readBlockPos(context.data, "HolePos");
        if (context.world.getBlockEntity(saved) instanceof PumpjackHoleBlockEntity)
            return saved;

        // MovementContext data is not guaranteed to be synchronized to the client every tick.
        // The pumpjack head's unrotated local column is the canonical place for its well head.
        BlockPos localColumn = context.contraption.anchor.offset(context.localPos);
        for (int depth = 0; depth <= 32; depth++) {
            BlockPos candidate = localColumn.below(depth);
            if (context.world.getBlockEntity(candidate) instanceof PumpjackHoleBlockEntity)
                return candidate;
        }

        // While the beam is moving, rounding its animated world position can move the search
        // into a neighbouring column. Search that small neighbourhood as a final fallback.
        if (context.position != null) {
            BlockPos animated = BlockPos.containing(context.position);
            for (int depth = 0; depth <= 32; depth++)
                for (int dx = -2; dx <= 2; dx++)
                    for (int dz = -2; dz <= 2; dz++) {
                        BlockPos candidate = animated.offset(dx, -depth, dz);
                        if (context.world.getBlockEntity(candidate) instanceof PumpjackHoleBlockEntity)
                            return candidate;
                    }
        }
        return null;
    }

    BlockPos holePos;
    BlockPos headPos;
    @Override
    public void tick(MovementContext context) {
        MovementBehaviour.super.tick(context);
        PumpjackBearingBlockEntity bearing = null;
        if (context.world.getBlockEntity(context.contraption.anchor.relative(((BearingContraption) context.contraption).getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity be)
            bearing = be;
        if (bearing == null)
            return;
        headPos = new BlockPos(
                context.contraption.anchor.getX() + context.localPos.getX(),
                context.contraption.anchor.getY() + context.localPos.getY(),
                context.contraption.anchor.getZ() + context.localPos.getZ());
        holePos = headPos;
        for (int i = 0; i < 32; i++) {
            if (context.world.getBlockEntity(holePos) instanceof PumpjackHoleBlockEntity phbe)
                break;
            else
                holePos = holePos.below();
        }

        if (context.world.getBlockEntity(holePos) instanceof PumpjackHoleBlockEntity holeBE && bearing.crankSpeed >= 8) {
            holeBE.headPos = bearing.getBlockState().getValue(FACING).getAxis() == Direction.Axis.X ? context.localPos.getZ() : context.localPos.getX();
            holeBE.bearingPos = bearing.getBlockState().getValue(FACING).getAxis() == Direction.Axis.X ? bearing.bearingBPos.getZ() : bearing.bearingBPos.getX();
            if ((bearing.crankAngle + 270) % 360 < (context.data.getFloatOr("OldCrankAngle", 0) + 270) % 360)
                holeBE.pumpjackRotation(bearing.isLarge);
        }
        context.data.putFloat("OldCrankAngle", bearing.crankAngle);

        context.data.put("HolePos", com.jesz.createdieselgenerators.foundation.FluidCompatibility.writeBlockPos(holePos));
    }
}
