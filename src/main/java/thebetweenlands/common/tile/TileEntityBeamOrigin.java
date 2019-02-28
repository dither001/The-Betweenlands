package thebetweenlands.common.tile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thebetweenlands.common.TheBetweenlands;
import thebetweenlands.common.block.structure.BlockBeamOrigin;
import thebetweenlands.common.block.structure.BlockBeamRelay;
import thebetweenlands.common.block.structure.BlockDiagonalEnergyBarrier;
import thebetweenlands.common.block.structure.BlockEnergyBarrierMud;
import thebetweenlands.common.network.clientbound.PacketParticle;
import thebetweenlands.common.network.clientbound.PacketParticle.ParticleType;

public class TileEntityBeamOrigin extends TileEntity implements ITickable {

	public boolean active;

	public TileEntityBeamOrigin() {
		super();
	}

	@Override
	public void update() {
		if (getWorld().getBlockState(getPos()).getBlock() != null) {
			if (getWorld().getBlockState(getPos()).getValue(BlockBeamOrigin.POWERED)) {
				if (!active)
					setActive(true);
			}

			if (!getWorld().getBlockState(getPos()).getValue(BlockBeamOrigin.POWERED)) {
				if (active)
					setActive(false);
			}
		}

		if (!getWorld().isRemote) {
			if (active)
				activateBlock();
			else
				deactivateBlock();
		}
	}

	//Temp particles
	private void sendParticleMessage(EnumFacing facing) {
		int distance = getDistanceToObstruction(facing);
		BlockPos targetPos = getPos().offset(facing, distance);
		Vec3d vector = new Vec3d((targetPos.getX() + 0.5D) - (getPos().getX() + 0.5D), (targetPos.getY() + 0.5D) - (getPos().getY() + 0.5D), (targetPos.getZ() + 0.5D) - (getPos().getZ() + 0.5D));
		vector = vector.normalize();
		for (float i = 0; i < distance; i += 0.125F)
			TheBetweenlands.networkWrapper.sendToAll(new PacketParticle(ParticleType.FLAME, getPos().getX() + 0.5F + ((float) vector.x * i), getPos().getY() + 0.5F + ((float) vector.y * i), getPos().getZ() + 0.5F + ((float) vector.z * i), 0F));
	}

	public void activateBlock() {
		IBlockState state = getWorld().getBlockState(getPos());
		EnumFacing facing = state.getValue(BlockBeamOrigin.FACING);

		BlockPos targetPos = getPos().offset(facing, getDistanceToObstruction(facing));
		IBlockState stateofTarget = getWorld().getBlockState(targetPos);

		if (getWorld().getTotalWorldTime() % 10 == 0)
			sendParticleMessage(facing);
		if (stateofTarget.getBlock() instanceof BlockBeamRelay) {
			if (getWorld().getTileEntity(targetPos) instanceof TileEntityBeamRelay) {
				TileEntityBeamRelay targetTile = (TileEntityBeamRelay) getWorld().getTileEntity(targetPos);
				targetTile.setTargetIncomingBeam(facing.getOpposite(), true);
				if (!getWorld().getBlockState(targetPos).getValue(BlockBeamRelay.POWERED)) {
					stateofTarget = stateofTarget.cycleProperty(BlockBeamRelay.POWERED);
					getWorld().setBlockState(targetPos, stateofTarget, 3);
				}
			}
		}
	}

	public void deactivateBlock() {
		IBlockState state = getWorld().getBlockState(getPos());
		EnumFacing facing = state.getValue(BlockBeamOrigin.FACING);

		BlockPos targetPos = getPos().offset(facing, getDistanceToObstruction(facing));
		IBlockState stateofTarget = getWorld().getBlockState(targetPos);

		if (stateofTarget.getBlock() instanceof BlockBeamRelay) {
			if (getWorld().getTileEntity(targetPos) instanceof TileEntityBeamRelay) {
				TileEntityBeamRelay targetTile = (TileEntityBeamRelay) getWorld().getTileEntity(targetPos);
				targetTile.setTargetIncomingBeam(facing.getOpposite(), false);
				if (!targetTile.isGettingBeamed())
					if (getWorld().getBlockState(targetPos).getValue(BlockBeamRelay.POWERED)) {
						stateofTarget = stateofTarget.cycleProperty(BlockBeamRelay.POWERED);
						getWorld().setBlockState(targetPos, stateofTarget, 3);
					}
			}
		}
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	public void setActive(boolean isActive) {
		active = isActive;
		getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
	}

	public int getDistanceToObstruction(EnumFacing facing) {
		int distance = 0;
		for (distance = 1; distance < 14; distance++) {
			IBlockState state = getWorld().getBlockState(getPos().offset(facing, distance));
			if (state != Blocks.AIR.getDefaultState() && !(state.getBlock() instanceof BlockDiagonalEnergyBarrier)&& !(state.getBlock() instanceof BlockEnergyBarrierMud))
				break;
		}
		return distance;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("active", active);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		active = nbt.getBoolean("active");
	}

	@Override
    public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbt = new NBTTagCompound();
        return writeToNBT(nbt);
    }

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return new SPacketUpdateTileEntity(getPos(), 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		readFromNBT(packet.getNbtCompound());
	}
}
