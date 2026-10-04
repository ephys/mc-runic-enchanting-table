package dev.ephys.runicrepository.block;

import dev.ephys.runicrepository.Config;
import dev.ephys.runicrepository.menu.RunicEnchantingTableMenu;
import dev.ephys.runicrepository.network.ClientboundSyncLibraryPacket;
import dev.ephys.runicrepository.network.NetworkHandler;
import dev.ephys.runicrepository.registry.ModBlockEntities;
import dev.ephys.runicrepository.util.EnchantmentLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Backs the runic enchanting table.
 * Holds the item-to-enchant and lapis slots,
 * and maintains state of the virtual enchantment library gathered from nearby bookshelves.
 */
public class RunicEnchantingTableBlockEntity extends BaseContainerBlockEntity {
  public static final int SLOT_ITEM = 0;
  public static final int SLOT_LAPIS = 1;

  private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
  private final Set<ServerPlayer> viewers = new HashSet<>();
  private Map<ResourceLocation, Integer> library = new HashMap<>();
  private int ticksSinceRescan = 0;

  // client-side book animation state (mirrors vanilla's enchanting table)
  public int time;
  public float flip, oFlip, flipT, flipA;
  public float open, oOpen;
  public float rot, oRot, tRot;

  public RunicEnchantingTableBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.RUNIC_ENCHANTING_TABLE.get(), pos, state);
  }

  public static void tick(Level level, BlockPos pos, BlockState state, RunicEnchantingTableBlockEntity be) {
    if (level.isClientSide) {
      be.bookAnimationTick(level, pos);
      return;
    }

    if (be.viewers.isEmpty()) {
      return;
    }

    be.ticksSinceRescan++;
    if (be.ticksSinceRescan >= Config.rescanIntervalTicks) {
      be.ticksSinceRescan = 0;
      be.rescanAndBroadcast();
    }
  }

  private void bookAnimationTick(Level level, BlockPos pos) {
    oOpen = open;
    oRot = rot;

    Player player = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 3.0D, false);
    if (player != null) {
      double dx = player.getX() - (pos.getX() + 0.5D);
      double dz = player.getZ() - (pos.getZ() + 0.5D);
      tRot = (float) Mth.atan2(dz, dx);
      open += 0.1F;
      if (open < 0.5F || level.random.nextInt(40) == 0) {
        float old = flipT;
        do {
          flipT += level.random.nextInt(4) - level.random.nextInt(4);
        } while (old == flipT);
      }
    } else {
      tRot += 0.02F;
      open -= 0.1F;
    }

    while (rot >= (float) Math.PI) {
      rot -= (float) (Math.PI * 2);
    }
    while (rot < -(float) Math.PI) {
      rot += (float) (Math.PI * 2);
    }
    while (tRot >= (float) Math.PI) {
      tRot -= (float) (Math.PI * 2);
    }
    while (tRot < -(float) Math.PI) {
      tRot += (float) (Math.PI * 2);
    }

    float diff = tRot - rot;
    while (diff >= (float) Math.PI) {
      diff -= (float) (Math.PI * 2);
    }
    while (diff < -(float) Math.PI) {
      diff += (float) (Math.PI * 2);
    }

    rot += diff * 0.4F;
    open = Mth.clamp(open, 0.0F, 1.0F);
    time++;
    oFlip = flip;
    float d = (flipT - flip) * 0.4F;
    d = Mth.clamp(d, -0.2F, 0.2F);
    flipA += (d - flipA) * 0.9F;
    flip += flipA;
  }

  private void rescanAndBroadcast() {
    if (level == null || level.isClientSide) {
      return;
    }

    Map<ResourceLocation, Integer> newLibrary = EnchantmentLibrary.scan(level, worldPosition);
    if (!newLibrary.equals(library)) {
      library = newLibrary;
      broadcastLibrary();
    }
  }

  private void addViewer(ServerPlayer player) {
    viewers.add(player);
    rescanAndBroadcast();
    NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
      new ClientboundSyncLibraryPacket(worldPosition, library));
  }

  private void removeViewer(ServerPlayer player) {
    viewers.remove(player);
  }

  private void broadcastLibrary() {
    ClientboundSyncLibraryPacket packet = new ClientboundSyncLibraryPacket(worldPosition, library);

    for (ServerPlayer viewer : viewers) {
      NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), packet);
    }
  }

  @OnlyIn(Dist.CLIENT)
  public Map<ResourceLocation, Integer> getLibrary() {
    return library;
  }

  /**
   * Client-side only: meant to synchronize the library from the server to the client.
   */
  @OnlyIn(Dist.CLIENT)
  public void setLibrary(Map<ResourceLocation, Integer> library) {
    this.library = library;
  }

  @Override
  protected Component getDefaultName() {
    return Component.translatable("block.runicrepository.runic_enchanting_table");
  }

  @Override
  protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
    return new RunicEnchantingTableMenu(containerId, playerInventory, this);
  }

  @Override
  public void startOpen(Player player) {
    super.startOpen(player);
    if (level != null && !level.isClientSide && player instanceof ServerPlayer serverPlayer) {
      addViewer(serverPlayer);
    }
  }

  @Override
  public void stopOpen(Player player) {
    super.stopOpen(player);
    if (level != null && !level.isClientSide && player instanceof ServerPlayer serverPlayer) {
      removeViewer(serverPlayer);
    }
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    this.items.clear();
    ContainerHelper.loadAllItems(tag, this.items);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    ContainerHelper.saveAllItems(tag, this.items);
  }

  @Override
  public int getContainerSize() {
    return items.size();
  }

  @Override
  public boolean isEmpty() {
    return items.stream().allMatch(ItemStack::isEmpty);
  }

  @Override
  public ItemStack getItem(int slot) {
    return items.get(slot);
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    return ContainerHelper.removeItem(items, slot, amount);
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    return ContainerHelper.takeItem(items, slot);
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    items.set(slot, stack);

    if (stack.getCount() > getMaxStackSize()) {
      stack.setCount(getMaxStackSize());
    }
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack stack) {
    if (slot == SLOT_ITEM) {
      // Like the anvil, items that already carry enchantments may still be placed in so more can be
      // added; only the outright non-enchantable items (or the wrong item type) are rejected.
      // Note: we do not provide a way to duplicate enchantments via the enchanting table,
      // so books & enchanting books do not go in the item slot.
      return stack.getItem().isEnchantable(stack);
    } else if (slot == SLOT_LAPIS) {
      return stack.is(net.minecraft.world.item.Items.LAPIS_LAZULI);
    }

    return false;
  }

  @Override
  public boolean stillValid(Player player) {
    return Container.stillValidBlockEntity(this, player);
  }

  @Override
  public void clearContent() {
    items.clear();
  }
}
