package com.xcom.oneblocksolutions;

import com.xcom.oneblocksolutions.gui.OneBlockSolutionsGuiHandler;
import com.xcom.oneblocksolutions.init.Blocks;
import com.xcom.oneblocksolutions.network.PacketHandler;
import com.xcom.oneblocksolutions.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MODID, name = Reference.MODNAME, version = Reference.VERSION, acceptedMinecraftVersions = Reference.ACCEPTED_MINECRAFT_VERSIONS)
public class OneBlockSolutions {
    public static final Logger logger = LogManager.getLogger(Reference.MODID);

    @SidedProxy(clientSide = "com.xcom.oneblocksolutions.proxy.ClientProxy", serverSide = "com.xcom.oneblocksolutions.proxy.CommonProxy")
    public static CommonProxy proxy;
    
    @Mod.Instance
    public static OneBlockSolutions instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PacketHandler.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new OneBlockSolutionsGuiHandler());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}
