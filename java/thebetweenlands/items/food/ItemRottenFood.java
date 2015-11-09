package thebetweenlands.items.food;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import thebetweenlands.manual.gui.entries.IManualEntryItem;

public class ItemRottenFood extends ItemFood implements IManualEntryItem
{
    public ItemRottenFood() {
        super(-1, -1.0F, false);
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        super.onFoodEaten(stack, world, player);

        if( player != null ) {
            player.addPotionEffect(new PotionEffect(Potion.hunger.getId(), 200, 1));
            player.addPotionEffect(new PotionEffect(Potion.poison.getId(), 200, 1));
        }
    }

    @Override
    public String manualName(int meta) {
        return "rottenFood";
    }

    @Override
    public Item getItem() {
        return this;
    }

    @Override
    public int[] recipeType(int meta) {
        return new int[0];
    }

    @Override
    public int[] metas( ) {
        return new int[0];
    }
}
