package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlockEntity;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TurretScenes {
    public static void chemical(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("chemical_turret", "Setting up a chemical turret");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        Selection largeCog = util.select().position(4, 0, 5);
        Selection cogShafts = util.select().fromTo(3, 1, 5, 3, 1, 3);
        Selection cog = util.select().fromTo(3, 1, 2, 3, 2, 2);
        Selection turret = util.select().position(2, 2, 2);
        Selection pump = util.select().position(2, 1, 3);
        Selection tank = util.select().fromTo(2, 1, 4, 2, 2, 4);
        Selection redstoneControl = util.select().fromTo(1, 1, 2, 1, 2, 2);
        Selection pipes = util.select().fromTo(2, 1, 2, 2, 1, 3);

        ElementLink<WorldSectionElement> turretSection = scene.world().showIndependentSection(turret, Direction.DOWN);
        scene.world().moveSection(turretSection, util.vector().of(0, -1, 0), 0);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Chemical turrets are standalone versions of chemical sprayers.")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.world().moveSection(turretSection, util.vector().of(0, 1, 0), 15);
        scene.world().showSection(pipes, Direction.NORTH);
        scene.world().showSection(tank, Direction.NORTH);
        scene.idle(20);
        scene.world().showSection(largeCog, Direction.DOWN);
        scene.world().showSection(cogShafts, Direction.DOWN);
        scene.world().showSection(cog, Direction.DOWN);

        scene.idle(20);
        scene.rotateCameraY(90);

        scene.world().setKineticSpeed(largeCog, 16);
        scene.world().setKineticSpeed(cogShafts, -32);
        scene.world().setKineticSpeed(pump, 32);
        scene.world().setKineticSpeed(cog, 32);
        scene.world().setKineticSpeed(turret, -32);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("They need to be supplied with a fluid, and powered by a cogwheel.")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.rotateCameraY(-90);
        scene.idle(20);
        scene.world().showSection(redstoneControl, Direction.DOWN);
        scene.idle(20);
        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("When powered with redstone, the turret will start spraying with the fluid in its tank.")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 2, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlock(util.grid().at(1, 2, 2), bs -> bs.setValue(BlockStateProperties.POWERED, true), false);
        scene.idle(60);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Additionally they can be upgraded with a lighter.")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 2, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);
        scene.overlay().showControls(util.vector().topOf(2, 2, 2), Pointing.DOWN, 15).withItem(CDGItems.LIGHTER.asStack());
        scene.idle(20);

        scene.world().modifyBlockEntityNBT(turret, ChemicalTurretBlockEntity.class, tag -> tag.putBoolean("LighterUpgrade", true));

        scene.idle(60);
    }

    public static void automatic(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("automatic_turret", "Automatically controlling turrets");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        Selection turret = util.select().position(2, 1, 2);
        Selection seat = util.select().position(2, 1, 3);

        scene.world().showSection(turret, Direction.DOWN);
        scene.idle(20);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Turrets can also be controlled by mobs.")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(seat, Direction.DOWN);
        scene.special().createBirb(util.vector().of(2.5, 1.5, 3.5), ParrotPose.FacePointOfInterestPose::new);
        scene.rotateCameraY(-90);
        scene.idle(20);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Simply add an entity filter, into the filter slot of the turret ...")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().of(2, 1.25, 2.5), Pointing.DOWN, 15).withItem(CDGItems.ENTITY_FILTER.asStack());
        scene.idle(20);

        scene.rotateCameraY(90);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("... the turret operator will shoot any mobs, that match the filter.")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), ChemicalTurretBlockEntity.class, be -> be.targetedHorizontalRotation = 60);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), ChemicalTurretBlockEntity.class, be -> be.targetedHorizontalRotation = -60);
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), ChemicalTurretBlockEntity.class, be -> be.targetedHorizontalRotation = 0);


        scene.idle(60);
    }
}
