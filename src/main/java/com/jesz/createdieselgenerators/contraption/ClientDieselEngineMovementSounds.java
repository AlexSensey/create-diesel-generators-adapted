package com.jesz.createdieselgenerators.contraption;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGSoundEvents;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineSoundInstance;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientDieselEngineMovementSounds {
    private static final Map<Pair<UUID, BlockPos>, EngineSoundInstance> SOUNDS = new HashMap<>();
    private ClientDieselEngineMovementSounds() {}

    public static void tick(MovementContext context) {
        CarriageContraption contraption = (CarriageContraption) context.contraption;
        CarriageContraptionEntity entity = (CarriageContraptionEntity) contraption.entity;
        if (!CDGConfig.ENGINES_EMIT_SOUND_ON_TRAINS.get() || entity.getCarriage().train.derailed)
            return;
        double speed = context.motion.length() * 2 / (entity.getCarriage().train.maxSpeed() / 28);
        double previous = context.data.getDouble("TrainSpeed").orElse(0.0);
        context.data.putDouble("TrainSpeed", speed);
        float throttle = Mth.lerp(.05f, context.data.getFloatOr("Throttle", 0),
                (float) Math.max(0, Math.min(1, speed - previous)));
        context.data.putFloat("Throttle", throttle);
        Pair<UUID, BlockPos> key = Pair.of(entity.getUUID(), context.localPos);
        EngineSoundInstance sound = SOUNDS.get(key);
        if (context.disabled) {
            if (sound != null) sound.fadeOut();
            return;
        }
        if (sound == null) {
            sound = new EngineSoundInstance(CDGSoundEvents.ENGINE_NORMAL.get(), SoundSource.NEUTRAL, context.position, .6f);
            sound.setVolume(1);
            Minecraft.getInstance().getSoundManager().play(sound);
            SOUNDS.put(key, sound);
        } else if (sound.isStopped()) {
            SOUNDS.remove(key);
            return;
        }
        sound.setPosition(context.position);
        if (sound.active()) {
            sound.keepAlive();
            sound.setPitch((float) Math.min(2, Math.max(Math.min(.14, throttle) * 5 + speed / 28, .1f)));
        }
    }
}
