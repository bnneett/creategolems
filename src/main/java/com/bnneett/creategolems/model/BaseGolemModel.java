package com.bnneett.creategolems.model;

import com.bnneett.creategolems.CreateGolems;
import com.bnneett.creategolems.animation.BaseGolemAnimation;
import com.bnneett.creategolems.entity.BaseGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

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

// Made with Blockbench 5.0.4
// Exported for Minecraft version 1.17 or later with Mojang mapping


/*
Blockbench is WRONG, actually!
What I had to change:
- ResourceLocation is WRONG and bad, the new method is: new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(YourMod.MODID, "name_of_model"), "main");
- remove the last 4 params from the renderToBuffer .render() call. it should be this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay);
- this needs to be a HierarchicalModel, not an EntityModel, if you use bones
- have the type extend your entity class rather than THE Entity class if you need any methods from your entity class LOL

*/


public class BaseGolemModel<T extends BaseGolem> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = 
		new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(CreateGolems.MODID, "base_golem"), "main"); //same id as base golem entity
	private final ModelPart smartgolem;
	private final ModelPart upperbody;
	private final ModelPart head;
	private final ModelPart rightarm;
	private final ModelPart leftarm;
	private final ModelPart torso;
	private final ModelPart key;
	private final ModelPart twisty;
	private final ModelPart leftleg;
	private final ModelPart rightleg;

	public BaseGolemModel(ModelPart root) {
		this.smartgolem = root.getChild("smartgolem");
		this.upperbody = this.smartgolem.getChild("upperbody");
		this.head = this.upperbody.getChild("head");
		this.rightarm = this.upperbody.getChild("rightarm");
		this.leftarm = this.upperbody.getChild("leftarm");
		this.torso = this.upperbody.getChild("torso");
		this.key = this.upperbody.getChild("key");
		this.twisty = this.key.getChild("twisty");
		this.leftleg = this.smartgolem.getChild("leftleg");
		this.rightleg = this.smartgolem.getChild("rightleg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition smartgolem = partdefinition.addOrReplaceChild("smartgolem", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition upperbody = smartgolem.addOrReplaceChild("upperbody", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, 0.0F));

		PartDefinition head = upperbody.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 22).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

		PartDefinition rightarm = upperbody.addOrReplaceChild("rightarm", CubeListBuilder.create().texOffs(24, 22).addBox(-3.0F, -2.0F, -2.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -7.0F, 0.0F));

		PartDefinition leftarm = upperbody.addOrReplaceChild("leftarm", CubeListBuilder.create().texOffs(0, 33).addBox(0.0F, -2.0F, -2.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -7.0F, 0.0F));

		PartDefinition torso = upperbody.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -6.0F, -4.0F, 10.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition key = upperbody.addOrReplaceChild("key", CubeListBuilder.create().texOffs(14, 37).addBox(-1.0F, -1.0F, -3.75F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, 6.75F, 0.0F, 0.0F, -1.5708F));

		PartDefinition twisty = key.addOrReplaceChild("twisty", CubeListBuilder.create().texOffs(24, 45).addBox(-0.5F, 0.0F, -3.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 45).addBox(-0.5F, -2.0F, -2.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 45).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 39).addBox(-0.5F, 3.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 37).addBox(-0.5F, 5.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 37).addBox(-0.5F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 39).addBox(-0.5F, -2.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 2.25F));

		PartDefinition leftleg = smartgolem.addOrReplaceChild("leftleg", CubeListBuilder.create().texOffs(36, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -6.0F, 0.0F));

		PartDefinition rightleg = smartgolem.addOrReplaceChild("rightleg", CubeListBuilder.create().texOffs(36, 10).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -6.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		float angle = 0;

		float rawCharge = entity.getCharge();
		float maxCharge = 6000.0F;

		float chargeFrac = Math.clamp(rawCharge / maxCharge, 0.0F, 1.0F);
		float spinSpeed = chargeFrac * 0.4F; //not trying to magic number but 0.0 is the min speed and 0.4 is the max speed in radians/tick approx

		if (entity.getState() != BaseGolem.BaseGolemState.NO_POWER) {
			angle = (ageInTicks*spinSpeed) % (2F*(float)Math.PI);
			this.head.xRot = headPitch * ((float)Math.PI / 180F);
			this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
			this.animateWalk(BaseGolemAnimation.walk, limbSwing, limbSwingAmount, 2.0F, 10.0F); //starting with guess values
		}
		if (entity.getState() == BaseGolem.BaseGolemState.POWER_DOWN) {
			this.head.yRot =  ( netHeadYaw * ((float)Math.PI / 180F) ) * (1 / (float)Math.pow(1.15, entity.getInStateTicks()));
		}
		this.key.zRot = angle;

		// animate spinny thing :3
		// - 72000 ticks (1 hour) is MAX charge, period
		// - but the golem will indicate "high" charge at 6000 ticks (5 min) and lerp down to 0 ticks for spinspeed

		this.animate(entity.powerDownAnimationState, BaseGolemAnimation.power_down, ageInTicks, 1.0F);
		this.animate(entity.powerUpAnimationState, BaseGolemAnimation.power_up, ageInTicks, 1.0F);
		this.animate(entity.noPowerAnimationState, BaseGolemAnimation.no_power, ageInTicks, 1.0F);
	}

	@Override
	public ModelPart root() {
		return this.smartgolem;
	}

	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		this.smartgolem.render(poseStack, vertexConsumer, packedLight, packedOverlay);
	}

}