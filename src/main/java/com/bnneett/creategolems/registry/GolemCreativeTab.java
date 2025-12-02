package com.bnneett.creategolems.registry;

import com.bnneett.creategolems.CreateGolems;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GolemCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateGolems.MODID);

        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATE_GOLEMS_TAB =
            CREATIVE_MODE_TABS.register("create_golems", () ->
                CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.creategolems"))
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon(() -> GolemItems.BASE_GOLEM_SPAWN_EGG.get().getDefaultInstance())
                .displayItems((params, output) -> {
                    // put things in the tab :3
                    output.accept(GolemItems.BASE_GOLEM_SPAWN_EGG.get());
                })
                .build()
        );
}
