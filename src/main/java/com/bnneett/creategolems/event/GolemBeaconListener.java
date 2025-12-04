package com.bnneett.creategolems.event;

import com.bnneett.creategolems.CreateGolems;
import com.bnneett.creategolems.ai.BaseGolemAi;
import com.bnneett.creategolems.entity.BaseGolem;
import com.bnneett.creategolems.registry.GolemBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class GolemBeaconListener implements GameEventListener {
    private final BaseGolem golem;
    private final int radius;
    private boolean active = true;
    private final PositionSource source;

    
    public GolemBeaconListener(BaseGolem golem, int radius) {
        this.golem = golem;
        this.radius = radius;
        this.source = new EntityPositionSource(golem, golem.getEyeHeight());
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public PositionSource getListenerSource() {
        return source;
    }

    @Override
    public int getListenerRadius() {
        return radius;
    }

    @Override
    public boolean handleGameEvent(ServerLevel level, Holder<GameEvent> event, Context context, Vec3 pos) {
        BlockPos blockPos = BlockPos.containing(pos);
        if (level.getBlockState(blockPos).is(GolemBlocks.GOLEM_BEACON.get())) { //if the block that fired the event is a golem beacon...
            
            if (event.is(GameEvent.BLOCK_ACTIVATE)) { //and it's an activation (beacon turned on)
                BaseGolemAi.setHome(golem, blockPos); //set the golem's home to the new position (overwrites old pos)

                ((ServerLevel)level).sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    blockPos.getX() + 0.5,
                    blockPos.getY() + 0.6,
                    blockPos.getZ() + 0.5,
                    8,
                    0.2, 0.3, 0.2,
                    0.01
                );

                if (!level.isClientSide()) CreateGolems.LOGGER.info("Activate Event received by: Golem {}", golem.getId()); //TODO DEBUG


                return true;

            } else if (event.is(GameEvent.BLOCK_DEACTIVATE)) { //or, if it's a deactivation (beacon turned off)
                if (BaseGolemAi.canForgetHome(golem, blockPos)) { //and if we SHOULD forget (only if the block being turned off is actually golem's current home)
                    BaseGolemAi.forgetHome(golem); //forget home
                    if (!level.isClientSide()) CreateGolems.LOGGER.info("Deactivate Event received by: Golem {}", golem.getId()); //TODO DEBUG
                    return true;
                }

            }

        }

        return false;

    }
}
