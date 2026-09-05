package com.jesz.createdieselgenerators.content.diesel_engine.huge;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.mojang.logging.LogUtils;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock.FACING;
import static com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock.AXIS;

/**
 * Owns both the powered shaft and the moving assemblies of all Huge Diesel
 * Engines connected to it. The shaft is the stable visualization anchor in
 * the 26.2 Flywheel pipeline; engine positions are kept relative to the same
 * render origin.
 */
public class PoweredEngineShaftVisual extends SingleAxisRotatingVisual<PoweredEngineShaftBlockEntity>
        implements SimpleDynamicVisual {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<BlockPos, EngineAssembly> engines = new HashMap<>();
    private final RotatingInstance transformedReference;
    private Set<BlockPos> lastFoundEngines;

    public PoweredEngineShaftVisual(VisualizationContext context,
                                    PoweredEngineShaftBlockEntity blockEntity,
                                    float partialTick) {
        super(context, blockEntity, partialTick, Models.partial(AllPartialModels.POWERED_SHAFT));
        Model referenceModel = Models.partial(AllPartialModels.POWERED_SHAFT);
        transformedReference = instancerProvider().instancer(AllInstanceTypes.ROTATING, referenceModel)
                .createInstance();
        transformedReference.setPosition(getVisualPosition().above(3))
                .setRotationAxis(Direction.Axis.Y)
                .setRotationalSpeed(0)
                .setRotationOffset(0)
                .light(0x00F000F0)
                .setChanged();
        LOGGER.info("[CDG-HUGE-FLYWHEEL] shaft visual created: shaft={}, renderOrigin={}, axis={}, speed={}",
                pos, renderOrigin(), blockEntity.getBlockState().getValue(AXIS), blockEntity.getSpeed());
        LOGGER.info("[CDG-HUGE-FLYWHEEL] transformed reference created: shaft={}, referencePos={}, " +
                        "meshes={}, vertices={}, visible={}, light={}",
                pos, getVisualPosition().above(3), referenceModel.meshes().size(), vertexCount(referenceModel),
                transformedReference.handle().isVisible(), transformedReference.light);
        syncEngines();
        animateEngines();
    }

    @Override
    public void beginFrame(DynamicVisual.Context context) {
        syncEngines();
        animateEngines();
    }

    private void syncEngines() {
        Set<BlockPos> found = new HashSet<>();
        Direction.Axis shaftAxis = blockEntity.getBlockState().getValue(AXIS);

        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == shaftAxis)
                continue;

            BlockPos enginePos = pos.relative(direction, 2);
            if (!(level.getBlockEntity(enginePos) instanceof HugeDieselEngineBlockEntity engine))
                continue;
            if (engine.getBlockState().getValue(FACING) != direction.getOpposite())
                continue;

            found.add(enginePos.immutable());
            engines.computeIfAbsent(enginePos.immutable(), ignored -> new EngineAssembly(engine));
        }

        engines.entrySet().removeIf(entry -> {
            if (found.contains(entry.getKey()))
                return false;
            entry.getValue().delete();
            return true;
        });

        if (lastFoundEngines == null || !lastFoundEngines.equals(found)) {
            LOGGER.info("[CDG-HUGE-FLYWHEEL] shaft scan: shaft={}, renderOrigin={}, axis={}, foundEngines={}",
                    pos, renderOrigin(), shaftAxis, found);
            lastFoundEngines = Set.copyOf(found);
        }
    }

    private void animateEngines() {
        engines.values().forEach(EngineAssembly::animate);
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        transformedReference.light(0x00F000F0).setChanged();
        engines.values().forEach(assembly ->
                relight(assembly.engine.getBlockPos(), assembly.piston, assembly.linkage, assembly.connector));
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        super.collectCrumblingInstances(consumer);
    }

    @Override
    protected void _delete() {
        LOGGER.info("[CDG-HUGE-FLYWHEEL] shaft visual deleted: shaft={}, assemblies={}", pos, engines.keySet());
        super._delete();
        transformedReference.delete();
        engines.values().forEach(EngineAssembly::delete);
        engines.clear();
    }

    private final class EngineAssembly {
        private final HugeDieselEngineBlockEntity engine;
        private final RotatingInstance piston;
        private final RotatingInstance linkage;
        private final RotatingInstance connector;
        private final Matrix4f pose = new Matrix4f();
        private final Vector3f transformedCenter = new Vector3f();
        private int diagnosticFrames;

        private EngineAssembly(HugeDieselEngineBlockEntity engine) {
            this.engine = engine;
            Model pistonModel = Models.partial(CDGPartialModels.ENGINE_PISTON);
            Model linkageModel = Models.partial(CDGPartialModels.ENGINE_PISTON_LINKAGE);
            Model connectorModel = Models.partial(CDGPartialModels.ENGINE_PISTON_CONNECTOR);
            piston = instancerProvider().instancer(AllInstanceTypes.ROTATING,
                            pistonModel)
                    .createInstance();
            linkage = instancerProvider().instancer(AllInstanceTypes.ROTATING,
                            linkageModel)
                    .createInstance();
            connector = instancerProvider().instancer(AllInstanceTypes.ROTATING,
                            connectorModel)
                    .createInstance();
            configureStaticRotation(piston);
            configureStaticRotation(linkage);
            configureStaticRotation(connector);
            LOGGER.info("[CDG-HUGE-FLYWHEEL] assembly created: shaft={}, engine={}, facing={}, " +
                            "pistonMeshes={}/vertices={}, linkageMeshes={}/vertices={}, connectorMeshes={}/vertices={}",
                    pos, engine.getBlockPos(), engine.getBlockState().getValue(FACING),
                    pistonModel.meshes().size(), vertexCount(pistonModel),
                    linkageModel.meshes().size(), vertexCount(linkageModel),
                    connectorModel.meshes().size(), vertexCount(connectorModel));
        }

        private void animate() {
            int packedLight = LightCoordsUtil.getLightCoords(level, engine.getBlockPos());
            piston.light(packedLight);
            linkage.light(packedLight);
            connector.light(packedLight);

            Float angle = HugeDieselEngineClient.getTargetAngle(engine);
            BlockState state = engine.getBlockState();
            Direction facing = state.getValue(FACING);
            Direction.Axis facingAxis = facing.getAxis();

            if (angle == null) {
                piston.setVisible(true);
                applyPose(piston, transformed(facing, false)
                        .translate(0, 0.53475f, 0));
                linkage.setVisible(false);
                connector.setVisible(false);
                logFrame(null, facing, "idle/no-angle");
                return;
            }

            piston.setVisible(true);
            linkage.setVisible(true);

            Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
            boolean roll90 = facingAxis.isHorizontal() && axis == Direction.Axis.Y
                    || facingAxis.isVertical() && axis == Direction.Axis.Z;
            float shaftRotation = facing == Direction.DOWN ? -90
                    : facing == Direction.UP ? 90
                    : facing == Direction.WEST ? -90
                    : facing == Direction.EAST ? 90 : 0;
            if (roll90)
                shaftRotation = facing == Direction.NORTH ? 180
                        : facing == Direction.SOUTH ? 0
                        : facing == Direction.EAST ? -90
                        : facing == Direction.WEST ? 90 : 0;
            angle += shaftRotation * Mth.DEG_TO_RAD;

            float directionSign = facingAxis == Direction.Axis.Y ? -1 : 1;
            float sine = Mth.sin(angle) * directionSign;
            float sine2 = Mth.sin(angle - Mth.HALF_PI) * directionSign;
            float pistonOffset = ((1 - sine) / 4) + 0.4375f;

            applyPose(piston, transformed(facing, roll90)
                    .translate(0, pistonOffset, 0));

            applyPose(linkage, transformed(facing, roll90)
                    .translate(.5f, .5f, .5f)
                    .translate(0, 1, 0)
                    .translate(-.5f, -.5f, -.5f)
                    .translate(0, pistonOffset, 0)
                    .translate(0, 4 / 16f, 8 / 16f)
                    .rotateX(sine2 * 23f * Mth.DEG_TO_RAD)
                    .translate(0, -4 / 16f, -8 / 16f));

            if (blockEntity.isEngineForConnectorDisplay(engine.getBlockPos())) {
                connector.setVisible(true);
                applyPose(connector, transformed(facing, roll90)
                        .translate(0, 2, 0)
                        .translate(.5f, .5f, .5f)
                        .rotateX((float) (-angle + Mth.HALF_PI
                                - (facingAxis.isVertical() ? Math.PI : 0)))
                        .translate(-.5f, -.5f, -.5f));
            } else {
                connector.setVisible(false);
            }

            logFrame(angle, facing, "animated");
        }

        private void logFrame(@Nullable Float angle, Direction facing, String branch) {
            if (diagnosticFrames++ >= 3)
                return;
            BlockPos visualPosition = engine.getBlockPos().subtract(renderOrigin());
            LOGGER.info("[CDG-HUGE-FLYWHEEL] frame: branch={}, shaft={}, engine={}, renderOrigin={}, " +
                            "engineVisualPos={}, facing={}, angle={}, visible=[{},{},{}], " +
                            "light=[{},{},{}], pistonMatrixTranslation=[{},{},{}]",
                    branch, pos, engine.getBlockPos(), renderOrigin(), visualPosition, facing, angle,
                    piston.handle().isVisible(), linkage.handle().isVisible(), connector.handle().isVisible(),
                    piston.light, linkage.light, connector.light,
                    piston.x, piston.y, piston.z);
        }

        private Matrix4f transformed(Direction facing, boolean roll90) {
            BlockPos visualPosition = engine.getBlockPos().subtract(renderOrigin());
            return pose.identity()
                    .translate(visualPosition.getX(), visualPosition.getY(), visualPosition.getZ())
                    .translate(.5f, .5f, .5f)
                    .rotateY(AngleHelper.horizontalAngle(facing) * Mth.DEG_TO_RAD)
                    .rotateX((AngleHelper.verticalAngle(facing) + 90) * Mth.DEG_TO_RAD)
                    .rotateY((roll90 ? -90 : 0) * Mth.DEG_TO_RAD)
                    .translate(-.5f, -.5f, -.5f);
        }

        private void applyPose(RotatingInstance instance, Matrix4f matrix) {
            transformedCenter.set(.5f, .5f, .5f);
            matrix.transformPosition(transformedCenter);
            instance.setPosition(transformedCenter.x - .5f,
                            transformedCenter.y - .5f,
                            transformedCenter.z - .5f);
            instance.rotation.setFromUnnormalized(matrix);
            instance.setChanged();
        }

        private void configureStaticRotation(RotatingInstance instance) {
            instance.setRotationAxis(Direction.Axis.Y)
                    .setRotationalSpeed(0)
                    .setRotationOffset(0);
        }

        private void delete() {
            LOGGER.info("[CDG-HUGE-FLYWHEEL] assembly deleted: shaft={}, engine={}", pos, engine.getBlockPos());
            piston.delete();
            linkage.delete();
            connector.delete();
        }
    }

    private static int vertexCount(Model model) {
        return model.meshes().stream().mapToInt(configured -> configured.mesh().vertexCount()).sum();
    }
}
