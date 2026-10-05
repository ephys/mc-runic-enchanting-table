package dev.ephys.runicrepository.network;

import dev.ephys.runicrepository.RunicRepository;
import dev.ephys.runicrepository.menu.RunicEnchantingTableMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Sent client->server when the player clicks the global "Enchant" button in the runic enchanting
 * table UI, carrying the player's full selection of enchantment ids to levels.
 */
public class ServerboundApplyEnchantmentPacket {
  private final Map<ResourceLocation, Integer> selection;

  public ServerboundApplyEnchantmentPacket(Map<ResourceLocation, Integer> selection) {
    this.selection = selection;
  }

  public static void encode(ServerboundApplyEnchantmentPacket packet, FriendlyByteBuf buf) {
    buf.writeVarInt(packet.selection.size());
    for (Map.Entry<ResourceLocation, Integer> entry : packet.selection.entrySet()) {
      buf.writeResourceLocation(entry.getKey());
      buf.writeVarInt(entry.getValue());
    }
  }

  public static ServerboundApplyEnchantmentPacket decode(FriendlyByteBuf buf) {
    int size = buf.readVarInt();
    Map<ResourceLocation, Integer> selection = new HashMap<>();
    for (int i = 0; i < size; i++) {
      selection.put(buf.readResourceLocation(), buf.readVarInt());
    }

    return new ServerboundApplyEnchantmentPacket(selection);
  }

  public static void handle(ServerboundApplyEnchantmentPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
    NetworkEvent.Context context = contextSupplier.get();
    context.enqueueWork(() -> {
      ServerPlayer player = context.getSender();
      if (player == null) {
        return;
      }

      if (player.containerMenu instanceof RunicEnchantingTableMenu menu) {
        try {
          boolean result = menu.enchant(player, packet.selection);
          if (!result) {
            RunicRepository.LOGGER.error("Failed to apply enchantments for player {}", player.getName().getString());
          }
        } catch (Throwable e) {
          RunicRepository.LOGGER.error("Error when applying enchantments", e);
          throw e;
        }
      }
    });
    context.setPacketHandled(true);
  }
}
