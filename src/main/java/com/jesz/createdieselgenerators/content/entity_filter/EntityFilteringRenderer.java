package com.jesz.createdieselgenerators.content.entity_filter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class EntityFilteringRenderer {
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        HitResult target = mc.hitResult;
        if (!(target instanceof BlockHitResult result))
            return;

        ClientLevel level = mc.level;
        if(level == null)
            return;
        BlockPos pos = result.getBlockPos();
        BlockState state = level.getBlockState(pos);

        FilteringBehaviour behaviour = BlockEntityBehaviour.get(level, pos, EntityFilteringBehaviour.TYPE);
        if (mc.player.isShiftKeyDown())
            return;

        ItemStack mainhandItem = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (behaviour == null)
            return;
        if (behaviour instanceof SidedFilteringBehaviour) {
            behaviour = ((SidedFilteringBehaviour) behaviour).get(result.getDirection());
            if (behaviour == null)
                return;
        }
        if (!behaviour.isActive())
            return;
        if (behaviour.getSlotPositioning() instanceof ValueBoxTransform.Sided)
            ((ValueBoxTransform.Sided) behaviour.getSlotPositioning()).fromSide(result.getDirection());
        if (!behaviour.getSlotPositioning().shouldRender(level, pos, state))
            return;

        ItemStack filter = behaviour.getFilter();
        boolean isFilterSlotted = filter.getItem() instanceof EntityFilterItem;
        Component label = behaviour.getLabel();
        boolean hit = behaviour.getSlotPositioning().testHit(level, pos, state, target.getLocation()
                .subtract(Vec3.atLowerCornerOf(pos)));

        AABB emptyBB = new AABB(Vec3.ZERO, Vec3.ZERO);
        AABB bb = isFilterSlotted ? emptyBB.inflate(.45f, .31f, .2f) : emptyBB.inflate(.25f);

        ValueBox box = new ValueBox.ItemValueBox(label, bb, pos, filter, behaviour.getCountLabelForValueBox());

        box.passive(!hit || AllBlocks.CLIPBOARD.isIn(mainhandItem));

        Outliner.getInstance().showOutline(Pair.of("filter" + behaviour.netId(), pos), box.transform(behaviour.getSlotPositioning()))
                .lineWidth(1 / 64f)
                .withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null)
                .highlightFace(result.getDirection());

        if (!hit)
            return;

        List<MutableComponent> tip = new ArrayList<>();
        tip.add(label.copy());
        tip.add(CreateLang
                .translateDirect(filter.isEmpty() ? "logistics.filter.click_to_set" : "logistics.filter.click_to_replace"));

        CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);
    }

    public static void renderOnBlockEntity(SmartBlockEntity be, float partialTicks, PoseStack ms,
                                           MultiBufferSource buffer, int light, int overlay) {

        if (be == null || be.isRemoved())
            return;

        Level level = be.getLevel();
        BlockPos blockPos = be.getBlockPos();

        for (BlockEntityBehaviour b : be.getAllBehaviours()) {
            if (!(b instanceof EntityFilteringBehaviour behaviour))
                continue;

            if (!be.isVirtual()) {
                Entity cameraEntity = Minecraft.getInstance().cameraEntity;
                if (cameraEntity != null && level == cameraEntity.level()) {
                    float max = behaviour.getRenderDistance();
                    if (cameraEntity.position()
                            .distanceToSqr(VecHelper.getCenterOf(blockPos)) > (max * max)) {
                        continue;
                    }
                }
            }

            if (!behaviour.isActive())
                continue;
            if (behaviour.getFilter()
                    .isEmpty())
                continue;

            ValueBoxTransform slotPositioning = behaviour.getSlotPositioning();
            BlockState blockState = be.getBlockState();

            if (slotPositioning instanceof ValueBoxTransform.Sided sided) {
                Direction side = sided.getSide();
                for (Direction d : Iterate.directions) {
                    ItemStack filter = behaviour.getFilter(d);
                    if (filter.isEmpty())
                        continue;

                    sided.fromSide(d);
                    if (!slotPositioning.shouldRender(level, blockPos, blockState))
                        continue;

                    ms.pushPose();
                    slotPositioning.transform(level, blockPos, blockState, ms);
                    if (AllBlocks.CONTRAPTION_CONTROLS.has(blockState))
                        ValueBoxRenderer.renderFlatItemIntoValueBox(filter, ms, buffer, light, overlay);
                    else
                        ValueBoxRenderer.renderItemIntoValueBox(filter, ms, buffer, light, overlay);
                    ms.popPose();
                }
                sided.fromSide(side);
                continue;
            } else if (slotPositioning.shouldRender(level, blockPos, blockState)) {
                ms.pushPose();
                slotPositioning.transform(level, blockPos, blockState, ms);
                ValueBoxRenderer.renderItemIntoValueBox(behaviour.getFilter(), ms, buffer, light, overlay);
                ms.popPose();
            }
        }
    }
}
