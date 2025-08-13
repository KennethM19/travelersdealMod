package net.kenikydev.travelersdeal.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kenikydev.travelersdeal.TravelersDeal;
import net.kenikydev.travelersdeal.entity.custom.TravelerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class TravelerModel<T extends TravelerEntity> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(TravelersDeal.MODID, "traveler"), "main");
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart body;
    private final ModelPart backpack;
    private final ModelPart right_arm;
    private final ModelPart right_arm2;
    private final ModelPart right_leg;
    private final ModelPart left_leg;
    private final ModelPart root;

    public TravelerModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.hat = this.head.getChild("hat");
        this.body = root.getChild("body");
        this.backpack = this.body.getChild("backpack");
        this.right_arm = root.getChild("right_arm");
        this.right_arm2 = root.getChild("right_arm2");
        this.right_leg = root.getChild("right_leg");
        this.left_leg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-4.5F, -8.5F, -4.5F, 8.0F, 7.0F, 9.0F, new CubeDeformation(0.07F)), PartPose.offset(0.5F, 4.5F, 2.5F));

        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -30.0F, -3.0F, 10.0F, 1.0F, 11.0F, new CubeDeformation(0.5F))
                .texOffs(0, 28).addBox(-4.0F, -32.0F, -2.0F, 8.0F, 1.0F, 9.0F, new CubeDeformation(0.5F)), PartPose.offset(-0.5F, 20.5F, -2.5F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(34, 12).addBox(-4.5F, -6.0F, -2.5F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 9.0F, 2.5F));

        PartDefinition backpack = body.addOrReplaceChild("backpack", CubeListBuilder.create().texOffs(34, 29).addBox(-3.5F, -4.6429F, -3.7857F, 6.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 50).addBox(-3.5F, -3.6429F, 0.2143F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(34, 51).addBox(-4.5F, -2.6429F, -2.7857F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(40, 51).addBox(2.5F, -2.6429F, -2.7857F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(28, 38).addBox(2.5F, -1.6429F, 0.2143F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(46, 51).addBox(-4.5F, -1.6429F, 0.2143F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(18, 38).addBox(-2.5F, -2.6429F, 2.2143F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.3571F, 6.2857F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(42, 0).addBox(-2.0F, -0.8372F, -2.2847F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(16, 51).addBox(-2.0F, 6.1628F, -2.2847F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.1F))
                .texOffs(38, 43).addBox(-2.0F, 7.1628F, -2.2847F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 3.8333F, 2.5F));

        PartDefinition right_arm2 = partdefinition.addOrReplaceChild("right_arm2", CubeListBuilder.create().texOffs(42, 0).addBox(-2.0F, -0.8376F, -2.7274F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(16, 51).addBox(-2.0F, 6.1624F, -2.7274F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.1F))
                .texOffs(38, 43).addBox(-2.0F, 7.1624F, -2.7274F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 3.8333F, 2.5F, 0.0F, 3.1416F, 0.0F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(18, 43).addBox(-2.5F, 6.5F, -3.1667F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-2.5F, -0.5F, -2.1667F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 15.5F, 2.1667F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(18, 43).addBox(-2.5F, 6.5F, -3.1667F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-2.5F, -0.5F, -2.1667F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 15.5F, 2.1667F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(TravelerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animateWalk(TravelerAnimations.TRAVELER_WALK, limbSwing, limbSwingAmount, 1.2f, 1.5f);
        this.animate(entity.idleAnimationState, TravelerAnimations.TRAVELER_IDLE, ageInTicks, 0.8f);
    }
}
