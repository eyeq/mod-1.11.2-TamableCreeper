package eyeq.tamablercreeper;

import eyeq.tamablercreeper.entity.passive.EntityCreeperTamable;
import eyeq.tamablercreeper.client.renderer.entity.RenderCreeperTamable;
import eyeq.util.client.renderer.ResourceLocationFactory;
import eyeq.util.client.resource.ULanguageCreator;
import eyeq.util.client.resource.lang.LanguageResourceManager;
import eyeq.util.common.registry.UEntityRegistry;
import eyeq.util.world.biome.BiomeUtils;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.List;

import static eyeq.tamablercreeper.TamableCreeper.MOD_ID;

@Mod(modid = MOD_ID, version = "1.0", dependencies = "after:eyeq_util")
public class TamableCreeper {
    public static final String MOD_ID = "eyeq_tamablecreeper";

    @Mod.Instance(MOD_ID)
    public static TamableCreeper instance;

    private static final ResourceLocationFactory resource = new ResourceLocationFactory(MOD_ID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        registerEntities();
        if(event.getSide().isServer()) {
            return;
        }
        registerEntityRenderings();
        createFiles();
    }

    public static void registerEntities() {
        EntityList.EntityEggInfo egg = EntityList.ENTITY_EGGS.get(new ResourceLocation("creeper"));
        UEntityRegistry.registerModEntity(resource, EntityCreeperTamable.class, "DwarfCreeper", 0, instance, egg);
        List<Biome> biomes = BiomeUtils.getSpawnBiomes(EntityCreeper.class, EnumCreatureType.MONSTER);
        EntityRegistry.addSpawn(EntityCreeperTamable.class, 10, 1, 1, EnumCreatureType.MONSTER, biomes.toArray(new Biome[0]));
    }

    @SideOnly(Side.CLIENT)
    public static void registerEntityRenderings() {
        RenderingRegistry.registerEntityRenderingHandler(EntityCreeperTamable.class, RenderCreeperTamable::new);
    }

    public static void createFiles() {
        File project = new File("../1.11.2-TamableCreeper");

        LanguageResourceManager language = new LanguageResourceManager();

        language.register(LanguageResourceManager.EN_US, EntityCreeperTamable.class, "Dwarf Creeper");
        language.register(LanguageResourceManager.JA_JP, EntityCreeperTamable.class, "コリーパー");

        ULanguageCreator.createLanguage(project, MOD_ID, language);
    }
}
