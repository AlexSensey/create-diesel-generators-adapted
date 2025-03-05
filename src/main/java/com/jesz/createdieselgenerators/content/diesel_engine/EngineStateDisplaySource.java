package com.jesz.createdieselgenerators.content.diesel_engine;

import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class EngineStateDisplaySource extends DisplaySource {
    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        if(context.getSourceBlockEntity() instanceof DieselEngineBlockEntity sourceBE) {
            if(sourceBE.enabled())
                return List.of(
                        Component.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
                        Component.translatable("createdieselgenerators.display_source.speed").append(Math.abs(sourceBE.getGeneratedSpeed()) + Component.translatable("create.generic.unit.rpm").toString()),
                        Component.translatable("createdieselgenerators.display_source.stress").append(Math.abs(sourceBE.calculateAddedStressCapacity() * sourceBE.getGeneratedSpeed()) + Component.translatable("create.generic.unit.stress").toString())
                );

            return List.of(
                    Component.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
                    Component.translatable("createdieselgenerators.display_source.idle")
            );



        } else if(context.getSourceBlockEntity() instanceof ModularDieselEngineBlockEntity sourceBE) {
            ModularDieselEngineBlockEntity frontEngine = sourceBE.controller;
            if(frontEngine != null && frontEngine.length != 0)
                if(frontEngine.validFS())
                    return List.of(
                            Component.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
                            Component.translatable("createdieselgenerators.display_source.speed").append(Math.abs(frontEngine.getGeneratedSpeed()) + Component.translatable("create.generic.unit.rpm").toString()),
                            Component.translatable("createdieselgenerators.display_source.stress").append(Math.abs(frontEngine.calculateAddedStressCapacity() * frontEngine.getGeneratedSpeed()) + Component.translatable("create.generic.unit.stress").toString())
                );

            return List.of(
                    Component.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
                    Component.translatable("createdieselgenerators.display_source.idle")
            );

        }
            return List.of();
    }
}
