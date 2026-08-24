package com.jesz.createdieselgenerators.content.distillation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

final class DistillationClientSounds {
    private static final Map<DistillationTankBlockEntity, LoopSound> SOUNDS = new WeakHashMap<>();
    private DistillationClientSounds() {}

    static void tick(DistillationTankBlockEntity be) {
        LoopSound sound = SOUNDS.get(be);
        boolean processing = be.isController() && be.isBottom() && be.processingTime > -1;
        if (processing && (sound == null || sound.isStopped())) {
            Vec3 pos = Vec3.atCenterOf(be.getBlockPos().offset(be.width / 2, be.height / 2, be.width / 2));
            sound = new LoopSound(pos);
            SOUNDS.put(be, sound);
            Minecraft.getInstance().getSoundManager().play(sound);
        } else if (!processing && sound != null) {
            Minecraft.getInstance().getSoundManager().stop(sound);
            SOUNDS.remove(be);
        }
    }

    private static final class LoopSound extends AbstractTickableSoundInstance {
        private LoopSound(Vec3 pos) {
            super(SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, RandomSource.create());
            x = pos.x; y = pos.y; z = pos.z;
            volume = .5f; pitch = .45f; looping = true; delay = 0; attenuation = Attenuation.LINEAR;
        }
        @Override public void tick() {}
    }
}
