package dev.ephys.runicrepository.menu;

import dev.ephys.runicrepository.Config;
import dev.ephys.runicrepository.block.RunicEnchantingTableBlockEntity;
import dev.ephys.runicrepository.registry.ModMenuTypes;
import dev.ephys.runicrepository.util.AnvilCost;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class RunicEnchantingTableMenu extends AbstractContainerMenu {
  private final Container tableContainer;
  private final ContainerLevelAccess access;

  public RunicEnchantingTableMenu(int containerId, Inventory playerInventory, Container tableContainer) {
    super(ModMenuTypes.RUNIC_ENCHANTING_TABLE.get(), containerId);
    this.tableContainer = tableContainer;
    this.access = tableContainer instanceof RunicEnchantingTableBlockEntity be
      ? ContainerLevelAccess.create(be.getLevel(), be.getBlockPos())
      : ContainerLevelAccess.NULL;

    checkContainerSize(tableContainer, 2);
    tableContainer.startOpen(playerInventory.player);

    this.addSlot(new Slot(tableContainer, RunicEnchantingTableBlockEntity.SLOT_ITEM, 15, 47) {
      @Override
      public boolean mayPlace(ItemStack stack) {
        return tableContainer.canPlaceItem(RunicEnchantingTableBlockEntity.SLOT_ITEM, stack);
      }

      @Override
      public int getMaxStackSize() {
        return 1;
      }
    });

    this.addSlot(new Slot(tableContainer, RunicEnchantingTableBlockEntity.SLOT_LAPIS, 35, 47) {
      @Override
      public boolean mayPlace(ItemStack stack) {
        return tableContainer.canPlaceItem(RunicEnchantingTableBlockEntity.SLOT_LAPIS, stack);
      }
    });

    // inventory
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 95 + row * 18));
      }
    }

    // hotbar
    for (int col = 0; col < 9; col++) {
      this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 153));
    }
  }

  private Map<ResourceLocation, Integer> getLibrary() {
    return tableContainer instanceof RunicEnchantingTableBlockEntity be ? be.getLibrary() : Map.of();
  }

  public ItemStack getItemToEnchant() {
    return tableContainer.getItem(RunicEnchantingTableBlockEntity.SLOT_ITEM);
  }

  public int getLapisCount() {
    return tableContainer.getItem(RunicEnchantingTableBlockEntity.SLOT_LAPIS).getCount();
  }

  /**
   * Returns the enchantments from the library that could be applied to the item currently in the
   * enchant slot.
   */
  public List<ApplicableEnchantment> getApplicableEnchantments(Set<ResourceLocation> selectedIds) {
    List<ApplicableEnchantment> result = new ArrayList<>();

    ItemStack stack = getItemToEnchant();
    if (stack.isEmpty()) {
      return result;
    }

    Map<Enchantment, Integer> existingEnchantments = EnchantmentHelper.getEnchantments(stack);

    List<Enchantment> selectedEnchantments = new ArrayList<>();
    for (ResourceLocation id : selectedIds) {
      Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(id);
      if (enchantment != null) {
        selectedEnchantments.add(enchantment);
      }
    }

    for (Map.Entry<ResourceLocation, Integer> entry : getLibrary().entrySet()) {
      Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(entry.getKey());
      if (enchantment == null) {
        continue;
      }

      // Already-applied enchantments are only offered if the library holds a higher level.
      int currentLevel = existingEnchantments.getOrDefault(enchantment, 0);
      if (entry.getValue() <= currentLevel) {
        continue;
      }

      if (!enchantment.canEnchant(stack)) {
        continue;
      }

      Enchantment incompatibleWith = findIncompatible(enchantment, existingEnchantments.keySet(), selectedEnchantments);
      result.add(new ApplicableEnchantment(enchantment, entry.getKey(), entry.getValue(), currentLevel, incompatibleWith));
    }

    result.sort((a, b) -> a.id().compareTo(b.id()));
    return result;
  }

  @Nullable
  private Enchantment findIncompatible(Enchantment candidate, Set<Enchantment> existing, List<Enchantment> selected) {
    for (Enchantment other : existing) {
      if (other != candidate && !candidate.isCompatibleWith(other)) {
        return other;
      }
    }

    for (Enchantment other : selected) {
      if (other != candidate && !candidate.isCompatibleWith(other)) {
        return other;
      }
    }

    return null;
  }

  public Cost computeCost(Map<ResourceLocation, Integer> selection) {
    if (selection.isEmpty()) {
      return new Cost(0, 0);
    }

    Map<Enchantment, Integer> existing = EnchantmentHelper.getEnchantments(getItemToEnchant());
    Map<Enchantment, Integer> selected = toEnchantmentMap(selection);

    int lapis = 0;
    for (Map.Entry<Enchantment, Integer> entry : selected.entrySet()) {
      int upgradedLevels = Math.max(0, entry.getValue() - existing.getOrDefault(entry.getKey(), 0));
      lapis += upgradedLevels * Config.lapisCostPerLevel;
    }
    // With a flat ceiling-break cost, levels above the max are priced at the max level, then the flat cost is added.
    Map<Enchantment, Integer> priced = selected;
    int xp = 0;
    if (Config.ceilingBreakCost > 0) {
      priced = new LinkedHashMap<>();
      for (Map.Entry<Enchantment, Integer> entry : selected.entrySet()) {
        Enchantment enchantment = entry.getKey();
        if (entry.getValue() > enchantment.getMaxLevel()) {
          xp += Config.ceilingBreakCost;
        }

        int capped = Math.min(entry.getValue(), enchantment.getMaxLevel());
        if (capped > existing.getOrDefault(enchantment, 0)) {
          priced.put(enchantment, capped);
        }
      }
    }
    xp += priced.isEmpty() ? 0 : AnvilCost.getEnchantCost(existing, priced);

    return new Cost(lapis, xp);
  }

  private Map<Enchantment, Integer> toEnchantmentMap(Map<ResourceLocation, Integer> selection) {
    Map<Enchantment, Integer> result = new LinkedHashMap<>();

    selection.forEach((id, level) -> {
      Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(id);
      if (enchantment != null) {
        result.put(enchantment, level);
      }
    });

    return result;
  }

  public boolean enchant(ServerPlayer player, Map<ResourceLocation, Integer> selection) {
    if (selection.isEmpty()) {
      return false;
    }

    ItemStack stack = getItemToEnchant();
    if (stack.isEmpty()) {
      return false;
    }

    Map<ResourceLocation, ApplicableEnchantment> applicable = getApplicableEnchantments(selection.keySet()).stream()
      .collect(Collectors.toMap(ApplicableEnchantment::id, a -> a));

    Map<Enchantment, Integer> toApply = new LinkedHashMap<>();
    for (Map.Entry<ResourceLocation, Integer> entry : selection.entrySet()) {
      ApplicableEnchantment a = applicable.get(entry.getKey());
      int level = entry.getValue();

      if (a == null || !a.isSelectable() || level <= a.currentLevel() || level > a.maxLevel()) {
        return false;
      }

      toApply.put(a.enchantment(), level);
    }

    Cost cost = computeCost(selection);
    boolean creative = player.getAbilities().instabuild;
    if (!creative) {
      if (getLapisCount() < cost.lapis()) {
        return false;
      }

      if (player.experienceLevel < cost.xpLevels()) {
        return false;
      }
    }

    // ItemStack#enchant appends a new entry, which would duplicate an enchantment being upgraded.
    Map<Enchantment, Integer> merged = new LinkedHashMap<>(EnchantmentHelper.getEnchantments(stack));
    merged.putAll(toApply);
    EnchantmentHelper.setEnchantments(merged, stack);

    if (!creative) {
      tableContainer.removeItem(RunicEnchantingTableBlockEntity.SLOT_LAPIS, cost.lapis());
      player.giveExperienceLevels(-cost.xpLevels());
    }

    int maxLevelApplied = toApply.values().stream().mapToInt(Integer::intValue).max().orElse(1);
    player.awardStat(Stats.ENCHANT_ITEM);
    CriteriaTriggers.ENCHANTED_ITEM.trigger(player, stack, maxLevelApplied);

    tableContainer.setChanged();
    access.execute((lvl, pos) -> lvl.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 0.9F + lvl.random.nextFloat() * 0.1F));

    return true;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    ItemStack result = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);
    if (slot == null || !slot.hasItem()) {
      return result;
    }

    ItemStack stackInSlot = slot.getItem();
    result = stackInSlot.copy();
    if (index < 2) {
      if (!this.moveItemStackTo(stackInSlot, 2, this.slots.size(), true)) {
        return ItemStack.EMPTY;
      }
    } else if (stackInSlot.is(Items.LAPIS_LAZULI)) {
      if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) {
        return ItemStack.EMPTY;
      }
    } else if (stackInSlot.getItem().isEnchantable(stackInSlot) && this.slots.get(0).getItem().isEmpty()) {
      if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
        return ItemStack.EMPTY;
      }
    } else if (!this.moveItemStackTo(stackInSlot, 2, this.slots.size(), false)) {
      return ItemStack.EMPTY;
    }

    if (stackInSlot.isEmpty()) {
      slot.setByPlayer(ItemStack.EMPTY);
    } else {
      slot.setChanged();
    }

    return result;
  }

  @Override
  public boolean stillValid(Player player) {
    return tableContainer.stillValid(player);
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    tableContainer.stopOpen(player);
  }

  /**
   * One enchantment the player could choose to apply or upgrade. {@code currentLevel} is the level
   * already on the item (0 if absent).
   */
  public record ApplicableEnchantment(Enchantment enchantment, ResourceLocation id, int maxLevel,
                                      int currentLevel, @Nullable Enchantment incompatibleWith) {
    public boolean isSelectable() {
      return incompatibleWith == null;
    }
  }

  /**
   * The lapis+XP cost of applying every selected enchantments.
   */
  public record Cost(int lapis, int xpLevels) {
  }
}
