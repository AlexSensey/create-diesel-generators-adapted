package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlockEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Contraption.class)
public abstract class ContraptionMixin {
    @Shadow protected abstract BlockPos toLocalPos(BlockPos globalPos);

    @Inject(method = "getBlockEntityNBT", at=@At("RETURN"), remap = false)
    public void getBlockEntityNBT(Level world, BlockPos pos, CallbackInfoReturnable<CompoundTag> cir){
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof OilBarrelBlockEntity))
            return;
        CompoundTag nbt = cir.getReturnValue();
        if (nbt == null)
            return;
        if (nbt.contains("Controller"))
            nbt.put("Controller", NbtUtils.writeBlockPos(toLocalPos(NBTHelper.readBlockPos(nbt, "Controller"))));

    }
}
