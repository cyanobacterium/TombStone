package TombStone;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet130UpdateSign;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;

public class DeathEventHook {
	
	public DeathEventHook()
	{
		
	}
	
	@ForgeSubscribe
	public void onEntityDeath(PlayerDropsEvent event)
	{
		EntityPlayer deadPlayer = event.entityPlayer;
		DamageSource attackSource = event.source;
		ArrayList<EntityItem> drops = event.drops;
		World world = deadPlayer.worldObj;
		
		
		//DEBUG//
		//FMLLog.log(Level.WARNING, "[TombStone] onEntityDeath(): " + attackSource.getDeathMessage(deadPlayer));
		
		//Calculate the spot to put the tombstone
		int tombX = (int) Math.floor(deadPlayer.posX);
		int tombY = (int) Math.floor(deadPlayer.posY);
		int tombZ = (int) Math.floor(deadPlayer.posZ);
		if(tombY < 0){
			// You fell into the void! Sucks to be you.
			return;
		} else if(tombY > 255){
			tombY = 255;
		}
		int rotation = TombStoneTileEntity.getRotationFromEntity(deadPlayer);
		// get team info for security
		Team t = deadPlayer.getTeam();
		String teamName;
		if(t != null){
			teamName = t.func_96661_b();
		} else {
			teamName = TombStoneTileEntity.nonteamName;
		}
		
		// move down to surface if in air
		if(world.isAirBlock(tombX, tombY, tombZ)){
			while(tombY > 0 && world.isAirBlock(tombX, tombY-1, tombZ)){
				tombY--;
			}
		}
		// move up to surface if buried
		while(tombY < 255 && world.isAirBlock(tombX, tombY, tombZ) == false){
			tombY++;
		}
		
		String dateOfDeath = TombStone.dateFormat
				.replace("m", world.getCurrentDate().get(Calendar.MONTH)+"")
				.replace("d",  world.getCurrentDate().get(Calendar.DAY_OF_MONTH)+"")
				.replace("y",  world.getCurrentDate().get(Calendar.YEAR)+"");
		String deathMessage = attackSource.getDeathMessage(deadPlayer) + " here\n Died " + dateOfDeath;
		
		//Place the tombstone
		world.setBlock(tombX, tombY, tombZ, TombStone.instance.tombStoneBlockId, rotation, 1 | 2);
		TombStoneTileEntity blockTileEntity = (TombStoneTileEntity) world.getBlockTileEntity(tombX, tombY, tombZ);
		
		//Move all items from the list to the tombstone inventory
		for(int i=0; i<drops.size(); i++)
		{
			ItemStack playerItem = drops.get(i).getEntityItem();
			blockTileEntity.setInventorySlotContents(i, playerItem);
		}
		//Set the other meta-data for the tile entity
		blockTileEntity.setOwner(deadPlayer.getEntityName());
		blockTileEntity.setTeam(teamName);
		blockTileEntity.setDeathText(deathMessage);
		blockTileEntity.setIsCrafted(false);
	//	blockTileEntity.setRotation(rotation); // rotation handled by metadata (just like a sign)
		
		event.setCanceled(true);
	}
}
