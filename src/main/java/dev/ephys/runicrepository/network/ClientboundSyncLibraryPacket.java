package dev.ephys.runicrepository.network;

import dev.ephys.runicrepository.block.RunicEnchantingTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Sent server->client whenever the runic enchanting table's scanned bookshelf library changes
 * while a player has its menu open.
 */
public class ClientboundSyncLibraryPacket {
  private final BlockPos tablePos;
  private final Map<ResourceLocation, Integer> library;

  public ClientboundSyncLibraryPacket(BlockPos tablePos, Map<ResourceLocation, Integer> library) {
    this.tablePos = tablePos;
    this.library = library;
  }

  public static void encode(ClientboundSyncLibraryPacket packet, FriendlyByteBuf buf) {
    buf.writeBlockPos(packet.tablePos);
    buf.writeVarInt(packet.library.size());
    packet.library.forEach((id, level) -> {
      buf.writeResourceLocation(id);
      buf.writeVarInt(level);
    });
  }

  public static ClientboundSyncLibraryPacket decode(FriendlyByteBuf buf) {
    BlockPos pos = buf.readBlockPos();
    int size = buf.readVarInt();
    Map<ResourceLocation, Integer> library = new HashMap<>();
    for (int i = 0; i < size; i++) {
      library.put(buf.readResourceLocation(), buf.readVarInt());
    }

    return new ClientboundSyncLibraryPacket(pos, library);
  }

  public static void handle(ClientboundSyncLibraryPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
    NetworkEvent.Context context = contextSupplier.get();
    context.enqueueWork(() -> {
      if (context.getDirection().getReceptionSide() != net.minecraftforge.fml.LogicalSide.CLIENT) {
        return;
      }

      handleClient(packet);
    });
    context.setPacketHandled(true);
  }

  /**
   * Stores the synced library directly on the client-side block entity (not persisted, just a
   * display cache) instead of the menu, so the data lives in one place regardless of which side
   * is reading it.
   */
  private static void handleClient(ClientboundSyncLibraryPacket packet) {
    var level = Minecraft.getInstance().level;
    if (level == null) {
      return;
    }

    BlockEntity be = level.getBlockEntity(packet.tablePos);
    if (be instanceof RunicEnchantingTableBlockEntity tableBe) {
      tableBe.setLibrary(packet.library);
    }
  }
}

