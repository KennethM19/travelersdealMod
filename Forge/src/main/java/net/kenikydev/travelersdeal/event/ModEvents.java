package net.kenikydev.travelersdeal.event;

import net.kenikydev.travelersdeal.TravelersDeal;
import net.kenikydev.travelersdeal.entity.ModEntities;
import net.kenikydev.travelersdeal.entity.custom.TravelerEntity;
import net.kenikydev.travelersdeal.util.TravelerSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = TravelersDeal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) player.level();
        TravelerSavedData data = TravelerSavedData.get(serverLevel);
        UUID playerId = player.getUUID();

        if (!data.hasSeenTraveler(playerId)) {
            data.setSeenTraveler(playerId, true);
            data.setHomePos(playerId, player.blockPosition());
            spawnTravelerNearPlayer(serverLevel, player, true);
        }
        /*else if (data.hasPendingRequest(playerId)) {
            BlockPos home = data.getHomePos(playerId);
            if (home != null) {
                spawnTravelerAtHome(serverLevel, home, playerId, data);
            }
        }*/

    }

    private static void spawnTravelerNearPlayer(ServerLevel serverLevel, Player player, boolean newRequest) {
        TravelerEntity traveler = ModEntities.TRAVELER.get().create(serverLevel);
        if (traveler != null) {
            traveler.moveTo(player.getX() + 2, player.getY(), player.getZ() + 2, 0, 0);
            serverLevel.addFreshEntity(traveler);
            if (newRequest) {
                traveler.assignRequest(player.getUUID(), Items.APPLE, 5, 20 * 60 * 3); // 3 min
            }
        }
    }

    private static void spawnTravelerAtHome(ServerLevel serverLevel, BlockPos homePos, UUID playerId, TravelerSavedData data) {
        TravelerEntity traveler = ModEntities.TRAVELER.get().create(serverLevel);
        if (traveler != null) {
            traveler.moveTo(homePos.getX() + 2, homePos.getY(), homePos.getZ() + 2, 0, 0);
            var req = data.getPendingRequest(playerId);
            if (req != null) {
                traveler.assignRequest(playerId, req.item(), req.amount(), req.expireTime() - serverLevel.getGameTime());
            }
            serverLevel.addFreshEntity(traveler);
        }
    }
}
