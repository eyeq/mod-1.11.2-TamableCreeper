package eyeq.tamablercreeper.client.renderer.entity;

import eyeq.tamablercreeper.entity.passive.EntityCreeperTamable;
import eyeq.tamablercreeper.client.model.ModelCreeperTamable;
import eyeq.tamablercreeper.client.renderer.entity.layers.LayerCreeperTamableCharge;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderCreeperTamable extends RenderLiving<EntityCreeperTamable> {
    private static final ResourceLocation CREEPER_TEXTURES = new ResourceLocation("textures/entity/creeper/creeper.png");

    public RenderCreeperTamable(RenderManager renderManager) {
        super(renderManager, new ModelCreeperTamable(), 0.5F);
        this.addLayer(new LayerCreeperTamableCharge(this));
    }

    @Override
    protected void preRenderCallback(EntityCreeperTamable entity, float partialTickTime) {
        float f = entity.getCreeperFlashIntensity(partialTickTime);
        float f1 = 1.0F + MathHelper.sin(f * 100.0F) * f * 0.01F;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        f = f * f;
        f = f * f;
        float xz = (1.0F + f * 0.4F) * f1;
        float y = (1.0F + f * 0.1F) / f1;
        GlStateManager.scale(xz * 0.9F, y * 0.9F, xz * 0.9F);
    }

    @Override
    protected int getColorMultiplier(EntityCreeperTamable entity, float lightBrightness, float partialTickTime) {
        float f = entity.getCreeperFlashIntensity(partialTickTime);
        if((int) (f * 10.0F) % 2 == 0) {
            return 0;
        }
        int i = (int) (f * 0.2F * 255.0F);
        i = MathHelper.clamp(i, 0, 255);
        return i << 24 | 822083583;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCreeperTamable entity) {
        return CREEPER_TEXTURES;
    }
}
