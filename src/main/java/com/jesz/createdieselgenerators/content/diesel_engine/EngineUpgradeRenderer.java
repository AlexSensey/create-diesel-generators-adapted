package com.jesz.createdieselgenerators.content.diesel_engine;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.FACING;

public final class EngineUpgradeRenderer {
    private EngineUpgradeRenderer() {}

    public static void render(EngineUpgrades upgrade, BlockEntity be, PoseStack ms, MultiBufferSource buffer, int light) {
        if (upgrade == EngineUpgrades.SILENCER)
            renderPartial(be, ms, buffer, CDGPartialModels.ENGINE_SILENCER, CDGPartialModels.ENGINE_SILENCER_VERTICAL,
                    CDGPartialModels.MODULAR_ENGINE_SILENCER, CDGPartialModels.HUGE_ENGINE_SILENCER, light);
        else if (upgrade == EngineUpgrades.TURBOCHARGER)
            renderPartial(be, ms, buffer, CDGPartialModels.ENGINE_TURBOCHARGER, CDGPartialModels.ENGINE_TURBOCHARGER_VERTICAL,
                    CDGPartialModels.MODULAR_TURBOCHARGER, CDGPartialModels.ENGINE_TURBOCHARGER, light);
    }

    private static void renderPartial(BlockEntity be, PoseStack ms, MultiBufferSource buffer,
                                      PartialModel normal, PartialModel vertical, PartialModel modular,
                                      PartialModel huge, int light) {
        PartialModel model;
        Direction facing;
        if (be instanceof DieselEngineBlockEntity) {
            facing = be.getBlockState().getValue(FACING);
            model = facing.getAxis().isVertical() ? vertical : normal;
        } else if (be instanceof ModularDieselEngineBlockEntity) {
            facing = be.getBlockState().getValue(ModularDieselEngineBlock.FACING);
            model = modular;
        } else if (be instanceof HugeDieselEngineBlockEntity) {
            facing = be.getBlockState().getValue(HugeDieselEngineBlock.FACING);
            model = huge;
        } else return;

        var rendered = com.jesz.createdieselgenerators.foundation.PartialBufferCompatibility
                .partial(model, be.getBlockState()).center();
        if (be instanceof HugeDieselEngineBlockEntity && facing.getAxis().isVertical())
            rendered.rotateZDegrees(90).rotateYDegrees(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 270 : 90);
        else if (be instanceof HugeDieselEngineBlockEntity)
            rendered.rotateYDegrees(facing.getAxis() == Direction.Axis.X ? facing.toYRot() : facing.toYRot() + 180);
        else if (facing.getAxis().isVertical())
            rendered.rotateYDegrees(facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 270 : 180);
        else rendered.rotateYDegrees(facing.toYRot());
        rendered.uncenter().light(light).renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }
}
