package thebetweenlands.common.entity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thebetweenlands.client.render.particle.BLParticles;
import thebetweenlands.client.render.particle.BatchedParticleRenderer;
import thebetweenlands.client.render.particle.DefaultParticleBatches;
import thebetweenlands.client.render.particle.ParticleFactory.ParticleArgs;
import thebetweenlands.client.render.shader.ShaderHelper;
import thebetweenlands.common.entity.mobs.EntityFortressBoss;
import thebetweenlands.common.tile.TileEntityDecayPitGroundChain;
import thebetweenlands.common.tile.TileEntityDecayPitHangingChain;
import thebetweenlands.util.RotationMatrix;

public class EntityDecayPitTarget extends Entity implements IEntityMultiPartPitTarget {
	private final RotationMatrix rotationMatrix = new RotationMatrix();
	
	public float animationTicksPrev = 0;
	public int animationTicksChain = 0;
	public int animationTicksChainPrev = 0;
	public final int MAX_PROGRESS = 768; // max distance of travel from origin so; 768 * 0.0078125F = 6 Blocks
	public final int MIN_PROGRESS = 0;
	public final float MOVE_UNIT = 0.0078125F; // unit of movement 
	public EntityDecayPitTargetPart[] parts;
	public EntityDecayPitTargetPart shield_1;
	public EntityDecayPitTargetPart shield_2;
	public EntityDecayPitTargetPart shield_3;
	public EntityDecayPitTargetPart shield_4;
	public EntityDecayPitTargetPart shield_5;
	public EntityDecayPitTargetPart shield_6;
	public EntityDecayPitTargetPart shield_7;
	public EntityDecayPitTargetPart shield_8;
	public EntityDecayPitTargetPart shield_9;
	public EntityDecayPitTargetPart shield_10;
	public EntityDecayPitTargetPart shield_11;
	public EntityDecayPitTargetPart shield_12;
	public EntityDecayPitTargetPart shield_13;
	public EntityDecayPitTargetPart shield_14;
	public EntityDecayPitTargetPart shield_15;
	public EntityDecayPitTargetPart shield_16;
	public EntityDecayPitTargetPart target;
	public EntityDecayPitTargetPart bottom;
	
	private static final DataParameter<Float> ANIMATION_TICKS = EntityDataManager.createKey(EntityDecayPitTarget.class, DataSerializers.FLOAT);
	private static final DataParameter<Boolean> IS_RAISING = EntityDataManager.createKey(EntityDecayPitTarget.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> IS_MOVING = EntityDataManager.createKey(EntityDecayPitTarget.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> IS_SLOW = EntityDataManager.createKey(EntityDecayPitTarget.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> PROGRESS = EntityDataManager.createKey(EntityDecayPitTarget.class, DataSerializers.VARINT);

	public EntityDecayPitTarget(World world) {
		super(world);
		setSize(5F, 5F);
		parts = new EntityDecayPitTargetPart[] {
				shield_1 = new EntityDecayPitTargetPart(this, "part1", 1F, 1F, true),
				shield_2 = new EntityDecayPitTargetPart(this, "part2", 1F, 1F, true),
				shield_3 = new EntityDecayPitTargetPart(this, "part3", 1F, 1F, true),
				shield_4 = new EntityDecayPitTargetPart(this, "part4", 1F, 1F, true),
				shield_5 = new EntityDecayPitTargetPart(this, "part5", 1F, 1F, true),
				shield_6 = new EntityDecayPitTargetPart(this, "part6", 1F, 1F, true),
				shield_7 = new EntityDecayPitTargetPart(this, "part7", 1F, 1F, true),
				shield_8 = new EntityDecayPitTargetPart(this, "part8", 1F, 1F, true),
				shield_9 = new EntityDecayPitTargetPart(this, "part9", 1F, 1F, true),
				shield_10 = new EntityDecayPitTargetPart(this, "part10", 1F, 1F, true),
				shield_11 = new EntityDecayPitTargetPart(this, "part11", 1F, 1F, true),
				shield_12 = new EntityDecayPitTargetPart(this, "part12", 1F, 1F, true),
				shield_13 = new EntityDecayPitTargetPart(this, "part13", 1F, 1F, true),
				shield_14 = new EntityDecayPitTargetPart(this, "part16", 1F, 1F, true),
				shield_15 = new EntityDecayPitTargetPart(this, "part15", 1F, 1F, true),
				shield_16 = new EntityDecayPitTargetPart(this, "part16", 1F, 1F, true),
				target = new EntityDecayPitTargetPart(this, "target", 3F, 2F, false),
				bottom = new EntityDecayPitTargetPart(this, "bottom", 3F, 1F, false),
				};
	}

	@Override
	protected void entityInit() {
		dataManager.register(IS_RAISING, false);
		dataManager.register(IS_MOVING, false);
		dataManager.register(IS_SLOW, true);
		dataManager.register(PROGRESS, 0);
		dataManager.register(ANIMATION_TICKS, 0.0F);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		
		float animationTicks = this.dataManager.get(ANIMATION_TICKS);
		
		animationTicksPrev = animationTicks;
		animationTicksChainPrev = animationTicksChain;
		
		if(!this.world.isRemote) {
			if (animationTicks + 1 >= 360F) {
				this.dataManager.set(ANIMATION_TICKS, 0.0F);
			} else {
				this.dataManager.set(ANIMATION_TICKS, animationTicks + 1);
			}
		}

		while(animationTicks - this.animationTicksPrev < -180.0F) {
			this.animationTicksPrev -= 360.0F;
        }
        while(animationTicks - this.animationTicksPrev >= 180.0F) {
        	this.animationTicksPrev += 360.0F;
        }
		
        //Set prev pos, rotation, etc.
        for(Entity entity : parts) {
        	entity.onUpdate();
        }
        
		setNewShieldHitboxPos(animationTicks, shield_1);
		setNewShieldHitboxPos(-animationTicks + 22.5F, shield_2);
		setNewShieldHitboxPos(animationTicks + 45F, shield_3);
		setNewShieldHitboxPos(-animationTicks + 67.5F, shield_4);
		setNewShieldHitboxPos(animationTicks + 90F, shield_5);
		setNewShieldHitboxPos(-animationTicks + 112.5F, shield_6);
		setNewShieldHitboxPos(animationTicks + 135F, shield_7);
		setNewShieldHitboxPos(-animationTicks + 157F, shield_8);

		setNewShieldHitboxPos(animationTicks + 180F, shield_9);
		setNewShieldHitboxPos(-animationTicks + 202.5F, shield_10);
		setNewShieldHitboxPos(animationTicks + 225F, shield_11);
		setNewShieldHitboxPos(-animationTicks + 247.5F, shield_12);
		setNewShieldHitboxPos(animationTicks + 270F, shield_13);
		setNewShieldHitboxPos(-animationTicks + 292.5F, shield_14);
		setNewShieldHitboxPos(animationTicks + 315F, shield_15);
		setNewShieldHitboxPos(-animationTicks + 337.5F, shield_16);
		
		target.setPosition(posX, posY + 3D, posZ);
		bottom.setPosition(posX, posY, posZ);

		if (isMoving()) {
			if (isSlow())
				animationTicksChain++;
			else
				animationTicksChain += 8;
			if(getHangingChains() != null)
				getHangingChains().setProgress(getProgress());

			if (!isRaising() && getProgress() < MAX_PROGRESS) {
				move(MoverType.SELF, 0D, -MOVE_UNIT * 8D, 0D);
				setProgress(getProgress() + 8);

				if (getHangingChains() != null) {
					getHangingChains().setMoving(true);
					getHangingChains().setSlow(false);
				}

				if (getGroundChains() != null) {
					for (TileEntityDecayPitGroundChain chain : getGroundChains()) {
						chain.setRaising(true);
						chain.setMoving(true);
						chain.setSlow(false);
					}
				}
			}

			if (isRaising() && getProgress() > MIN_PROGRESS) {
				move(MoverType.SELF, 0D, +MOVE_UNIT, 0D);
				setProgress(getProgress() - 1);

				if (getHangingChains() != null) {
					getHangingChains().setMoving(true);
					getHangingChains().setSlow(true);
				}

				if (getGroundChains() != null) {
					for (TileEntityDecayPitGroundChain chain : getGroundChains()) {
						chain.setRaising(false);
						chain.setMoving(true);
						chain.setSlow(true);
					}
				}
			}
		}

		if (animationTicksChainPrev >= 128) {
			animationTicksChain = animationTicksChainPrev = 0;
			setMoving(false);
		}

		if (getProgress() > MAX_PROGRESS)
			setProgress(MAX_PROGRESS);
		
		if (getProgress() < MIN_PROGRESS)
			setProgress(MIN_PROGRESS);

		if (!getEntityWorld().isRemote && getProgress() > MIN_PROGRESS && getEntityWorld().getTotalWorldTime() % 60 == 0) // upsy-daisy
			moveUp();
	}

	protected void setHangingLength(EntityDecayPitTargetPart chain, float extended) {
		chain.height = extended;
		AxisAlignedBB axisalignedbb = new AxisAlignedBB(chain.posX - chain.width * 0.5D, posY + height, chain.posZ - chain.width * 0.5D, chain.posX + chain.width * 0.5D, posY + height + 2F + getProgress() * MOVE_UNIT, chain.posZ + chain.width * 0.5D);
		chain.setEntityBoundingBox(axisalignedbb);
		chain.onUpdate();
	}

	protected void setNewShieldHitboxPos(float animationTicks, EntityDecayPitTargetPart shield) {
		double a = Math.toRadians(animationTicks);
		double offSetX = -Math.sin(a) * 2.825D;
		double offSetZ = Math.cos(a) * 2.825D;
		float wobble = 0F;
		if (shield == shield_1 || shield == shield_3 || shield == shield_5 || shield == shield_7 || shield == shield_9 || shield == shield_11 || shield == shield_13 || shield == shield_15)
			wobble = MathHelper.sin(animationTicks * 0.14F) * 1.2F;
		else
			wobble = -MathHelper.sin(animationTicks * 0.14F) * 1.2F;
		float squarePoint = Math.signum(wobble);
		if(squarePoint == -1F)
			wobble = 0F;
		shield.setPosition(posX + offSetX, target.posY + target.height / 2.0D - shield.height + wobble, posZ + offSetZ);
		shield.rotationYaw = animationTicks + 180F;
		
		while(shield.rotationYaw - shield.prevRotationYaw < -180.0F) {
			shield.prevRotationYaw -= 360.0F;
        }
        while(shield.rotationYaw - shield.prevRotationYaw >= 180.0F) {
        	shield.prevRotationYaw += 360.0F;
        }
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Nullable
	public Entity[] getParts() {
		return parts;
	}

	@Override
	public void addVelocity(double x, double y, double z) {
		motionX = 0D;
		motionY = 0D;
		motionZ = 0D;
	}

	@Override
	public boolean getIsInvulnerable() {
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float damage) {
		return false;
	}

	@Override
	public boolean attackEntityFromPart(EntityDecayPitTargetPart part, DamageSource source, float damage) {
		boolean wasBlocked = false;
		
		if(source instanceof EntityDamageSource) {
			EntityDamageSource entityDamage = (EntityDamageSource) source;
			
			Entity sourceEntity = entityDamage.getTrueSource();
			Entity immediateEntity = entityDamage.getImmediateSource();

			Entity attackingEntity = immediateEntity != null ? immediateEntity : sourceEntity;
			
			if(attackingEntity == null) {
				wasBlocked = true;
			} else {
				Vec3d pos = new Vec3d(attackingEntity.posX, attackingEntity.posY + attackingEntity.getEyeHeight(), attackingEntity.posZ);
				
				Vec3d ray;
				if(attackingEntity instanceof EntityLivingBase) {
					ray = attackingEntity.getLookVec();
				} else {
					ray = new Vec3d(attackingEntity.motionX, attackingEntity.motionY, attackingEntity.motionZ).normalize();
				}
				
				EntityDecayPitTargetPart hitShield = this.rayTraceShields(pos, ray);
				if(hitShield != null) {
					wasBlocked = true;
				}
			}
		}

		if(part == target && !wasBlocked) {
			if(!this.world.isRemote) {
				moveDown();
			}
			return true;
		} else if(wasBlocked) {
			if(!this.world.isRemote && source instanceof EntityDamageSourceIndirect) {
				Entity sourceEntity = ((EntityDamageSourceIndirect) source).getTrueSource();
				if(sourceEntity != null && !world.isAirBlock(sourceEntity.getPosition().down())) {
					EntityRootGrabber grabber = new EntityRootGrabber(this.world, true);
					grabber.setPosition(source.getTrueSource().getPosition().down(), 40);
					getEntityWorld().spawnEntity(grabber);
				}
			}
			return false;
		}

		//TODO This should be done through entity status event sent from server side
		if (getEntityWorld().isRemote)
			if (part == target) {
				shootBeamsAtThings(new Vec3d(0D, -2.5D + getProgress() * MOVE_UNIT, -12D));
				shootBeamsAtThings(new Vec3d(12D, -2.5D + getProgress() * MOVE_UNIT, 0D));
				shootBeamsAtThings(new Vec3d(0D, -2.5D + getProgress() * MOVE_UNIT, 12D));
				shootBeamsAtThings(new Vec3d(-12D, -2.5D + getProgress() * MOVE_UNIT, 0D));
			}
		return false;
	}

	@Nullable
	public EntityDecayPitTargetPart rayTraceShields(Vec3d pos, Vec3d dir) {
		Vec3d ray = dir.normalize().scale(3);
		
		float shieldSize = 0.6F;
		
		Vec3d v0 = new Vec3d(-shieldSize, -shieldSize, 0);
		Vec3d v1 = new Vec3d(shieldSize, -shieldSize, 0);
		Vec3d v2 = new Vec3d(shieldSize, shieldSize, 0);
		Vec3d v3 = new Vec3d(-shieldSize, shieldSize, 0);
		
		for(EntityDecayPitTargetPart shieldPart : parts) {
			if(shieldPart.isShield) {
				Vec3d center = shieldPart.getPositionVector().add(0, shieldPart.height / 2, 0);
				
				this.rotationMatrix.setRotations(0, (float)Math.toRadians(shieldPart.rotationYaw), 0);
				
				Vec3d relPos = this.rotationMatrix.transformVec(pos.subtract(center), Vec3d.ZERO);;
				Vec3d relRay = this.rotationMatrix.transformVec(ray, Vec3d.ZERO);
				
				if(EntityFortressBoss.rayTraceTriangle(relPos, relRay, v0, v1, v2) || EntityFortressBoss.rayTraceTriangle(relPos, relRay, v2, v3, v0)) {
					return shieldPart;
				}
			}
		}
		
		return null;
	}
	
	private void moveUp() {
		if (getProgress() > MIN_PROGRESS) {
			setRaising(true);
			setMoving(true);
			setSlow(true);
		}
	}

	private void moveDown() {
		if (getProgress() < MAX_PROGRESS) {
			setRaising(false);
			setMoving(true);
			setSlow(false);
		}
	}

	@SideOnly(Side.CLIENT)
	private void shootBeamsAtThings(Vec3d target) {
		BatchedParticleRenderer.INSTANCE.addParticle(DefaultParticleBatches.BEAM, BLParticles.PUZZLE_BEAM_2.create(world, this.target.posX + target.x, this.target.posY + this.target.height * 0.5D + target.y, this.target.posZ + target.z, ParticleArgs.get().withMotion(0, 0, 0).withColor(255F, 102F, 0F, 1F).withScale(3.5F).withData(30, target.scale(-1))));
		for(int i = 0; i < 2; i++) {
			float offsetLen = this.world.rand.nextFloat();
			Vec3d offset = new Vec3d(target.x * offsetLen + world.rand.nextFloat() * 0.2f - 0.1f, target.y * offsetLen + world.rand.nextFloat() * 0.2f - 0.1f, target.z * offsetLen + world.rand.nextFloat() * 0.2f - 0.1f);
			float vx = (world.rand.nextFloat() * 2f - 1) * 0.0025f;
			float vy = (world.rand.nextFloat() * 2f - 1) * 0.0025f + 0.008f;
			float vz = (world.rand.nextFloat() * 2f - 1) * 0.0025f;
			float scale = 0.5f + world.rand.nextFloat();
			if(ShaderHelper.INSTANCE.canUseShaders() && world.rand.nextBoolean()) {
				BatchedParticleRenderer.INSTANCE.addParticle(DefaultParticleBatches.HEAT_HAZE_BLOCK_ATLAS, BLParticles.SMOOTH_SMOKE.create(world, this.target.posX + offset.x, this.target.posY + this.target.height * 0.5D + offset.y, this.target.posZ + offset.z, ParticleArgs.get().withMotion(vx, vy, vz).withColor(1, 1, 1, 0.2F).withScale(scale * 8).withData(80, true, 0.0F, true)));
			} else {
				BatchedParticleRenderer.INSTANCE.addParticle(DefaultParticleBatches.TRANSLUCENT_GLOWING_NEAREST_NEIGHBOR, BLParticles.PUZZLE_BEAM.create(world, this.target.posX + offset.x, this.target.posY + this.target.height * 0.5D + offset.y, this.target.posZ + offset.z, ParticleArgs.get().withMotion(vx, vy, vz).withColor(255F, 102F, 0F, 1F).withScale(scale).withData(100)));
			}
		}
	}

	public TileEntityDecayPitHangingChain getHangingChains() {
		TileEntityDecayPitHangingChain tile = null;
		for (int x = -1; x < 1; x++)
			for (int y = 0; y < 15; y++)
				for (int z = -1; z < 1; z++) {
					if (getWorld().getTileEntity(getPosition().add(x, y, z)) instanceof TileEntityDecayPitHangingChain) {
						tile = (TileEntityDecayPitHangingChain) getWorld().getTileEntity(getPosition().add(x, y, z));
						tile.setProgress(getProgress());
						
					}
				}
		return tile;
	}

	public List<TileEntityDecayPitGroundChain> getGroundChains() {
		TileEntityDecayPitGroundChain tile = null;
		List<TileEntityDecayPitGroundChain> chains = new ArrayList<TileEntityDecayPitGroundChain>();
		BlockPos posEntity = getPosition();
		Iterable<BlockPos> blocks = BlockPos.getAllInBox(posEntity.add(-12.0F, 3F, -12F), posEntity.add(12F, 9F, 12F));
		for (BlockPos pos : blocks)
			if (getWorld().getTileEntity(pos) instanceof TileEntityDecayPitGroundChain) {
				tile = (TileEntityDecayPitGroundChain) getWorld().getTileEntity(pos);
				chains.add(tile);
			}
		return chains;
	}

	public void setProgress(int progress) {
		dataManager.set(PROGRESS, progress);
	}

	public int getProgress() {
		return dataManager.get(PROGRESS);
	}

	public void setRaising(boolean raising) {
		dataManager.set(IS_RAISING, raising);
	}

	public boolean isRaising() {
		return dataManager.get(IS_RAISING);
	}

	public void setMoving(boolean moving) {
		dataManager.set(IS_MOVING, moving);
	}
	
	public boolean isMoving() {
		return dataManager.get(IS_MOVING);
	}

	public void setSlow(boolean slow) {
		dataManager.set(IS_SLOW, slow);
	}

	public boolean isSlow() {
		return dataManager.get(IS_SLOW);
	}

	@Override
	public World getWorld() {
		return getEntityWorld();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		setProgress(nbt.getInteger("progress"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("progress", getProgress());
	}
}