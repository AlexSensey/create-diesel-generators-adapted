package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.andesite_girder.AndesiteGirderBlock;
import com.jesz.createdieselgenerators.content.andesite_girder.AndesiteGirderEncasedShaftBlock;
import com.jesz.createdieselgenerators.content.basin_lid.BasinLidBlock;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlock;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterCTBehavior;
import com.jesz.createdieselgenerators.content.burner.BurnerBlock;
import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import com.jesz.createdieselgenerators.content.canister.CanisterBlock;
import com.jesz.createdieselgenerators.content.canister.CanisterBlockItem;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineStateDisplaySource;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.PoweredEngineShaftBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineCTBehavior;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlock;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankModel;
import com.jesz.createdieselgenerators.content.items.MultiBlockContainerBlockItem;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlock;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelCTBehavior;
import com.jesz.createdieselgenerators.content.pumpjack.*;
import com.jesz.createdieselgenerators.content.sheetmetal.SheetMetalPanelBlock;
import com.jesz.createdieselgenerators.content.sheetmetal.SheetMetalPanelModel;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlock;
import com.jesz.createdieselgenerators.contraption.DieselEngineMovementBehaviour;
import com.jesz.createdieselgenerators.contraption.PumpjackBearingBMovementBehaviour;
import com.jesz.createdieselgenerators.contraption.PumpjackHeadMovementBehaviour;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.content.fluids.tank.BoilerHeaters;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.List;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;
import static com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType.mountedFluidStorage;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class CDGBlocks {
    
    public static final BlockEntry<BurnerBlock> BURNER = REGISTRATE.block("burner", BurnerBlock::new)
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .onRegister((b) -> BoilerHeater.REGISTRY.register(b, ((level, pos, state) -> {
                if(level.getBlockEntity(pos) instanceof BurnerBlockEntity be)
                    return be.heat;
                return -1;
            })))
            .simpleItem()
            .register();

    public static final BlockEntry<ChemicalTurretBlock> CHEMICAL_TURRET = REGISTRATE.block("chemical_turret", ChemicalTurretBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .simpleItem()
            .register();
    public static final BlockEntry<DieselEngineBlock> DIESEL_ENGINE = REGISTRATE.block("diesel_engine", DieselEngineBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .onRegister(assignDataBehaviour(new EngineStateDisplaySource()))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .onRegister(movementBehaviour(new DieselEngineMovementBehaviour()))
            .simpleItem()
            .register();


    public static final BlockEntry<ModularDieselEngineBlock> MODULAR_DIESEL_ENGINE = REGISTRATE.block("large_diesel_engine", ModularDieselEngineBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .onRegister(assignDataBehaviour(new EngineStateDisplaySource()))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .onRegister(connectedTextures(ModularDieselEngineCTBehavior::new))
            .onRegister(movementBehaviour(new DieselEngineMovementBehaviour()))
            .simpleItem()
            .register();
    public static final BlockEntry<HugeDieselEngineBlock> HUGE_DIESEL_ENGINE = REGISTRATE.block("huge_diesel_engine", HugeDieselEngineBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .onRegister(assignDataBehaviour(new EngineStateDisplaySource()))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .simpleItem()
            .register();
    public static final BlockEntry<PoweredEngineShaftBlock> POWERED_ENGINE_SHAFT = REGISTRATE.block("powered_engine_shaft", PoweredEngineShaftBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.METAL))
            .transform(pickaxeOnly())
            .register();
    public static final BlockEntry<BasinLidBlock> BASIN_LID = REGISTRATE.block("basin_lid", BasinLidBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .simpleItem()
            .register();

    public static final BlockEntry<PumpjackBearingBlock> PUMPJACK_BEARING = REGISTRATE.block("pumpjack_bearing", PumpjackBearingBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .simpleItem()
            .register();
    public static final BlockEntry<PumpjackHeadBlock> PUMPJACK_HEAD = REGISTRATE.block("pumpjack_head", PumpjackHeadBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .onRegister(movementBehaviour(new PumpjackHeadMovementBehaviour()))
            .simpleItem()
            .register();
    public static final BlockEntry<PumpjackBearingBBlock> PUMPJACK_BEARING_B = REGISTRATE.block("pumpjack_bearing_b", PumpjackBearingBBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .onRegister(movementBehaviour(new PumpjackBearingBMovementBehaviour()))
            .register();
    public static final BlockEntry<PumpjackHoleBlock> PUMPJACK_HOLE = REGISTRATE.block("pumpjack_hole", PumpjackHoleBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_ORANGE))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .onRegister(assignDataBehaviour(new OilAmountDisplaySource()))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .simpleItem()
            .register();
    public static final BlockEntry<PumpjackCrankBlock> PUMPJACK_CRANK = REGISTRATE.block("pumpjack_crank", PumpjackCrankBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .simpleItem()
            .register();

    public static final BlockEntry<CanisterBlock> CANISTER = REGISTRATE.block("canister", CanisterBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .item(CanisterBlockItem::new)
            .transform(customItemModel())
            .register();

    public static final BlockEntry<DistillationTankBlock> DISTILLATION_TANK = REGISTRATE.block("distillation_tank", DistillationTankBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .onRegister(CreateRegistrate.blockModel(() -> DistillationTankModel::new))
            .register();

    public static final BlockEntry<BulkFermenterBlock> BULK_FERMENTER = REGISTRATE.block("bulk_fermenter", BulkFermenterBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .onRegister(CreateRegistrate.connectedTextures(BulkFermenterCTBehavior::new))
            .item(MultiBlockContainerBlockItem::new)
            .build()
            .register();

    public static final BlockEntry<OilBarrelBlock> OIL_BARREL = REGISTRATE.block("oil_barrel", OilBarrelBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .onRegister(CreateRegistrate.connectedTextures(OilBarrelCTBehavior::new))
            .transform(mountedFluidStorage(CDGMountedStorageTypes.OIL_BARREL))
            .item(MultiBlockContainerBlockItem::new)
            .build()
            .register();

    public static final BlockEntry<RotatedPillarBlock> CHIP_WOOD_BLOCK = REGISTRATE.block("chip_wood_block", RotatedPillarBlock::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .properties(p -> p)
            .simpleItem()
            .register();

    public static final BlockEntry<RotatedPillarBlock> CHIP_WOOD_BEAM = REGISTRATE.block("chip_wood_beam", RotatedPillarBlock::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .properties(p -> p)
            .simpleItem()
            .register();

    public static final BlockEntry<SlabBlock> CHIP_WOOD_SLAB = REGISTRATE.block("chip_wood_slab", SlabBlock::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .properties(p -> p)
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> CHIP_WOOD_STAIRS = REGISTRATE.block("chip_wood_stairs", p -> new StairBlock(Blocks.ANDESITE_STAIRS::defaultBlockState, p))
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .properties(p -> p)
            .simpleItem()
            .register();

    public static final BlockEntry<Block> ASPHALT_BLOCK = REGISTRATE.block("asphalt_block", Block::new)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .properties(p -> p.sound(SoundType.STONE))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .properties(p -> p.speedFactor(1.25f))
            .simpleItem()
            .register();
    public static final BlockEntry<SlabBlock> ASPHALT_SLAB = REGISTRATE.block("asphalt_slab", SlabBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .properties(p -> p.sound(SoundType.STONE))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .properties(p -> p.speedFactor(1.25f))
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> ASPHALT_STAIRS = REGISTRATE.block("asphalt_stairs", p -> new StairBlock(Blocks.ANDESITE_STAIRS::defaultBlockState, p))
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .properties(p -> p.sound(SoundType.STONE))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .properties(p -> p.speedFactor(1.25f))
            .simpleItem()
            .register();

    public static final BlockEntry<AndesiteGirderEncasedShaftBlock> ANDESITE_GIRDER_ENCASED_SHAFT =
            REGISTRATE.block("andesite_girder_encased_shaft", AndesiteGirderEncasedShaftBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
                    .register();

    public static final BlockEntry<AndesiteGirderBlock> ANDESITE_GIRDER =
            REGISTRATE.block("andesite_girder", AndesiteGirderBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
                    .simpleItem()
                    .register();

    public static final BlockEntry<SheetMetalPanelBlock> SHEET_METAL_PANEL =
            REGISTRATE.block("sheet_metal_panel", SheetMetalPanelBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY).sound(SoundType.NETHERITE_BLOCK))
                    .onRegister(CreateRegistrate.blockModel(() -> SheetMetalPanelModel::new))
                    .simpleItem()
                    .register();
    public static void register() {
    }
    private static NonNullConsumer<? super Block> assignDataBehaviour(DisplaySource displaySource) {
        return b -> DisplaySource.BY_BLOCK.register(b, List.of(displaySource));
    }

    private static NonNullConsumer<? super Block> movementBehaviour(MovementBehaviour movementBehaviour) {
        return b -> MovementBehaviour.REGISTRY.register(b, movementBehaviour);
    }

}
