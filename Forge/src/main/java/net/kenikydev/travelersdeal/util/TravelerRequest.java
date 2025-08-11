package net.kenikydev.travelersdeal.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Random;

public class TravelerRequest {
    private final ItemStack itemStack;

    public TravelerRequest(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static TravelerRequest generatedBasedOnProgress(Player player) {
        Random rand = new Random();
        int stage = player.experienceLevel/10;
        ItemStack request;

        if (stage < 2) {
            request = new ItemStack(Items.OAK_LOG, 5);
        } else if (stage < 4) {
            request = new ItemStack(Items.IRON_INGOT, 3);
        } else {
            request = new ItemStack(Items.DIAMOND, 1);
        }

        return new TravelerRequest(request);
    }

    public String getItemName() {
        return itemStack.getHoverName().getString();
    }

    public ItemStack getItemStack() {
        return itemStack.copy();
    }

    public ItemStack getRewardForKarma(int karma) {
        if (karma >= 3) {
            return new ItemStack(Items.NETHERITE_SCRAP, 1);
        } else if (karma >= 0) {
            return new ItemStack(Items.GOLDEN_APPLE, 1);
        } else {
            return new ItemStack(Items.ROTTEN_FLESH, 5); // castigo
        }
    }

    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ItemId", BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());
        tag.putInt("Count", itemStack.getCount());
        return tag;
    }

    public static TravelerRequest loadFromNBT(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("ItemId"));
        if (id == null) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) {
            return null; // Evitar errores si no existe
        }
        int count = tag.getInt("Count");
        return new TravelerRequest(new ItemStack(item, count));
    }
}
