/**
 * 2013.06.17 - Update for Minecraft 1.5.2 and MinecraftFortge 7.8.1.737 by 
 * Cyanobacterium (Minecraft user Synechocystis)
 */
package TombStone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import TombStone.client.ClientProxy;

import net.minecraft.block.Block;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ModLoader;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraftforge.common.Configuration;

@Mod(modid="tombstone", name="TombStone", version="0.7.5")
@NetworkMod(clientSideRequired=true, serverSideRequired=false)
public class TombStone {
	public static int tombStoneBlockId;
	
	// texture instances
	public static ResourceLocation tombstoneTex1;
	public static ResourceLocation tombstoneTex2;
	public static ResourceLocation tombstoneGUI;
	
	public static TombStoneBlock tombStoneBlock;
	
	public static boolean canCraft = true;
	
	public static String dateFormat = "m/d/y";
	
	/** security level: 
	 * 0=anyone can loot from tombstone, 
	 * 1=only same team can loot from tombstone, 
	 * 2+=only the owner can loot from tombstone */
	public static int security = 0;
	
	//Keeps track of the existing tombs
	public static List<TombStoneTileEntity> tombList = new ArrayList<TombStoneTileEntity>();
	
	// The instance of your mod that Forge uses.
	@Instance("tombstone")
	public static TombStone instance;
	
	// Says where the client and server 'proxy' code is loaded.
	@SidedProxy(clientSide="TombStone.client.ClientProxy", serverSide="TombStone.CommonProxy")
	public static CommonProxy proxy;
	
	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		// Stub Method
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		tombStoneBlockId = config.getBlock("blockID", 3000).getInt();
		tombStoneBlock = new TombStoneBlock(tombStoneBlockId);
		
		canCraft = config.get("Options", "can_craft_tombstones", canCraft,"if true, then decorative tombstones can be crafted").getBoolean(false);
		security = config.get("Options", "security_level", security, "Security access to tombstone loot: 0=public, 1=team, 2=owner only").getInt();
		dateFormat = config.get("Options", "date_format", "m/d/y","Format of date on tombstones. American style is m/d/y while international standard is y-m-d").getString();
		
		
		config.save();
		
		tombstoneTex1 = new ResourceLocation("tombstone:textures/models/tombstone.png");
		tombstoneTex2 = new ResourceLocation("tombstone:textures/models/tombstone2.png");
		tombstoneGUI = new ResourceLocation("minecraft:textures/gui/container/generic_54.png");
	}
	
	@Init
	public void load(FMLInitializationEvent event) {
		proxy.registerRenderers();
		
		if(event.getSide().isClient())
			ClientProxy.setCustomRenderers();
		
		//Register the tombstone block
		GameRegistry.registerBlock(tombStoneBlock, "tombStoneBlock");
		LanguageRegistry.addName(tombStoneBlock, "TombStone Block");
		MinecraftForge.setBlockHarvestLevel(tombStoneBlock, "pickaxe", 0);
		
		//Register the death hook
		MinecraftForge.EVENT_BUS.register(new DeathEventHook());
		
		//Register the tombstone tile entity
		GameRegistry.registerTileEntity(TombStoneTileEntity.class, "tombStoneTileEntity");

		//Register the tombstone gui
		NetworkRegistry.instance().registerGuiHandler(this, new TombStoneGUIHandler());
		    
		//Item stack (ID, Count, Meta)
		ItemStack stoneStack = new ItemStack(Block.stone);
		ItemStack signStack = new ItemStack(Item.sign);
		
		//3x3 shaped crafting
		if(canCraft){
			GameRegistry.addRecipe(new ItemStack(tombStoneBlock), " x ", "xyx", "xxx",
		    'x', stoneStack, 'y', signStack);
		}    
	}
	
	@PostInit
	public void postInit(FMLPostInitializationEvent event) {
		// Stub Method
	}
	
	@ServerStarting
	public void onServerStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new ChatHandler());
	}
}