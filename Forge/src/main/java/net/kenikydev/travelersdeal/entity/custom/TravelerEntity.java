package net.kenikydev.travelersdeal.entity.custom;

import net.kenikydev.travelersdeal.effect.ModEffects;
import net.kenikydev.travelersdeal.entity.ModEntities;
import net.kenikydev.travelersdeal.util.TravelerRequest;
import net.kenikydev.travelersdeal.util.TravelerSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TravelerEntity extends PathfinderMob {

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private UUID targetPlayerUUID;
    private long expireTime;

    public TravelerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this)); //Flotar en agua
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2D)); //Huir en peligro
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F)); //Mirar a jugadores cercanos
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this)); //Girar la cabeza aleatoriamente
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35D) //Velocidad de movimiento
                .add(Attributes.ENTITY_INTERACTION_RANGE, 2.0D) //Rango de interaccion
                .add(Attributes.MAX_HEALTH, 20.0D); //Vida máxima
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            setupAnimationStates();
        } else {
            if (expireTime > 0 && level().getGameTime() > expireTime) {
                sendMessageToNearbyPlayers(Component.translatable("dialog.traveler.fail"));
                failRequest(5);
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;

        ServerLevel serverLevel = (ServerLevel) level();
        TravelerSavedData data = TravelerSavedData.get(serverLevel);
        ItemStack heldItem = player.getItemInHand(hand);

        var request = data.getPendingRequest(player.getUUID());

        if (request != null && heldItem.is(request.item())) {
            if (heldItem.getCount() >= request.amount()) {
                heldItem.shrink(request.amount());
                reward(player);
                return InteractionResult.SUCCESS;
            } else {
                sendMessageToNearbyPlayers(Component.translatable(
                        "dialog.traveler.need",
                        request.amount() - heldItem.getCount(),
                        request.item().getDescription()
                ));
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (!level().isClientSide && source.getEntity() instanceof Player) {
            sendMessageToNearbyPlayers(Component.translatable("dialog.traveler.hurt"));
            failRequest(10);
        }
        return result;
    }

    public static void spawnTraveler(ServerLevel serverLevel, BlockPos homePos, UUID playerId, TravelerSavedData data, boolean firstRequest) {
        long currentTime = serverLevel.getDayTime();
        TravelerEntity traveler = ModEntities.TRAVELER.get().create(serverLevel);

        if (currentTime < data.getNextTravelerSpawnTime()) {
            return;
        }

        boolean travelerExists = false;
        for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
            if (!level.getEntitiesOfClass(
                    TravelerEntity.class,
                    new AABB(
                            level.getWorldBorder().getMinX(), level.getMinBuildHeight(),
                            level.getWorldBorder().getMinZ(), level.getWorldBorder().getMaxX(),
                            level.getMaxBuildHeight(), level.getWorldBorder().getMaxZ()
                    ),
                    e -> true
            ).isEmpty()) {
                travelerExists = true;
                break;
            }
        }

        // Si ya hay uno, no hacer nada
        if (travelerExists) {
            return;
        }

        if (traveler != null) {
            BlockPos safePos = findSafeSpawnPos(serverLevel, homePos.offset(2, 0, 2), 5);
            traveler.moveTo(safePos, 0, 0);
            var req = data.getPendingRequest(playerId);
            if (firstRequest) {
                traveler.assignRequest(playerId, true);
            } else if (req == null) {
                traveler.assignRequest(playerId, false);
            }
            serverLevel.addFreshEntity(traveler);
        }
    }

    private void assignRequest(UUID playerId, Boolean firstRequest) {
        long daysPlayed = level().getDayTime() / 24000L;

        List<TravelerRequest.RequestOption> options = null;
        for (Map.Entry<int[], List<TravelerRequest.RequestOption>> entry : TravelerRequest.REQUEST_POOLS.entrySet()) {
            int[] range = entry.getKey();
            if (daysPlayed >= range[0] && daysPlayed <= range[1]) {
                options = entry.getValue();
                break;
            }
        }

        if (options == null) {
            options = TravelerRequest.REQUEST_POOLS.values().stream()
                    .reduce((first, second) -> second)
                    .orElse(Collections.emptyList());
        }

        if (options.isEmpty()) return;

        TravelerRequest.RequestOption chosen = options.get(level().random.nextInt(options.size()));

        int amount = chosen.minAmount + level().random.nextInt(chosen.maxAmount - chosen.minAmount + 1);

        long durationTicks = 20 * 60 * 3; // 3 minutos

        // Guardar datos del pedido
        this.targetPlayerUUID = playerId;
        this.expireTime = level().getGameTime() + durationTicks;
        TravelerSavedData.get((ServerLevel) level()).setPendingRequest(playerId, chosen.item, amount, expireTime);

        ServerLevel serverLevel = (ServerLevel) level();
        ServerPlayer serverPlayer = serverLevel.getServer().getPlayerList().getPlayer(playerId);
        if (serverPlayer != null) {
            serverPlayer.addEffect(new MobEffectInstance(
                    ModEffects.REQUEST_EFFECT.getHolder().get(),
                    (int) durationTicks,
                    0
            ));
        }

        MutableComponent message = firstRequest
                ? Component.translatable("dialog.traveler.welcome")
                : Component.empty();

        message = message.append(
                Component.translatable("dialog.traveler.need", amount, chosen.item.getDescription())
        );

        sendMessageToNearbyPlayers(message);

    }

    private void reward(Player player) {
        if (this.targetPlayerUUID != null) {
            ServerLevel serverLevel = (ServerLevel) level();
            TravelerSavedData data = TravelerSavedData.get(serverLevel);
            data.clearPendingRequest(targetPlayerUUID, serverLevel);

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.removeEffect(ModEffects.REQUEST_EFFECT.getHolder().get());
            }

            data.setKarma(targetPlayerUUID, data.getKarma(targetPlayerUUID) + 5);

            int karma = data.getKarma(targetPlayerUUID);

            List<TravelerRequest.RewardsOptions> rewardOptions = null;
            for (Map.Entry<int[], List<TravelerRequest.RewardsOptions>> entry : TravelerRequest.REWARD_POOLS.entrySet()) {
                int[] range = entry.getKey();
                if (karma >= range[0] && karma <= range[1]) {
                    rewardOptions = entry.getValue();
                    break;
                }
            }

            if (rewardOptions == null) {
                rewardOptions = TravelerRequest.REWARD_POOLS.values().stream()
                        .reduce((first, second) -> second)
                        .orElse(Collections.emptyList());
            }

            sendMessageToNearbyPlayers(Component.translatable("dialog.traveler.thanks"));

            if (!rewardOptions.isEmpty()) {
                TravelerRequest.RewardsOptions chosenReward = rewardOptions.get(level().random.nextInt(rewardOptions.size()));
                player.addItem(new ItemStack(chosenReward.item, chosenReward.amount));
            }

            this.discard();
        }
    }

    private void failRequest(int karma) {
        if (this.targetPlayerUUID != null) {
            ServerLevel serverLevel = (ServerLevel) level();
            TravelerSavedData data = TravelerSavedData.get(serverLevel);
            data.clearPendingRequest(targetPlayerUUID, serverLevel);

            ServerPlayer serverPlayer = serverLevel.getServer().getPlayerList().getPlayer(targetPlayerUUID);
            if (serverPlayer != null) {
                serverPlayer.removeEffect(ModEffects.REQUEST_EFFECT.getHolder().get());
            }

            data.setKarma(targetPlayerUUID, data.getKarma(targetPlayerUUID) - karma);
            spawnHostileMobs(data);
            this.discard();
        }
    }

    private void sendMessageToNearbyPlayers(Component message) {
        Component prefix = Component.translatable("dialog.traveler.traveler");
        this.level().players().forEach(p ->
                p.sendSystemMessage(prefix.copy().append(message))
        );
    }

    //Maneja animaciones
    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) { //Controla la animación de reposo
            this.idleAnimationTimeout = 40;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    private void spawnHostileMobs(TravelerSavedData data) {
        ServerLevel serverLevel = (ServerLevel) level();

        // Obtener karma actual del jugador
        int karma = data.getKarma(targetPlayerUUID);

        if (karma >= 0) {
            return;
        }
        // Buscar opciones según el karma
        List<TravelerRequest.HostileOptions> hostileOptions = null;
        for (Map.Entry<int[], List<TravelerRequest.HostileOptions>> entry : TravelerRequest.HOSTILE_POOLS.entrySet()) {
            int[] range = entry.getKey();
            if (karma >= range[0] && karma <= range[1]) {
                hostileOptions = entry.getValue();
                break;
            }
        }

        // Si no hay rango, usar el último
        if (hostileOptions == null) {
            hostileOptions = TravelerRequest.HOSTILE_POOLS.values().stream()
                    .reduce((first, second) -> second)
                    .orElse(Collections.emptyList());
        }

        // Si no hay nada, salir
        if (hostileOptions.isEmpty()) return;

        // Elegir mob aleatorio del pool
        TravelerRequest.HostileOptions chosen = hostileOptions.get(level().random.nextInt(hostileOptions.size()));

        // Determinar cantidad aleatoria dentro del rango
        int count = chosen.minCount + level().random.nextInt(chosen.maxCount - chosen.minCount + 1);

        // Spawnear
        for (int i = 0; i < count; i++) {
            Monster mob = chosen.mobType.create(serverLevel);
            if (mob != null) {
                mob.moveTo(getX() + level().random.nextDouble() * 3 - 1.5, getY(), getZ() + level().random.nextDouble() * 3 - 1.5);
                serverLevel.addFreshEntity(mob);
            }
        }
    }

    private static BlockPos findSafeSpawnPos(ServerLevel level, BlockPos basePos, int searchRadius) {
        BlockPos.MutableBlockPos pos = basePos.mutable();

        // Subir si está atrapado
        while (!level.getBlockState(pos).isAir() && pos.getY() < level.getMaxBuildHeight()) {
            pos.move(0, 1, 0);
        }

        // Bajar si está flotando
        while (level.getBlockState(pos.below()).isAir() && pos.getY() > level.getMinBuildHeight()) {
            pos.move(0, -1, 0);
        }

        if (isSafeSpawn(level, pos)) {
            return pos.immutable();
        }

        // Buscar en el radio
        for (int r = 1; r <= searchRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos candidate = pos.offset(dx, 0, dz);
                    BlockPos adjusted = adjustVertical(level, candidate);

                    if (isSafeSpawn(level, adjusted)) {
                        return adjusted;
                    }
                }
            }
        }

        return basePos;
    }

    private static boolean isSafeSpawn(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid() &&
                level.getBlockState(pos).isAir() &&
                level.getBlockState(pos.above()).isAir() &&
                !level.getFluidState(pos).isSource() && // Evita agua/lava en el bloque actual
                !level.getFluidState(pos.above()).isSource(); // Evita agua/lava en la cabeza
    }

    private static BlockPos adjustVertical(ServerLevel level, BlockPos start) {
        BlockPos.MutableBlockPos pos = start.mutable();

        // Subir si está atrapado
        while (!level.getBlockState(pos).isAir() && pos.getY() < level.getMaxBuildHeight()) {
            pos.move(0, 1, 0);
        }

        // Bajar si está flotando
        while (level.getBlockState(pos.below()).isAir() && pos.getY() > level.getMinBuildHeight()) {
            pos.move(0, -1, 0);
        }

        return pos.immutable();
    }
}
