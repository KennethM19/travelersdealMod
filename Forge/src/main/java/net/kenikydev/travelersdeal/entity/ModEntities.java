package net.kenikydev.travelersdeal.entity;

import net.kenikydev.travelersdeal.TravelersDeal;
import net.kenikydev.travelersdeal.entity.custom.TravelerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
            ForgeRegistries.ENTITY_TYPES,
            TravelersDeal.MODID
    );

    public static final RegistryObject<EntityType<TravelerEntity>> TRAVELER = ENTITY_TYPES.register("traveler",
            () -> EntityType.Builder.of(TravelerEntity::new, MobCategory.CREATURE).sized(0.6f, 2f).build("traveler"));

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
