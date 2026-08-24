package com.jesz.createdieselgenerators.foundation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Renders 26.2 scroll-option markers for add-on block entities. */
public final class ScrollOptionOverlayRenderer {
    private ScrollOptionOverlayRenderer() {}

    public static void render(BlockEntity be, ScrollOptionBehaviour<?> option, PoseStack ms,
                              SubmitNodeCollector collector) {
        if (option == null || !option.isActive() || be.getLevel() == null)
            return;

        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (!(hitResult instanceof BlockHitResult blockHit) || !blockHit.getBlockPos().equals(be.getBlockPos()))
            return;

        BlockPos pos = be.getBlockPos();
        Direction side = blockHit.getDirection();
        BlockState state = be.getBlockState();
        ValueBoxTransform slot = option.getSlotPositioning();
        if (slot instanceof ValueBoxTransform.Sided sided)
            sided.fromSide(side);
        if (!slot.shouldRender(be.getLevel(), pos, state))
            return;

        Vec3 localHit = blockHit.getLocation().subtract(Vec3.atLowerCornerOf(pos));
        if (!slot.testHit(be.getLevel(), pos, state, localHit))
            return;

        Vec3 offset = slot.getLocalOffset(be.getLevel(), pos, state);
        Vec3 normal = Vec3.atLowerCornerOf(side.getUnitVec3i());
        AllIcons icon = option.get().getIcon();

        ms.pushPose();
        ms.translate(offset.x + normal.x / 32d + normal.x / 512d,
                offset.y + normal.y / 32d + normal.y / 512d,
                offset.z + normal.z / 32d + normal.z / 512d);
        rotateOverlay(ms, state, side);
        ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180));
        ms.scale(-1, 1, 1);
        collector.submitCustomGeometry(ms, RenderTypes.debugQuads(), ScrollOptionOverlayRenderer::renderFrame);

        ms.scale(.25f, .25f, .25f);
        ms.translate(-.5f, -.5f, 1 / 256f);
        collector.submitCustomGeometry(ms, RenderTypes.textSeeThrough(AllIcons.ICON_ATLAS),
                (pose, consumer) -> icon.renderDoubleSided(pose, consumer, 0xDDDDDD));
        ms.popPose();
    }

    private static void rotateOverlay(PoseStack ms, BlockState state, Direction face) {
        switch (face) {
            case NORTH -> ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
            case EAST -> ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
            case WEST -> ms.mulPose(com.mojang.math.Axis.YP.rotationDegrees(270));
            case UP -> ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(270));
            case DOWN -> ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
            case SOUTH -> {}
        }
        if (!face.getAxis().isVertical() || !state.hasProperty(BlockStateProperties.FACING))
            return;
        Direction facing = state.getValue(BlockStateProperties.FACING);
        if (!facing.getAxis().isVertical())
            ms.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(AngleHelper.horizontalAngle(facing) + 180));
    }

    private static void renderFrame(Pose pose, VertexConsumer consumer) {
        int color = 0xFFFFFFFF;
        renderCorner(pose, consumer, 5, 5, 1, 1, color);
        renderCorner(pose, consumer, 10, 5, -1, 1, color);
        renderCorner(pose, consumer, 5, 10, 1, -1, color);
        renderCorner(pose, consumer, 10, 10, -1, -1, color);
    }

    private static void renderCorner(Pose pose, VertexConsumer consumer, int x, int y, int xStep, int yStep,
                                     int color) {
        pixel(pose, consumer, x, y, color);
        pixel(pose, consumer, x + xStep, y, color);
        pixel(pose, consumer, x, y + yStep, color);
    }

    private static void pixel(Pose pose, VertexConsumer consumer, int x, int y, int color) {
        float pixel = 1 / 16f;
        float x0 = x * pixel - .5f;
        float y0 = y * pixel - .5f;
        float x1 = (x + 1) * pixel - .5f;
        float y1 = (y + 1) * pixel - .5f;
        consumer.addVertex(pose, x0, y0, 0).setColor(color);
        consumer.addVertex(pose, x1, y0, 0).setColor(color);
        consumer.addVertex(pose, x1, y1, 0).setColor(color);
        consumer.addVertex(pose, x0, y1, 0).setColor(color);
        consumer.addVertex(pose, x0, y1, 0).setColor(color);
        consumer.addVertex(pose, x1, y1, 0).setColor(color);
        consumer.addVertex(pose, x1, y0, 0).setColor(color);
        consumer.addVertex(pose, x0, y0, 0).setColor(color);
    }
}
