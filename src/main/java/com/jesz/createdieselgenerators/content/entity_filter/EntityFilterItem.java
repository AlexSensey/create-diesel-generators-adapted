package com.jesz.createdieselgenerators.content.entity_filter;

import com.jesz.createdieselgenerators.CDGMenuTypes;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntityFilterItem extends Item {
    public EntityFilterItem(Properties properties) {
        super(properties);
    }
    static Map<EntityAttribute, CompoundTag> getEntries(ItemStack stack){
        Map<EntityAttribute, CompoundTag> entries = new HashMap<>();
        CompoundTag tag = stack.getTag();
        if(tag == null)
            return entries;
        NBTHelper.iterateCompoundList(tag.getList("MatchedAttributes", Tag.TAG_COMPOUND), t -> {
            EntityAttribute attribute = EntityAttribute.fromNBT(t);
            if (attribute != null)
                entries.put(attribute, t);

        });
        return entries;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (AllKeys.shiftDown())
            return;
        List<Component> makeSummary = makeSummary(stack);
        if (makeSummary.isEmpty())
            return;
        tooltip.add(CommonComponents.SPACE);
        tooltip.addAll(makeSummary);
    }

    private List<Component> makeSummary(ItemStack filter) {
        List<Component> list = new ArrayList<>();
        if (!filter.hasTag())
            return list;

        AttributeFilterMenu.WhitelistMode whitelistMode = AttributeFilterMenu.WhitelistMode.values()[filter.getOrCreateTag()
                .getInt("WhitelistMode")];
        list.add((whitelistMode == AttributeFilterMenu.WhitelistMode.WHITELIST_CONJ
                ? CreateLang.translateDirect("gui.attribute_filter.allow_list_conjunctive")
                : whitelistMode == AttributeFilterMenu.WhitelistMode.WHITELIST_DISJ
                ? CreateLang.translateDirect("gui.attribute_filter.allow_list_disjunctive")
                : CreateLang.translateDirect("gui.attribute_filter.deny_list")).withStyle(ChatFormatting.GOLD));

        int count = 0;
        ListTag attributes = filter.getOrCreateTag()
                .getList("MatchedAttributes", Tag.TAG_COMPOUND);
        for (Tag inbt : attributes) {
            CompoundTag compound = (CompoundTag) inbt;
            EntityAttribute attribute = EntityAttribute.fromNBT(compound);
            if (attribute == null)
                continue;
            boolean inverted = compound.getBoolean("Inverted");
            if (count > 3) {
                list.add(Component.literal("- ...")
                        .withStyle(ChatFormatting.DARK_GRAY));
                break;
            }
            list.add(Component.literal("- ")
                    .append(attribute.format(inverted)));
            count++;
        }

        if (count == 0)
            return Collections.emptyList();


        return list;
    }

    public static boolean test(ItemStack stack, Entity entity){
        CompoundTag tag = stack.getTag();
        if(tag == null)
            return false;
        AtomicBoolean passed = new AtomicBoolean(false);
        if(tag.getInt("Whitelist") == 1)
            passed.set(true);
        getEntries(stack).forEach((entry, data) -> {
            boolean currentAttributePassed = entry.test(entity)^data.getBoolean("Inverted");
            if(tag.getInt("Whitelist") != 1)
                if(!passed.get())
                    passed.set(currentAttributePassed);
            if(tag.getInt("Whitelist") == 1)
                passed.set(currentAttributePassed && passed.get());
        });
        return passed.get()^tag.getInt("Whitelist") == 2;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if(level.isClientSide)
            return InteractionResultHolder.consume(stackInHand);
        NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((int id, Inventory inventory, Player player1) ->
                new EntityFilterMenu(CDGMenuTypes.ENTITY_FILTER.get(), id, inventory, stackInHand), getDescription()), buf -> {
            buf.writeItem(stackInHand);
        });
        return InteractionResultHolder.success(stackInHand);
    }

}
