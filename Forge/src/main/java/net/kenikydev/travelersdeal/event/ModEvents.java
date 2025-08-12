package net.kenikydev.travelersdeal.event;

import net.kenikydev.travelersdeal.TravelersDeal;
import net.kenikydev.travelersdeal.entity.ModEntities;
import net.kenikydev.travelersdeal.entity.custom.TravelerEntity;
import net.kenikydev.travelersdeal.util.TravelerSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TravelersDeal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) return; // Solo servidor

        ServerLevel serverLevel = (ServerLevel) player.level();

        // Cargar o crear el archivo traveler_data.dat
        TravelerSavedData data = serverLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        TravelerSavedData::new,
                        TravelerSavedData::load,null
                ),
                "traveler_data"
        );

        // Si el jugador nunca ha visto al Traveler, lo spawneamos
        if (!data.hasSeenTraveler(player.getUUID())) {
            data.setSeenTraveler(player.getUUID());

            TravelerEntity traveler = ModEntities.TRAVELER.get().create(serverLevel);
            if (traveler != null) {
                traveler.moveTo(player.getX() + 2, player.getY(), player.getZ() + 2, 0, 0);
                serverLevel.addFreshEntity(traveler);
            }
        }
    }
}
