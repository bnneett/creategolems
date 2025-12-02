package com.bnneett.creategolems.client.renderer.entity;

import com.bnneett.creategolems.CreateGolems;
import com.bnneett.creategolems.entity.BaseGolem;
import com.bnneett.creategolems.model.BaseGolemModel;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BaseGolemRenderer extends MobRenderer<BaseGolem, BaseGolemModel<BaseGolem>> {

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(CreateGolems.MODID, "textures/entity/base_golem.png");

    public BaseGolemRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BaseGolemModel<>(
            ctx.bakeLayer(BaseGolemModel.LAYER_LOCATION)
            ),
            0.45F
    
        );
    }

    @Override
    public ResourceLocation getTextureLocation(BaseGolem golem) {
        return TEXTURE;
    }
    
}
