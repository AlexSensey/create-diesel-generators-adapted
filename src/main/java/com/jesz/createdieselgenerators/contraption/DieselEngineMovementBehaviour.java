package com.jesz.createdieselgenerators.contraption;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jesz.createdieselgenerators.CDGSounds;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class DieselEngineMovementBehaviour implements MovementBehaviour {
    @Override
    public boolean isActive(MovementContext context) {
        return context.contraption instanceof CarriageContraption && MovementBehaviour.super.isActive(context);
    }

    @Override
    public void tick(MovementContext context) {
        MovementBehaviour.super.tick(context);
        if(!context.world.isClientSide)
            return;
        CarriageContraption contraption = ((CarriageContraption)context.contraption);
        CarriageContraptionEntity entity = (CarriageContraptionEntity) contraption.entity;

        if(!context.data.contains("StoppedTicks"))
            context.data.putInt("StoppedTicks", 31);
        if(context.motion.length() < 0.05) {
            if (context.data.getInt("StoppedTicks") >= 30)
                return;
            context.data.putInt("StoppedTicks", context.data.getInt("StoppedTicks")+1);
        }else
            context.data.putInt("StoppedTicks", 0);


        if(!CDGConfig.ENGINES_EMIT_SOUND_ON_TRAINS.get() || entity.getCarriage().train.derailed)
            return;

        int ticks = context.data.getInt("Tick");
        if(ticks >= 2){
            double trainSpeed = context.motion.length()*2;
            double lastTrainSpeed = context.data.getDouble("TrainSpeed");
            double acc = (trainSpeed-0.0)-lastTrainSpeed;
            context.data.putDouble("TrainSpeed", trainSpeed);

            float throttle = Mth.lerp(0.1f, context.data.getFloat("Throttle"),
                    (float) Math.max(0, acc)*20);
            context.data.putFloat("Throttle", throttle);

            float pitch = 1f+throttle;
            float volume = 0.125f+throttle/5;
            context.world.playLocalSound(context.position.x, context.position.y, context.position.z, CDGSounds.DIESEL_ENGINE_SOUND.get(), SoundSource.BLOCKS, volume, pitch, false);

            ticks = 0;
        }
        context.data.putInt("Tick", ticks+1);

    }
}
