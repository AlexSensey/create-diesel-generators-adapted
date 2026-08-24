package com.jesz.createdieselgenerators.content.diesel_engine;

import com.jesz.createdieselgenerators.CDGSoundEvents;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

public final class ClientEngineSounds {
    private static final Map<SmartBlockEntity, EngineSoundInstance> SOUNDS = new WeakHashMap<>();
    private ClientEngineSounds() {}

    public static <T extends SmartBlockEntity & IEngine> void tick(T engine, Vec3 position, float pitchScale,
                                                                   boolean overStressed) {
        EngineSoundInstance sound = SOUNDS.get(engine);
        if (engine.enabled() && engine.getThrottle() > 0 && !overStressed) {
            if (sound == null || sound.isStopped() || sound.getX() != position.x || sound.getZ() != position.z) {
                sound = new EngineSoundInstance(CDGSoundEvents.ENGINE_NORMAL.get(), SoundSource.NEUTRAL, position, .2f);
                SOUNDS.put(engine, sound);
                Minecraft.getInstance().getSoundManager().play(sound);
            } else if (sound.active()) {
                sound.keepAlive();
                sound.setPitch(engine.getUpgrade().getPitchMultiplier(engine) * engine.getFuelSoundPitch()
                        * engine.getThrottle() * pitchScale);
                sound.setVolume(engine.getUpgrade().getVolume(engine) * engine.getThrottle());
            }
        } else if (sound != null) {
            sound.fadeOut();
            SOUNDS.remove(engine);
        }
    }
}
