package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.tools.ChemicalSprayerProjectileEntity;
import com.jesz.createdieselgenerators.content.tools.ChemicalSprayerProjectileRenderer;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.world.entity.MobCategory;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class CDGEntityTypes {

    public static final EntityEntry<ChemicalSprayerProjectileEntity> CHEMICAL_SPRAYER_PROJECTILE = REGISTRATE.entity("chemical_sprayer_projectile", ChemicalSprayerProjectileEntity::new, MobCategory.MISC)
                    .properties(b -> b.sized(0.5f, 0.5f).clientTrackingRange(4)
                            .setUpdateInterval(20)
                            .setShouldReceiveVelocityUpdates(true))
                    .renderer(() -> ChemicalSprayerProjectileRenderer::new).register();
    public static void register(){}
}
