package com.bnneett.creategolems.ai;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.bnneett.creategolems.CreateGolems;
import com.bnneett.creategolems.entity.BaseGolem;
import com.bnneett.creategolems.entity.BaseGolem.BaseGolemState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomLookAround;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class BaseGolemAi {
    private static final ImmutableList<SensorType<? extends Sensor<? super BaseGolem>>> SENSOR_TYPES;
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES;
    private static final int WORK_RANGE = 32; // this will eventually be able to be upgraded, but for now...
    
    public BaseGolemAi() {
        super();
    }

    /* TODO home ai

    - golemstaynearhome needs start and stop methods & conditions (&redoing)

    - golem job-specific ai should NOT run if golem has no home. golem will wander uselessly instead LMAO
    - golem should always wander back within 32 blocks of home block (L1 norm, this is minecraft) if wander pathing takes it outside
    - golem should try to get close to its home block if it is low on power to prevent getting lost

    */

    public static Brain.Provider<BaseGolem> brainProvider() { // you NEED this if you want to use memories or sensors at all. certain behaviors (lookattargetsink, movetotargetsink) need these to work
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    public static Brain<?> makeBrain(Brain<BaseGolem> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initNoPowerActivity(brain);

        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.PLAY_DEAD);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<BaseGolem> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
            new Swim(0.5F),
            new LookAtTargetSink(45,90),
            new MoveToTargetSink() {
                protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
                    if (mob instanceof BaseGolem) {
                        BaseGolem golem = (BaseGolem)mob;
                        if (golem.isPoweredDown()) {
                            return false; //activity behaviors cannot start if golem powered down
                        }
                    }

                    return super.checkExtraStartConditions(level, mob);
                }
            }));
    }

    private static void initIdleActivity(Brain<BaseGolem> brain) {
        brain.addActivity(Activity.IDLE, ImmutableList.of(
            Pair.of(0, new BaseGolemAi.GolemGoHomeWhenLow(WORK_RANGE)),
            Pair.of(1, new BaseGolemAi.GolemStayNearHome(WORK_RANGE)),
            Pair.of(2, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(20, 40))),
            Pair.of(3, new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F)),
            Pair.of(4, new RunOne<BaseGolem>(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), ImmutableList.of(
                Pair.of(RandomStroll.stroll(1.0F), 1),
                Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                Pair.of(new DoNothing(20, 40), 1)    
            )))

        ));
    }

    private static void initNoPowerActivity(Brain<BaseGolem> brain) {
        brain.addActivity(Activity.PLAY_DEAD, ImmutableList.of(
            Pair.of(0, new BaseGolemAi.GolemPowerDown())
        ));
    }

    public static void updateActivity(BaseGolem golem) {
        if (golem.canStartOrStayPoweredDown() || golem.isPoweredDown()) {
            golem.getBrain().setActiveActivityIfPossible(Activity.PLAY_DEAD);
        } else {
            golem.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
        }
    }

    public static void setHome(BaseGolem golem, BlockPos pos) {
        Brain brain = golem.getBrain();
        GlobalPos gPos = GlobalPos.of(golem.level().dimension(), pos);
        
        brain.setMemory(MemoryModuleType.HOME, gPos);

        ((ServerLevel)golem.level()).sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                golem.getX() + 0.5,
                golem.getY() + 0.7,
                golem.getZ() + 0.5,
                4,
                0.2, 0.3, 0.2,
                0.01
            );

    }

    public static boolean canForgetHome(BaseGolem golem, BlockPos pos) {
        Brain brain = golem.getBrain();
        Optional<GlobalPos> memory = brain.getMemory(MemoryModuleType.HOME);

        GlobalPos gPos = GlobalPos.of(golem.level().dimension(), pos);

        if (memory.isPresent()) {
            GlobalPos home = memory.get();
            return (home.equals(gPos));
        }
        return false;
    
    }

    public static void forgetHome(BaseGolem golem) {
        golem.getBrain().eraseMemory(MemoryModuleType.HOME);
    }

    static {
        SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY,
            SensorType.IS_IN_WATER //TODO maybe implement losing charge faster in water? could also be dependent on golem type tho (based on material)
        );
        
        MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.HOME
        );
            
    }

    public static class GolemStayNearHome extends Behavior<BaseGolem> {
        private final double radius;
        
        public GolemStayNearHome(double radius) {
            super(Map.of(MemoryModuleType.HOME, MemoryStatus.VALUE_PRESENT)); //memory requirements are a continuous gate (meaning that they trigger behavior when TRUE and stop behavior when FALSE)
            this.radius = radius;
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, BaseGolem golem) {
            Optional<GlobalPos> home = golem.getBrain().getMemory(MemoryModuleType.HOME);
            if (home.isEmpty()) return false;
            
            // if we have a home, only start the behavior when we're outside of our work area
            BlockPos homePos = home.get().pos();
            return golem.blockPosition().distSqr(homePos) > (radius * radius);

        }

        @Override
        protected void start(ServerLevel level, BaseGolem golem, long gameTime) {
            GlobalPos home = golem.getBrain().getMemory(MemoryModuleType.HOME).get();
            BlockPos homePos = home.pos();

            golem.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(homePos, 1.0F, 4));

        }

    }

    public static class GolemGoHomeWhenLow extends Behavior<BaseGolem> {
        private final int radius;
        //this is dependent on the range of the golem. but basically we want to prevent golem getting lost when on low power, and immediately start moving home when under ~200 ticks of charge (about 32ish blocks of movement)
    
        public GolemGoHomeWhenLow(int radius) {
            super(Map.of(), 1200); //behavior will expire after 1 minute if not ended otherwise 
            this.radius = radius;
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, BaseGolem golem) {
            //start behavior when golem charge is less than approx. the buffer-included time to walk about radius # blocks (AND has a home)
            Optional<GlobalPos> home = golem.getBrain().getMemory(MemoryModuleType.HOME);
            if (home.isEmpty()) return false;
            
            return golem.getCharge() < this.radius * 6;
        }

        @Override
        protected void start(ServerLevel level, BaseGolem golem, long gameTime) {
            //immediately start booking it back home (a little faster than base movement)
            GlobalPos home = golem.getBrain().getMemory(MemoryModuleType.HOME).get();
            BlockPos homePos = home.pos();

            golem.getNavigation().moveTo(homePos.getX(), homePos.getY(), homePos.getZ(), 1.2); //overrides other pathing behavior (WALK_TARGET too slow)
        }

        @Override
        protected boolean canStillUse(ServerLevel level, BaseGolem golem, long gameTime) {
            Optional<GlobalPos> home = golem.getBrain().getMemory(MemoryModuleType.HOME);
            if (home.isEmpty()) return false;
            BlockPos homePos = home.get().pos();

            //if within ~4 blocks of the beacon, we can stop running
            if (golem.blockPosition().distSqr(homePos) < (4*4)) {
                return false;
            }
            return true;
        }
    
    
    }

    public static class GolemPowerDown extends Behavior<BaseGolem> {
        static final int GOLEM_STATE_MAX_VALUE = Integer.MAX_VALUE;

        public GolemPowerDown() {
            super(Map.of(), GOLEM_STATE_MAX_VALUE);
        }

        protected void tick(ServerLevel level, BaseGolem golem, long gameTime) {
            super.tick(level, golem, gameTime);
            /*
            if (!level.isClientSide()) {
                CreateGolems.LOGGER.info("Golem {} GolemPowerDown.tick state={} charge={} inStateTicks={}", //TODO DEBUG
                    golem.getId(), golem.getState(), golem.getCharge(), golem.getInStateTicks());
            }
            */

            // switch from POWER_DOWN to NO_POWER if needed
            if (golem.shouldSwitchToNoPowerState()) { // requires golem to be in POWER_DOWN
                golem.switchToState(BaseGolem.BaseGolemState.NO_POWER);
                // play a no power sound!
            } else if (golem.getState() == BaseGolem.BaseGolemState.NO_POWER && golem.getCharge() > BaseGolem.BaseGolemState.POWER_UP.animationDuration()) {
                golem.switchToState(BaseGolem.BaseGolemState.POWER_UP); //if we're powered down and have enough charge to power back up, then power up
            }
    
        }

        protected boolean checkExtraStartConditions(ServerLevel level, BaseGolem golem) {
            return golem.canStartOrStayPoweredDown(); //checks if charge == 0
        }

        protected void start(ServerLevel level, BaseGolem golem, long gameTime) {
            /*
            CreateGolems.LOGGER.info("Golem {} GolemPowerDown.start, charge={}",
                golem.getId(), golem.getCharge());
            */
            golem.powerDown(); //changes the state to POWER_DOWN from IDLE

        }

        protected boolean canStillUse(ServerLevel level, BaseGolem golem, long gameTime) { //to prevent a forever stunlock, we must trigger the stop() block when charge > 0 AND we've finished the POWER_UP animation
            if (golem.canStartOrStayPoweredDown()) {
                return true; // continue if charge = 0
            } else if (golem.getState() == BaseGolem.BaseGolemState.POWER_UP) {
                if (golem.shouldFinishPoweringUp()) {
                    return false; //exit the behavior if we're finished powering up
                } else {
                    return true;
                }
            }
            return true;
        }

        protected void stop(ServerLevel level, BaseGolem golem, long gameTime) {
            /*
            CreateGolems.LOGGER.info("Golem {} GolemPowerDown.stop state={} charge={}",
                golem.getId(), golem.getState(), golem.getCharge());
            */

            golem.powerUp(); //this is what checks the state and changes it to IDLE from POWER_UP
        }

    }

}

