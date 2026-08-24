package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.tools.lighter.LighterModel;
import com.jesz.createdieselgenerators.content.tools.lighter.LighterState;
import it.unimi.dsi.fastutil.ints.IntList;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/** Ports the add-on's legacy BEWLR/override item states to the 26.2 ItemModel API. */
@EventBusSubscriber(value = Dist.CLIENT, modid = CreateDieselGenerators.ID)
public final class CDGItemModelSwapper {
    private CDGItemModelSwapper() {}

    @SubscribeEvent
    public static void modifyItemModels(ModelEvent.ModifyBakingResult event) {
        Map<Identifier, ItemModel> models = event.getBakingResult().itemStackModels();

        replace(models, "lighter", base -> new SelectingModel(base, models, (stack, all) -> lighterModel(stack, all, base)));
        replace(models, "mold", base -> new SelectingModel(base, models, (stack, all) -> {
            Identifier type = stack.get(CDGDataComponents.MOLD_TYPE);
            return type == null ? base : all.getOrDefault(id("mold_" + type.getPath()), base);
        }));
        replace(models, "oil_scanner", base -> new SelectingModel(base, models, (stack, all) -> {
            int scannerState = stack.getOrDefault(CDGDataComponents.OIL_SCANNER_STATE, 0);
            String suffix = switch (scannerState) {
                case 1 -> "_none";
                case 2 -> "_medium";
                case 3 -> "_high";
                default -> "";
            };
            return suffix.isEmpty() ? base : all.getOrDefault(id("oil_scanner" + suffix), base);
        }));
        replace(models, "track_layers_bag", base -> new SelectingModel(base, models, (stack, all) -> {
            var tracks = stack.get(CDGDataComponents.TRACKS);
            return tracks != null && tracks.count() > 0
                    ? all.getOrDefault(id("track_layers_bag_filled"), base) : base;
        }));
        replace(models, "wire_cutters", base -> new SelectingModel(base, models, (stack, all) -> {
            if (!stack.has(CDGDataComponents.PROCESSING_ITEM))
                return base;
            float phase = (AnimationTickHolder.getRenderTime() % 10f) / 10f;
            return phase < .5f ? all.getOrDefault(id("wire_cutters_cut"), base) : base;
        }));
        replace(models, "hammer", HammerModel::new);

        ItemModel cog = models.get(id("chemical_sprayer_cog"));
        if (cog != null) {
            replace(models, "chemical_sprayer", base -> new ChemicalSprayerModel(base, cog));
            replace(models, "chemical_sprayer_lighter", base -> new ChemicalSprayerModel(base, cog));
        }
    }

    private static ItemModel lighterModel(ItemStack stack, Map<Identifier, ItemModel> models, ItemModel fallback) {
        LighterState state = stack.getOrDefault(CDGDataComponents.LIGHTER_STATE, LighterState.CLOSED);
        String customName = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
        String skin = LighterModel.lighterSkinIDs.get(customName);
        if (skin == null && state == LighterState.CLOSED)
            return fallback;
        String stateSuffix = switch (state) {
            case OPEN -> "_open";
            case OPEN_IGNITED -> "_ignited";
            default -> "";
        };
        String lookup = skin == null ? "lighter" + stateSuffix : "lighter_" + skin + stateSuffix;
        ItemModel selected = models.get(id(lookup));
        return selected == null || selected instanceof SelectingModel ? fallback : selected;
    }

    private static void replace(Map<Identifier, ItemModel> models, String name,
                                java.util.function.Function<ItemModel, ItemModel> replacement) {
        Identifier key = id(name);
        ItemModel base = models.get(key);
        if (base != null)
            models.put(key, replacement.apply(base));
    }

    private static Identifier id(String path) {
        return CreateDieselGenerators.id(path);
    }

    private interface Selector {
        ItemModel select(ItemStack stack, Map<Identifier, ItemModel> models);
    }

    private record SelectingModel(ItemModel fallback, Map<Identifier, ItemModel> models,
                                  Selector selector) implements ItemModel {
        @Override
        public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                           ItemDisplayContext displayContext, ClientLevel level, ItemOwner owner, int seed) {
            selector.select(stack, models).update(state, stack, resolver, displayContext, level, owner, seed);
        }
    }

    private static final class ChemicalSprayerModel implements ItemModel {
        private final ItemModel base;
        private final ItemModel cog;

        private ChemicalSprayerModel(ItemModel base, ItemModel cog) {
            this.base = base;
            this.cog = cog;
        }

        @Override
        public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                           ItemDisplayContext context, ClientLevel level, ItemOwner owner, int seed) {
            ItemTransform[] sharedTransform = new ItemTransform[1];
            base.update(new TransformedState(state, new Matrix4f(), sharedTransform, false),
                    stack, resolver, context, level, owner, seed);

            LivingEntity living = owner == null ? null : owner.asLivingEntity();
            boolean active = living != null && living.getUseItem() == stack && living.getUseItemRemainingTicks() > 0;
            float angle = (AnimationTickHolder.getRenderTime() / 10f) * (active ? -200f : -25f);
            angle %= 360f;
            Matrix4f cogTransform = new Matrix4f()
                    .translate(.5f, .5f, .53125f)
                    .rotateZ((float) Math.toRadians(angle));
            state.setAnimated();
            state.appendModelIdentityElement((int) angle);
            cog.update(new TransformedState(state, cogTransform, sharedTransform, true),
                    stack, resolver, context, level, owner, seed + 1);
        }
    }

    private static final class HammerModel implements ItemModel {
        private final ItemModel base;

        private HammerModel(ItemModel base) {
            this.base = base;
        }

        @Override
        public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                           ItemDisplayContext context, ClientLevel level, ItemOwner owner, int seed) {
            Matrix4f transform = new Matrix4f();
            if (stack.has(CDGDataComponents.PROCESSING_ITEM)) {
                float phase = (AnimationTickHolder.getRenderTime() % 10f) / 10f - .5f;
                float angle = Math.abs(phase * phase * phase) * 300f;
                transform.translate(.5f, .5f, .5f)
                        .rotateZ((float) Math.toRadians(angle))
                        .translate(-.5f, -.5f, -.5f);
                state.setAnimated();
                state.appendModelIdentityElement((int) angle);
            }
            base.update(new TransformedState(state, transform, null, false),
                    stack, resolver, context, level, owner, seed);
        }
    }

    private static final class TransformedState extends ItemStackRenderState {
        private final ItemStackRenderState delegate;
        private final Matrix4fc transform;
        private final ItemTransform[] sharedTransform;
        private final boolean reuseTransform;

        private TransformedState(ItemStackRenderState delegate, Matrix4fc transform,
                                 ItemTransform[] sharedTransform, boolean reuseTransform) {
            this.delegate = delegate;
            this.transform = transform;
            this.sharedTransform = sharedTransform;
            this.reuseTransform = reuseTransform;
        }

        @Override public LayerRenderState newLayer() {
            return new TransformedLayer(delegate, delegate.newLayer(), transform, sharedTransform, reuseTransform);
        }
        @Override public void setAnimated() { delegate.setAnimated(); }
        @Override public void appendModelIdentityElement(Object value) { delegate.appendModelIdentityElement(value); }
        @Override public void setOversizedInGui(boolean value) { delegate.setOversizedInGui(value); }
    }

    private static final class TransformedLayer extends ItemStackRenderState.LayerRenderState {
        private final ItemStackRenderState.LayerRenderState delegate;
        private final Matrix4fc transform;
        private final ItemTransform[] sharedTransform;
        private final boolean reuseTransform;

        private TransformedLayer(ItemStackRenderState owner, ItemStackRenderState.LayerRenderState delegate,
                                 Matrix4fc transform, ItemTransform[] sharedTransform, boolean reuseTransform) {
            owner.super();
            this.delegate = delegate;
            this.transform = transform;
            this.sharedTransform = sharedTransform;
            this.reuseTransform = reuseTransform;
        }

        @Override public List<BakedQuad> prepareQuadList() { return delegate.prepareQuadList(); }
        @Override public void setUsesBlockLight(boolean value) { delegate.setUsesBlockLight(value); }
        @Override public void setExtents(Supplier<Vector3fc[]> value) { delegate.setExtents(value); }
        @Override public void setParticleMaterial(Material.Baked value) { delegate.setParticleMaterial(value); }
        @Override public void setItemTransform(ItemTransform value) {
            if (sharedTransform != null && reuseTransform && sharedTransform[0] != null)
                delegate.setItemTransform(sharedTransform[0]);
            else {
                if (sharedTransform != null)
                    sharedTransform[0] = value;
                delegate.setItemTransform(value);
            }
        }
        @Override public void setLocalTransform(Matrix4fc value) {
            delegate.setLocalTransform(new Matrix4f(transform).mul(value));
        }
        @Override public <T> void setupSpecialModel(SpecialModelRenderer<T> renderer, T value) {
            delegate.setupSpecialModel(renderer, value);
        }
        @Override public void setFoilType(FoilType value) { delegate.setFoilType(value); }
        @Override public IntList tintLayers() { return delegate.tintLayers(); }
    }
}
