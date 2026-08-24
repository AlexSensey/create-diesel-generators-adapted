package com.jesz.createdieselgenerators.content.pumpjack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

final class PumpjackClientSounds {
    private static final Map<PumpjackCrankBlockEntity, CrankState> CRANKS = new WeakHashMap<>();
    private static final Map<PumpjackHoleBlockEntity, LoopSound> HOLES = new WeakHashMap<>();

    private PumpjackClientSounds() {}

    static void tickCrank(PumpjackCrankBlockEntity be) {
        if (be.getLevel() == null)
            return;
        CrankState state = CRANKS.computeIfAbsent(be, ignored -> new CrankState());
        long gameTime = be.getLevel().getGameTime();
        if (state.lastTick == gameTime)
            return;
        state.lastTick = gameTime;

        Minecraft minecraft = Minecraft.getInstance();
        boolean active = be.getSpeed() != 0 && be.getBearing() != null;
        if (!active) {
            state.stop(minecraft);
            return;
        }

        Vec3 pos = Vec3.atCenterOf(be.getBlockPos());
        if (state.rumble == null || state.rumble.isStopped()
                || !minecraft.getSoundManager().isActive(state.rumble)) {
            state.rumble = new LoopSound(SoundEvents.MINECART_RIDING, .3f, .4f, pos);
            minecraft.getSoundManager().play(state.rumble);
        }
        if (state.hiss == null || state.hiss.isStopped()) {
            state.hiss = new LoopSound(SoundEvents.BLASTFURNACE_FIRE_CRACKLE, .1f, 1.2f, pos);
            minecraft.getSoundManager().play(state.hiss);
        }

        float previous = be.prevAngle % 360;
        float current = be.angle % 360;
        boolean crossedBottom = previous < 10 && current >= 10 || previous > 350 && current <= 10;
        boolean crossedTop = previous < 190 && current >= 190 || previous > 170 && current <= 170;
        if (minecraft.level == null)
            return;
        if (crossedBottom) {
            minecraft.level.playLocalSound(be.getBlockPos(), SoundEvents.PISTON_EXTEND,
                    SoundSource.BLOCKS, .2f, .3f, false);
            minecraft.level.playLocalSound(be.getBlockPos(), SoundEvents.ANVIL_HIT,
                    SoundSource.BLOCKS, .1f, .5f, false);
            if (Math.random() < .3)
                minecraft.level.playLocalSound(be.getBlockPos(), SoundEvents.IRON_DOOR_OPEN,
                        SoundSource.BLOCKS, .15f, .5f, false);
        }
        if (crossedTop) {
            minecraft.level.playLocalSound(be.getBlockPos(), SoundEvents.PISTON_CONTRACT,
                    SoundSource.BLOCKS, .15f, .3f, false);
            minecraft.level.playLocalSound(be.getBlockPos(), SoundEvents.CHAIN_STEP,
                    SoundSource.BLOCKS, .1f, .6f, false);
        }
    }

    static void tickHole(PumpjackHoleBlockEntity be) {
        if (be.getLevel() == null)
            return;
        LoopSound sound = HOLES.get(be);
        boolean active = be.started && be.valid && be.oilAmount > 0;
        if (active && (sound == null || sound.isStopped())) {
            sound = new LoopSound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
                    .2f, .4f, Vec3.atCenterOf(be.getBlockPos()));
            HOLES.put(be, sound);
            Minecraft.getInstance().getSoundManager().play(sound);
        } else if (!active && sound != null) {
            Minecraft.getInstance().getSoundManager().stop(sound);
            HOLES.remove(be);
        }
    }

    private static final class CrankState {
        private long lastTick = Long.MIN_VALUE;
        private LoopSound rumble;
        private LoopSound hiss;

        private void stop(Minecraft minecraft) {
            if (rumble != null)
                minecraft.getSoundManager().stop(rumble);
            if (hiss != null)
                minecraft.getSoundManager().stop(hiss);
            rumble = null;
            hiss = null;
        }
    }

    private static final class LoopSound extends AbstractTickableSoundInstance {
        private LoopSound(SoundEvent event, float volume, float pitch, Vec3 pos) {
            super(event, SoundSource.BLOCKS, RandomSource.create());
            x = pos.x;
            y = pos.y;
            z = pos.z;
            this.volume = volume;
            this.pitch = pitch;
            looping = true;
            delay = 0;
            attenuation = Attenuation.LINEAR;
        }

        @Override
        public void tick() {}
    }
}
