package com.flansmod.client.gui;

import java.util.Random;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;

import com.flansmod.client.ClientProxy;
import com.flansmod.client.model.GunAnimations;
import com.flansmod.common.FlansMod;
import com.flansmod.common.guns.ContainerGunModTable;
import com.flansmod.common.guns.GunType;
import com.flansmod.common.guns.ItemGun;
import com.flansmod.common.guns.Paintjob;
import com.flansmod.common.network.PacketGunPaint;

public class GuiGunModTable extends GuiContainer 
{
	private static final ResourceLocation texture = new ResourceLocation("flansmod", "gui/gunTable.png");
	private static final Random rand = new Random();
	private Paintjob hoveringOver = null;
	private int mouseX, mouseY;
	private InventoryPlayer inventory;
	
	public GuiGunModTable(InventoryPlayer inv, World w) 
	{
		super(new ContainerGunModTable(inv, w));
		inventory = inv;
		ySize = 256;
	}
	
    @Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
    {
    	fontRendererObj.drawString("Inventory", 8, (ySize - 94) + 2, 0x404040);
    	fontRendererObj.drawString("Gun Modification Table", 8, 6, 0x404040);
       	
		//If a gun is in the table, display its 3D model.
        ItemStack gunStack = inventorySlots.getSlot(0).getStack();
        if(gunStack != null && gunStack.getItem() instanceof ItemGun)
        {
        	GunType gunType = ((ItemGun)gunStack.getItem()).type;
        	if(gunType.model != null)
        	{
        		GL11.glPushMatrix();
				{
					GL11.glColor3f(1F, 1F, 1F);
					GL11.glTranslatef(110, 54, 100);
					
					GL11.glRotatef(160, 1F, 0F, 0F);
					GL11.glRotatef(20, 0F, 1F, 0F);
					GL11.glScalef(-50F, 50F, 50F);
					RenderHelper.enableStandardItemLighting();
					ClientProxy.gunRenderer.renderGun(gunStack, gunType, 1F / 16F, gunType.model, GunAnimations.defaults, 0F, IItemRenderer.ItemRenderType.ENTITY);
				}
        		GL11.glPopMatrix();
        	}
        }
    }
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) 
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.renderEngine.bindTexture(texture);
		FontRenderer fontrenderer = mc.fontRenderer;
        
        int xOrigin = (width - xSize) / 2;
        int yOrigin = (height - ySize) / 2;
        drawTexturedModalRect(xOrigin, yOrigin, 0, 0, xSize, ySize);
                
        for(int z = 1; z < 14; z++)
        	inventorySlots.getSlot(z).yDisplayPosition = -1000;
        
        ItemStack gunStack = inventorySlots.getSlot(0).getStack();
        if(gunStack != null && gunStack.getItem() instanceof ItemGun)
        {
        	GunType gunType = ((ItemGun)gunStack.getItem()).type;
        	if(gunType.allowBarrelAttachments)
        	{
        		drawTexturedModalRect(xOrigin + 51, yOrigin + 107, 176, 122, 22, 22);
        		inventorySlots.getSlot(1).yDisplayPosition = 110;
        	}
        	if(gunType.allowScopeAttachments)
        	{
        		drawTexturedModalRect(xOrigin + 77, yOrigin + 81, 202, 96, 22, 22);
        		inventorySlots.getSlot(2).yDisplayPosition = 84;
        	}
        	if(gunType.allowStockAttachments)
        	{
        		drawTexturedModalRect(xOrigin + 103, yOrigin + 107, 228, 122, 22, 22);
        		inventorySlots.getSlot(3).yDisplayPosition = 110;
        	}
        	if(gunType.allowGripAttachments)
        	{
        		drawTexturedModalRect(xOrigin + 77, yOrigin + 133, 202, 148, 22, 22);
        		inventorySlots.getSlot(4).yDisplayPosition = 136;
        	}
        	if(gunType.allowGadgetAttachments)
        	{
        		drawTexturedModalRect(xOrigin + 51, yOrigin + 133, 176, 96, 22, 22);
        		inventorySlots.getSlot(5).yDisplayPosition = 136;
        	}
        	
        	for(int x = 0; x < 2; x++)
        	{
        		for(int y = 0; y < 4; y++)
        		{
        			if(x + y * 2 < gunType.numGenericAttachmentSlots)
        			{
        				inventorySlots.getSlot(6 + x + y * 2).yDisplayPosition = 83 + 18 * y;
        				drawTexturedModalRect(xOrigin + 9 + 18 * x, yOrigin + 82 + 18 * y, 178, 54, 18, 18);
        			}
        		}
        	}
        	
			//Get the number of paintjobs
        	int numPaintjobs = gunType.paintjobs.size();
        	int numRows = numPaintjobs / 2 + 1;
        	
            for(int y = 0; y < numRows; y++)
            {
            	for(int x = 0; x < 2; x++)
            	{
            		//If this row has only one paintjob, don't try and render the second one
            		if(2 * y + x >= numPaintjobs)
            			continue;
            		
            		drawTexturedModalRect(xOrigin + 131 + 18 * x, yOrigin + 82 + 18 * y, 178, 54, 18, 18);
            	}
            }
			
        	//Display paintjob slots
            for(int y = 0; y < numRows; y++)
            {
            	for(int x = 0; x < 2; x++)
            	{
            		//If this row has only one paintjob, don't try and render the second one
            		if(2 * y + x >= numPaintjobs)
            			continue;
            		
            		Paintjob paintjob = gunType.paintjobs.get(2 * y + x);
            		ItemStack stack = gunStack.copy();
            		stack.stackTagCompound.setString("Paint", paintjob.iconName);
            		itemRender.renderItemIntoGUI(this.fontRendererObj, mc.getTextureManager(), stack, xOrigin + 132 + x * 18, yOrigin + 83 + y * 18);
            	}
            }
        }
        
        //Draw hover box for paintjob
        if(hoveringOver != null)
        {
        	int originX = mouseX + 6;
        	int originY = mouseY - 20;
        	
        	//Get the paintjob name
			String paintjobName = hoveringOver.paintjobName;
			
			if (paintjobName.equals("<default>"))
			paintjobName = "Default";
	        
			if(!paintjobName.equals("null"))
			{
				GL11.glColor4f(1F, 1F, 1F, 1F);
	        	GL11.glDisable(GL11.GL_LIGHTING);
	        	
	        	int stringLength = mc.fontRenderer.getStringWidth(paintjobName);
				
	        	drawRect(originX - Math.round(stringLength/2) - 1, originY + 1, originX + Math.round(stringLength/2) + 2, originY + 2 + 10, 0x54000000);
				
				drawCenteredString(fontrenderer, paintjobName, originX + 0, originY + 3, 0xffffff);
			}
			
			int numDyes = hoveringOver.dyesNeeded.length;
        	//Only draw box if there are dyes needed
        	if(numDyes != 0 && !inventory.player.capabilities.isCreativeMode)
        	{
        		//Calculate which dyes we have in our inventory
	        	boolean[] haveDyes = new boolean[numDyes];
	        	for(int n = 0; n < numDyes; n++)
	        	{
	        		int amountNeeded = hoveringOver.dyesNeeded[n].stackSize;
	        		for(int s = 0; s < inventory.getSizeInventory(); s++)
	        		{
	        			ItemStack stack = inventory.getStackInSlot(s);
	        			if(stack != null && stack.getItem() == Items.dye && stack.getItemDamage() == hoveringOver.dyesNeeded[n].getItemDamage())
	        			{
	        				amountNeeded -= stack.stackSize;
	        			}
	        		}
					if(amountNeeded <= 0)
						haveDyes[n] = true;
	        	}
	        	        	
	        	GL11.glColor4f(1F, 1F, 1F, 1F);
	        	GL11.glDisable(GL11.GL_LIGHTING);
	        	mc.renderEngine.bindTexture(texture);
	
				//If we have only one, use the double ended slot 
	        	if(numDyes == 1)
	        	{
	        		drawTexturedModalRect(originX, originY + 12, (haveDyes[0] ? 201 : 178), 218, 22, 22);
	        	}
	        	else
	        	{
	        		//First slot
	        		drawTexturedModalRect(originX, originY + 12, 178, (haveDyes[0] ? 195 : 172), 20, 22);
	        		//Middle slots
	        		for(int s = 1; s < numDyes - 1; s++)
	        		{
	        			drawTexturedModalRect(originX + 2 + 18 * s, originY + 12, 199, (haveDyes[s] ? 195 : 172), 18, 22);
	        		}
	        		//Last slot
	        		drawTexturedModalRect(originX + 2 + 18 * (numDyes - 1), originY + 12, 218, (haveDyes[numDyes - 1] ? 195 : 172), 20, 22);
	        	}
	        	
	        	for(int s = 0; s < numDyes; s++)
	        	{
	        		itemRender.renderItemIntoGUI(this.fontRendererObj, mc.getTextureManager(), hoveringOver.dyesNeeded[s], originX + 3 + s * 18, originY + 3 + 12);
	        		itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, mc.getTextureManager(), hoveringOver.dyesNeeded[s], originX + 3 + s * 18, originY + 3 + 12);
	        	}
        	}
        }
	}
	
	@Override
    public void handleMouseInput()
	{
		super.handleMouseInput();
		
		mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
		mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;	

		int mouseXInGUI = mouseX - guiLeft;
		int mouseYInGUI = mouseY - guiTop;
		
		hoveringOver = null;
		
		ItemStack gunStack = inventorySlots.getSlot(0).getStack();
        if(gunStack != null && gunStack.getItem() instanceof ItemGun)
        {
        	GunType gunType = ((ItemGun)gunStack.getItem()).type;
        	int numPaintjobs = gunType.paintjobs.size();
        	int numRows = numPaintjobs / 2 + 1;
        	
            for(int j = 0; j < numRows; j++)
            {
            	for(int i = 0; i < 2; i++)
            	{
            		if(2 * j + i >= numPaintjobs)
            			continue;
            		
            		Paintjob paintjob = gunType.paintjobs.get(2 * j + i);
            		ItemStack stack = gunStack.copy();
            		stack.stackTagCompound.setString("Paint", paintjob.iconName);
            		int slotX = 131 + i * 18;
            		int slotY = 82 + j * 18;
            		if(mouseXInGUI >= slotX && mouseXInGUI < slotX + 18 && mouseYInGUI >= slotY && mouseYInGUI < slotY + 18)
            			hoveringOver = paintjob;
            	}
            }
        }
	}
	
	@Override
    protected void mouseClicked(int x, int y, int button)
    {
		super.mouseClicked(x, y, button);
		if(button != 0)
			return;
		if(hoveringOver == null)
			return;
		
		FlansMod.getPacketHandler().sendToServer(new PacketGunPaint(hoveringOver.iconName));
		((ContainerGunModTable)inventorySlots).clickPaintjob(hoveringOver);
    }
}
