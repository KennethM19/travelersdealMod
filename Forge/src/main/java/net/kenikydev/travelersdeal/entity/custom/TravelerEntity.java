package net.kenikydev.travelersdeal.entity.custom;

import net.kenikydev.travelersdeal.entity.ModEntities;
import net.kenikydev.travelersdeal.util.TravelerSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class TravelerEntity extends PathfinderMob {

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private UUID targetPlayerUUID;
    private long expireTime;
    private int newKarma;

    public TravelerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this)); //Flotar en agua
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2D)); //Huir en peligro
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D)); //Caminar aleatoriamente
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F)); //Mirar a jugadores cercanos
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this)); //Girar la cabeza aleatoriamente
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
                failRequest();
                spawnHostileMobs();
                this.discard();
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;

        TravelerSavedData data = TravelerSavedData.get((ServerLevel) level());
        ItemStack heldItem = player.getItemInHand(hand);

        var request = data.getPendingRequest(player.getUUID());

        if (request != null && heldItem.is(request.item())) {
            if (heldItem.getCount() >= request.amount()) {
                heldItem.shrink(request.amount());
                data.clearPendingRequest(player.getUUID());

                newKarma = data.getKarma(player.getUUID()) + 5;
                data.setKarma(player.getUUID(), newKarma);
                player.addItem(new ItemStack(Items.DIAMOND, 1));

                sendMessageToNearbyPlayers("¡Muchas gracias!");
                this.discard();
                return InteractionResult.SUCCESS;
            } else {
                sendMessageToNearbyPlayers("Aún me faltan " + (request.amount() - heldItem.getCount()) + " " +
                        request.item().getDescription().getString());
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (!level().isClientSide && source.getEntity() instanceof Player player) {
            TravelerSavedData data = TravelerSavedData.get((ServerLevel) level());
            sendMessageToNearbyPlayers("¡Me has herido! Esto tendrá consecuencias...");
            newKarma = data.getKarma(player.getUUID()) - 10;
            data.setKarma(player.getUUID(), newKarma);
            spawnHostileMobs();
            data.clearPendingRequest(player.getUUID());
            this.discard();
        }
        return result;
    }

    public void assignRequest(UUID playerid, Item item, int amount, long durationTicks, Boolean firstRequest) {
        String text;
        this.targetPlayerUUID = playerid;
        this.expireTime = level().getGameTime() + durationTicks;
        TravelerSavedData.get((ServerLevel) level()).setPendingRequest(playerid, item, amount, expireTime);

        if (firstRequest) {
            text = "¡Bienvenido! ";
        } else {
            text = "";
        }
        sendMessageToNearbyPlayers(text + "Necesito " + amount + " " + item.getDescription().getString());
    }

    public static void spawnTraveler(ServerLevel serverLevel, BlockPos homePos, UUID playerId, TravelerSavedData data, boolean newRequest) {
        TravelerEntity traveler = ModEntities.TRAVELER.get().create(serverLevel);
        long durationTicks = 20 * 60 * 3;
        if (traveler != null) {
            traveler.moveTo(homePos.getX() + 2, homePos.getY(), homePos.getZ() + 2, 0, 0);
            var req = data.getPendingRequest(playerId);
            if (newRequest) {
                traveler.assignRequest(playerId, Items.APPLE, 5, durationTicks, true);
            } else if (req == null) {
                traveler.assignRequest(playerId, Items.DIAMOND, 5, durationTicks, false);
            }
            serverLevel.addFreshEntity(traveler);
        }
    }

    private void failRequest() {
        if (this.targetPlayerUUID != null) {
            TravelerSavedData data = TravelerSavedData.get((ServerLevel) level());
            data.clearPendingRequest(targetPlayerUUID);
            data.setKarma(targetPlayerUUID, data.getKarma(targetPlayerUUID) - 5);
            sendMessageToNearbyPlayers("Me has fallado");
        }
    }

    private void sendMessageToNearbyPlayers(String text) {
        this.level().players().forEach(p -> p.sendSystemMessage(Component.literal("[Traveler] " + text)));
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

    private void spawnHostileMobs() {
        ServerLevel serverLevel = (ServerLevel) level();
        Zombie zombie = EntityType.ZOMBIE.create(serverLevel);
        if (zombie != null) {
            zombie.moveTo(getX(), getY(), getZ());
            serverLevel.addFreshEntity(zombie);
        }
    }
}
