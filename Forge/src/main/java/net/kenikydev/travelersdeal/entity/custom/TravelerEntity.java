package net.kenikydev.travelersdeal.entity.custom;

import net.kenikydev.travelersdeal.util.TravelerRequest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class TravelerEntity extends PathfinderMob {

    private TravelerRequest currentRequest;
    private long requestStartTime;
    private int karma = 0;
    private UUID targetPlayer;

    public TravelerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    public void tick() {
        super.tick();

        if (currentRequest != null && !level().isClientSide) {
            long elapsed =  level().getGameTime() - requestStartTime;
            long maxTime = 20L * 60L * 3L;

            if (elapsed > maxTime) {
                failRequest();
            }

        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Karma", karma);
        if (currentRequest != null) {
            tag.put("CurrentRequest", currentRequest.saveToNBT());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        karma = tag.getInt("Karma");
        if (tag.contains("CurrentRequest")) {
            currentRequest = TravelerRequest.loadFromNBT(tag.getCompound("CurrentRequest"));
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1,new LookAtPlayerGoal(this, Player.class, 8.0f)); //FIJARSE EN UN JUGADOR
        this.goalSelector.addGoal(2,new RandomStrollGoal(this, 0.6D));//PASEAR ALEATORIAMENTE
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide()) {
            if (currentRequest == null) {
                // Generar nueva petición
                currentRequest = TravelerRequest.generatedBasedOnProgress(player);
                requestStartTime = level().getGameTime();
                targetPlayer = player.getUUID();

                player.sendSystemMessage(Component.literal(
                        "Viajero: Necesito " + currentRequest.getItemName()
                ));
            } else if (player.getUUID().equals(targetPlayer)) {
                // Revisar si el jugador tiene el objeto
                if (player.getInventory().contains(currentRequest.getItemStack())) {
                    fulfillRequest(player);
                } else {
                    player.sendSystemMessage(Component.literal("Viajero: Aún no tienes lo que pedí."));
                }
            } else {
                player.sendSystemMessage(Component.literal("Viajero: Estoy hablando con otra persona."));
            }
        }

        return InteractionResult.SUCCESS;
    }

    private void fulfillRequest(Player player) {
        // Quitar el item del inventario
        player.getInventory().removeItem(currentRequest.getItemStack());

        // Dar recompensa
        ItemStack reward = currentRequest.getRewardForKarma(karma);
        level().addFreshEntity(new ItemEntity(level(), getX(), getY(), getZ(), reward));

        karma++;
        player.sendSystemMessage(Component.literal("Viajero: ¡Gracias! Aquí tienes tu recompensa."));
        currentRequest = null;
    }

    private void failRequest() {
        Player player = level().getPlayerByUUID(targetPlayer);
        if (player != null) {
            player.sendSystemMessage(Component.literal("Viajero: Me has fallado..."));
        }
        karma--;
        spawnHostileMobs();
        currentRequest = null;
    }

    private void spawnHostileMobs() {
        // Aquí puedes spawnear zombis/esqueletos cerca del jugador
        // Ejemplo simple: nivel de hostilidad según karma negativo
        if (karma < -2) {
            // spawnear más mobs o más fuertes
        }
    }
}
