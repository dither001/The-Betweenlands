package thebetweenlands.common.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

public class TileEntityWeedwoodWorkbench extends TileEntity {
	public NonNullList<ItemStack> craftingSlots = NonNullList.withSize(9, ItemStack.EMPTY);
	public ItemStack craftResult;
	public byte rotation = 0;

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.writeNbt(new NBTTagCompound()));
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		this.readNBT(packet.getNbtCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.readNBT(nbt);
	}

	private NBTTagCompound readNBT(NBTTagCompound nbt) {
		NBTTagList items = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);

		int count = items.tagCount();
		for (int i = 0; i < count; i++) {
			NBTTagCompound nbtItem = items.getCompoundTagAt(i);
			this.craftingSlots.set(nbtItem.getByte("Slot"), new ItemStack(nbtItem));
		}

		this.rotation = nbt.getByte("Rotation");

		return nbt;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		return this.writeNbt(nbt);
	}

	private NBTTagCompound writeNbt(NBTTagCompound nbt) {
		NBTTagList items = new NBTTagList();

		for (int i = 0; i < craftingSlots.size(); i++) {
			if (!this.craftingSlots.get(i).isEmpty()) {
				NBTTagCompound nbtItem = new NBTTagCompound();
				nbtItem.setByte("Slot", (byte) i);
				this.craftingSlots.get(i).writeToNBT(nbtItem);
				items.appendTag(nbtItem);
			}
		}

		nbt.setTag("Items", items);

		nbt.setByte("Rotation", this.rotation);

		return nbt;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeNbt(super.getUpdateTag());
	}
}
