package com.jesz.createdieselgenerators;

import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/** Minecraft 26.2 no longer creates render models from Registrate fluid sprites. */
@EventBusSubscriber(value = Dist.CLIENT, modid = CreateDieselGenerators.ID)
public final class CDGFluidModels {
    private CDGFluidModels() {}

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        register(event, CDGFluids.PLANT_OIL, "block/fluid/plant_oil_still", "block/fluid/plant_oil_flow");
        register(event, CDGFluids.CRUDE_OIL, "block/crude_oil_still", "block/crude_oil_flow");
        register(event, CDGFluids.BIODIESEL, "block/biodiesel_still", "block/biodiesel_flow");
        register(event, CDGFluids.DIESEL, "block/diesel_still", "block/diesel_flow");
        register(event, CDGFluids.GASOLINE, "block/gasoline_still", "block/gasoline_flow");
        register(event, CDGFluids.ETHANOL, "block/fluid/ethanol_still", "block/fluid/ethanol_flow");

        for (DyeColor color : DyeColor.values()) {
            String path = "block/cement/" + color.getName();
            register(event, CDGFluids.CONCRETE[color.ordinal()], path + "_still", path + "_flow");
        }
    }

    private static void register(RegisterFluidModelsEvent event,
                                 FluidEntry<? extends BaseFlowingFluid> entry,
                                 String still,
                                 String flowing) {
        BaseFlowingFluid source = entry.getSource();
        FluidModel.Unbaked model = new FluidModel.Unbaked(
                new Material(CreateDieselGenerators.id(still)),
                new Material(CreateDieselGenerators.id(flowing)),
                null,
                state -> 0xffffffff
        );
        event.register(model, source, source.getFlowing());
    }
}
