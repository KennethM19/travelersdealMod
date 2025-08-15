package net.kenikydev.travelersdeal.util;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TravelerRequest {
    public static final Map<int[], List<RequestOption>> REQUEST_POOLS = new HashMap<>();
    public static final Map<int[], List<RewardsOptions>> REWARD_POOLS = new HashMap<>();
    public static final Map<int[], List<HostileOptions>> HOSTILE_POOLS = new HashMap<>();

    static {
        // 0 - 10 días
        REQUEST_POOLS.put(new int[]{0, 10}, Arrays.asList(
                new RequestOption(Items.APPLE, 3, 6),
                new RequestOption(Items.OAK_LOG, 15, 25),
                new RequestOption(Items.COAL, 8, 15)
        ));

        // 11 - 20 días
        REQUEST_POOLS.put(new int[]{11, 20}, Arrays.asList(
                new RequestOption(Items.IRON_INGOT, 10, 18),
                new RequestOption(Items.GOLD_INGOT, 8, 14),
                new RequestOption(Items.POTATO, 12, 20)
        ));

        // 21 - 30 días
        REQUEST_POOLS.put(new int[]{21, 30}, Arrays.asList(
                new RequestOption(Items.DIAMOND, 4, 8),
                new RequestOption(Items.EMERALD, 6, 10),
                new RequestOption(Items.BREAD, 15, 25)
        ));

        // Más rangos...
    }

    static {
        REWARD_POOLS.put(new int[]{0, 15}, Arrays.asList(
                new RewardsOptions(Items.STONE_PICKAXE, 1),
                new RewardsOptions(Items.STONE_AXE,1),
                new RewardsOptions(Items.FURNACE, 1)
        ));
        REWARD_POOLS.put(new int[]{16, 40}, Arrays.asList(
                new RewardsOptions(Items.IRON_PICKAXE, 1),
                new RewardsOptions(Items.IRON_BLOCK,3),
                new RewardsOptions(Items.COOKED_BEEF, 7)
        ));
        REWARD_POOLS.put(new int[]{41, 60}, Arrays.asList(
                new RewardsOptions(Items.GOLDEN_APPLE, 2),
                new RewardsOptions(Items.DIAMOND_HELMET,1),
                new RewardsOptions(Items.DIAMOND_AXE, 1)
        ));
    }

    static {
        HOSTILE_POOLS.put(new int[]{-15, -5}, Arrays.asList(
                new HostileOptions(EntityType.ZOMBIE, 1, 3),
                new HostileOptions(EntityType.SKELETON, 1, 2)
        ));
        HOSTILE_POOLS.put(new int[]{-40, -16}, Arrays.asList(
                new HostileOptions(EntityType.SKELETON, 2, 4),
                new HostileOptions(EntityType.SPIDER, 1, 3)
        ));
        HOSTILE_POOLS.put(new int[]{-60, -41}, Arrays.asList(
                new HostileOptions(EntityType.CREEPER, 1, 2),
                new HostileOptions(EntityType.ENDERMAN, 1, 1)
        ));
    }

    public static class RequestOption {
        public final Item item;
        public final int minAmount;
        public final int maxAmount;

        public RequestOption(Item item, int minAmount, int maxAmount) {
            this.item = item;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }
    }

    public static class RewardsOptions {
        public final Item item;
        public final int amount;

        public RewardsOptions(Item item, int amount) {
            this.item = item;
            this.amount = amount;
        }
    }

    public static class HostileOptions {
        public final EntityType<? extends Monster> mobType;
        public final int minCount;
        public final int maxCount;

        public HostileOptions(EntityType<? extends Monster> mobType, int minCount, int maxCount) {
            this.mobType = mobType;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }
    }
}
