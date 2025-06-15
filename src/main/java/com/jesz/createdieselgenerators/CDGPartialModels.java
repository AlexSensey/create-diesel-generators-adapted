package com.jesz.createdieselgenerators;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class CDGPartialModels {
    public static final PartialModel MODULAR_ENGINE_PISTONS_0 = model("block/modular_diesel_engine/pistons/pistons_0");
    public static final PartialModel MODULAR_ENGINE_PISTONS_1 = model("block/modular_diesel_engine/pistons/pistons_1");
    public static final PartialModel MODULAR_ENGINE_PISTONS_2 = model("block/modular_diesel_engine/pistons/pistons_2");
    public static final PartialModel MODULAR_ENGINE_PISTONS_3 = model("block/modular_diesel_engine/pistons/pistons_3");
    public static final PartialModel MODULAR_ENGINE_PISTONS_4 = model("block/modular_diesel_engine/pistons/pistons_4");
    public static final PartialModel ENGINE_PISTONS_0 = model("block/diesel_engine/pistons/pistons_0");
    public static final PartialModel ENGINE_PISTONS_1 = model("block/diesel_engine/pistons/pistons_1");
    public static final PartialModel ENGINE_PISTONS_2 = model("block/diesel_engine/pistons/pistons_2");
    public static final PartialModel ENGINE_PISTONS_3 = model("block/diesel_engine/pistons/pistons_3");
    public static final PartialModel ENGINE_PISTONS_4 = model("block/diesel_engine/pistons/pistons_4");
    public static final PartialModel ENGINE_PISTONS_VERTICAL_0 = model("block/diesel_engine/pistons/vertical_0");
    public static final PartialModel ENGINE_PISTONS_VERTICAL_1 = model("block/diesel_engine/pistons/vertical_1");
    public static final PartialModel ENGINE_PISTONS_VERTICAL_2 = model("block/diesel_engine/pistons/vertical_2");
    public static final PartialModel ENGINE_PISTONS_VERTICAL_3 = model("block/diesel_engine/pistons/vertical_3");
    public static final PartialModel ENGINE_PISTONS_VERTICAL_4 = model("block/diesel_engine/pistons/vertical_4");

    public static final PartialModel ENGINE_SILENCER = model("block/diesel_engine/silencer");
    public static final PartialModel ENGINE_SILENCER_VERTICAL = model("block/diesel_engine/vertical_silencer");
    public static final PartialModel ENGINE_TURBOCHARGER = model("block/diesel_engine/turbocharger");
    public static final PartialModel ENGINE_TURBOCHARGER_VERTICAL = model("block/diesel_engine/vertical_turbocharger");
    public static final PartialModel MODULAR_ENGINE_SILENCER = model("block/modular_diesel_engine/silencer");
    public static final PartialModel MODULAR_TURBOCHARGER = model("block/modular_diesel_engine/turbocharger");
    public static final PartialModel HUGE_ENGINE_SILENCER = model("block/huge_diesel_engine/silencer");

    public static final PartialModel ENGINE_PISTON = model("block/huge_diesel_engine/piston");
    public static final PartialModel ENGINE_PISTON_LINKAGE = model("block/huge_diesel_engine/linkage");
    public static final PartialModel ENGINE_PISTON_CONNECTOR = model("block/huge_diesel_engine/shaft_connector");

    public static final PartialModel PUMPJACK_ROPE = model("block/pumpjack_rope");
    public static final PartialModel PUMPJACK_CRANK_SMALL = model("block/pumpjack_crank/small_counterweight");
    public static final PartialModel PUMPJACK_CRANK_ROD_SMALL = model("block/pumpjack_crank/small_rod");
    public static final PartialModel PUMPJACK_CRANK_LARGE = model("block/pumpjack_crank/large_counterweight");
    public static final PartialModel PUMPJACK_CRANK_ROD_LARGE = model("block/pumpjack_crank/large_rod");
    public static final PartialModel SMALL_GAUGE_DIAL = model("block/basin_lid/gauge_dial");
    public static final PartialModel DISTILLATION_GAUGE = model("block/distillation_tank/gauge");
    public static final PartialModel BULK_FERMENTER_GAUGE = model("block/bulk_fermenter_gauge");
    public static final PartialModel JEI_DISTILLER_TOP = model("block/jei_distiller/top");
    public static final PartialModel JEI_DISTILLER_MIDDLE = model("block/jei_distiller/middle");
    public static final PartialModel JEI_DISTILLER_BOTTOM = model("block/jei_distiller/bottom");
    public static final PartialModel JEI_ENGINE_PISTON = model("block/huge_diesel_engine/jei_piston");
    public static final PartialModel JEI_BULK_FERMENTER = model("block/bulk_fermenter_jei");

    public static final PartialModel CHEMICAL_TURRET_CONNECTOR = model("block/chemical_turret/connector");
    public static final PartialModel CHEMICAL_TURRET_LIGHTER = model("block/chemical_turret/lighter");
    public static final PartialModel CHEMICAL_TURRET_BODY = model("block/chemical_turret/body");
    public static final PartialModel CHEMICAL_TURRET_SMALL_COG = model("block/chemical_turret/small_cog");
    public static final PartialModel CHEMICAL_TURRET_COG = model("block/chemical_turret/cog");
    public static final PartialModel TURRET_OPERATOR_HAT = model("entity/turret_operator_hat");
    public static PartialModel model(String id){
        return PartialModel.of(CreateDieselGenerators.rl(id));
    }
    public static void init(){}
}

