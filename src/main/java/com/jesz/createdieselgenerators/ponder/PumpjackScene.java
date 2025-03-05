package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.content.pumpjack.PumpjackCrankBlockEntity;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.ponder.*;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PumpjackScene {
    public static void scene(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("pumpjack", "Setting up a Pumpjack");
        scene.configureBasePlate(0, 0, 9);
        scene.setSceneOffsetY(-2);
        scene.showBasePlate();
        scene.scaleSceneView(0.75f);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 8, 1, 8), Direction.UP);

        Selection pipes = util.select().fromTo(4, 2, 1, 8, 2, 3);
        Selection pipes2 = util.select().fromTo(9, 1, 3, 9, 2, 3);
        Selection cogs = util.select().fromTo(7, 2, 4, 9, 2, 4);
        Selection cogs2 = util.select().position(9, 1, 5);
        Selection cogs3 = util.select().position(3, 1, 9);
        Selection cogs4 = util.select().position(4, 2, 9);
        Selection pumpjackStand0 = util.select().fromTo(3, 2, 4, 3, 4, 4);
        Selection pumpjackStand1 = util.select().fromTo(5, 2, 4, 5, 4, 4);
        Selection pumpjackPole = util.select().fromTo(4, 5, 1, 4, 5, 7);

        scene.world().showSection(pumpjackStand1, Direction.DOWN);
        scene.world().showSection(pumpjackStand0, Direction.DOWN);
        scene.idle(15);

        scene.world().showSection(util.select().position(5, 5, 4), Direction.DOWN);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Every Pumpjack needs a Pumpjack Bearing")
                .pointAt(util.vector().topOf(5, 5, 4))
                .placeNearTarget();
        scene.idle(65);

        scene.world().showSection(util.select().position(3, 5, 4), Direction.DOWN);
        scene.idle(15);

        scene.world().showSection(pumpjackPole, Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Next, build the body of the Pumpjack")
                .pointAt(util.vector().topOf(4, 5, 1))
                .placeNearTarget();
        scene.idle(65);

        scene.world().showSection(util.select().position(4, 5, 0), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Attach a Pumpjack Head ...")
                .pointAt(util.vector().topOf(4, 5, 0))
                .placeNearTarget();
        scene.idle(65);

        scene.world().showSection(util.select().position(4, 5, 8), Direction.DOWN);
        scene.idle(15);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("... and another Pumpjack Bearing")
                .pointAt(util.vector().topOf(4, 5, 8))
                .placeNearTarget();
        scene.idle(65);

        scene.overlay().showControls(util.vector().topOf(4, 5, 8), Pointing.LEFT, 15).withItem(AllItems.WRENCH.asStack());
        scene.idle(15);
        scene.world().setBlock(new BlockPos(4, 5, 8), CDGBlocks.PUMPJACK_BEARING_B.getDefaultState(), false);
        scene.idle(15);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Don't forget to glue the contraption!")
                .pointAt(util.vector().topOf(4, 5, 4))
                .placeNearTarget();
        scene.idle(65);

        scene.overlay().showControls(util.vector().topOf(4, 5, 8), Pointing.LEFT, 15).withItem(AllItems.SUPER_GLUE.asStack());
        scene.idle(25);
        scene.overlay().showControls(util.vector().centerOf(4, 5, 1), Pointing.LEFT, 15).withItem(AllItems.SUPER_GLUE.asStack());
        scene.idle(25);
        scene.overlay().chaseBoundingBoxOutline(PonderPalette.GREEN, util.select().fromTo(4, 5, 9, 4, 5, 0), new AABB(4, 6, 9, 5, 5, 0), 30);
        scene.idle(15);

        scene.rotateCameraY(-180);
        scene.idle(40);

        scene.world().showSection(util.select().position(4,2, 8), Direction.WEST);
        scene.idle(15);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("To power the Pumpjack, you need to use a Pumpjack Crank.")
                .pointAt(util.vector().topOf(4, 2, 8))
                .placeNearTarget();
        scene.idle(65);

        scene.overlay().chaseBoundingBoxOutline(PonderPalette.RED, util.select().fromTo(4, 3, 8, 4, 4, 8), new AABB(4, 5, 9, 5, 3, 8), 35);
        scene.idle(45);

        scene.world().showSection(cogs3, Direction.UP);
        scene.world().showSection(cogs4, Direction.UP);
        scene.idle(15);

        scene.world().setKineticSpeed(cogs3, 32);
        scene.world().setKineticSpeed(cogs4, -16);
        scene.world().setKineticSpeed(util.select().position(4,2, 8), -16);

        scene.rotateCameraY(-90);
        scene.idle(50);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("To extract Crude Oil from the chunk, will need to excavate a hole below the pumpjack head ...")
                .pointAt(util.vector().topOf(4, 1, 0))
                .placeNearTarget();
        scene.idle(65);

        scene.world().setBlock(new BlockPos(4, 1, 0), Blocks.AIR.defaultBlockState(), true);
        scene.idle(15);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("... and place a straight, vertical pipe going all the way to bedrock ...")
                .pointAt(util.vector().topOf(4, 1, 0))
                .placeNearTarget();
        scene.idle(65);

        scene.world().setBlock(new BlockPos(4, 1, 0), AllBlocks.FLUID_PIPE.getDefaultState().setValue(FluidPipeBlock.EAST, false)
                .setValue(FluidPipeBlock.NORTH, false)
                .setValue(FluidPipeBlock.WEST, false)
                .setValue(FluidPipeBlock.SOUTH, false), false);
        scene.idle(15);

        scene.world().showSection(util.select().position(4, 2, 0), Direction.UP);
        scene.idle(15);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("... finally, attach a Pumpjack Hole")
                .pointAt(util.vector().topOf(4, 2, 0))
                .placeNearTarget();
        scene.idle(65);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Once you assemble the contraption ...")
                .pointAt(util.vector().topOf(4, 5, 4))
                .placeNearTarget();
        scene.idle(65);

        scene.overlay().showControls(util.vector().topOf(5, 5, 4), Pointing.LEFT, 15).rightClick();
        scene.idle(35);

        ElementLink<WorldSectionElement> pumpjackBody = scene.world().makeSectionIndependent(util.select().fromTo(4, 5, 0, 4, 5, 8));
        scene.addInstruction(new PumpjackAnimateInstruction(pumpjackBody, new BlockPos(4, 2, 8)));
        scene.idle(35);

        scene.world().showSection(pipes, Direction.UP);
        scene.world().showSection(pipes2, Direction.UP);
        scene.idle(15);

        scene.world().showSection(cogs, Direction.UP);
        scene.world().showSection(cogs2, Direction.UP);
        scene.idle(15);

        scene.world().setKineticSpeed(cogs, 32);
        scene.world().setKineticSpeed(cogs2, -16);
        scene.world().setKineticSpeed(util.select().position(7, 2, 3), -32);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("... you can extract the crude oil with a Mechanical Pump.")
                .pointAt(util.vector().topOf(4, 2, 0))
                .placeNearTarget();
        scene.idle(65);

        scene.idle(60);
    }
    static class PumpjackAnimateInstruction extends TickingInstruction {
        ElementLink<WorldSectionElement> pumpjackBody;
        WorldSectionElement pumpjackElement;
        BlockPos crankPos;
        PumpjackCrankBlockEntity crankBE;
        public PumpjackAnimateInstruction(ElementLink<WorldSectionElement> element, BlockPos crankPos){
            super(false, Integer.MAX_VALUE);
            pumpjackBody = element;
            this.crankPos = crankPos;
        }
        @Override
        public boolean isComplete() {
            return false;
        }

        @Override
        protected void firstTick(PonderScene scene) {
            pumpjackElement = scene.resolve(pumpjackBody);
            crankBE = scene.getWorld().getBlockEntity(crankPos, CDGBlockEntityTypes.PUMPJACK_CRANK.get()).orElse(null);
        }

        @Override
        public void tick(PonderScene scene) {
            super.tick(scene);

            if (pumpjackElement == null || crankBE == null)
                return;
//            int ticks = Integer.MAX_VALUE - remainingTicks;
//            float rot = (float) (Math.sin(((double) ticks / 180 * Math.PI) / 1.6 * 2.5)*15);
            float crankAngle = crankBE.angle;
            float[] angleAnimation = {
                    0, 70, 130, 180, 220, 255, 280, 300, 260, 199, 127, 67};

            int lIndex = (int) Math.abs(Math.floor(crankAngle/((double) 360 /angleAnimation.length)) % angleAnimation.length);

            float partialAngle = (float) Math.abs(Math.abs(crankAngle/((double) 360 /angleAnimation.length)) - lIndex);
            float newCrankAngle;

            newCrankAngle = AngleHelper.angleLerp(partialAngle, angleAnimation[(angleAnimation.length - lIndex) % angleAnimation.length], angleAnimation[(angleAnimation.length - (lIndex+1)) % angleAnimation.length]);

            float a = (float) Math.pow(Math.sin((double) newCrankAngle/((double) 360 /angleAnimation.length) / Math.PI / 2.2), 2)*2+1;
            if(Math.abs(newCrankAngle) >= 359.5)
                a = 0.99f;

            float rot = (a*13f-13f);

            crankBE.crankBearingLocation = new Vec3(4, 0, 0);
            crankBE.inPonderAngle = rot;
            crankBE.bearingPos = new BlockPos(4, 5, 4);
            pumpjackElement.setAnimatedRotation(new Vec3(rot, 0, 0), true);
        }
    }
}
