package net.kenikydev.travelersdeal.item;

import net.kenikydev.travelersdeal.TravelersDeal;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TravelersDeal.MODID);

    public static final RegistryObject<CreativeModeTab>TRAVELERSDEAL_TAB =CREATIVE_MODE_TAB.register("travelersdeal_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Items.ACACIA_WOOD))
                    .title(Component.translatable("creativetab.travelersdeal_tab"))
                    .displayItems((pParameters,pOutput) ->{
                        pOutput.accept(ModItems.TRAVELER_SPAWN_EGG.get());
                    }).build());

    public static void register(IEventBus bus) { CREATIVE_MODE_TAB.register(bus); }
}
