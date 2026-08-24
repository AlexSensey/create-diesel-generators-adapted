package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.entity_filter.EntityAttribute;
import com.jesz.createdieselgenerators.content.tools.lighter.LighterState;
import com.jesz.createdieselgenerators.content.track_layers_bag.TrackLayersBagItemDataComponent;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItemComponent;
import net.createmod.catnip.api.data.codec.CatnipCodecs;
import net.createmod.catnip.api.data.codec.stream.CatnipStreamCodecBuilders;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.UnaryOperator;

public class CDGDataComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateDieselGenerators.ID);

    public static final DataComponentType<LighterState> LIGHTER_STATE = register("lighter_state",
            builder -> builder.persistent(LighterState.CODEC).networkSynchronized(LighterState.STREAM_CODEC));

    public static final DataComponentType<SimpleFluidContent> FLUID_CONTENTS = register("fluid_contents",
            builder -> builder.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC));

    public static final DataComponentType<TrackLayersBagItemDataComponent> TRACKS = register("tracks",
            builder -> builder.persistent(TrackLayersBagItemDataComponent.CODEC).networkSynchronized(TrackLayersBagItemDataComponent.STREAM_CODEC));

    public static final DataComponentType<Identifier> MOLD_TYPE = register("mold_type",
            builder -> builder.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC));

    public static final DataComponentType<SandPaperItemComponent> PROCESSING_ITEM = register("processing_item",
            builder -> builder.persistent(SandPaperItemComponent.CODEC).networkSynchronized(SandPaperItemComponent.STREAM_CODEC));

    public static final DataComponentType<List<EntityAttribute.EntityAttributeEntry>> ENTITY_FILTER_MATCHED_ATTRIBUTES = register(
            "entity_filter_matched_attributes",
            builder -> builder.persistent(EntityAttribute.EntityAttributeEntry.CODEC.listOf()).networkSynchronized(CatnipStreamCodecBuilders.list(EntityAttribute.EntityAttributeEntry.STREAM_CODEC)));

    public static final DataComponentType<Integer> OIL_SCANNER_PROGRESS = register("oil_scanner_progress",
            builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DataComponentType<Integer> OIL_SCANNER_STATE = register("oil_scanner_state",
            builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));


    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        DATA_COMPONENTS.register(name, () -> type);
        return type;
    }

    @ApiStatus.Internal
    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}
