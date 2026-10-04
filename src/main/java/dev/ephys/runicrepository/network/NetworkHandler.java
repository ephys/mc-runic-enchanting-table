package dev.ephys.runicrepository.network;

import dev.ephys.runicrepository.RunicRepository;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
  private static final String PROTOCOL_VERSION = "1";

  public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
    ResourceLocation.fromNamespaceAndPath(RunicRepository.MODID, "main"),
    () -> PROTOCOL_VERSION,
    PROTOCOL_VERSION::equals,
    PROTOCOL_VERSION::equals
  );

  private static int id = 0;

  public static void register() {
    CHANNEL.registerMessage(id++, ClientboundSyncLibraryPacket.class,
      ClientboundSyncLibraryPacket::encode, ClientboundSyncLibraryPacket::decode, ClientboundSyncLibraryPacket::handle);

    CHANNEL.registerMessage(id++, ServerboundApplyEnchantmentPacket.class,
      ServerboundApplyEnchantmentPacket::encode, ServerboundApplyEnchantmentPacket::decode, ServerboundApplyEnchantmentPacket::handle);
  }
}
