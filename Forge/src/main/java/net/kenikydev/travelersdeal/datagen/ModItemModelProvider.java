package net.kenikydev.travelersdeal.datagen;

import net.kenikydev.travelersdeal.TravelersDeal;
import net.kenikydev.travelersdeal.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, TravelersDeal.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        registerSpawningEgg(ModItems.TRAVELER_SPAWN_EGG);

    }

    private void registerSpawningEgg(RegistryObject<? extends Item> spawnEggItem) {
        this.withExistingParent(spawnEggItem.getId().getPath(), modLoc("item/spawn_egg"));
    }
}
