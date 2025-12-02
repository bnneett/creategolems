package com.bnneett.creategolems.entity;

import java.util.UUID;
import java.util.function.IntFunction;

import org.jetbrains.annotations.Nullable;

import com.bnneett.creategolems.ai.BaseGolemAi;
import com.mojang.serialization.Dynamic;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugPackets;

public class BaseGolem extends PathfinderMob implements InventoryCarrier {

    @Nullable 
    private UUID ownerId;
    @Nullable 
    private BlockPos homePos;
    private boolean triedInitHome = false;
    private long inStateTicks = 0L;

    public final AnimationState powerDownAnimationState = new AnimationState();
    public final AnimationState noPowerAnimationState = new AnimationState();
    public final AnimationState powerUpAnimationState = new AnimationState();

    private final SimpleContainer inventory = new SimpleContainer(1); // size (slots) of container currently 1
    
    private static final EntityDataAccessor<Integer> BASE_GOLEM_STATE_ID =
        SynchedEntityData.defineId(BaseGolem.class, EntityDataSerializers.INT); // we literally can't make our own entitydataserializer so this is the only move

    private static final EntityDataAccessor<Integer> FUEL_TICKS =
        SynchedEntityData.defineId(BaseGolem.class, EntityDataSerializers.INT); // create synched int field for current fuel level

    /* TODO list
    - golem beacon object (another file lol)
    - golem "detect home" radius (around self) smaller than golem operation radius (around home)
    - determine golem operation radius LOL
    - base golem should remain within operation radius / path back if outside
    - base golem should also do its best to get to its beacon object before it powers down (to prevent getting lost lol)
    
    */

    // constructor

    public BaseGolem(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.getNavigation().setCanFloat(true);
    }
 
    // attribute shit

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes(
        ).add(Attributes.MAX_HEALTH, 20.0D
        ).add(Attributes.MOVEMENT_SPEED, 0.15D
        );
    }

    // inventory management and accessors
    // TODO manage inventory

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public int getCharge() {
        return this.entityData.get(FUEL_TICKS);
    }

    public void setCharge(int value) {
        this.entityData.set(FUEL_TICKS, Math.clamp(Math.max(value, 0), 0, 72000));
    }

    public void addFuel(int value) { //only use this one in other methods ... u can subtract via this as well :3
        this.setCharge(this.getCharge() + value);
    }

    private int getFuelBurnTime(ItemStack stack) {
        return stack.getBurnTime(null);
    }

    public boolean hasOwner() {
        return this.ownerId != null;
    }

    @Nullable
    public UUID getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(@Nullable UUID uuid) {
        this.ownerId = uuid;
    }
    
    public BlockPos getHomePos() {
        return this.homePos;
    }

    public void setHomePos( @Nullable BlockPos pos) {
        this.homePos = pos;
    }

    // data syncing

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) { // i guess we gotta sync our internal data with the whatever whatever
        super.defineSynchedData(builder);
        builder.define(BASE_GOLEM_STATE_ID, BaseGolemState.IDLE.id()); // instead of saving the state object (hard), we save the enum id and translate (cool and epic)
        builder.define(FUEL_TICKS, 0);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("state", this.getState().getSerializedName());
        tag.putInt("charge", this.getCharge());
        if (this.ownerId != null) {
            tag.putUUID("owner", this.ownerId);
        }
        if (this.homePos != null) {
            tag.putInt("homeX", this.homePos.getX());
            tag.putInt("homeY", this.homePos.getY());
            tag.putInt("homeZ", this.homePos.getZ());
        }
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.switchToState(BaseGolem.BaseGolemState.fromName(tag.getString("state")));
        this.setCharge(tag.getInt("charge"));
        if (tag.hasUUID("owner")) {
            this.ownerId = tag.getUUID("owner");
        }
        if (tag.contains("homeX", Tag.TAG_INT)) {
            int x = tag.getInt("homeY");
            int y = tag.getInt("homeY");
            int z = tag.getInt("homeZ");
            this.homePos = new BlockPos(x, y, z);
        }
    }

    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) { //resets the in state timer whenever we change states
        if (BASE_GOLEM_STATE_ID.equals(key)) {
            this.inStateTicks = 0L;
        }
        super.onSyncedDataUpdated(key);
    }


    // helpers

    public boolean isPoweredDown() { // returns false when IDLE, else true
        return this.getState().isPoweredDown;
    }

    public boolean canStartOrStayPoweredDown() { // charge == 0, UNRELATED to state
        return this.getCharge() == 0;
    }

    public boolean shouldSwitchToNoPowerState() { // returns true when we're done with the power down animation, used to switch to no power
        return this.getState() == BaseGolem.BaseGolemState.POWER_DOWN && this.inStateTicks > (long)BaseGolem.BaseGolemState.POWER_DOWN.animationDuration();
    }

    public boolean shouldFinishPoweringUp() { //returns true when we're done with the power up animation, used to switch to idle
        return this.getState() == BaseGolem.BaseGolemState.POWER_UP && this.inStateTicks > (long)BaseGolem.BaseGolemState.POWER_UP.animationDuration();
    }

    public long getInStateTicks() {
        return this.inStateTicks;
    }

    public void powerDown() {
        if (!this.isPoweredDown()) { // if currently in IDLE state
            this.stopInPlace();
            this.playSound(SoundEvents.REDSTONE_TORCH_BURNOUT);
            this.switchToState(BaseGolem.BaseGolemState.POWER_DOWN); // start the power down cycle
        }
    }

    public void powerUp() {
        if (this.isPoweredDown()) { // if NOT currently IDLE
            this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP);
            this.switchToState(BaseGolem.BaseGolemState.IDLE); // finish the power down cycle
        }
    }

    // core methods

    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    protected void actuallyHurt(DamageSource source, float amount) {
        super.actuallyHurt(source, amount);
    }

    public BaseGolemState getState() { //IMPORTANT. returns the actual state object from the ID register
        return BaseGolemState.byId(this.entityData.get(BASE_GOLEM_STATE_ID));
    }

    public void switchToState(BaseGolem.BaseGolemState state) { //IMPORTANT. sets the saved data as ID from desired state. used above
        //BaseGolemState old = this.getState();
        this.entityData.set(BASE_GOLEM_STATE_ID, state.id());
        /*
        if (!this.level().isClientSide()) {
            CreateGolems.LOGGER.info("Golem {} state {} -> {}",
                this.getId(), old, state);
        }
        */
    }

    // player right click

    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.isEmpty() && player.isShiftKeyDown()) {
            if (!this.level().isClientSide) {
                this.setOwnerId(player.getUUID());
                // play a sound here! or particles !
                this.playSound(SoundEvents.ANVIL_PLACE);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        
        if (!stack.isEmpty()) {
            int burnTime = getFuelBurnTime(stack);
            int count = stack.getCount();

            if (burnTime > 0) {
                if (!this.level().isClientSide) {
                    if (!player.getAbilities().instabuild) { // if player is in creative mode, don't shrink item stack
                        stack.shrink(count); //consume the whole stack at once
                    }

                    this.addFuel(burnTime * count);
                    this.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH);

                    // COULD kickstart power up here, but since multiple things can do this, may reserve to creating memory / using Ai
                }

                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    //receive the brain

    protected Brain.Provider<BaseGolem> brainProvider() {
        return BaseGolemAi.brainProvider();
    }

    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return BaseGolemAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    public Brain<BaseGolem> getGolemBrain() {
        return (Brain<BaseGolem>)this.getBrain();
    }

    protected void customServerAiStep() {
        this.level().getProfiler().push("baseGolemBrain");
        this.getGolemBrain().tick((ServerLevel)this.level(), this);
        this.level().getProfiler().pop();

        this.level().getProfiler().push("baseGolemActivityUpdate");
        BaseGolemAi.updateActivity(this);
        this.level().getProfiler().pop();

        super.customServerAiStep();

    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
        if (!this.triedInitHome) {
            this.triedInitHome = true;

            if (this.homePos == null) {
                BlockPos below = this.blockPosition().below();
                if (this.level().getBlockState(below).is(Blocks.JACK_O_LANTERN)) {
                    this.setHomePos(below.immutable());
                }
            }
        }

        ++this.inStateTicks;
        if (this.getCharge() > 0) { // avoid pointless calls when charge already 0 (even if it catches it in setCharge)
            this.addFuel(-1);
        }

        /*
        if (!level().isClientSide()) {
            if (this.tickCount % 10 == 0) {
                CreateGolems.LOGGER.info("Golem {} state={} charge={}",
                    this.getId(), this.getState(), this.getCharge());
            }
        }
        */
    }


    private void setupAnimationStates() { // is used in the model file to link to animations
        switch(this.getState().ordinal()) { //mapped to id of state machine enum states
        case 0:
            this.powerDownAnimationState.stop();
            this.powerUpAnimationState.stop();
            this.noPowerAnimationState.stop();
            break;
        case 1:
            this.powerDownAnimationState.startIfStopped(this.tickCount);
            this.powerUpAnimationState.stop();
            this.noPowerAnimationState.stop();
            break;
        case 2:
            this.powerDownAnimationState.stop();
            this.powerUpAnimationState.stop();
            this.noPowerAnimationState.startIfStopped(this.tickCount);
            break;
        case 3:
            this.powerDownAnimationState.stop();
            this.powerUpAnimationState.startIfStopped(this.tickCount);
            this.noPowerAnimationState.stop();
        }
    }

    // TODO sounds
    //- ambient sound
    //- death sound
    //- hurt sound
    //- step sound

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.CHAIN_STEP);
    }

    public int getMaxHeadYRot() { // don't allow head movement if frozen. default is, uh... high LOL let's see what happens
        return this.isPoweredDown() ? 0 : 170;
    }

    protected BodyRotationControl createBodyControl() { // don't allow body movement if frozen!
        return new BodyRotationControl(this) {
            @Override
            public void clientTick() {
                if (!BaseGolem.this.isPoweredDown()) {
                    super.clientTick();
                }
            }
        };
    }

    public static enum BaseGolemState implements StringRepresentable {

        IDLE("idle", false, 0, 0) {},
        POWER_DOWN("power_down", true, 30, 1) {}, //actively powering down
        NO_POWER("no_power", true, 80, 2) {}, //frozen
        POWER_UP("power_up", true, 30, 3) {}; //actively powering up

        private static final StringRepresentable.EnumCodec<BaseGolem.BaseGolemState> CODEC = StringRepresentable.fromEnum(BaseGolem.BaseGolemState::values);
        private static final IntFunction<BaseGolem.BaseGolemState> BY_ID = ByIdMap.continuous(BaseGolem.BaseGolemState::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, BaseGolem.BaseGolemState> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, BaseGolem.BaseGolemState::id);
        private final String name;
        private final boolean isPoweredDown;
        private final int animationDuration;
        private final int id;

        BaseGolemState(final String name, final boolean isPoweredDown, final int animationDuration, final int id) {
            this.name = name;
            this.isPoweredDown = isPoweredDown;
            this.animationDuration = animationDuration;
            this.id = id;
        }


        public static BaseGolem.BaseGolemState fromName(String name) {
            return (BaseGolem.BaseGolemState)CODEC.byName(name, IDLE);
        }

        public String getSerializedName() {
            return this.name;
        }
        
        private int id() {
            return this.id;
        }

        public static BaseGolem.BaseGolemState byId(int id) {
            return BY_ID.apply(id);
        }

        public boolean isPoweredDown() {
            return this.isPoweredDown;
        }

        public int animationDuration() {
            return this.animationDuration;
        }
    }



    

}
