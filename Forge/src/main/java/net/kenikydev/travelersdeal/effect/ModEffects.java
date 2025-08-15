package net.kenikydev.travelersdeal.effect;

import net.kenikydev.travelersdeal.TravelersDeal;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TravelersDeal.MODID);

    public static final RegistryObject<MobEffect> REQUEST_EFFECT =
            MOB_EFFECTS.register("request_effect",
                    () -> new RequestEffect(MobEffectCategory.BENEFICIAL, 0xFFD700)
            );

    public static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
    }
}
