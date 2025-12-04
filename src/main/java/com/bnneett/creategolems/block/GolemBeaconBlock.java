package com.bnneett.creategolems.block;

import javax.annotation.Nullable;

import com.bnneett.creategolems.CreateGolems;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;

public class GolemBeaconBlock extends Block {
    public static final BooleanProperty POWERED;

    //TODO still needs to be registered & linked to textures (per state) + model json
    

    public GolemBeaconBlock(BlockBehaviour.Properties state) {
        super(state);
        this.registerDefaultState((BlockState)this.defaultBlockState().setValue(POWERED, false));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) { //needed so the block, when placed, places in the correct state (powered if next to active redstone, etc.)
        return (BlockState)this.defaultBlockState().setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) { // the actual behavior that determines redstone based state changes
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            boolean wasPowered = state.getValue(POWERED);

            if (powered != wasPowered) {
                level.setBlock(pos, state.setValue(POWERED, powered), 3); // 3 = 1+2 behaviors combined (notify neighboring blocks AND client)

                level.gameEvent(null, powered ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
                // the block fires a game event when powered & not powered so golems can listen for them & find (or forget) the block! 
                
                if (!level.isClientSide()) CreateGolems.LOGGER.info("Event fired!"); //TODO DEBUG
                
                

            }

        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(POWERED); //automatically saved per block in chunk NBT, no codec needed
    }

    static {
        POWERED = BlockStateProperties.POWERED; //POWERED is a known & recognized block state in minecraft that all redstone blocks use (so we must also)
    }


}
