package com.jesz.createdieselgenerators.compat.strut_your_stuff;

import com.cake.struts.content.StrutModelType;
import com.cake.struts.content.block.StrutBlockEntity;
import com.cake.struts.content.block.StrutBlockItem;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.simibubi.create.foundation.item.ItemDescription;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class StrutYourStuffRegistryEntries {
    public static final StrutModelType ANDESITE_GIRDER_MODEL =
            new StrutModelType(legacyId("block/girder_strut/andesite_girder"),
                    legacyId("block/andesite_girder_strut_end"), StrutYourStuffClient::cutoutRenderType);


    public static final BlockEntry<TrussGirderStrutBlock> ANDESITE_GIRDER_STRUT = registerAndesiteGirderStrut();

    public static final BlockEntityEntry<StrutBlockEntity> ANDESITE_GIRDER_STRUT_BLOCK_ENTITY = REGISTRATE
            .blockEntity("andesite_girder_strut", StrutBlockEntity::new)
            .visual(StrutYourStuffClient::visualFactory, false)
            .validBlocks(ANDESITE_GIRDER_STRUT)
            .renderer(StrutYourStuffClient::rendererFactory)
            .register();

    public static void register() {

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockEntry<TrussGirderStrutBlock> registerAndesiteGirderStrut() {
        var itemBuilder = REGISTRATE.block("andesite_girder_strut",
                        props -> new TrussGirderStrutBlock(props, ANDESITE_GIRDER_MODEL))
                .properties(p -> p.strength(3f, 6f))
                .properties(BlockBehaviour.Properties::noOcclusion)
                .onRegisterAfter(
                        Registries.ITEM,
                        v -> ItemDescription.useKey(v, "block.bits_n_bobs.girder_strut")
                )
                .item(StrutBlockItem::new);
        itemBuilder.model((NonNullBiConsumer) StrutYourStuffClient::generateItemModel);
        return itemBuilder.build().register();
    }

    private static ResourceLocation legacyId(String path) {
        return ResourceLocation.fromNamespaceAndPath(CreateDieselGenerators.ID, path);
    }

    public static void fillCreativeTab(CreativeModeTab.Output output) {
        output.accept(ANDESITE_GIRDER_STRUT.get());
    }
}
