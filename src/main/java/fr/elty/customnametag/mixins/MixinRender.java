package fr.elty.customnametag.mixins;

import fr.elty.customnametag.CustomNametagMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Mixin(Render.class)
public abstract class MixinRender<T extends Entity> {

    @Shadow public abstract FontRenderer getFontRendererFromRenderManager();

    @Unique
    private static final ResourceLocation ICON_TEXTURE = new ResourceLocation(CustomNametagMod.MODID, "icon/femboy.png");

    @Unique
    private static final String SPACING = "   ";

    @Unique
    private boolean isPlayer;

    @Unique
    private String nametag;

    @Inject(method = "renderLivingLabel", at = @At("HEAD"))
    private void isPlayer(T entity, String nametag, double x, double y, double z, int maxDistance, CallbackInfo ci) {
        isPlayer = entity instanceof EntityOtherPlayerMP;
    }

    @ModifyArg(method = "renderLivingLabel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;getStringWidth(Ljava/lang/String;)I", ordinal = 0))
    private String changeGrayAreaWidth(String str) {
        nametag = str;
        if (isPlayer)
            return SPACING + str;
        else
            return str;
    }

    @ModifyArg(method = "renderLivingLabel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I"), index = 1)
    private int changeStringSize(int x) {
        if (!isPlayer) return x;
        FontRenderer fontRenderer = getFontRendererFromRenderManager();
        return -fontRenderer.getStringWidth(SPACING + nametag)/2 + fontRenderer.getStringWidth(SPACING) - 1;
    }

    @Inject(method = "renderLivingLabel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;enableTexture2D()V", shift = At.Shift.AFTER))
    private void renderIcon(T entity, String nametag, double x, double y, double z, int maxDistance, CallbackInfo ci) {
        if (!isPlayer) return;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ICON_TEXTURE);
        int size = getFontRendererFromRenderManager().getStringWidth(SPACING + nametag)/2;
        worldRenderer.pos((double)(-size), (double)(-1), 0.0D).tex(0, 1).endVertex();
        worldRenderer.pos((double)(-size), (double)(8), 0.0D).tex(0, 0).endVertex();
        worldRenderer.pos((double)(-size + 9), (double)(8), 0.0D).tex(1,0 ).endVertex();
        worldRenderer.pos((double)(-size + 9), (double)(-1), 0.0D).tex(1, 1).endVertex();
        tessellator.draw();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
    }
}
