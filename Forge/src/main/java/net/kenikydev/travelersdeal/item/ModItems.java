package net.kenikydev.travelersdeal.item;

import net.kenikydev.travelersdeal.TravelersDeal;
import net.kenikydev.travelersdeal.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TravelersDeal.MODID);

    public static final RegistryObject<Item> TRAVELER_SPAWN_EGG = ITEMS.register("traveler_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.TRAVELER, 0x53524b, 0xdac741, new Item.Properties()));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
