package com.jesz.createdieselgenerators.commands;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.world.OilChunksSavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSpecialTextures;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CDGCommands {
    public CDGCommands (CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cdg").then(Commands.literal("oil")
                .then(Commands.literal("get").executes((command) -> getOilChunk(command.getSource())))
                .then(Commands.literal("locate").executes((command) -> locateOilChunk(command.getSource())))
                .then(Commands.literal("regenerate").executes((command) -> refreshOilChunk(command.getSource())))
                .then(Commands.literal("set").then(Commands.argument("amount", IntegerArgumentType.integer(0, 100000)).executes((command) -> setOilChunk(command.getSource(), command))))

                ));
    }

    private int getOilChunk(CommandSourceStack source) throws CommandSyntaxException {
        if(!source.hasPermission(2))
            return 0;
        ChunkPos chunkPos = new ChunkPos(new BlockPos((int) source.getPosition().x, (int) source.getPosition().y, (int) source.getPosition().z));

        int amount = OilChunksSavedData.getChunkOilAmount(source.getLevel(), chunkPos);

        if (amount == Integer.MAX_VALUE)
            source.sendSuccess(() -> Component.literal("This oil chunk is infinite.").withStyle(ChatFormatting.GRAY), false);
        else
            source.sendSuccess(() -> Component.literal("There is ").withStyle(ChatFormatting.GRAY).append(Component.literal(String.format("%,1d", amount) + "mB").withStyle(ChatFormatting.GOLD)).append(" of Oil in this Chunk.").withStyle(ChatFormatting.GRAY), false);

        return 1;
    }
    private int refreshOilChunk(CommandSourceStack source) throws CommandSyntaxException {
        if(!source.hasPermission(2))
            return 0;
        ChunkPos chunkPos = new ChunkPos(new BlockPos((int) source.getPosition().x, (int) source.getPosition().y, (int) source.getPosition().z));

        OilChunksSavedData.removeChunk(source.getLevel(), chunkPos);

        source.sendSuccess(() -> Component.literal("Refreshed this chunks oil contents").withStyle(ChatFormatting.GRAY), false);

        return 1;
    }

    private int setOilChunk(CommandSourceStack source, CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(!source.hasPermission(2))
            return 0;
        ChunkPos chunkPos = new ChunkPos(new BlockPos((int) source.getPosition().x, (int) source.getPosition().y, (int) source.getPosition().z));

        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        OilChunksSavedData.setChunkOilAmount(source.getLevel(), chunkPos, amount);
        source.sendSuccess(() -> Component.literal("Set this chunk's oil deposits to  ").withStyle(ChatFormatting.GRAY).append(Component.literal(String.format("%,1d", amount) + "mB").withStyle(ChatFormatting.GOLD)), false);

        return 1;
    }

    private int locateOilChunk(CommandSourceStack source) throws CommandSyntaxException {
        if (!source.hasPermission(2))
            return 0;

        BlockPos sourcePos = BlockPos.containing(source.getPosition());
        ChunkPos centerChunk = new ChunkPos(sourcePos);

        int radius = 10;
        int dx = 0, dz = -1;

        for (int i = 0; i < (radius * 2 + 1) * (radius * 2 + 1); i++) {
            int cx = centerChunk.x + dx;
            int cz = centerChunk.z + dz;
            ChunkPos currentChunk = new ChunkPos(cx, cz);

            int amount = OilChunksSavedData.getChunkOilAmount(source.getLevel(), currentChunk);

            if (amount != 0) {
                int blockX = cx * 16 + 8;
                int blockZ = cz * 16 + 8;
                source.sendSuccess(() -> Component.literal("There is oil in chunk ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(cx + " " + cz)
                                .withStyle(ChatFormatting.GOLD)
                                .withStyle(style -> style.withClickEvent(new ClickEvent(
                                        ClickEvent.Action.SUGGEST_COMMAND,
                                        "/tp @s " + blockX + " ~ " + blockZ))))
                        .append(" with ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%,1d", amount) + "mB ")
                                .withStyle(ChatFormatting.GOLD))
                        .append(Component.literal("of oil.").withStyle(ChatFormatting.GRAY)), false);
                return 1;
            }

            // Spiral coordinate step logic
            if (dx == dz || (dx < 0 && dx == -dz) || (dx > 0 && dx == 1 - dz)) {
                int temp = dx;
                dx = -dz;
                dz = temp;
            }

            dx += Integer.signum(dx);
            dz += Integer.signum(dz);
        }

        source.sendFailure(Component.literal("There is no oil chunk nearby").withStyle(ChatFormatting.RED));
        return 1;
    }
}
