package com.bnneett.creategolems.registry;

import com.bnneett.creategolems.CreateGolems;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GolemItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, CreateGolems.MODID);


    public static final DeferredHolder<Item, SpawnEggItem> BASE_GOLEM_SPAWN_EGG =
        ITEMS.register("base_golem_spawn_egg", () ->
            new DeferredSpawnEggItem(
                GolemEntities.BASE_GOLEM,
                0xCCCCCC,
                0x222222,
                new Item.Properties()
            )
        );

    public static final DeferredHolder<Item, BlockItem> GOLEM_BEACON_ITEM =
        ITEMS.register("golem_beacon",
                () -> new BlockItem(GolemBlocks.GOLEM_BEACON.get(),
                        new Item.Properties())
        );
        
    
}
