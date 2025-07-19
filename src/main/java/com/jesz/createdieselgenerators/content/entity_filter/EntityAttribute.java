package com.jesz.createdieselgenerators.content.entity_filter;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.*;
import java.util.function.Predicate;

public interface EntityAttribute {
    Codec<EntityAttribute> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(EntityAttribute::getId),
            CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(EntityAttribute::write)
    ).apply(i,  (id, data) -> {
        EntityAttribute attribute = EntityAttribute.getById(id);
        if (attribute == null)
            return null;
        return attribute.read(data);
    }));

    StreamCodec<ByteBuf, EntityAttribute> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, EntityAttribute::getId,
            ByteBufCodecs.COMPOUND_TAG, EntityAttribute::write,
            (id, data) -> {
                EntityAttribute attribute = EntityAttribute.getById(id);
                if (attribute == null)
                    return null;
                return attribute.read(data);
            }
    );

    List<EntityAttribute> all = new LinkedList<>();
    EntityAttribute STANDARD_TRAITS = register(StandardTraits.IS_HOSTILE);
    EntityAttribute IS_MOB = register(new IsMob(EntityType.PIG));

    static EntityAttribute register(EntityAttribute attribute) {
        all.add(attribute);
        return attribute;
    }

    static EntityAttribute getById(ResourceLocation id) {
        for (EntityAttribute attribute : all){
            if(attribute.getId().equals(id))
                return attribute;
        }
        return null;
    }

    ResourceLocation getId();

    boolean test(Entity entity);

    EntityAttribute read(CompoundTag tag);

    default CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", getId().toString());
        return tag;
    }

    List<EntityAttribute> listAttributesOf(ItemStack stack);

    boolean appliesTo(ItemStack stack);

    Component format(boolean inverted);

    default EntityAttribute register() {
        all.add(this);
        return this;
    }

    static List<EntityType<?>> getAllEntityTypesFromStack(ItemStack stack) {
        List<EntityType<?>> list = new LinkedList();
        if (stack.getItem() instanceof SpawnEggItem item)
            list.add(item.getType(stack));

        for (Map.Entry<Item, List<EntityType<?>>> entry : ReverseLootTable.ALL.entrySet()){
            if(entry.getKey() == stack.getItem())
                list.addAll(entry.getValue());
        }
        return list;
    }

    enum StandardTraits implements EntityAttribute {
        IS_HOSTILE(e -> e instanceof Monster, stack -> {
            boolean hostile = false;
            for (EntityType<?> type : getAllEntityTypesFromStack(stack)) {
                if (type.getCategory() == MobCategory.MONSTER)
                    hostile = true;
            }
            return hostile;
        });
        Predicate<ItemStack> itemTest;
        Predicate<Entity> test;
        StandardTraits(Predicate<Entity> test, Predicate<ItemStack> itemTest) {
            this.test = test;
            this.itemTest = itemTest;
        }

        @Override
        public ResourceLocation getId() {
            return CreateDieselGenerators.rl(name().toLowerCase(Locale.ROOT));
        }

        @Override
        public boolean appliesTo(ItemStack stack) {
            return itemTest.test(stack);
        }

        @Override
        public boolean test(Entity entity) {
            return test.test(entity);
        }

        @Override
        public CompoundTag write() {
            CompoundTag tag = EntityAttribute.super.write();
            tag.putString("TraitType", getId().toString());
            return tag;
        }

        @Override
        public EntityAttribute read(CompoundTag tag) {
            EntityAttribute attribute = null;
            for(EntityAttribute possibleAttribute : values()){
                if(possibleAttribute.getId().toString().equals(tag.getString("TraitType")))
                    attribute = possibleAttribute;
            }
            return attribute;
        }

        @Override
        public List<EntityAttribute> listAttributesOf(ItemStack stack) {
            List<EntityAttribute> attributes = new LinkedList<>();
            for(StandardTraits attribute : values()){
                if(attribute.itemTest.test(stack))
                    attributes.add(attribute);
            }
            return attributes;
        }

        @Override
        public Component format(boolean inverted) {
            return CreateDieselGenerators.lang("entity_attributes."+getId().getPath()+(inverted ? ".inverted" : ""));
        }
    }

    class IsMob implements EntityAttribute {

        EntityType<?> type;
        public IsMob(EntityType<?> type){
            this.type = type;
        }
        @Override
        public ResourceLocation getId() {
            return CreateDieselGenerators.rl("is_mob");
        }

        @Override
        public boolean test(Entity entity) {
            return entity.getType() == type;
        }

        @Override
        public CompoundTag write() {
            CompoundTag tag = EntityAttribute.super.write();
            tag.putString("Entity", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
            return tag;
        }
        @Override
        public EntityAttribute read(CompoundTag tag) {
            return new IsMob(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(tag.getString("Entity"))));
        }

        @Override
        public List<EntityAttribute> listAttributesOf(ItemStack stack) {
            List<EntityAttribute> attributes = new LinkedList<>();
            for (EntityType<?> type : getAllEntityTypesFromStack(stack))
                attributes.add(new IsMob(type));
            return attributes;
        }

        @Override
        public boolean appliesTo(ItemStack stack) {
            return !getAllEntityTypesFromStack(stack).isEmpty();
        }

        @Override
        public Component format(boolean inverted) {
            return CreateDieselGenerators.lang("entity_attributes.is_mob"+(inverted ? ".inverted" : ""), type.getDescription());
        }
    }

    record EntityAttributeEntry(EntityAttribute attribute, boolean inverted) {
        public static final Codec<EntityAttribute.EntityAttributeEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                EntityAttribute.CODEC.fieldOf("attribute").forGetter(EntityAttributeEntry::attribute),
                Codec.BOOL.fieldOf("inverted").forGetter(EntityAttributeEntry::inverted)
        ).apply(i, EntityAttribute.EntityAttributeEntry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, EntityAttributeEntry> STREAM_CODEC = StreamCodec.composite(
                EntityAttribute.STREAM_CODEC, EntityAttributeEntry::attribute,
                ByteBufCodecs.BOOL, EntityAttributeEntry::inverted,
                EntityAttributeEntry::new
        );

        @Override
        public int hashCode() {
            return Objects.hash(inverted, attribute.write());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EntityAttributeEntry entry))
                return false;
            if (entry.inverted != inverted())
                return false;
            return entry.attribute.write().equals(attribute.write());
        }
    }
}
