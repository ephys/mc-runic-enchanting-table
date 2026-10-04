package dev.ephys.runicrepository;

import com.mojang.logging.LogUtils;
import dev.ephys.runicrepository.network.NetworkHandler;
import dev.ephys.runicrepository.registry.ModBlockEntities;
import dev.ephys.runicrepository.registry.ModBlocks;
import dev.ephys.runicrepository.registry.ModItems;
import dev.ephys.runicrepository.registry.ModMenuTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RunicRepository.MODID)
public class RunicRepository {
  public static final String MODID = "runicrepository";
  public static final Logger LOGGER = LogUtils.getLogger();

  public RunicRepository(FMLJavaModLoadingContext context) {
    IEventBus modEventBus = context.getModEventBus();

    ModBlocks.BLOCKS.register(modEventBus);
    ModItems.ITEMS.register(modEventBus);
    ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
    ModMenuTypes.MENUS.register(modEventBus);

    modEventBus.addListener(this::addCreative);

    NetworkHandler.register();
    context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
  }

  private void addCreative(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
      event.accept(ModItems.RUNIC_ENCHANTING_TABLE_ITEM);
    }
  }
}
