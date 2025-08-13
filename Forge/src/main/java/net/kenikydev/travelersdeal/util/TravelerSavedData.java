package net.kenikydev.travelersdeal.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.BlockPos;

import java.util.*;

public class TravelerSavedData extends SavedData {

    //Datos por jugador
    private final Map<UUID, BlockPos> playersHomes = new HashMap<>();
    private final Map<UUID, Integer> playersKarma = new HashMap<>();
    private final Map<UUID, PendingRequest> pendingRequests = new HashMap<>();
    private final Set<UUID> seenPlayers = new HashSet<>();

    //Constructor vacío para datos nuevos
    public TravelerSavedData() {
    }

    public record PendingRequest(Item item, int amount, long expireTime) {
    }

    //Cargar desde NBT
    public static TravelerSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        TravelerSavedData data = new TravelerSavedData();

        long[] seenArray = tag.getLongArray("SeenPlayers");
        for (int i = 0; i < seenArray.length; i += 2) {
            data.seenPlayers.add(new UUID(seenArray[i], seenArray[i + 1]));
        }

        //Casa
        ListTag homesList = tag.getList("Homes", ListTag.TAG_COMPOUND);
        for (Tag t : homesList) {
            CompoundTag homeTag = (CompoundTag) t;
            UUID uuid = homeTag.getUUID("Player");
            BlockPos pos = new BlockPos(homeTag.getInt("X"), homeTag.getInt("Y"), homeTag.getInt("Z"));
            data.playersHomes.put(uuid, pos);
        }

        //Karma
        ListTag karmaList = tag.getList("Karma", ListTag.TAG_COMPOUND);
        for (Tag t : karmaList) {
            CompoundTag karmaTag = (CompoundTag) t;
            data.playersKarma.put(karmaTag.getUUID("Player"), karmaTag.getInt("Amount"));
        }

        //Pedidos
        ListTag requestList = tag.getList("Requests", ListTag.TAG_COMPOUND);
        for (Tag t : requestList) {
            CompoundTag requestTag = (CompoundTag) t;
            ResourceLocation itemId = ResourceLocation.parse(requestTag.getString("Item"));
            PendingRequest request = new PendingRequest(
                    BuiltInRegistries.ITEM.get(itemId),
                    requestTag.getInt("Amount"),
                    requestTag.getLong("ExpireTime")
            );
            data.pendingRequests.put(requestTag.getUUID("Player"), request);
        }
        return data;
    }

    //Guardar a NBT
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {

        long[] seenArray = new long[seenPlayers.size() * 2];
        int i = 0;
        for (UUID uuid : seenPlayers) {
            seenArray[i++] = uuid.getMostSignificantBits();
            seenArray[i++] = uuid.getLeastSignificantBits();
        }
        tag.putLongArray("SeenPlayers", seenArray);

        ListTag homesList = new ListTag();
        for (Map.Entry<UUID, BlockPos> e : playersHomes.entrySet()) {
            CompoundTag homeTag = new CompoundTag();
            homeTag.putUUID("Player", e.getKey());
            homeTag.putInt("X", e.getValue().getX());
            homeTag.putInt("Y", e.getValue().getY());
            homeTag.putInt("Z", e.getValue().getZ());
            homesList.add(homeTag);
        }
        tag.put("Homes", homesList);

        ListTag karmaList = new ListTag();
        for (Map.Entry<UUID, Integer> e : playersKarma.entrySet()) {
            CompoundTag kTag = new CompoundTag();
            kTag.putUUID("Player", e.getKey());
            kTag.putInt("Value", e.getValue());
            karmaList.add(kTag);
        }
        tag.put("Karma", karmaList);

        ListTag requestList = new ListTag();
        for (Map.Entry<UUID, PendingRequest> e : pendingRequests.entrySet()) {
            CompoundTag rTag = new CompoundTag();
            rTag.putUUID("Player", e.getKey());
            rTag.putString("Item", BuiltInRegistries.ITEM.getKey(e.getValue().item()).toString());
            rTag.putInt("Amount", e.getValue().amount());
            rTag.putLong("ExpireTime", e.getValue().expireTime());
            requestList.add(rTag);
        }
        tag.put("Requests", requestList);

        return tag;
    }

    // Accesos
    public static TravelerSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(TravelerSavedData::new, TravelerSavedData::load, null),
                "traveler_data"
        );
    }

    public boolean hasSeenTraveler(UUID playerId) {
        return seenPlayers.contains(playerId);
    }

    public void setSeenTraveler(UUID playerId, boolean seen) {
        if (seen) seenPlayers.add(playerId);
        setDirty();
    }

    public void setHomePos(UUID playerId, BlockPos pos) {
        playersHomes.put(playerId, pos);
        setDirty();
    }

    public BlockPos getHomePos(UUID playerId) {
        return playersHomes.get(playerId);
    }

    public void setKarma(UUID playerId, int value) {
        playersKarma.put(playerId, value);
        setDirty();
    }

    public int getKarma(UUID playerId) {
        return playersKarma.getOrDefault(playerId, 0);
    }

    public void setPendingRequest(UUID playerId, Item item, int amount, long expireTime) {
        pendingRequests.put(playerId, new PendingRequest(item, amount, expireTime));
        setDirty();
    }

    public PendingRequest getPendingRequest(UUID playerId) {
        return pendingRequests.get(playerId);
    }

    public boolean hasPendingRequest(UUID playerId) {
        return pendingRequests.containsKey(playerId);
    }

    public void clearPendingRequest(UUID playerId) {
        pendingRequests.remove(playerId);
        setDirty();
    }
}
