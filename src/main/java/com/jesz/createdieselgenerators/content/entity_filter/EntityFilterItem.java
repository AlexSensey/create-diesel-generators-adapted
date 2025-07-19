package com.jesz.createdieselgenerators.content.entity_filter;

import com.jesz.createdieselgenerators.CDGDataComponents;
import com.jesz.createdieselgenerators.CDGMenuTypes;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntityFilterItem extends Item {
    public EntityFilterItem(Properties properties) {
        super(properties);
    }
    static List<EntityAttribute.EntityAttributeEntry> getEntries(ItemStack stack){
        return stack.getOrDefault(CDGDataComponents.ENTITY_FILTER_MATCHED_ATTRIBUTES, Collections.emptyList());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        if (AllKeys.shiftDown())
            return;
        List<Component> makeSummary = makeSummary(stack);
        if (makeSummary.isEmpty())
            return;
        tooltipComponents.add(CommonComponents.SPACE);
        tooltipComponents.addAll(makeSummary);
    }

    private List<Component> makeSummary(ItemStack stack) {
        List<Component> list = new ArrayList<>();

        AttributeFilterWhitelistMode whitelistMode = stack.get(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE);

        list.add((whitelistMode == AttributeFilterWhitelistMode.WHITELIST_CONJ
                ? CreateLang.translateDirect("gui.attribute_filter.allow_list_conjunctive")
                : whitelistMode == AttributeFilterWhitelistMode.WHITELIST_DISJ
                ? CreateLang.translateDirect("gui.attribute_filter.allow_list_disjunctive")
                : CreateLang.translateDirect("gui.attribute_filter.deny_list")).withStyle(ChatFormatting.GOLD));

        int count = 0;

        for (EntityAttribute.EntityAttributeEntry entry : getEntries(stack)) {


            if (count > 5) {
                list.add(Component.literal("- ...")
                        .withStyle(ChatFormatting.DARK_GRAY));
                break;
            }
            list.add(Component.literal("- ")
                    .append(entry.attribute().format(entry.inverted())));
            count++;
        }

        if (count == 0)
            return Collections.emptyList();


        return list;
    }

    public static boolean test(ItemStack stack, Entity entity) {

        AttributeFilterWhitelistMode whitelistMode = stack.getOrDefault(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);
        AtomicBoolean passed = new AtomicBoolean(false);
        if (whitelistMode == AttributeFilterWhitelistMode.WHITELIST_CONJ)
            passed.set(true);
        getEntries(stack).forEach((entry) -> {
            boolean currentAttributePassed = entry.attribute().test(entity) ^ entry.inverted();
            if (whitelistMode != AttributeFilterWhitelistMode.WHITELIST_CONJ)
                if (!passed.get())
                    passed.set(currentAttributePassed);
            if (whitelistMode == AttributeFilterWhitelistMode.WHITELIST_CONJ)
                passed.set(currentAttributePassed && passed.get());
        });
        return passed.get()^whitelistMode == AttributeFilterWhitelistMode.BLACKLIST;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (player.isShiftKeyDown() || hand != InteractionHand.MAIN_HAND)
            return InteractionResultHolder.pass(stackInHand);
        if (level.isClientSide || !(player instanceof ServerPlayer sp))
            return InteractionResultHolder.success(stackInHand);

        sp.openMenu(new SimpleMenuProvider((int id, Inventory inventory, Player player1) ->
                new EntityFilterMenu(CDGMenuTypes.ENTITY_FILTER.get(), id, inventory, player1.getMainHandItem()), getDescription()), buf -> {
            ItemStack.STREAM_CODEC.encode(buf, stackInHand);
        });
        return InteractionResultHolder.success(stackInHand);
    }

}
