package com.bnneett.creategolems.registry;

import com.bnneett.creategolems.CreateGolems;
import com.bnneett.creategolems.entity.BaseGolem;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GolemEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
        DeferredRegister.create(Registries.ENTITY_TYPE, CreateGolems.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<BaseGolem>> BASE_GOLEM =
        ENTITIES.register("base_golem", () ->
            EntityType.Builder
                .of(BaseGolem::new, MobCategory.MISC)
                .sized(1.0F, 1.2F)
                .clientTrackingRange(8)
                .build(CreateGolems.MODID + "base_golem")
    
    );
    
}
