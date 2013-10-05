package TombStone;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

public class TombStoneItemRenderer implements IItemRenderer
{
   
     private final TombStoneModel modelBox;
    public TombStoneItemRenderer()
    {
    	modelBox = new TombStoneModel();
    }
    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        return true;
    }
     
    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return true;
    }
     
    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        switch(type)
        {
            case ENTITY:{
                renderTombStoneItem(0f, 0f, 0f, 1.0f);
                return;
            }
             
            case EQUIPPED:{
                renderTombStoneItem(0f, 1f, 1f, 1f);
                return;
            }
            case EQUIPPED_FIRST_PERSON:{
            	renderTombStoneItem(0f, 0f, 0f, 1.0f);
                return;
            }
            case INVENTORY:{
                renderTombStoneItem(0f, 0f, 0f, 1.0f);
                return;
            }
             
            default:return;
        }
    }
    
    private void renderTombStoneItem(float x, float y, float z, float scale)
    {
    GL11.glPushMatrix();
	
	// disable lighting in inventory render
    GL11.glDisable(GL11.GL_LIGHTING);
	
    GL11.glTranslatef((float)x + 0.5F, (float)y + 0.75F * 1.0F, (float)z + 0.5F);

    GL11.glScalef(scale, scale, scale);
	
    FMLClientHandler.instance().getClient().renderEngine.getTexture(TombStone.tombstoneTex1);
	this.modelBox.renderBase();
	FMLClientHandler.instance().getClient().renderEngine.getTexture(TombStone.tombstoneTex2);
	this.modelBox.renderHeadstone();
	
	// re-enable lighting
	GL11.glEnable(GL11.GL_LIGHTING);
	
	GL11.glPopMatrix();
    }
}
