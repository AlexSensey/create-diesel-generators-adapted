package com.jesz.createdieselgenerators.fuel_type;

import com.jesz.createdieselgenerators.content.diesel_engine.EngineTypes;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;

public record FuelType(HolderSet<Fluid> fluid, PerEngineProperties normal, PerEngineProperties modular, PerEngineProperties huge, float soundPitch, float burnerStrength) {

    public static final Codec<FuelType> CODEC = RecordCodecBuilder.create(i -> i.group(
            RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluid").forGetter(FuelType::fluid),
            PerEngineProperties.CODEC.fieldOf("normal").forGetter(FuelType::normal),
            PerEngineProperties.CODEC.fieldOf("modular").forGetter(FuelType::modular),
            PerEngineProperties.CODEC.fieldOf("huge").forGetter(FuelType::huge),
            Codec.FLOAT.optionalFieldOf("sound_pitch", 1f).forGetter(FuelType::soundPitch),
            Codec.FLOAT.optionalFieldOf("burner_multiplier", 1f).forGetter(FuelType::burnerStrength)
    ).apply(i, FuelType::new));

    // Since the client doesn't have the tags when it joins a server and receives fuel types, this different codec is needed to not cause an error when joining a server.
    // this codec sends all the fluids, instead of sometimes sending just the tag.
    public static final Codec<FuelType> NCODEC = RecordCodecBuilder.create(i -> i.group(
            RegistryCodecs.homogeneousList(Registries.FLUID).fieldOf("fluid").forGetter(type -> HolderSet.direct(type.fluid.stream().toList())),
            PerEngineProperties.CODEC.fieldOf("normal").forGetter(FuelType::normal),
            PerEngineProperties.CODEC.fieldOf("modular").forGetter(FuelType::modular),
            PerEngineProperties.CODEC.fieldOf("huge").forGetter(FuelType::huge),
            Codec.FLOAT.optionalFieldOf("sound_pitch", 1f).forGetter(FuelType::soundPitch),
            Codec.FLOAT.optionalFieldOf("burner_multiplier", 1f).forGetter(FuelType::burnerStrength)
    ).apply(i, FuelType::new));

    public static final FuelType EMPTY = new FuelType(null, new PerEngineProperties(0, 0, 0),
            new PerEngineProperties(0, 0, 0),
            new PerEngineProperties(0, 0, 0), 0, 0);

    public PerEngineProperties getGenerated(BlockEntity be) {
        if(be instanceof HugeDieselEngineBlockEntity)
            return huge;
        if(be instanceof ModularDieselEngineBlockEntity)
            return modular;
        return normal;
    }

    public static FuelType getTypeFor(HolderLookup.RegistryLookup<FuelType> registry, Fluid fluid) {
        if (registry == null)
            return EMPTY;
        var type = registry.listElements()
                .filter(r -> r.value().fluid().contains(fluid.builtInRegistryHolder()))
                .findFirst();
        return type.isEmpty() ? EMPTY : type.get().value();
    }

    public PerEngineProperties getGenerated(EngineTypes currentEngine) {
        if(currentEngine == EngineTypes.HUGE)
            return huge;
        if(currentEngine == EngineTypes.MODULAR)
            return modular;
        return normal;
    }

    public record PerEngineProperties(float speed, float strength, float burn) {
        public static final Codec<PerEngineProperties> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.fieldOf("speed").forGetter(PerEngineProperties::speed),
            Codec.FLOAT.fieldOf("strength").forGetter(PerEngineProperties::strength),
            Codec.FLOAT.fieldOf("burn_rate").forGetter(PerEngineProperties::burn)
        ).apply(i, PerEngineProperties::new));
    }
}
