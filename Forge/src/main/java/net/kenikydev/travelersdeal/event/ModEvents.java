package net.kenikydev.travelersdeal.event;

import net.kenikydev.travelersdeal.TravelersDeal;
import net.kenikydev.travelersdeal.entity.custom.TravelerEntity;
import net.kenikydev.travelersdeal.util.TravelerSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = TravelersDeal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) player.level();
        TravelerSavedData data = TravelerSavedData.get(serverLevel);
        UUID playerId = player.getUUID();

        BlockPos spawnPos;
        boolean newRequest;

        if (!data.hasSeenTraveler(playerId)) {
            spawnPos = player.blockPosition().offset(2, 0, 2);
            data.setSeenTraveler(playerId, true);
            data.setHomePos(playerId, player.blockPosition());
            newRequest = true;
        } else {
            BlockPos homePos = data.getHomePos(playerId);
            spawnPos = homePos != null ? homePos : serverLevel.getSharedSpawnPos();
            newRequest = false;
        }

        TravelerEntity.spawnTraveler(serverLevel, spawnPos, playerId, data, newRequest);
    }

    @SubscribeEvent
    public static void onPlayerSleepInBed(PlayerSleepInBedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) player.level();
        TravelerSavedData data = TravelerSavedData.get(serverLevel);

        BlockPos bedPos = event.getPos();

        if (bedPos != null) {
            data.setHomePos(player.getUUID(), bedPos);
        }
    }
}
