package com.jesz.createdieselgenerators.foundation;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.createmod.catnip.api.client.render.SuperBufferFactory;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.SpriteShiftEntry;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import java.util.List;

/** Bridges Flywheel's 26.2 standalone model parts to Catnip's legacy buffer renderer. */
public final class PartialBufferCompatibility {
    private PartialBufferCompatibility() {
    }

    public static LegacyBuffer partial(PartialModel partial, BlockState referenceState) {
        BlockStateModel model = new BlockStateModel() {
            @Override
            public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
                output.add(partial.get());
            }

            @Override
            public Material.Baked particleMaterial() {
                return partial.get().particleMaterial();
            }

            @Override
            public int materialFlags() {
                return partial.get().materialFlags();
            }
        };
        return new LegacyBuffer(SuperBufferFactory.getInstance().createForBlock(model, referenceState));
    }

    /** Restores the fluent transform surface that Create add-ons used before 26.2. */
    public static final class LegacyBuffer implements SuperByteBuffer {
        private final SuperByteBuffer delegate;

        private LegacyBuffer(SuperByteBuffer delegate) {
            this.delegate = delegate;
        }

        public LegacyBuffer center() { getTransforms().translate(.5f, .5f, .5f); return this; }
        public LegacyBuffer uncenter() { getTransforms().translate(-.5f, -.5f, -.5f); return this; }
        public LegacyBuffer translate(double x, double y, double z) { getTransforms().translate((float) x, (float) y, (float) z); return this; }
        public LegacyBuffer scale(float x, float y, float z) { getTransforms().scale(x, y, z); return this; }
        public LegacyBuffer transform(PoseStack stack) { getTransforms().last().pose().mul(stack.last().pose()); return this; }
        public LegacyBuffer rotateX(float radians) { getTransforms().mulPose(Axis.XP.rotation(radians)); return this; }
        public LegacyBuffer rotateXDegrees(float degrees) { getTransforms().mulPose(Axis.XP.rotationDegrees(degrees)); return this; }
        public LegacyBuffer rotateYDegrees(float degrees) { getTransforms().mulPose(Axis.YP.rotationDegrees(degrees)); return this; }
        public LegacyBuffer rotateZDegrees(float degrees) { getTransforms().mulPose(Axis.ZP.rotationDegrees(degrees)); return this; }
        public LegacyBuffer rotateCentered(float radians, Direction direction) {
            center();
            float signed = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? radians : -radians;
            switch (direction.getAxis()) {
                case X -> getTransforms().mulPose(Axis.XP.rotation(signed));
                case Y -> getTransforms().mulPose(Axis.YP.rotation(signed));
                case Z -> getTransforms().mulPose(Axis.ZP.rotation(signed));
            }
            return uncenter();
        }

        @Override public void renderInto(PoseStack ms, VertexConsumer consumer) { delegate.renderInto(ms, consumer); }
        @Override public boolean isEmpty() { return delegate.isEmpty(); }
        @Override public PoseStack getTransforms() { return delegate.getTransforms(); }
        @Override public LegacyBuffer reset() { delegate.reset(); return this; }
        @Override public LegacyBuffer color(int color) { delegate.color(color); return this; }
        @Override public LegacyBuffer color(int r, int g, int b, int a) { delegate.color(r, g, b, a); return this; }
        @Override public LegacyBuffer disableDiffuse() { delegate.disableDiffuse(); return this; }
        @Override public LegacyBuffer shiftUV(SpriteShiftEntry entry) { delegate.shiftUV(entry); return this; }
        @Override public LegacyBuffer shiftUVScrolling(SpriteShiftEntry entry, float u, float v) { delegate.shiftUVScrolling(entry, u, v); return this; }
        @Override public LegacyBuffer shiftUVtoSheet(SpriteShiftEntry entry, float u, float v, int size) { delegate.shiftUVtoSheet(entry, u, v, size); return this; }
        @Override public LegacyBuffer overlay(int overlay) { delegate.overlay(overlay); return this; }
        @Override public LegacyBuffer light(int light) { delegate.light(light); return this; }
        @Override public LegacyBuffer useLevelLight(BlockAndTintGetter level) { delegate.useLevelLight(level); return this; }
        @Override public LegacyBuffer useLevelLight(BlockAndTintGetter level, Matrix4f transform) { delegate.useLevelLight(level, transform); return this; }
    }
}
