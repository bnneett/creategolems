package com.bnneett.creategolems.registry;

import com.bnneett.creategolems.CreateGolems;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// note: ATTRIBUTES register already on the bus in the mod constructor

public class GolemAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(Registries.ATTRIBUTE, CreateGolems.MODID);
    

    public static final DeferredHolder<Attribute,Attribute> GOLEM_CHARGE =
        ATTRIBUTES.register("golem_charge", () ->
            new RangedAttribute(
                "attribute.name.creategolems.golem_charge", // name
                0.0D, // default
                0.0D, // min value
                100.D // max value
            ).setSyncable(true)
    
    );

}
