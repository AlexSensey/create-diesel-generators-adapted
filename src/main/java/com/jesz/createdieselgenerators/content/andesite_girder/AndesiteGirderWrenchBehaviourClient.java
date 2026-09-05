package com.jesz.createdieselgenerators.content.andesite_girder;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.AllItems;
import net.createmod.catnip.api.client.outliner.Outliner;
import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.api.data.Pair;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public final class AndesiteGirderWrenchBehaviourClient {
    private AndesiteGirderWrenchBehaviourClient() {
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult result))
            return;

        ClientLevel world = mc.level;
        BlockPos pos = result.getBlockPos();
        Player player = mc.player;
        ItemStack heldItem = player.getMainHandItem();

        if (player.isSteppingCarefully() || !CDGBlocks.ANDESITE_GIRDER.has(world.getBlockState(pos))
                || !AllItems.WRENCH.isIn(heldItem))
            return;

        Pair<Direction, AndesiteGirderWrenchBehaviour.Action> dirPair =
                AndesiteGirderWrenchBehaviour.getDirectionAndAction(result, world, pos);
        if (dirPair == null)
            return;

        Vec3 center = VecHelper.getCenterOf(pos);
        Vec3 edge = center.add(Vec3.atLowerCornerOf(dirPair.getFirst().getUnitVec3i()).scale(0.4));
        Direction.Axis[] axes = Arrays.stream(Iterate.axes)
                .filter(axis -> axis != dirPair.getFirst().getAxis())
                .toArray(Direction.Axis[]::new);

        double normalMultiplier = dirPair.getSecond() == AndesiteGirderWrenchBehaviour.Action.PAIR ? 4 : 1;
        Vec3 corner1 = edge
                .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[0], Direction.AxisDirection.POSITIVE)
                        .getUnitVec3i()).scale(0.3))
                .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[1], Direction.AxisDirection.POSITIVE)
                        .getUnitVec3i()).scale(0.3))
                .add(Vec3.atLowerCornerOf(dirPair.getFirst().getUnitVec3i()).scale(0.1 * normalMultiplier));

        normalMultiplier = dirPair.getSecond() == AndesiteGirderWrenchBehaviour.Action.HORIZONTAL ? 9 : 2;
        Vec3 corner2 = edge
                .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[0], Direction.AxisDirection.NEGATIVE)
                        .getUnitVec3i()).scale(0.3))
                .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[1], Direction.AxisDirection.NEGATIVE)
                        .getUnitVec3i()).scale(0.3))
                .add(Vec3.atLowerCornerOf(dirPair.getFirst().getOpposite().getUnitVec3i())
                        .scale(0.1 * normalMultiplier));

        Outliner.getInstance().showAABB("andesiteGirderWrench", new AABB(corner1, corner2))
                .lineWidth(1 / 32f)
                .colored(new Color(95, 95, 255));
    }
}
