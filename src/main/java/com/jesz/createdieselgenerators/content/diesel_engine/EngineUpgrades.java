package com.jesz.createdieselgenerators.content.diesel_engine;

import com.jesz.createdieselgenerators.*;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.FACING;

public interface EngineUpgrades {
    List<EngineUpgrades> allUpgrades = new ArrayList<>();
    EngineUpgrades EMPTY = register(new EmptyUpgrade());
    EngineUpgrades SILENCER = register(new SilencerUpgrade());
    EngineUpgrades TURBOCHARGER = register(new TurbochargerUpgrade());

    static EngineUpgrades register(EngineUpgrades upgrade) {
        allUpgrades.add(upgrade);
        return upgrade;
    }

    static EngineUpgrades get(Identifier rl) {

        for (EngineUpgrades upgrade : EngineUpgrades.allUpgrades) {
            if (upgrade.getId().equals(rl)) {
                return upgrade;
            }
        }
        return EMPTY;
    }
    Identifier getId();
    default boolean canAddOn(IEngine engine) {
        return true;
    }

    default float getSpeed(float speed, IEngine engine) {
        return speed;
    }
    default float getCapacity(float capacity, IEngine engine) {
        return capacity;
    }

    default <T extends SmartBlockEntity & IEngine> float getPitchMultiplier(T engine) {
        return 1f;
    }

    default <T extends SmartBlockEntity & IEngine> float getVolume(T engine) {
        return 0.5f;
    }

    ItemStack getItem();

    class EmptyUpgrade implements EngineUpgrades {
        @Override
        public Identifier getId() {
            return CreateDieselGenerators.rl("none");
        }

        @Override
        public ItemStack getItem() {
            return ItemStack.EMPTY;
        }
    }

    class SilencerUpgrade implements EngineUpgrades {
        @Override
        public Identifier getId() {
            return CreateDieselGenerators.rl("silencer");
        }

        @Override
        public ItemStack getItem() {
            return CDGItems.ENGINE_SILENCER.get().getDefaultInstance();
        }

        @Override
        public <T extends SmartBlockEntity & IEngine> float getVolume(T engine) {
            return 0.02f;
        }
    }

    class TurbochargerUpgrade implements EngineUpgrades {
        @Override
        public Identifier getId() {
            return CreateDieselGenerators.rl("turbocharger");
        }

        @Override
        public float getSpeed(float speed, IEngine engine) {
            return (float) (speed * CDGConfig.TURBOCHARGED_ENGINE_MULTIPLIER.get());
        }

        @Override
        public float getCapacity(float capacity, IEngine engine) {
            return (float) (capacity * CDGConfig.TURBOCHARGED_ENGINE_MULTIPLIER.get());
        }

        @Override
        public ItemStack getItem() {
            return CDGItems.ENGINE_TURBO.get().getDefaultInstance();
        }

        @Override
        public boolean canAddOn(IEngine engine) {
            return engine instanceof DieselEngineBlockEntity || engine instanceof ModularDieselEngineBlockEntity;
        }

        @Override
        public <T extends SmartBlockEntity & IEngine> float getPitchMultiplier(T engine) {
            return 1.5f;
        }
    }
}
