package net.kenikydev.travelersdeal.event;

import net.kenikydev.travelersdeal.TravelersDeal;
import net.kenikydev.travelersdeal.entity.custom.TravelerEntity;
import net.kenikydev.travelersdeal.util.TravelerSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
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

        if (!data.hasSeenTraveler(playerId)) {
            spawnPos = player.blockPosition().offset(2, 0, 2);
            data.setSeenTraveler(playerId, true);
            data.setHomePos(playerId, player.blockPosition());
            TravelerEntity.spawnTraveler(serverLevel, spawnPos, playerId, data, true);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Level level = event.level;
        if (level.isClientSide()) return;

        if (!(level instanceof ServerLevel serverLevel)) return;

        TravelerSavedData data = TravelerSavedData.get(serverLevel);

        for (ServerPlayer player : serverLevel.players()) {
            UUID playerId = player.getUUID();
            long gameTime = serverLevel.getDayTime();

            boolean travelerExists = !serverLevel.getEntitiesOfClass(
                    TravelerEntity.class,
                    new AABB(player.blockPosition()).inflate(200)
            ).isEmpty();

            if (travelerExists) continue;

            long nextSpawnTick = data.getNextTravelerSpawnTime();
            if (nextSpawnTick != 0 && gameTime >= nextSpawnTick) {
                BlockPos homePos = data.getHomePos(playerId);
                if (homePos == null) homePos = serverLevel.getSharedSpawnPos();

                TravelerEntity.spawnTraveler(serverLevel, homePos, playerId, data, false);
                data.setNextTravelerSpawnTime(0);
            }
        }
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
