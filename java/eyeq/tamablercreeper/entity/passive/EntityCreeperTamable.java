package eyeq.tamablercreeper.entity.passive;

import eyeq.tamablercreeper.entity.ai.EntityAICreeperTamableBeg;
import eyeq.tamablercreeper.entity.ai.EntityAICreeperTamableSwell;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;

public class EntityCreeperTamable extends EntityTameable {
    private static final DataParameter<Integer> STATE = EntityDataManager.createKey(EntityTameable.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> POWERED = EntityDataManager.createKey(EntityTameable.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> IGNITED = EntityDataManager.createKey(EntityTameable.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> BEGGING = EntityDataManager.createKey(EntityCreeperTamable.class, DataSerializers.BOOLEAN);

    private int lastActiveTime;
    private int timeSinceIgnited;
    private int fuseTime = 30;
    private int explosionRadius = 3;

    private float headRotationCourse;
    private float headRotationCourseOld;

    public EntityCreeperTamable(World world) {
        super(world);
        this.setSize(0.54F, 1.53F);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(1, new EntityAISwimming(this));
        // creeper
        tasks.addTask(2, new EntityAICreeperTamableSwell(this));
        tasks.addTask(3, new EntityAIAvoidEntity(this, EntityOcelot.class, 6.0F, 0.25, 0.3));
        tasks.addTask(4, new EntityAIAttackMelee(this, 1.0, false));
        // tamable
        tasks.addTask(5, new EntityAILeapAtTarget(this, 0.4F));
        tasks.addTask(6, new EntityAIFollowOwner(this, 1.0, 10, 2.0F));
        tasks.addTask(7, new EntityAIMate(this, 1.0));
        tasks.addTask(8, new EntityAIWanderAvoidWater(this, 0.8));
        tasks.addTask(9, new EntityAICreeperTamableBeg(this, 8.0F));
        // common
        tasks.addTask(10, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(10, new EntityAILookIdle(this));

        targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25);
        if(this.isTamed()) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0);
        }
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(STATE, -1);
        this.dataManager.register(POWERED, Boolean.FALSE);
        this.dataManager.register(IGNITED, Boolean.FALSE);
        this.dataManager.register(BEGGING, Boolean.FALSE);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if(this.dataManager.get(POWERED)) {
            compound.setBoolean("powered", true);
        }
        compound.setShort("Fuse", (short) this.fuseTime);
        compound.setByte("ExplosionRadius", (byte) this.explosionRadius);
        compound.setBoolean("ignited", this.hasIgnited());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(POWERED, compound.getBoolean("powered"));
        if(compound.hasKey("Fuse", 99)) {
            this.fuseTime = compound.getShort("Fuse");
        }
        if(compound.hasKey("ExplosionRadius", 99)) {
            this.explosionRadius = compound.getByte("ExplosionRadius");
        }
        if(compound.getBoolean("ignited")) {
            this.ignite();
        }
    }

    @Override
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
        if(tamed) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0);
        }
    }

    private void spawnLingeringCloud() {
        Collection<PotionEffect> potionEffects = this.getActivePotionEffects();
        if(potionEffects.isEmpty()) {
            return;
        }
        EntityAreaEffectCloud cloud = new EntityAreaEffectCloud(this.world, this.posX, this.posY, this.posZ);
        cloud.setRadius(2.5F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setDuration(cloud.getDuration() / 2);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        for(PotionEffect potioneffect : potionEffects) {
            cloud.addEffect(new PotionEffect(potioneffect));
        }
        this.world.spawnEntity(cloud);
    }

    private void explode() {
        if(this.world.isRemote) {
            return;
        }
        boolean isSmoking = this.world.getGameRules().getBoolean("mobGriefing");
        float power = this.getPowered() ? 2.0F : 1.0F;
        this.dead = true;
        this.world.createExplosion(this, this.posX, this.posY, this.posZ, (float) this.explosionRadius * power, isSmoking);
        this.setDead();
        this.spawnLingeringCloud();
    }

    @Override
    public void onUpdate() {
        if(this.isEntityAlive()) {
            this.lastActiveTime = this.timeSinceIgnited;
            if(this.hasIgnited()) {
                this.setCreeperState(1);
            }
            int i = this.getCreeperState();
            if(i > 0 && this.timeSinceIgnited == 0) {
                this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
            }
            this.timeSinceIgnited += i;
            if(this.timeSinceIgnited < 0) {
                this.timeSinceIgnited = 0;
            }
            if(this.timeSinceIgnited >= this.fuseTime) {
                this.timeSinceIgnited = this.fuseTime;
                this.explode();
            }
        }

        super.onUpdate();

        this.headRotationCourseOld = this.headRotationCourse;
        if(this.isBegging()) {
            this.headRotationCourse += (1.0F - this.headRotationCourse) * 0.4F;
        } else {
            this.headRotationCourse += (0.0F - this.headRotationCourse) * 0.4F;
        }
    }

    @Override
    public void onLivingUpdate() {
        if(getBrightness(1.0F) > 0.5F) {
            this.entityAge += 2;
        }
        super.onLivingUpdate();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_CREEPER_HURT;
    }

    @Override
    protected SoundEvent getHurtSound() {
        return SoundEvents.ENTITY_CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CREEPER_DEATH;
    }

    public boolean attackEntityAsMob(Entity entity) {
        boolean flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), (int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        if(flag) {
            this.applyEnchantments(this, entity);
        }
        return flag;
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        return 0.5F - world.getLightBrightness(pos);
    }

    protected boolean isValidLightLevel() {
        BlockPos pos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        if(this.world.getLightFor(EnumSkyBlock.SKY, pos) > this.rand.nextInt(32)) {
            return false;
        }
        int light = this.world.getLightFromNeighbors(pos);
        if(this.world.isThundering()) {
            int skyLight = this.world.getSkylightSubtracted();
            this.world.setSkylightSubtracted(10);
            light = this.world.getLightFromNeighbors(pos);
            this.world.setSkylightSubtracted(skyLight);
        }
        return light <= this.rand.nextInt(8);
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.isValidLightLevel() && super.getCanSpawnHere();
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt lightningBolt) {
        super.onStruckByLightning(lightningBolt);
        this.dataManager.set(POWERED, Boolean.TRUE);
    }

    @Override
    public boolean isBreedingItem(ItemStack itemStack) {
        return itemStack.getItem() == Item.getItemFromBlock(Blocks.TNT);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemStack = player.inventory.getCurrentItem();
        Item item = itemStack.getItem();
        if(item == Item.getItemFromBlock(Blocks.TNT)) {
            if(!this.isTamed()) {
                if(!world.isRemote) {
                    if(!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    if(rand.nextInt(3) == 0) {
                        this.setTamed(true);
                        this.navigator.clearPathEntity();
                        this.setAttackTarget(null);
                        this.setHealth(30.0F);
                        this.setOwnerId(player.getUniqueID());
                        this.playTameEffect(true);
                        this.world.setEntityState(this, (byte) 7);
                    } else {
                        this.playTameEffect(false);
                        this.world.setEntityState(this, (byte) 6);
                    }
                }
                return true;
            }
        } else if(item == Items.GUNPOWDER) {
            if(!player.isCreative()) {
                itemStack.shrink(1);
            }
            if(!world.isRemote) {
                heal(5);
                double dx = rand.nextGaussian() * 0.02;
                double dy = rand.nextGaussian() * 0.02;
                double dz = rand.nextGaussian() * 0.02;
                world.spawnParticle(EnumParticleTypes.NOTE, posX, posY + height + 0.1, posZ, dx, dy, dz);
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @SideOnly(Side.CLIENT)
    public float getInterestedAngle(float partialTickTime) {
        return (this.headRotationCourseOld + (this.headRotationCourse - this.headRotationCourseOld) * partialTickTime) * 0.15F * (float) Math.PI;
    }

    @SideOnly(Side.CLIENT)
    public float getCreeperFlashIntensity(float partialTickTime) {
        return (this.lastActiveTime + (this.timeSinceIgnited - this.lastActiveTime) * partialTickTime) / (this.fuseTime - 2);
    }

    @Override
    public float getEyeHeight() {
        return height * 0.8F;
    }

    @Override
    protected boolean canDespawn() {
        return !isTamed();
    }

    @Override
    protected ResourceLocation getLootTable() {
        return LootTableList.ENTITIES_CREEPER;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable entity) {
        return new EntityCreeperTamable(world);
    }

    public int getCreeperState() {
        return this.dataManager.get(STATE);
    }

    public void setCreeperState(int state) {
        this.dataManager.set(STATE, state);
    }

    public boolean getPowered() {
        return this.dataManager.get(POWERED);
    }

    public boolean hasIgnited() {
        return this.dataManager.get(IGNITED);
    }

    public void ignite() {
        this.dataManager.set(IGNITED, Boolean.TRUE);
    }

    public boolean isBegging() {
        return this.dataManager.get(BEGGING);
    }

    public void setBegging(boolean beg) {
        this.dataManager.set(BEGGING, beg);
    }
}
