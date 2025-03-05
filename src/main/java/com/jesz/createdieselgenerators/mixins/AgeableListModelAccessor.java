package com.jesz.createdieselgenerators.mixins;

import net.minecraft.client.model.AgeableListModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AgeableListModel.class)
public interface AgeableListModelAccessor {
    @Accessor
    boolean getScaleHead();
    @Accessor
    float getBabyHeadScale();
    @Accessor
    float getBabyYHeadOffset();
    @Accessor
    float getBabyZHeadOffset();
}
