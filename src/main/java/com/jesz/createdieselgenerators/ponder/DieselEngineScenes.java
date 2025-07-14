package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.CDGFluids;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.function.Supplier;

import static com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock.PIPE;

public class DieselEngineScenes {
    static FluidStack currentFuel;
    public static void small(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("diesel_engine", "Setting up a Diesel Engine");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        Selection tank = util.select().fromTo(4, 1, 1, 4, 2, 1);
        BlockPos pumpPos = util.grid().at(3, 1, 2);
        BlockPos enginePos = util.grid().at(1, 2, 2);
        Selection engine = util.select().position(enginePos);
        Selection pump = util.select().position(pumpPos);
        Selection pipe = util.select().fromTo(1, 1, 2, 4, 1, 2);
        Selection pumpShaft = util.select().fromTo(3, 1, 3, 5, 1, 3);
        Selection shaft = util.select().fromTo(1, 2, 0, 1, 2, 4);
        Selection cog = util.select().position(5, 0, 4);
        Selection lever = util.select().fromTo(0, 1, 2, 0, 2, 2);

        scene.idle(15);
        ElementLink<WorldSectionElement> engineElement =
                scene.world().showIndependentSection(engine, Direction.DOWN);
        scene.world().moveSection(engineElement, util.vector().of(0, -1, 0), 0);
        scene.idle(15);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Diesel Engines are a compact way of generating kinetic energy.")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.world().hideIndependentSection(engineElement, Direction.UP);
        scene.idle(15);
        scene.world().moveSection(engineElement, util.vector().of(0, 1, 0), 0);
        scene.world().showSection(engine, Direction.DOWN);

        scene.world().showSection(pipe, Direction.WEST);
        scene.world().showSection(tank, Direction.NORTH);
        scene.idle(30);


        Supplier<FluidStack> content = DieselEngineScenes::randomFuel;

        scene.world().modifyBlockEntity(util.grid().at(4, 1, 1), FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(content.get(), IFluidHandler.FluidAction.EXECUTE));

        scene.world().showSection(pumpShaft, Direction.NORTH);
        scene.world().showSection(cog, Direction.NORTH);
        scene.idle(30);

        scene.overlay().showText(50)
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .text("Once you give the engine some fuel, it will start generating rotational force.")
                .pointAt(util.vector().blockSurface(enginePos, Direction.NORTH))
                .placeNearTarget();

        scene.idle(30);

        scene.idle(30);
        scene.world().setKineticSpeed(cog, 16f);
        scene.world().setKineticSpeed(pump, 32f);
        scene.world().setKineticSpeed(pumpShaft, -32f);
        scene.effects().rotationSpeedIndicator(enginePos);
        scene.world().setKineticSpeed(shaft, 96f);

        scene.effects().rotationSpeedIndicator(enginePos);

        scene.idle(20);
        scene.world().showSection(shaft.substract(engine), Direction.DOWN);
        scene.idle(40);

        scene.world().showSection(lever, Direction.DOWN);
        scene.idle(40);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Engines can be stopped with a redstone signal")
                .pointAt(util.vector().blockSurface(util.grid().at(0, 2, 2), Direction.DOWN))
                .placeNearTarget();

        scene.idle(60);

        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(0, 2, 2), Direction.DOWN), Pointing.DOWN, 20)
                .rightClick();

        scene.idle(40);

        scene.world().modifyBlock(util.grid().at(0, 2, 2), b -> b.setValue(LeverBlock.POWERED, true), false);

        scene.world().setKineticSpeed(shaft, 0f);
        scene.effects().rotationSpeedIndicator(enginePos);

        scene.idle(40);

        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(0, 2, 2), Direction.DOWN), Pointing.DOWN, 20)
                .rightClick();

        scene.idle(40);

        scene.world().modifyBlock(util.grid().at(0, 2, 2), b -> b.setValue(LeverBlock.POWERED, false), false);

        scene.world().setKineticSpeed(shaft, 96f);
        scene.effects().rotationSpeedIndicator(enginePos);

        scene.idle(40);

        scene.overlay().showText(50)
                .colored(PonderPalette.BLUE)
                .attachKeyFrame()
                .text("You can also add upgrades on diesel engines")
                .pointAt(util.vector().blockSurface(enginePos, Direction.UP))
                .placeNearTarget();

        scene.idle(60);

        scene.overlay().showControls(util.vector().blockSurface(enginePos, Direction.UP), Pointing.DOWN, 20)
                .withItem(CDGItems.ENGINE_SILENCER.asStack())
                .rightClick();

        scene.idle(40);

        scene.world().modifyBlockEntity(enginePos, DieselEngineBlockEntity.class, be -> be.setUpgrade(EngineUpgrades.SILENCER));

        scene.idle(40);
    }

    public static void huge(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("huge_diesel_engine", "Setting up a Diesel Engine");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        Selection tank = util.select().fromTo(4, 1, 3, 4, 2, 3);
        Selection pipes = util.select().fromTo(0, 2, 0, 3, 2, 4);
        Selection engines = util.select().fromTo(0, 1, 0, 2, 1, 4);
        Selection shafts = util.select().fromTo(0, 1, 2, 2, 1, 2);
        Selection shafts2 = util.select().fromTo(3, 1, 2, 4, 1, 2);

        scene.world().showSection(engines, Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(20)
                .attachKeyFrame()
                .text("Huge Diesel Engines connect to Shafts ...")
                .pointAt(util.vector().blockSurface(util.grid().at(0, 1, 0), Direction.NORTH))
                .placeNearTarget();
        scene.idle(30);
        scene.world().showSection(shafts2, Direction.DOWN);
        scene.idle(15);
        scene.world().showSection(tank, Direction.DOWN);
        scene.idle(15);
        scene.world().showSection(pipes, Direction.DOWN);
        scene.idle(15);

        Supplier<FluidStack> content = DieselEngineScenes::randomFuel;

        scene.world().modifyBlockEntity(util.grid().at(4, 1, 3), FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(content.get(), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(15);
        scene.overlay().showText(40)
                .attachKeyFrame()
                .text("... they will start generating Kinetic Energy, once you give them some fuel.")
                .pointAt(util.vector().blockSurface(util.grid().at(0, 1, 0), Direction.NORTH))
                .placeNearTarget();
        scene.idle(50);
        scene.world().setKineticSpeed(shafts2, 16f);
        scene.world().setKineticSpeed(shafts, 16f);
        scene.world().setKineticSpeed(util.select().position(3, 2, 3), -32f);
        scene.idle(30);
        scene.world().setKineticSpeed(shafts2, 128f);
        scene.world().setKineticSpeed(shafts, 128f);
        scene.world().setKineticSpeed(util.select().position(3, 2, 3), -64f);
        scene.idle(10);
    }

    public static void modular(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("large_diesel_engine", "Setting up a Modular Diesel Engine");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        BlockPos pumpPos = util.grid().at(3, 1, 3);
        Selection mainEngine = util.select().position(1, 1, 1);
        Selection engines = util.select().fromTo(1, 1, 2, 1, 1, 3);

        Selection pump = util.select().position(pumpPos);
        Selection pipe = util.select().fromTo(1, 2, 1, 2 ,2, 1);
        Selection pipe2 = util.select().fromTo(2, 1, 1, 2 ,1, 3);
        Selection pipe3 = util.select().fromTo(3, 1, 3, 4, 2, 3);
        Selection cogs = util.select().fromTo(5, 1, 2, 3, 1, 2);
        Selection largeCog = util.select().position(5, 0, 1);

        scene.idle(15);
        scene.world().showSection(mainEngine, Direction.DOWN);

        scene.idle(15);
        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Modular Diesel Generators function like normal Diesel generators.")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(60);

        scene.world().showSection(pipe, Direction.DOWN);
        scene.world().showSection(pipe2, Direction.DOWN);
        scene.world().showSection(pipe3, Direction.DOWN);

        Supplier<FluidStack> content = DieselEngineScenes::randomFuel;

        scene.world().modifyBlockEntity(util.grid().at(4, 1, 3), FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(content.get(), IFluidHandler.FluidAction.EXECUTE));

        scene.idle(15);

        scene.world().showSection(cogs, Direction.DOWN);
        scene.world().showSection(largeCog, Direction.DOWN);

        scene.idle(15);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Once you give them some fuel, they will produce Kinetic Energy ...")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();

        scene.idle(60);

        scene.world().modifyKineticSpeed(largeCog, s -> 16f);
        scene.world().modifyKineticSpeed(cogs, s -> -32f);
        scene.world().modifyKineticSpeed(pump, s -> 32f);
        scene.idle(10);


        scene.world().modifyKineticSpeed(mainEngine, s -> 96f);

        scene.effects().rotationSpeedIndicator(util.grid().at(1, 1, 1));

        scene.idle(15);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("... They can be stacked.")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();

        scene.idle(60);

        scene.world().showSection(engines, Direction.EAST);

        scene.world().modifyBlocks(engines, s -> s.setValue(PIPE, true), false);

        scene.world().modifyKineticSpeed(engines, s -> 96f);
        scene.idle(20);
        scene.overlay().showControls(util.vector().topOf(1, 1, 2), Pointing.DOWN, 15).withItem(AllItems.WRENCH.asStack());
        scene.world().modifyBlock(util.grid().at(1,1,2), s -> s.setValue(PIPE, false), false);
        scene.idle(30);
        scene.overlay().showControls(util.vector().topOf(1, 1, 3), Pointing.DOWN, 15).withItem(AllItems.WRENCH.asStack());
        scene.world().modifyBlock(util.grid().at(1,1,3), s -> s.setValue(PIPE, false), false);
        scene.idle(30);
        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("They will generate stress proportionally to how much engines you stack.")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(60);
    }

    public static FluidStack randomFuel() {
        if (Minecraft.getInstance().level != null) {
            Registry<FuelType> registry = Minecraft.getInstance().level.registryAccess().registryOrThrow(CDGRegistries.FUEL_TYPE);
            Holder<FuelType> randomType = registry.getRandom(RandomSource.create()).orElse(null);
            if (randomType == null || randomType.get().fluid().size() == 0)
                currentFuel = new FluidStack(CDGFluids.DIESEL.get(), 16000);
            else {
                Holder<Fluid> randomFluid = randomType.get().fluid().getRandomElement(RandomSource.create()).orElse(null);
                if (randomFluid == null)
                    currentFuel = new FluidStack(CDGFluids.DIESEL.get(), 16000);
                else
                    currentFuel = new FluidStack(randomFluid.get(), 16000);
            }
        } else
            currentFuel = new FluidStack(CDGFluids.DIESEL.get(), 16000);
        return currentFuel;
    }
}
