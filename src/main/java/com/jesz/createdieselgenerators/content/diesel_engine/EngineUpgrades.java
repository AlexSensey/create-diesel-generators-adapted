package com.jesz.createdieselgenerators.content.diesel_engine;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.jesz.createdieselgenerators.fuel_type.FuelTypeManager;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.FACING;

public interface EngineUpgrades {
    List<EngineUpgrades> allUpgrades = new ArrayList<>();
    EngineUpgrades NONE = add(new NoUpgrade());
    EngineUpgrades SILENCER = add(new SilencerUpgrade());
    EngineUpgrades TURBOCHARGER = add(new TurbochargerUpgrade());
    static EngineUpgrades add(EngineUpgrades upgrade){
        allUpgrades.add(upgrade);
        return upgrade;
    }
    ResourceLocation getId();
    default boolean canAddOn(IEngine engine){
        return true;
    }

    default float getSpeed(float speed, IEngine engine){
        return speed;
    }
    default float getStress(float stress, IEngine engine){
        return stress;
    }

    default void playSounds(int tick, IEngine engine){
        if ((tick % Math.max(1, FuelTypeManager.getSoundSpeed(engine.fs().getFluid()))) == 0)
            engine.playSound();
    }
    ItemStack getItem();

    default void render(BlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light) {}
    static void renderPartial(BlockEntity be, PoseStack ms, MultiBufferSource buffer,
                              PartialModel normalModel, PartialModel normalVerticalModel,
                              PartialModel modularModel, PartialModel hugeModel, int light) {
        if (be instanceof DieselEngineBlockEntity) {
            Direction facing = be.getBlockState().getValue(FACING);
            if (facing.getAxis() == Direction.Axis.Y) {
                CachedBuffers.partial(normalVerticalModel, be.getBlockState())
                        .center()
                        .rotateYDegrees(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 270 : 180)
                        .uncenter()
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
            } else {
                CachedBuffers.partial(normalModel, be.getBlockState())
                        .center()
                        .rotateYDegrees(facing.toYRot())
                        .uncenter()
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
            }
        } else if (be instanceof ModularDieselEngineBlockEntity) {
            Direction facing = be.getBlockState().getValue(ModularDieselEngineBlock.FACING);

            CachedBuffers.partial(modularModel, be.getBlockState())
                    .center()
                    .rotateYDegrees(facing.toYRot())
                    .uncenter()
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.cutout()));

        } else if (be instanceof HugeDieselEngineBlockEntity) {
            Direction facing = be.getBlockState().getValue(HugeDieselEngineBlock.FACING);

            if (facing.getAxis() == Direction.Axis.Y) {
                CachedBuffers.partial(hugeModel, be.getBlockState())
                        .center().rotateZ(90)
                        .rotateYDegrees(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 270 : 180)
                        .uncenter()
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
            } else {
                CachedBuffers.partial(hugeModel, be.getBlockState())
                        .center()
                        .rotateYDegrees(facing.toYRot())
                        .uncenter()
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderType.cutout()));
            }

        }
    }
    class NoUpgrade implements EngineUpgrades{
        @Override
        public ResourceLocation getId() {
            return CreateDieselGenerators.rl("none");
        }

        @Override
        public ItemStack getItem() {
            return ItemStack.EMPTY;
        }
    }
    class SilencerUpgrade implements EngineUpgrades{
        @Override
        public ResourceLocation getId() {
            return CreateDieselGenerators.rl("silencer");
        }

        @Override
        public void render(BlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light) {
            renderPartial(be, ms, buffer, CDGPartialModels.ENGINE_SILENCER, CDGPartialModels.ENGINE_SILENCER_VERTICAL,
                    CDGPartialModels.MODULAR_ENGINE_SILENCER, CDGPartialModels.HUGE_ENGINE_SILENCER, light);
        }

        @Override
        public void playSounds(int tick, IEngine engine) {}

        @Override
        public ItemStack getItem() {
            return CDGItems.ENGINE_SILENCER.get().getDefaultInstance();
        }

    }
    class TurbochargerUpgrade implements EngineUpgrades{
        @Override
        public ResourceLocation getId() {
            return CreateDieselGenerators.rl("turbocharger");
        }

        @Override
        public float getSpeed(float speed, IEngine engine) {
            return (float) (speed * CDGConfig.TURBOCHARGED_ENGINE_MULTIPLIER.get());
        }

        @Override
        public float getStress(float stress, IEngine engine) {
            return (float) (stress * CDGConfig.TURBOCHARGED_ENGINE_MULTIPLIER.get());
        }

        @Override
        public void render(BlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light) {
           renderPartial(be, ms, buffer, CDGPartialModels.ENGINE_TURBOCHARGER, CDGPartialModels.ENGINE_TURBOCHARGER_VERTICAL,
                   CDGPartialModels.ENGINE_TURBOCHARGER, CDGPartialModels.ENGINE_TURBOCHARGER, light);
        }

        @Override
        public void playSounds(int tick, IEngine engine) {
            if ((tick % Math.max(1, FuelTypeManager.getSoundSpeed(engine.fs().getFluid()) / 2)) == 0)
                engine.playSound();
        }

        @Override
        public ItemStack getItem() {
            return CDGItems.ENGINE_TURBO.get().getDefaultInstance();
        }

        @Override
        public boolean canAddOn(IEngine engine) {
            return engine instanceof DieselEngineBlockEntity;
        }
    }
}
