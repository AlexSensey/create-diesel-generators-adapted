package com.jesz.createdieselgenerators.content.diesel_engine;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class EngineSoundInstance extends AbstractTickableSoundInstance {
    private boolean active;
    private int keepAlive;
    private float volumeTarget = 0.5f;
    private float pitchTarget = 0.5f;
    private final float pitchChangeSpeed;

    public EngineSoundInstance(SoundEvent soundEvent, SoundSource soundSource, Vec3 pos, float pitchChangeSpeed) {
        super(soundEvent, soundSource, SoundInstance.createUnseededRandom());
        this.pitchChangeSpeed = pitchChangeSpeed;
        looping = true;
        active = true;
        volume = 0.05f;
        pitch = 0.0f;
        delay = 0;
        keepAlive();
        x = pos.x;
        y = pos.y;
        z = pos.z;
    }

    public void fadeOut() {
        this.active = false;
    }

    public void keepAlive() {
        keepAlive = 2;
    }

    public void setPitch(float pitch) {
        this.pitchTarget = pitch;
    }

    public void setVolume(float vol) {
        this.volumeTarget = vol;
    }

    @Override
    public void tick() {
        if (active) {
            volume = Mth.lerp(0.3f, volume, volumeTarget);
            pitch = Mth.lerp(pitchChangeSpeed, pitch, pitchTarget);
            keepAlive--;
            if (keepAlive == 0)
                fadeOut();
            return;

        }
        volume = Math.max(0, volume - .04f);
        pitch = Math.max(0, pitch - .08f);
        if (volume == 0)
            stop();
    }

    public void setPosition(Vec3 pos) {
        x = pos.x;
        y = pos.y;
        z = pos.z;
    }

    public boolean active() {
        return active;
    }
}
