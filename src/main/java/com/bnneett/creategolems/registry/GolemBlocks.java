package com.bnneett.creategolems.registry;

import com.bnneett.creategolems.CreateGolems;
import com.bnneett.creategolems.block.GolemBeaconBlock;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GolemBlocks {
    public static final DeferredRegister<Block> BLOCKS = 
        DeferredRegister.create(Registries.BLOCK, CreateGolems.MODID);
    

    public static final DeferredHolder<Block, GolemBeaconBlock> GOLEM_BEACON = 
        BLOCKS.register("golem_beacon", () ->
            new GolemBeaconBlock(BlockBehaviour.Properties.of()
                .strength(2.0F)
                .lightLevel(state -> state.getValue(GolemBeaconBlock.POWERED) ? 10 : 0)
            )
        );

    

}
