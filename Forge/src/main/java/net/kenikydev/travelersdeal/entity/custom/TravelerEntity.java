package net.kenikydev.travelersdeal.entity.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class TravelerEntity extends PathfinderMob {

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private Item requestedItem = Items.APPLE;
    private int requestedAmount = 5;
    private boolean hasGreated = false;

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

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }

        if (!hasGreated && !level().isClientSide) {
            sendMessageToNearbyPlayers("Bienvenido al mundo, necesitaré tu ayuda para sobrevivir acá. Espero contar contigo. Necesito " +
                    requestedAmount + " " + requestedItem.getDescription().getString() + ".");
            hasGreated = true;
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (!level().isClientSide && heldItem.is(requestedItem)) {
            if (heldItem.getCount() >= requestedAmount) {
                heldItem.shrink(requestedAmount);
                sendMessageToNearbyPlayers("¡Gracias! Con esto podré sobrevivir. ¡Hasta pronto!");
                this.discard();
                return InteractionResult.SUCCESS;
            } else {
                sendMessageToNearbyPlayers("Todavia necesito más " + requestedItem.getDescription().getString() + ".");
            }
        }

        return InteractionResult.sidedSuccess(level().isClientSide);
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

    private void sendMessageToNearbyPlayers(String text) {
        this.level().players().forEach(p -> p.sendSystemMessage(Component.literal("[Traveler] " + text)));
    }
}
