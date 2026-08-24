package com.jesz.createdieselgenerators.events;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGFluids;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlockEntity;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterUnpackingHandler;
import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import com.jesz.createdieselgenerators.content.canister.CanisterBlockEntity;
import com.jesz.createdieselgenerators.content.canister.SpoutCanisterFilling;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlockEntity;
import com.jesz.createdieselgenerators.content.molds.BasinSpoutCasting;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackHoleBlockEntity;
import com.jesz.createdieselgenerators.content.tools.FueledToolItem;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlockEntity;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = CreateDieselGenerators.ID)
public final class CommonModEvents {
    private CommonModEvents() {}

    @SubscribeEvent
    public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(CDGRegistries.FUEL_TYPE, FuelType.CODEC, FuelType.NCODEC);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.Fluid.ITEM,
                (stack, context) -> new ItemAccessFluidHandler(
                        context,
                        CDGDataComponents.FLUID_CONTENTS,
                        ((FueledToolItem) stack.getItem()).getCapacity(stack)),
                CDGItems.LIGHTER,
                CDGItems.CHEMICAL_SPRAYER,
                CDGItems.CHEMICAL_SPRAYER_LIGHTER,
                CDGBlocks.CANISTER);

        for (FluidEntry<BaseFlowingFluid.Flowing> entry : CDGFluids.CONCRETE) {
            event.registerItem(
                    Capabilities.Fluid.ITEM,
                    (stack, context) -> com.jesz.createdieselgenerators.foundation.FluidCompatibility.resourceHandler(
                            new FluidBucketWrapper(stack)),
                    entry.getBucket().orElseThrow());
        }
        BulkFermenterBlockEntity.registerCapabilities(event);
        BurnerBlockEntity.registerCapabilities(event);
        CanisterBlockEntity.registerCapabilities(event);
        DieselEngineBlockEntity.registerCapabilities(event);
        ModularDieselEngineBlockEntity.registerCapabilities(event);
        HugeDieselEngineBlockEntity.registerCapabilities(event);
        DistillationTankBlockEntity.registerCapabilities(event);
        OilBarrelBlockEntity.registerCapabilities(event);
        PumpjackHoleBlockEntity.registerCapabilities(event);
        ChemicalTurretBlockEntity.registerCapabilities(event);
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(
                    CDGBlockEntityTypes.CANISTER.get(), new SpoutCanisterFilling());
            BlockSpoutingBehaviour.BY_BLOCK_ENTITY.register(
                    AllBlockEntityTypes.BASIN.get(), new BasinSpoutCasting());
            BulkFermenterUnpackingHandler.register();
        });
    }
}
