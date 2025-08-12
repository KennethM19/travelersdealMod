package net.kenikydev.travelersdeal.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TravelerSavedData extends SavedData {
    private final Set<UUID> playersWithTraveler = new HashSet<>();

    // Constructor vacío para datos nuevos
    public TravelerSavedData() {}

    // Cargar desde NBT
    public static TravelerSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        TravelerSavedData data = new TravelerSavedData();
        if (tag.contains("Players")) {
            long[] longs = tag.getLongArray("Players");
            for (int i = 0; i < longs.length; i += 2) {
                data.playersWithTraveler.add(new UUID(longs[i], longs[i + 1]));
            }
        }
        return data;
    }

    // Guardar a NBT
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        long[] longs = new long[playersWithTraveler.size() * 2];
        int i = 0;
        for (UUID uuid : playersWithTraveler) {
            longs[i++] = uuid.getMostSignificantBits();
            longs[i++] = uuid.getLeastSignificantBits();
        }
        tag.putLongArray("Players", longs);
        return tag;
    }

    public boolean hasSeenTraveler(UUID playerId) {
        return playersWithTraveler.contains(playerId);
    }

    public void setSeenTraveler(UUID playerId) {
        playersWithTraveler.add(playerId);
        setDirty();
    }
}
