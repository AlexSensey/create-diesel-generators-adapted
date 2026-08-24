package com.jesz.createdieselgenerators.content.molds;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import net.minecraft.resources.Identifier;

import java.util.LinkedList;
import java.util.List;

public class MoldType {
    public static final List<MoldType> types = new LinkedList<>();

    public static MoldType BOWL_MOLD = new MoldType(CreateDieselGenerators.id("bowl"));
    public static MoldType LINES_MOLD = new MoldType(CreateDieselGenerators.id("lines"));
    public static MoldType CHAIN_MOLD = new MoldType(CreateDieselGenerators.id("chain"));
    public static MoldType BAR_MOLD = new MoldType(CreateDieselGenerators.id("bar"));

    Identifier id;

    public MoldType(Identifier id) {
        this.id = id;
        types.add(this);
    }

    public Identifier getId() {
        return id;
    }

    public Identifier getModelId() {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), "item/mold/"+id.getPath());
    }

    public static MoldType findById(Identifier id) {
        for (MoldType type : types){
            if(type.id.equals(id))
                return type;
        }
        return null;
    }

    public static void register() {

    }
}
