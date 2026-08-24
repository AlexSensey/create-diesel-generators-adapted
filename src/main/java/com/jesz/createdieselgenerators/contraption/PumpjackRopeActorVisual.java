package com.jesz.createdieselgenerators.contraption;

import static com.jesz.createdieselgenerators.CDGPartialModels.PUMPJACK_ROPE;

import com.mojang.logging.LogUtils;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/** Flywheel render path for the polished rod between the horsehead and well head. */
public class PumpjackRopeActorVisual extends ActorVisual {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean constructorLogged;
    private static boolean holeFoundLogged;
    private static boolean holeMissingLogged;
    private final TransformedInstance rope;

    public PumpjackRopeActorVisual(VisualizationContext visualizationContext,
                                   VirtualRenderWorld simulationWorld,
                                   MovementContext movementContext) {
        super(visualizationContext, simulationWorld, movementContext);
        rope = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(PUMPJACK_ROPE))
                .createInstance();
        rope.light(localBlockLight(), 0);
        rope.setIdentityTransform()
                .scale(0, 0, 0)
                .setChanged();
        if (!constructorLogged) {
            constructorLogged = true;
            LOGGER.info("[CDG Pumpjack Rope] Flywheel ActorVisual created");
        }
    }

    @Override
    public void beginFrame() {
        BlockPos hole = PumpjackHeadMovementBehaviour.findHoleForRendering(context);
        if (hole == null || context.position == null
                || !(context.contraption instanceof BearingContraption bearingContraption)
                || !(context.contraption.entity instanceof ControlledContraptionEntity entity)) {
            if (!holeMissingLogged) {
                holeMissingLogged = true;
                LOGGER.info("[CDG Pumpjack Rope] ActorVisual active but render target is unavailable: hole={}, position={}, contraption={}, entity={}",
                        hole, context.position, context.contraption.getClass().getSimpleName(),
                        context.contraption.entity == null ? "null" : context.contraption.entity.getClass().getSimpleName());
            }
            rope.setIdentityTransform().scale(0, 0, 0).setChanged();
            return;
        }

        if (!holeFoundLogged) {
            holeFoundLogged = true;
            LOGGER.info("[CDG Pumpjack Rope] Render target found: head={}, hole={}", context.position, hole);
        }

        float partialTicks = AnimationTickHolder.getPartialTicks();
        Vec3 previousPosition = context.position.subtract(context.motion);
        Direction.Axis bearingAxis = bearingContraption.getFacing().getOpposite().getAxis();

        rope.setIdentityTransform()
                .translate(context.localPos)
                .translate(.5f, .5f, .5f);

        if (bearingAxis == Direction.Axis.X) {
            double zDistance = Mth.lerp(partialTicks, previousPosition.z, context.position.z) - hole.getZ() - .5;
            double yDistance = Mth.lerp(partialTicks, previousPosition.y, context.position.y) - hole.getY() - .8;
            float length = (float) Math.sqrt(zDistance * zDistance + yDistance * yDistance);
            float angle = (float) (-entity.getAngle(partialTicks)
                    - Math.toDegrees(Math.atan2(yDistance, zDistance)) + 90);
            rope.rotateXDegrees(angle).scale(1, length, 1);
        } else {
            double xDistance = Mth.lerp(partialTicks, previousPosition.x, context.position.x) - hole.getX() - .5;
            double yDistance = Mth.lerp(partialTicks, previousPosition.y, context.position.y) - hole.getY() - .8;
            float length = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);
            float angle = (float) (-entity.getAngle(partialTicks)
                    + Math.toDegrees(Math.atan2(yDistance, xDistance)) - 90);
            rope.rotateZDegrees(angle).scale(1, length, 1);
        }

        rope.setChanged();
    }

    @Override
    protected void _delete() {
        rope.delete();
    }
}
