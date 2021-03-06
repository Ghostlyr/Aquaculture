package com.teammetallurgy.aquaculture.handlers;

import com.teammetallurgy.aquaculture.enchantments.AquacultureEnchants;
import com.teammetallurgy.aquaculture.items.ItemAdminFishingRod;
import com.teammetallurgy.aquaculture.items.ItemAquacultureFishingRod;
import com.teammetallurgy.aquaculture.items.ItemAquacultureWoodenFishingRod;
import com.teammetallurgy.aquaculture.loot.FishLoot;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EntityCustomFishHook extends EntityFishHook {
    public int shake;
    public EntityPlayer angler;
    public Entity bobber;
    private int xTile;
    private int yTile;
    private int zTile;
    private Block inTile;
    private boolean inGround;
    private int ticksInGround;
    private int ticksInAir;
    private int baseCatchTime = 300;
    private int ticksCatchable;
    private int fishPosRotationIncrements;
    private double fishX;
    private double fishY;
    private double fishZ;
    private double fishYaw;
    private double fishPitch;
    @SideOnly(Side.CLIENT)
    private double velocityX;
    @SideOnly(Side.CLIENT)
    private double velocityY;
    @SideOnly(Side.CLIENT)
    private double velocityZ;

    private boolean isAdmin = false;

    public EntityCustomFishHook(World par1World) {
        super(par1World);
        this.xTile = -1;
        this.yTile = -1;
        this.zTile = -1;
        this.inGround = false;
        this.shake = 0;
        this.ticksInAir = 0;
        this.ticksCatchable = 0;
        this.bobber = null;
        this.setSize(0.25F, 0.25F);
        this.ignoreFrustumCheck = true;
    }

    @SideOnly(Side.CLIENT)
    public EntityCustomFishHook(World par1World, double par2, double par4, double par6, EntityPlayer par8EntityPlayer) {
        super(par1World, par2, par4, par6, par8EntityPlayer);
        this.setPosition(par2, par4, par6);
        this.ignoreFrustumCheck = true;
        this.angler = par8EntityPlayer;
        par8EntityPlayer.fishEntity = this;

        this.xTile = -1;
        this.yTile = -1;
        this.zTile = -1;
        this.inGround = false;
        this.shake = 0;
        this.ticksInAir = 0;
        this.ticksCatchable = 0;
        this.bobber = null;
        this.setSize(0.25F, 0.25F);
        this.ignoreFrustumCheck = true;
    }

    public EntityCustomFishHook(World par1World, EntityPlayer par2EntityPlayer) {
        super(par1World, par2EntityPlayer);
        this.xTile = -1;
        this.yTile = -1;
        this.zTile = -1;
        this.inGround = false;
        this.shake = 0;
        this.ticksInAir = 0;
        this.ticksCatchable = 0;
        this.bobber = null;
        this.ignoreFrustumCheck = true;
        this.angler = par2EntityPlayer;
        this.angler.fishEntity = this;
        this.setSize(0.25F, 0.25F);
        this.setLocationAndAngles(par2EntityPlayer.posX, par2EntityPlayer.posY + par2EntityPlayer.getEyeHeight(), par2EntityPlayer.posZ, par2EntityPlayer.rotationYaw, par2EntityPlayer.rotationPitch);
        this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        this.posY -= 0.10000000149011612D;
        this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        this.setPosition(this.posX, this.posY, this.posZ);
        float f = 0.4F;
        this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * f;
        this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * f;
        this.motionY = -MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI) * f;

        int shortCast = EnchantmentHelper.getEnchantmentLevel(AquacultureEnchants.shortcast.effectId, par2EntityPlayer.getCurrentEquippedItem());
        int longCast = EnchantmentHelper.getEnchantmentLevel(AquacultureEnchants.longcast.effectId, par2EntityPlayer.getCurrentEquippedItem());

        float velocity = 1.5F;
        if (shortCast > 0) {
            velocity *= (0.1F * shortCast);
        }
        if (longCast > 0) {
            velocity += velocity * (0.2F * longCast);
        }

        this.calculateVelocity(this.motionX, this.motionY, this.motionZ, velocity, 1.0F);
    }

    public EntityCustomFishHook(World world, EntityPlayer entityplayer, boolean b) {
        this(world, entityplayer);

        isAdmin = b;
    }

    public void setBaseCatchTime(int amount) {
        this.baseCatchTime = amount;
    }

    @Override
    protected void entityInit() {
    }

    public void calculateVelocity(double par1, double par3, double par5, float par7, float par8) {
        float f2 = MathHelper.sqrt_double(par1 * par1 + par3 * par3 + par5 * par5);
        par1 /= f2;
        par3 /= f2;
        par5 /= f2;
        par1 += this.rand.nextGaussian() * 0.007499999832361937D * par8;
        par3 += this.rand.nextGaussian() * 0.007499999832361937D * par8;
        par5 += this.rand.nextGaussian() * 0.007499999832361937D * par8;
        par1 *= par7;
        par3 *= par7;
        par5 *= par7;
        this.motionX = par1;
        this.motionY = par3;
        this.motionZ = par5;
        float f3 = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(par1, par5) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(par3, f3) * 180.0D / Math.PI);
        this.ticksInGround = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation. Args: posX,
     * posY, posZ, yaw, pitch
     */
    public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9, boolean p_180426_10_) {
        this.fishX = par1;
        this.fishY = par3;
        this.fishZ = par5;
        this.fishYaw = par7;
        this.fishPitch = par8;
        this.fishPosRotationIncrements = par9;
        this.motionX = this.velocityX;
        this.motionY = this.velocityY;
        this.motionZ = this.velocityZ;
    }

    @Override
    @SideOnly(Side.CLIENT)
    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    public void setVelocity(double par1, double par3, double par5) {
        this.velocityX = this.motionX = par1;
        this.velocityY = this.motionY = par3;
        this.velocityZ = this.motionZ = par5;
    }

    public boolean isFishingRod(ItemStack stack) {
        Item item = stack.getItem();

        return (item instanceof ItemAquacultureFishingRod) || (item instanceof ItemAquacultureWoodenFishingRod) || (item instanceof ItemAdminFishingRod);
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {
        // super.onUpdate();

        if (!worldObj.isRemote && this.angler == null)
            this.setDead();

        this.onEntityUpdate();

        if (this.fishPosRotationIncrements > 0) {
            double d0 = this.posX + (this.fishX - this.posX) / this.fishPosRotationIncrements;
            double d1 = this.posY + (this.fishY - this.posY) / this.fishPosRotationIncrements;
            double d2 = this.posZ + (this.fishZ - this.posZ) / this.fishPosRotationIncrements;
            double d3 = MathHelper.wrapAngleTo180_double(this.fishYaw - this.rotationYaw);
            this.rotationYaw = (float) (this.rotationYaw + d3 / this.fishPosRotationIncrements);
            this.rotationPitch = (float) (this.rotationPitch + (this.fishPitch - this.rotationPitch) / this.fishPosRotationIncrements);
            --this.fishPosRotationIncrements;
            this.setPosition(d0, d1, d2);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        } else {
            if (!this.worldObj.isRemote && this.angler != null) {
                ItemStack itemstack = this.angler.getCurrentEquippedItem();

                if (this.angler.isDead || !this.angler.isEntityAlive() || itemstack == null || !this.isFishingRod(itemstack) || this.getDistanceSqToEntity(this.angler) > 4 * 1024.0D) {
                    this.setDead();
                    this.angler.fishEntity = null;
                    return;
                }

                if (this.bobber != null) {
                    if (!this.bobber.isDead) {
                        this.posX = this.bobber.posX;
                        this.posY = this.bobber.getEntityBoundingBox().minY + this.bobber.height * 0.8D;
                        this.posZ = this.bobber.posZ;
                        return;
                    }

                    this.bobber = null;
                }
            }

            if (this.shake > 0) {
                --this.shake;
            }

            if (this.inGround) {
                Block block = this.worldObj.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock();

                if (block == this.inTile) {
                    ++this.ticksInGround;

                    if (this.ticksInGround == 1200) {
                        this.setDead();
                    }

                    return;
                }

                this.inGround = false;
                this.motionX *= this.rand.nextFloat() * 0.2F;
                this.motionY *= this.rand.nextFloat() * 0.2F;
                this.motionZ *= this.rand.nextFloat() * 0.2F;
                this.ticksInGround = 0;
                this.ticksInAir = 0;
            } else {
                ++this.ticksInAir;
            }

            Vec3 vec3 = new Vec3(this.posX, this.posY, this.posZ);
            Vec3 vec31 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec3, vec31);
            vec31 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (movingobjectposition != null) {
                vec31 = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double d4 = 0.0D;
            double d5;

            for (int j = 0; j < list.size(); ++j) {
                Entity entity1 = list.get(j);

                if (entity1.canBeCollidedWith() && (entity1 != this.angler || this.ticksInAir >= 5)) {
                    float f = 0.3F;
                    AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f, f, f);
                    MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);

                    if (movingobjectposition1 != null) {
                        d5 = vec3.distanceTo(movingobjectposition1.hitVec);

                        if (d5 < d4 || d4 == 0.0D) {
                            entity = entity1;
                            d4 = d5;
                        }
                    }
                }
            }

            if (entity != null) {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            if (movingobjectposition != null) {
                if (movingobjectposition.entityHit != null && this.angler != null) {
                    if (!(movingobjectposition.entityHit instanceof EntityBoat) && (this.angler.capabilities.isCreativeMode)) {
                        if (movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.angler), 0)) {
                            this.bobber = movingobjectposition.entityHit;
                        }
                    }
                } else {
                    this.inGround = true;
                }
            }

            if (!this.inGround) {
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
                this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

                for (this.rotationPitch = (float) (Math.atan2(this.motionY, f1) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
                    ;
                }

                while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
                    this.prevRotationPitch += 360.0F;
                }

                while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
                    this.prevRotationYaw -= 360.0F;
                }

                while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
                    this.prevRotationYaw += 360.0F;
                }

                this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
                this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
                float f2 = 0.92F;

                if (this.onGround || this.isCollidedHorizontally) {
                    f2 = 0.5F;
                }

                byte b0 = 5;
                double d6 = 0.0D;

                for (int k = 0; k < b0; ++k) {
                    AxisAlignedBB boundingBox = this.getEntityBoundingBox();
                    double d7 = boundingBox.minY + (boundingBox.maxY - boundingBox.minY) * (k + 0) / b0 - 0.125D + 0.125D;
                    double d8 = boundingBox.minY + (boundingBox.maxY - boundingBox.minY) * (k + 1) / b0 - 0.125D + 0.125D;
                    AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(boundingBox.minX, d7, boundingBox.minZ, boundingBox.maxX, d8, boundingBox.maxZ);

                    if (this.worldObj.isAABBInMaterial(axisalignedbb1, Material.water)) {
                        d6 += 1.0D / b0;
                    }
                }

                if (d6 > 0.0D) {
                    if (this.ticksCatchable > 0) {
                        --this.ticksCatchable;
                    } else {
                        BlockPos blockpos = (new BlockPos(this)).up();
                        short biteChance = (short) (baseCatchTime / 4);

                        if (this.worldObj.isRainingAt(blockpos)) {
                            biteChance = (short) (biteChance * 0.5);
                        }

                        if (this.angler != null) {
                            int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, this.angler.getCurrentEquippedItem());
                            biteChance *= 1.0 - 0.1 * efficiency;
                        }

                        if (this.rand.nextInt(biteChance) == 0) {
                            this.ticksCatchable = this.rand.nextInt(30) + 10;

                            if (this.angler != null) {
                                int barbed = EnchantmentHelper.getEnchantmentLevel(AquacultureEnchants.barbedHook.effectId, this.angler.getCurrentEquippedItem());
                                ticksCatchable += 4 * barbed;
                            }

                            if (this.angler != null) {
                                int loot = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, this.angler.getCurrentEquippedItem());
                                ticksCatchable += 4 * loot;
                            }

                            this.motionY -= 0.20000000298023224D;
                            this.playSound("random.splash", 0.25F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                            // this.playSound("note.harp", 0.55F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);

                            // this.playSoundEffect((double)par2 + 0.5D, (double)par3 + 0.5D, (double)par4 + 0.5D, "note." + "harp", 3.0F, 1);
                            // this.worldObj.spawnParticle("note", posX, posY + 1.2D, posZ, (double)16 / 24.0D, 0.0D, 0.0D);

                            float f3 = MathHelper.floor_double(this.getEntityBoundingBox().minY);
                            int l;
                            float f4;
                            float f5;

                            for (l = 0; l < 1.0F + this.width * 20.0F; ++l) {
                                f5 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                                f4 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                                this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + f5, f3 + 1.0F, this.posZ + f4, this.motionX, this.motionY - this.rand.nextFloat() * 0.2F, this.motionZ);
                            }

                            for (l = 0; l < 1.0F + this.width * 20.0F; ++l) {
                                f5 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                                f4 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
                                this.worldObj.spawnParticle(EnumParticleTypes.WATER_WAKE, this.posX + f5, f3 + 1.0F, this.posZ + f4, this.motionX, this.motionY, this.motionZ);
                            }
                        }
                    }
                }

                if (this.ticksCatchable > 0) {
                    this.motionY -= this.rand.nextFloat() * this.rand.nextFloat() * this.rand.nextFloat() * 0.2D;
                }

                d5 = d6 * 2.0D - 1.0D;
                this.motionY += 0.03999999910593033D * d5;

                if (d6 > 0.0D) {
                    f2 = (float) (f2 * 0.9D);
                    this.motionY *= 0.8D;
                }

                this.motionX *= f2;
                this.motionY *= f2;
                this.motionZ *= f2;
                this.setPosition(this.posX, this.posY, this.posZ);
            }
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
        par1NBTTagCompound.setShort("xTile", (short) this.xTile);
        par1NBTTagCompound.setShort("yTile", (short) this.yTile);
        par1NBTTagCompound.setShort("zTile", (short) this.zTile);
        par1NBTTagCompound.setByte("inTile", (byte) Block.getIdFromBlock(this.inTile));
        par1NBTTagCompound.setByte("shake", (byte) this.shake);
        par1NBTTagCompound.setByte("inGround", (byte) (this.inGround ? 1 : 0));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
        this.xTile = par1NBTTagCompound.getShort("xTile");
        this.yTile = par1NBTTagCompound.getShort("yTile");
        this.zTile = par1NBTTagCompound.getShort("zTile");
        this.inTile = Block.getBlockById(par1NBTTagCompound.getByte("inTile") & 255);
        this.shake = par1NBTTagCompound.getByte("shake") & 255;
        this.inGround = par1NBTTagCompound.getByte("inGround") == 1;
    }

    /*
    @Override
    @SideOnly(Side.CLIENT)
    public float getShadowSize() {
        return 0.0F;
    }
    */

    @Override
    public int handleHookRetraction() {
        if (this.worldObj.isRemote) {
            return 0;
        } else {
            byte b0 = 0;

            if (this.bobber != null) {
                double d0 = this.angler.posX - this.posX;
                double d1 = this.angler.posY - this.posY;
                double d2 = this.angler.posZ - this.posZ;
                double d3 = MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
                double d4 = 0.1D;
                this.bobber.motionX += d0 * d4;
                this.bobber.motionY += d1 * d4 + MathHelper.sqrt_double(d3) * 0.08D;
                this.bobber.motionZ += d2 * d4;
                b0 = 3;
            } else if (this.ticksCatchable > 0 || isAdmin) {
                BiomeGenBase currentBiome = worldObj.getBiomeGenForCoords(new BlockPos(MathHelper.floor_double(angler.posX), 0, MathHelper.floor_double(angler.posZ)));

                ItemStack fishLoot;
                int count = 1;

                float doubleOdds = 0;

                int doubleHook = EnchantmentHelper.getEnchantmentLevel(AquacultureEnchants.doubleHook.effectId, this.angler.getCurrentEquippedItem());
                if (doubleHook > 0) {
                    doubleOdds = 0.1F * doubleHook;

                    if (Math.random() < doubleOdds)
                        count++;
                }

                do {
                    float fishOdds = 0.5F;

                    int magnetic = EnchantmentHelper.getEnchantmentLevel(AquacultureEnchants.magnetic.effectId, this.angler.getCurrentEquippedItem());
                    int appealing = EnchantmentHelper.getEnchantmentLevel(AquacultureEnchants.appealing.effectId, this.angler.getCurrentEquippedItem());

                    if (magnetic > 0) {
                        fishOdds -= (0.08 * magnetic);
                    } else if (appealing > 0) {
                        fishOdds += (0.08 * appealing);
                    }

                    float roll = (float) Math.random();

                    int heavyLine = 0;

                    if (this.angler != null) {
                        heavyLine = EnchantmentHelper.getEnchantmentLevel(AquacultureEnchants.heavyLine.effectId, angler.getCurrentEquippedItem());
                    }

                    if (roll < fishOdds) {
                        if ((this.angler.getCurrentEquippedItem().getItem() instanceof ItemAdminFishingRod) && this.angler.isSneaking())
                            fishLoot = FishLoot.instance().getRandomFish(currentBiome.biomeID);
                        else
                            fishLoot = FishLoot.instance().getRandomFish(currentBiome.biomeID, heavyLine);
                    } else {
                        fishLoot = FishLoot.instance().getRandomJunk(currentBiome.biomeID);
                    }

                    if (fishLoot != null) {
                        EntityItem entityitem = new EntityItem(worldObj, posX, posY, posZ, fishLoot);

                        double d5 = this.angler.posX - this.posX;
                        double d6 = this.angler.posY - this.posY;
                        double d7 = this.angler.posZ - this.posZ;
                        double d8 = MathHelper.sqrt_double(d5 * d5 + d6 * d6 + d7 * d7);
                        double d9 = 0.1D;
                        entityitem.motionX = d5 * d9;
                        entityitem.motionY = d6 * d9 + MathHelper.sqrt_double(d8) * 0.08D;
                        entityitem.motionZ = d7 * d9;
                        this.worldObj.spawnEntityInWorld(entityitem);
                        this.angler.addStat(StatList.fishCaughtStat, 1);
                        this.angler.worldObj.spawnEntityInWorld(new EntityXPOrb(this.angler.worldObj, this.angler.posX, this.angler.posY + 0.5D, this.angler.posZ + 0.5D, this.rand.nextInt(6) + 1));
                        b0 = 1;
                    }

                    count--;

                } while (count > 0);
            }

            if (this.inGround) {
                b0 = 2;
            }

            this.setDead();
            this.angler.fishEntity = null;
            return b0;
        }
    }

    /**
     * Will get destroyed next tick.
     */
    @Override
    public void setDead() {
        super.setDead();

        if (this.angler != null) {
            this.angler.fishEntity = null;
        }
    }
}
