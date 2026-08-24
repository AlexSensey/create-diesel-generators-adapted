package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.foundation.ItemCompatibility;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BasinRenderer.class)
public abstract class BasinRendererMixin {

    @Invoker("renderItems")
    private static void createdieselgenerators$renderVanillaItems(BasinBlockEntity basin, float partialTicks,
                                                                  float fluidLevel, PoseStack ms,
                                                                  SubmitNodeCollector collector, int light) {
        throw new AssertionError();
    }

    @Redirect(
            method = "submit",
            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/processing/basin/BasinRenderer;renderItems(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V"),
            remap = false
    )
    private static void createdieselgenerators$renderMoldContents(BasinBlockEntity basin, float partialTicks,
                                                                  float fluidLevel, PoseStack ms,
                                                                  SubmitNodeCollector collector, int light) {
        IItemHandler inventory = ItemCompatibility.itemHandler(
                basin.getLevel().getCapability(Capabilities.Item.BLOCK, basin.getBlockPos(), null));
        boolean hasMold = false;
        if (inventory != null) {
            for (int slot = 0; slot < inventory.getSlots(); slot++)
                hasMold |= CDGItems.MOLD.isIn(inventory.getStackInSlot(slot));
        }
        if (!hasMold) {
            createdieselgenerators$renderVanillaItems(basin, partialTicks, fluidLevel, ms, collector, light);
            return;
        }

        RandomSource random = RandomSource.create(basin.getBlockPos().hashCode());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty())
                continue;

            ms.pushPose();
            if (CDGItems.MOLD.isIn(stack)) {
                TransformStack.of(ms)
                        .translate(.5, .7, .5)
                        .rotateXDegrees(90)
                        .scale(1.75f)
                        .translate(0, -.125, 0);
                submitItem(stack, ItemDisplayContext.GROUND, ms, collector, light);
            } else {
                TransformStack.of(ms)
                        .translate(.5, .74, .5)
                        .rotateXDegrees(90)
                        .scale(.5f);
                Vec3 offset = VecHelper.offsetRandomly(Vec3.ZERO, random, 1f / 16);
                ms.translate(offset.x, offset.y, offset.z);
                submitItem(stack, ItemDisplayContext.FIXED, ms, collector, light);
            }
            ms.popPose();
        }
    }

    private static void submitItem(ItemStack stack, ItemDisplayContext context, PoseStack ms,
                                   SubmitNodeCollector collector, int light) {
        ItemStackRenderState itemState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver()
                .updateForTopItem(itemState, stack, context, null, null, 0);
        itemState.submit(ms, collector, light, OverlayTexture.NO_OVERLAY, 0);
    }
}
