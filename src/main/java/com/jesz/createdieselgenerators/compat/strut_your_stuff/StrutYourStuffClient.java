package com.jesz.createdieselgenerators.compat.strut_your_stuff;

import com.cake.struts.compat.flywheel.StrutFlywheelVisual;
import com.cake.struts.content.block.StrutBlockEntity;
import com.cake.struts.content.block.StrutBlockEntityRenderer;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public final class StrutYourStuffClient {
    private StrutYourStuffClient() {
    }

    public static Supplier<RenderType> cutoutRenderType() {
        return RenderType::cutout;
    }

    public static SimpleBlockEntityVisualizer.Factory<StrutBlockEntity> visualFactory() {
        return StrutFlywheelVisual::new;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static NonNullFunction rendererFactory() {
        return context -> new StrutBlockEntityRenderer((BlockEntityRendererProvider.Context) context);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void generateItemModel(Object context, Object provider) {
        DataGenContext dataGenContext = (DataGenContext) context;
        RegistrateItemModelProvider modelProvider = (RegistrateItemModelProvider) provider;
        modelProvider.withExistingParent(dataGenContext.getName(),
                ResourceLocation.fromNamespaceAndPath(CreateDieselGenerators.ID,
                        "block/girder_strut/andesite_girder_item"));
    }
}
