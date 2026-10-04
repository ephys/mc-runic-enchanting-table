package dev.ephys.runicrepository.client;

import dev.ephys.runicrepository.RunicRepository;
import dev.ephys.runicrepository.registry.ModMenuTypes;
import dev.ephys.runicrepository.registry.ModBlockEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RunicRepository.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
  @SubscribeEvent
  public static void onClientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> MenuScreens.register(ModMenuTypes.RUNIC_ENCHANTING_TABLE.get(), RunicEnchantingTableScreen::new));
  }

  @SubscribeEvent
  public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(ModBlockEntities.RUNIC_ENCHANTING_TABLE.get(), RunicEnchantingTableRenderer::new);
  }
}
