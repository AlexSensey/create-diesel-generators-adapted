package com.jesz.createdieselgenerators.content.tools.hammer;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItemComponent;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.Optional;

public class HammerItem extends Item {
    Multimap<Attribute, AttributeModifier> toolAttributes;

    public HammerItem(Properties properties) {
        super(properties);

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE.value(), new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 6, AttributeModifier.Operation.ADD_VALUE));
        builder.put(Attributes.ATTACK_SPEED.value(), new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.1, AttributeModifier.Operation.ADD_VALUE));
        toolAttributes = builder.build();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 9, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -1.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionHand otherHand = InteractionHand.values()[(~hand.ordinal()) & 1];
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack itemInOtherHand = player.getItemInHand(otherHand);

        HammerRecipe.HammerInv hammerInv = new HammerRecipe.HammerInv(itemInOtherHand);
        Optional<RecipeHolder<HammerRecipe>> recipe = level.getRecipeManager().getRecipeFor(CDGRecipes.HAMMERING.getType(), hammerInv, level);
        if (recipe.isPresent()) {
            ItemStack processingItem = itemInOtherHand.copy();
            itemInOtherHand.shrink(1);
            processingItem.setCount(1);

            itemInHand.set(CDGDataComponents.PROCESSING_ITEM,  new SandPaperItemComponent(processingItem));
            player.startUsingItem(hand);
            return InteractionResultHolder.success(itemInHand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player))
            return stack;
        synchronized ("hammer_release") {
            if (!stack.has(CDGDataComponents.PROCESSING_ITEM))
                return stack;
            ItemStack processingItem = stack.get(CDGDataComponents.PROCESSING_ITEM).item();

            HammerRecipe.HammerInv hammerInv = new HammerRecipe.HammerInv(processingItem);
            Optional<RecipeHolder<HammerRecipe>> recipe = level.getRecipeManager().getRecipeFor(CDGRecipes.HAMMERING.getType(), hammerInv, level);

            stack.remove(CDGDataComponents.PROCESSING_ITEM);

            if (recipe.isEmpty()) {
                player.getInventory().placeItemBackInInventory(processingItem);
                return stack;
            }
            recipe.get().value().rollResults(level.random);
            player.getInventory().placeItemBackInInventory(recipe.get().value().assemble(hammerInv, level.registryAccess()).copy());
            if (level instanceof ServerLevel sl)
                stack.hurtAndBreak(1, sl, player, i -> {});
            return stack;
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int tick) {
        if (AnimationTickHolder.getTicks() % 10 == 0) {
            level.playLocalSound(entity.xo, entity.yo, entity.zo, SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.3f, 1f, true);

            if (!stack.has(CDGDataComponents.PROCESSING_ITEM)) {
                super.onUseTick(level, entity, stack, tick);
                return;
            }

            ItemStack processingItem = stack.get(CDGDataComponents.PROCESSING_ITEM).item();

            for (int i = 0; i < 30; i++) {
                Vec3 offset = VecHelper.offsetRandomly(entity.position().add(Math.sin(-entity.getYRot() / 180 * Math.PI) / 2, 1.3, Math.cos(-entity.getYRot() / 180 * Math.PI) / 2), level.getRandom(), .3f);
                Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, level.getRandom(), .1f);

                level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, processingItem), offset.x(), offset.y(),
                        offset.z(), motion.x(), motion.y(), motion.z());
            }
        }
        super.onUseTick(level, entity, stack, tick);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int tick) {
        synchronized ("hammer_release") {
            if (!(entity instanceof Player player))
                return;
            if (!stack.has(CDGDataComponents.PROCESSING_ITEM))
                return;

            ItemStack processingItem = stack.get(CDGDataComponents.PROCESSING_ITEM).item();
            player.getInventory().placeItemBackInInventory(processingItem);
            stack.remove(CDGDataComponents.PROCESSING_ITEM);
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
       releaseUsing(stack, entity.level(), entity, 0);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 90;
    }

    @OnlyIn(Dist.CLIENT)
    public void registerExtension(RegisterClientExtensionsEvent event) {
        event.registerItem(SimpleCustomRenderer.create(this, new HammerItemRenderer()), this);
    }

}
