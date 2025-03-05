package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.cement.CementFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import java.util.HashMap;
import java.util.Map;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class CDGFluids {

    public static final FluidEntry<ForgeFlowingFluid.Flowing> PLANT_OIL =
            REGISTRATE.fluid("plant_oil", CreateDieselGenerators.rl("block/plant_oil_still"), CreateDieselGenerators.rl("block/plant_oil_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> CRUDE_OIL =
            REGISTRATE.fluid("crude_oil", CreateDieselGenerators.rl("block/crude_oil_still"), CreateDieselGenerators.rl("block/crude_oil_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(100))
                    .fluidProperties(p -> p.levelDecreasePerBlock(3)
                            .tickRate(25)
                            .slopeFindDistance(2)
                            .explosionResistance(100f))
                    .register();

    public static final FluidEntry<ForgeFlowingFluid.Flowing> BIODIESEL =
            REGISTRATE.fluid("biodiesel", CreateDieselGenerators.rl("block/biodiesel_still"), CreateDieselGenerators.rl("block/biodiesel_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> DIESEL =
            REGISTRATE.fluid("diesel", CreateDieselGenerators.rl("block/diesel_still"), CreateDieselGenerators.rl("block/diesel_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> GASOLINE =
            REGISTRATE.fluid("gasoline", CreateDieselGenerators.rl("block/gasoline_still"), CreateDieselGenerators.rl("block/gasoline_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> ETHANOL =
            REGISTRATE.fluid("ethanol", CreateDieselGenerators.rl("block/ethanol_still"), CreateDieselGenerators.rl("block/ethanol_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(5)
                            .explosionResistance(100f))
                    .register();
    public static final Map<DyeColor, FluidEntry<ForgeFlowingFluid.Flowing>> CEMENT = new HashMap<>();
    static {
        for (DyeColor color : DyeColor.values()) {
            CEMENT.put(color,
                    REGISTRATE.fluid(color.getName() + "_cement", CreateDieselGenerators.rl("block/cement/" + color.getName() + "_still"), CreateDieselGenerators.rl("block/cement/" + color.getName() + "_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(3)
                            .tickRate(12)
                            .slopeFindDistance(2)
                            .explosionResistance(100f)).source(p -> new CementFluid(p, color))
                    .register()
            );
        }
    }

    public static void register() {}


}
