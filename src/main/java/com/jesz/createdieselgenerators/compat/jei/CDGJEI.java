package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.*;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermentingRecipe;
import com.jesz.createdieselgenerators.content.distillation.DistillationRecipe;
import com.jesz.createdieselgenerators.content.molds.CastingRecipe;
import com.jesz.createdieselgenerators.content.tools.hammer.HammerRecipe;
import com.jesz.createdieselgenerators.content.tools.wire_cutters.WireCuttingRecipe;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.*;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.createmod.catnip.config.ConfigBase;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.simibubi.create.compat.jei.CreateJEI.*;
import static mezz.jei.api.recipe.RecipeType.createRecipeHolderType;

@JeiPlugin
@ParametersAreNonnullByDefault
public class CDGJEI implements IModPlugin {

    private static final ResourceLocation ID = CreateDieselGenerators.rl("jei_plugin");
    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }


    private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();

    private void loadCategories() {
        allCategories.clear();

        CreateRecipeCategory<?>
        basin_fermenting = builder(BasinRecipe.class)
                .addTypedRecipes(CDGRecipes.BASIN_FERMENTING)
                .catalyst(CDGBlocks.BASIN_LID::get)
                .catalyst(AllBlocks.BASIN::get)
                .doubleItemIcon(AllBlocks.BASIN.get(), CDGBlocks.BASIN_LID.get())
                .emptyBackground(177, 100)
                .build("basin_fermenting", BasinFermentingCategory::new),
        bulk_fermenting = builder(BulkFermentingRecipe.class)
                .addTypedRecipes(CDGRecipes.BULK_FERMENTING)
                .catalyst(CDGBlocks.BULK_FERMENTER::get)
                .doubleItemIcon(CDGBlocks.BULK_FERMENTER.get(), Items.CLOCK)
                .emptyBackground(177, 100)
                .build("bulk_fermenting", BulkFermentingCategory::new),
        compression_molding = builder(BasinRecipe.class)
                .addTypedRecipes(CDGRecipes.COMPRESSION_MOLDING)
                .catalyst(AllBlocks.MECHANICAL_PRESS::get)
                .catalyst(CDGItems.MOLD::get)
                .catalyst(AllBlocks.BASIN::get)
                .doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), CDGItems.MOLD.get())
                .emptyBackground(177, 100)
                .build("compression_molding", CompressionMoldingCategory::new),
        casting = builder(CastingRecipe.class)
                .addTypedRecipes(CDGRecipes.CASTING)
                .catalyst(AllBlocks.SPOUT::get)
                .catalyst(CDGItems.MOLD::get)
                .catalyst(AllBlocks.BASIN::get)
                .doubleItemIcon(AllBlocks.SPOUT.get(), CDGItems.MOLD.get())
                .emptyBackground(177, 100)
                .build("casting", CastingCategory::new),
        distillation = builder(DistillationRecipe.class)
                .addTypedRecipes(CDGRecipes.DISTILLATION)
                .catalyst(AllBlocks.FLUID_TANK::get)
                .catalyst(CDGItems.DISTILLATION_CONTROLLER::get)
                .doubleItemIcon(AllBlocks.FLUID_TANK.get(), CDGItems.DISTILLATION_CONTROLLER.get())
                .emptyBackground(177, 200)
                .build("distillation", DistillationCategory::new),
        hammering = builder(HammerRecipe.class)
                .addTypedRecipes(CDGRecipes.HAMMERING)
                .catalyst(CDGItems.HAMMER::get)
                .doubleItemIcon(CDGItems.HAMMER.get(), AllItems.IRON_SHEET.get())
                .emptyBackground(177, 55)
                .build("hammering", HammeringCategory::new),
        wire_cutting = builder(WireCuttingRecipe.class)
                .addTypedRecipes(CDGRecipes.WIRE_CUTTING)
                .catalyst(CDGItems.WIRE_CUTTERS::get)
                .itemIcon(CDGItems.WIRE_CUTTERS.get())
                .emptyBackground(177, 55)
                .build("wire_cutting", WireCuttingCategory::new);
    }

    private <T extends Recipe<?>> CategoryBuilder<T> builder(Class<? extends T> recipeClass) {
        return new CategoryBuilder<>(recipeClass);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        loadCategories();
        registration.addRecipeCategories(allCategories.toArray(IRecipeCategory[]::new));
    }
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        allCategories.forEach(c -> c.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        allCategories.forEach(c -> c.registerCatalysts(registration));
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(CDGItems.MOLD.get(), new ISubtypeInterpreter<>() {
            @Override
            public Object getSubtypeData(ItemStack stack, UidContext context) {
                return stack.get(CDGDataComponents.MOLD_TYPE);
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                ResourceLocation mold = stack.get(CDGDataComponents.MOLD_TYPE);
                if (mold == null)
                    return "";
                return "createdieselgenerators:mold:" + mold.toString();
            }
        });
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AbstractSimiContainerScreen.class, new SlotMover());
    }

    private class CategoryBuilder<T extends Recipe<?>> {
        private final Class<? extends T> recipeClass;
        private Predicate<CRecipes> predicate = cRecipes -> true;

        private IDrawable background;
        private IDrawable icon;

        private final List<Consumer<List<RecipeHolder<T>>>> recipeListConsumers = new ArrayList<>();
        private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

        public CategoryBuilder(Class<? extends T> recipeClass) {
            this.recipeClass = recipeClass;
        }

        public CategoryBuilder<T> addRecipeListConsumer(Consumer<List<RecipeHolder<T>>> consumer) {
            recipeListConsumers.add(consumer);
            return this;
        }

        public CategoryBuilder<T> addTypedRecipes(IRecipeTypeInfo recipeTypeEntry) {
            return addTypedRecipes(recipeTypeEntry::getType);
        }

        public <I extends RecipeInput, R extends Recipe<I>> CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<R>> recipeType) {
            return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipe -> {
                if (recipeClass.isInstance(recipe.value()))
                    //noinspection unchecked - checked by if statement above
                    recipes.add((RecipeHolder<T>) recipe);
            }, recipeType.get()));
        }

        public CategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
            catalysts.add(supplier);
            return this;
        }

        public CategoryBuilder<T> catalyst(Supplier<ItemLike> supplier) {
            return catalystStack(() -> new ItemStack(supplier.get()
                    .asItem()));
        }

        public CategoryBuilder<T> icon(IDrawable icon) {
            this.icon = icon;
            return this;
        }

        public CategoryBuilder<T> itemIcon(ItemLike item) {
            icon(new ItemIcon(() -> new ItemStack(item)));
            return this;
        }

        public CategoryBuilder<T> doubleItemIcon(ItemLike item1, ItemLike item2) {
            icon(new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2)));
            return this;
        }

        public CategoryBuilder<T> background(IDrawable background) {
            this.background = background;
            return this;
        }

        public CategoryBuilder<T> emptyBackground(int width, int height) {
            background(new EmptyBackground(width, height));
            return this;
        }

        public CreateRecipeCategory<T> build(String name, CreateRecipeCategory.Factory<T> factory) {

            Supplier<List<RecipeHolder<T>>> recipesSupplier;
            recipesSupplier = () -> {
                List<RecipeHolder<T>> recipes = new ArrayList<>();
                for (Consumer<List<RecipeHolder<T>>> consumer : recipeListConsumers) {
                    consumer.accept(recipes);
                }
                return recipes;
            };

            CreateRecipeCategory.Info<T> info = new CreateRecipeCategory.Info<>(
                    createRecipeHolderType(CreateDieselGenerators.rl(name)),
                    Component.translatable(CreateDieselGenerators.ID + ".recipe." + name),
                    background,
                    icon,
                    recipesSupplier,
                    catalysts
            );
            CreateRecipeCategory<T> category = factory.create(info);
            allCategories.add(category);
            return category;
        }
    }
}
