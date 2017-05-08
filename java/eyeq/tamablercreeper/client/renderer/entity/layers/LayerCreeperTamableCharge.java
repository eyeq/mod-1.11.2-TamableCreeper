package eyeq.tamablercreeper.client.renderer.entity.layers;

import eyeq.tamablercreeper.entity.passive.EntityCreeperTamable;
import eyeq.tamablercreeper.client.model.ModelCreeperTamable;
import eyeq.tamablercreeper.client.renderer.entity.RenderCreeperTamable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCreeperTamableCharge implements LayerRenderer<EntityCreeperTamable> {
    private static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    private final RenderCreeperTamable render;
    private final ModelCreeperTamable model = new ModelCreeperTamable(2.0F);

    public LayerCreeperTamableCharge(RenderCreeperTamable render) {
        this.render = render;
    }

    public void doRenderLayer(EntityCreeperTamable entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if(!entity.getPowered()) {
            return;
        }
        boolean isInvisible = entity.isInvisible();
        GlStateManager.depthMask(!isInvisible);
        this.render.bindTexture(LIGHTNING_TEXTURE);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        float f = entity.ticksExisted + partialTicks;
        GlStateManager.translate(f * 0.01F, f * 0.01F, 0.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.enableBlend();
        float color = 0.5F;
        GlStateManager.color(color, color, color, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        this.model.setModelAttributes(this.render.getMainModel());
        Minecraft.getMinecraft().entityRenderer.func_191514_d(true);
        this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        Minecraft.getMinecraft().entityRenderer.func_191514_d(false);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(isInvisible);
    }

    public boolean shouldCombineTextures() {
        return false;
    }
}
