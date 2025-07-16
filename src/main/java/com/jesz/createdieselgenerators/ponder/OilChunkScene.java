package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class OilChunkScene {

    public static void scene(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("oil_chunk", "Finding an Oil Chunk");
        scene.configureBasePlate(0, 0, 3);
        scene.setSceneOffsetY(0);
        scene.showBasePlate();
        scene.scaleSceneView(2f);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 2, 1, 2), Direction.UP);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Beneath the surface, some chunks hide Crude Oil deposits")
                .pointAt(util.vector().topOf(1, 1, 1))
                .placeNearTarget();
        scene.idle(65);

        ElementLink<EntityElement> scanner = scene.world().createItemEntity(util.vector().centerOf(1, 5, 1), Vec3.ZERO, CDGItems.OIL_SCANNER.asStack());
        scene.idle(60);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("To find these Chunks, you'll need to use an Oil Scanner")
                .pointAt(util.vector().topOf(1, 1, 1))
                .placeNearTarget();
        scene.idle(65);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("Right-click the Oil Scanner to see how much oil is in the chunk")
                .pointAt(util.vector().topOf(1, 1, 1))
                .placeNearTarget();
        scene.idle(65);
        modifyScanner(scene, scanner, 0);
        scene.idle(20);
        modifyScanner(scene, scanner, 2);
        scene.idle(20);

        // Different Biomes

        scene.overlay().showText(50)
                .attachKeyFrame()
                .colored(PonderPalette.BLUE)
                .text("Additionally, different Biomes will have different levels of Crude Oil")
                .pointAt(util.vector().topOf(1, 1, 1))
                .placeNearTarget();
        scene.idle(65);

        scene.world().modifyBlocks(util.select().fromTo(0, 1, 0, 2, 1, 2), bs -> Blocks.SAND.defaultBlockState(), true);

        for (BlockPos pos : List.of(new BlockPos(2, 2, 1), new BlockPos(2, 3, 1))) {
            scene.world().setBlock(pos, Blocks.CACTUS.defaultBlockState(), false);
            scene.idle(10);
            scene.world().showSection(util.select().position(pos), Direction.DOWN);
        }
        scene.idle(10);
        scene.world().setBlock(new BlockPos(0, 2, 2), Blocks.DEAD_BUSH.defaultBlockState(), false);
        scene.world().showSection(util.select().position(new BlockPos(0, 2, 2)), Direction.DOWN);

        scene.idle(10);
        modifyScanner(scene, scanner, 0);
        scene.idle(20);
        modifyScanner(scene, scanner, 3);
        scene.idle(60);

        scene.overlay().showText(50)
                .attachKeyFrame()
                .text("You can extract the Crude Oil with a pumpjack")
                .pointAt(util.vector().topOf(1, 1, 1))
                .placeNearTarget();
        scene.idle(65);
    }

    static void modifyScanner(SceneBuilder scene, ElementLink<EntityElement> scanner, int type){
        scene.addInstruction(sc -> {
            sc.resolve(scanner).ifPresent(entity -> {
                if (entity instanceof ItemEntity ie) {
                    ie.getItem().set(CDGDataComponents.OIL_SCANNER_STATE, type);
                }
            });
        });
    }
}


