package com.flansmod.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.opengl.GL11;

import com.flansmod.client.FlansModClient;

import com.flansmod.client.tmt.ModelRendererTurbo;

public class ModelAttachment extends ModelBase 
{
	public ModelRendererTurbo[] attachmentModel = new ModelRendererTurbo[0];
	public ModelRendererTurbo[] bipodRaisedModel = new ModelRendererTurbo[0];
	public ModelRendererTurbo[] bipodDeployedModel = new ModelRendererTurbo[0];
	
	
	//lighting stuff
	private static float lightmapLastX;
    private static float lightmapLastY;
	private static boolean optifineBreak = false;
	
	public static void glowOn()
	{
		glowOn(15);
	}
	
    public static void glowOn(int glow)
    {
        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        
        GL11.glEnable(GL11.GL_BLEND);
        //GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        
        try {
        	lightmapLastX = OpenGlHelper.lastBrightnessX;
        	lightmapLastY = OpenGlHelper.lastBrightnessY;
        } catch(NoSuchFieldError e) {
        	optifineBreak = true;
        }
        
        float glowRatioX = Math.min((glow/15F)*240F + lightmapLastX, 240);
        float glowRatioY = Math.min((glow/15F)*240F + lightmapLastY, 240);
        
        if(!optifineBreak)
        {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, glowRatioX, glowRatioY);        	
        }
    }

    public static void glowOff() 
    {
        GL11.glEnable(GL11.GL_LIGHTING);
    	if(!optifineBreak)
    	{
    		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapLastX, lightmapLastY);
    	}
    	
        GL11.glPopAttrib();
    }
	
	/** For big scopes, so that the player actually looks through them properly */
	public float renderOffset = 0F;
	
	public void renderAttachment(float f)
	{
		for(ModelRendererTurbo model : attachmentModel)
			if(model != null)
				model.render(f);
		for(ModelRendererTurbo model : bipodRaisedModel)
			if(model != null)
				model.render(f);
	}
	
	public void renderDeployedBipod(float f)
	{
		for(ModelRendererTurbo model : attachmentModel)
			if(model != null)
				model.render(f);
		for(ModelRendererTurbo model : bipodDeployedModel)
			if(model != null)
				model.render(f);
	}

	public void flipAll()
	{
		for (ModelRendererTurbo anAttachmentModel : attachmentModel) {
			anAttachmentModel.doMirror(false, true, true);
			anAttachmentModel.setRotationPoint(anAttachmentModel.rotationPointX, -anAttachmentModel.rotationPointY, -anAttachmentModel.rotationPointZ);
		}
	}
}