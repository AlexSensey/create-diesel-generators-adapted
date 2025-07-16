package com.jesz.createdieselgenerators.content.molds;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.LinkedList;
import java.util.List;

public class MoldType {
    public static final List<MoldType> types = new LinkedList<>();

    public static MoldType BOWL_MOLD = new MoldType(CreateDieselGenerators.rl("bowl"));
    public static MoldType LINES_MOLD = new MoldType(CreateDieselGenerators.rl("lines"));
    public static MoldType CHAIN_MOLD = new MoldType(CreateDieselGenerators.rl("chain"));
    public static MoldType BAR_MOLD = new MoldType(CreateDieselGenerators.rl("bar"));

    ResourceLocation id;

    @OnlyIn(Dist.CLIENT)
    public BakedModel model;

    public MoldType(ResourceLocation id) {
        this.id = id;
        types.add(this);
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceLocation getModelId() {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "item/mold/"+id.getPath());
    }

    public static MoldType findById(ResourceLocation id) {
        for (MoldType type : types){
            if(type.id.equals(id))
                return type;
        }
        return null;
    }

    public static void register() {

    }
}
