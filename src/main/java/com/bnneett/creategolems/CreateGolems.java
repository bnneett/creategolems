package com.bnneett.creategolems;

import org.slf4j.Logger;

import com.bnneett.creategolems.entity.BaseGolem;
import com.bnneett.creategolems.registry.GolemAttributes;
import com.bnneett.creategolems.registry.GolemBlocks;
import com.bnneett.creategolems.registry.GolemCreativeTab;
import com.bnneett.creategolems.registry.GolemEntities;
import com.bnneett.creategolems.registry.GolemItems;
import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateGolems.MODID)
public class CreateGolems {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "creategolems";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    
    // mod constructor :3 (first code EVER that is run!!!)

    public CreateGolems(IEventBus modEventBus, ModContainer modContainer) {

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerEntityAttributes);

        // CUSTOM REGISTER STUFF
        /* 
        explanation 
        - the thing in caps is an object of type DeferredRegister
        - DeferredRegister objects are the guys with the bags containing all the stuff you want added to minecraft
        - the register needs to go on the bus to get to the mod... or he will be left at home when the mod starts
        */


        // erm basically it's way better practice to have different files for all mod attributes, entities, items, blocks, creative tabs, etc. 
        // and NOT put them in the main file
        // so that's what we do here :3

        GolemAttributes.ATTRIBUTES.register(modEventBus);
        GolemEntities.ENTITIES.register(modEventBus);
        GolemItems.ITEMS.register(modEventBus);
        GolemBlocks.BLOCKS.register(modEventBus);
        GolemCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);


        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (CreateGolems) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(
            GolemEntities.BASE_GOLEM.get(),
            BaseGolem.createAttributes().build()
        );
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
