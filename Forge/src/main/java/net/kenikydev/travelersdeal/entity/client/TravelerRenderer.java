package net.kenikydev.travelersdeal.entity.client;

import net.kenikydev.travelersdeal.TravelersDeal;
import net.kenikydev.travelersdeal.entity.custom.TravelerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TravelerRenderer extends MobRenderer<TravelerEntity, TravelerModel<TravelerEntity>> {

    public TravelerRenderer(EntityRendererProvider.Context context) {
        super(context, new TravelerModel<>(context.bakeLayer(TravelerModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(TravelerEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(TravelersDeal.MODID, "textures/entity/traveler/traveler.png");
    }
}
