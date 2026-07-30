package com.jesz.createdieselgenerators.compat.strut_your_stuff;

import com.cake.struts.content.StrutModelType;
import com.cake.struts.content.block.StrutBlock;
import com.cake.struts.content.block.StrutBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrussGirderStrutBlock extends StrutBlock {
    public TrussGirderStrutBlock(Properties properties, StrutModelType modelType) {
        super(properties, modelType);
    }

    @Override
    protected BlockEntityType<? extends StrutBlockEntity> getStrutBlockEntityType() {
        return StrutYourStuffRegistryEntries.ANDESITE_GIRDER_STRUT_BLOCK_ENTITY.get();
    }
}
