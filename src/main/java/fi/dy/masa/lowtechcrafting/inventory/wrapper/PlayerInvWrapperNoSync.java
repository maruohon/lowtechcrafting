package fi.dy.masa.lowtechcrafting.inventory.wrapper;

import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;

public class PlayerInvWrapperNoSync extends CombinedInvWrapper
{
    public PlayerInvWrapperNoSync(Inventory inv)
    {
        super(new PlayerMainInvWrapperNoSync(inv), new PlayerArmorInvWrapperLimited(inv), new PlayerOffhandInvWrapper(inv));
    }
}
