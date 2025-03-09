package com.jesz.createdieselgenerators.world;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.compat.kubejs.CDGKubeJSPlugin;
import com.simibubi.create.AllTags;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class OilChunksSavedData extends SavedData {

    Map<ChunkPos, Integer> chunks = new HashMap<>();
    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag lt = new ListTag();
        chunks.forEach((pos,amount) -> {
            CompoundTag c = new CompoundTag();
            c.put("x", IntTag.valueOf(pos.x));
            c.put("z", IntTag.valueOf(pos.z));
            c.put("Amount", IntTag.valueOf(amount));
            lt.add(c);
        });

        compound.put("OilChunks", lt);

        return compound;
    }

    private OilChunksSavedData() {

    }

    private static OilChunksSavedData load(CompoundTag compound){
        OilChunksSavedData sd = new OilChunksSavedData();

        sd.chunks = new HashMap<>();
        NBTHelper.iterateCompoundList(compound.getList("OilChunks", Tag.TAG_COMPOUND), c -> {
            sd.chunks.put(new ChunkPos(c.getInt("x"), c.getInt("z")), c.getInt("Amount"));
        });

        return sd;
    }

    public static OilChunksSavedData load(ServerLevel level){
        return level.getDataStorage().computeIfAbsent(OilChunksSavedData::load, OilChunksSavedData::new, "cdg_oil_chunks");
    }
    public void setChunkAmount(ChunkPos chunk, int amount){
        if(chunks.containsKey(chunk))
            chunks.replace(chunk, amount);
        else
            chunks.put(chunk, amount);

        setDirty();
    }
    public void removeChunkAmount(ChunkPos chunk){
        chunks.remove(chunk);
        setDirty();
    }
    public int getChunkOilAmount(ChunkPos chunk){
        if(chunks.containsKey(chunk))
            return CDGConfig.OIL_DEPOSITS_INFINITE.get() ? Integer.MAX_VALUE : chunks.get(chunk);
        return -1;
    }

    public static int getOilAmount(ServerLevel level, ChunkPos pos){
        long seed = level.getSeed();
        List<Holder<Biome>> biomes = getBiomesInChunk(level, pos);

        if(ModList.get().isLoaded("kubejs")) {
            int amount = CDGKubeJSPlugin.calculateOilChunks(biomes, pos, seed);
            if(amount != -1)
                return amount;
        }

        Random random = new Random(new Random(seed).nextLong() + (long) pos.x * pos.z);
        int amount = Math.abs(random.nextInt());

        boolean isHighInOil = false;
        boolean isDenied = false;
        for (Holder<Biome> biome : biomes){
            if(biome.is(AllTags.optionalTag(ForgeRegistries.BIOMES, CreateDieselGenerators.rl("oil_biomes"))))
                isHighInOil = true;
            if(biome.is(AllTags.optionalTag(ForgeRegistries.BIOMES, CreateDieselGenerators.rl("deny_oil_biomes"))))
                isDenied = true;
        }
        if(isDenied)
            return 0;
        if(isHighInOil ? (random.nextFloat(0, 100) >= CDGConfig.HIGH_OIL_PERCENTAGE.get()) : (amount % 100 >= CDGConfig.OIL_PERCENTAGE.get()))
            return 0;
        if(CDGConfig.OIL_DEPOSITS_INFINITE.get())
            return Integer.MAX_VALUE;
        if(isHighInOil)
            return (int) (Mth.clamp(amount % 400000, 8000, 400000)* CDGConfig.HIGH_OIL_MULTIPLIER.get());
        return (int) (Mth.clamp(amount % 15000, 0, 12000)* CDGConfig.OIL_MULTIPLIER.get());
    }
    public static List<Holder<Biome>> getBiomesInChunk(ServerLevel level, ChunkPos chunkPos){
        List<Holder<Biome>> list = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                for (int z = 0; z < 16; z++) {
                    Holder<Biome> biome = level.getBiome(new BlockPos((chunkPos.x*16)+x, y, (chunkPos.z*16)+z));
                    if(!list.contains(biome))
                        list.add(biome);
                }
            }
        }
        return list;
    }
}
