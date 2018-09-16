package fi.dy.masa.lowtechcrafting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import fi.dy.masa.lowtechcrafting.gui.LowTechCraftingGuiHandler;
import fi.dy.masa.lowtechcrafting.network.PacketHandler;
import fi.dy.masa.lowtechcrafting.proxy.CommonProxy;
import fi.dy.masa.lowtechcrafting.reference.Reference;

@Mod(modid = Reference.MOD_ID, name = Reference.MODNAME, version = Reference.VERSION, acceptedMinecraftVersions = Reference.ACCEPTED_MINECRAFT_VERSIONS)
public class LowTechCrafting
{
    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

    @SidedProxy(clientSide = "fi.dy.masa.lowtechcrafting.proxy.ClientProxy", serverSide = "fi.dy.masa.lowtechcrafting.proxy.CommonProxy")
    public static CommonProxy proxy;
    
    @Mod.Instance
    public static LowTechCrafting instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        PacketHandler.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new LowTechCraftingGuiHandler());
    }
}
