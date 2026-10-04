package dev.ephys.runicrepository.util;

import dev.ephys.runicrepository.Config;
import dev.ephys.runicrepository.tags.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

import java.util.*;

/**
 * Scans bookshelves around a position for enchanted books and combines their enchantments into a
 * "virtual library" of the maximum level each enchantment can be applied at, using the optimized
 * combination rules of anvil combining (same level + same level = level + 1).
 */
public final class EnchantmentLibrary {
  private EnchantmentLibrary() {
  }

  /**
   * Scans the configurable volume around {@code tablePos} for bookshelf-like inventory blocks, reads every
   * enchanted book stored in them, and returns the combined max-accessible-level per enchantment.
   */
  public static Map<ResourceLocation, Integer> scan(Level level, BlockPos tablePos) {
    Map<Enchantment, List<Integer>> levelsPerEnchantment = new HashMap<>();
    Map<Enchantment, Integer> tomesPerEnchantment = new HashMap<>();

    for (BlockPos pos : getScanPositions(tablePos)) {
      if (!level.isLoaded(pos)) {
        continue;
      }

      // This allow-list controls what is considered to be a bookshelf.
      // Just storing your enchanted books in a chest just does not have the same pizazz as a bookshelf.
      if (!level.getBlockState(pos).is(ModTags.Blocks.ENCHANTING_BOOKSHELVES)) {
        continue;
      }

      BlockEntity be = level.getBlockEntity(pos);
      if (be == null) {
        continue;
      }

      for (ItemStack stack : getStoredStacks(be)) {
        if (stack.isEmpty()) {
          continue;
        }

        if (stack.is(ModTags.Items.ANCIENT_TOMES)) {
          Enchantment tomeEnchantment = getAncientTomeEnchantment(stack);
          if (tomeEnchantment != null) {
            tomesPerEnchantment.merge(tomeEnchantment, 1, Integer::sum);
          }

          continue;
        }

        if (!stack.is(ModTags.Items.ENCHANTED_BOOKS)) {
          continue;
        }

        EnchantmentHelper.getEnchantments(stack).forEach((enchantment, lvl) ->
          levelsPerEnchantment.computeIfAbsent(enchantment, e -> new ArrayList<>()).add(lvl));
      }
    }

    Map<ResourceLocation, Integer> result = new HashMap<>();
    levelsPerEnchantment.forEach((enchantment, levels) -> {
      ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
      if (id != null) {
        int combined = combineLevels(levels, enchantment.getMaxLevel());
        int tomes = tomesPerEnchantment.getOrDefault(enchantment, 0);
        result.put(id, applyAncientTomes(combined, enchantment.getMaxLevel(), tomes));
      }
    });

    return result;
  }

  /**
   * Each Quark ancient tome raises the level by one, but never more than one above the enchantment's hard maximum.
   */
  public static int applyAncientTomes(int level, int maxLevel, int tomes) {
    return Math.max(level, Math.min(level + tomes, maxLevel + 1));
  }

  private static List<ItemStack> getStoredStacks(BlockEntity be) {
    if (be instanceof LecternBlockEntity lectern) {
      return List.of(lectern.getBook());
    }

    IItemHandler handler = be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
    if (handler == null) {
      return List.of();
    }

    List<ItemStack> stacks = new ArrayList<>(handler.getSlots());
    for (int slot = 0; slot < handler.getSlots(); slot++) {
      stacks.add(handler.getStackInSlot(slot));
    }
    return stacks;
  }

  /**
   * Returns the enchantment targeted by a Quark ancient tome, or null if the stack is not a configured tome.
   */
  @Nullable
  private static Enchantment getAncientTomeEnchantment(ItemStack stack) {
    for (net.minecraft.nbt.Tag tag : EnchantedBookItem.getEnchantments(stack)) {
      if (tag instanceof CompoundTag c) {
        ResourceLocation id = ResourceLocation.tryParse(c.getString("id"));
        Enchantment enchantment = id == null ? null : ForgeRegistries.ENCHANTMENTS.getValue(id);
        if (enchantment != null) {
          return enchantment;
        }
      }
    }

    return null;
  }

  /**
   * Combines a list of enchantment levels the same way an anvil combines two enchantments at a time,
   * repeatedly merging the two lowest levels found (equal levels add one, differing levels keep the
   * higher one, each result capped at {@code maxLevel}) until a single, maximal level remains.
   * <p>
   * Merging the two lowest levels first (rather than folding in encounter order) matters: four
   * level-1 books should combine up to level 3 (1+1=2, 1+1=2, 2+2=3), not get stuck at level 2 as a
   * naive left-to-right fold would produce.
   */
  public static int combineLevels(List<Integer> levels, int maxLevel) {
    PriorityQueue<Integer> queue = new PriorityQueue<>(levels);
    while (queue.size() > 1) {
      int a = queue.poll();
      int b = queue.poll();
      int merged = (a == b) ? a + 1 : Math.max(a, b);
      queue.add(Math.min(merged, maxLevel));
    }
    return queue.poll();
  }

  /**
   * Returns every block position in the box formed by the configuration.
   */
  private static Iterable<BlockPos> getScanPositions(BlockPos tablePos) {
    int horizontalRange = Config.bookshelfHorizontalRange;
    int minY = -Config.bookshelfBelowRange;
    int maxY = Config.bookshelfAboveRange;

    List<BlockPos> positions = new ArrayList<>();
    for (int dx = -horizontalRange; dx <= horizontalRange; dx++) {
      for (int dy = minY; dy <= maxY; dy++) {
        for (int dz = -horizontalRange; dz <= horizontalRange; dz++) {
          if (dx == 0 && dy == 0 && dz == 0) {
            continue;
          }
          positions.add(tablePos.offset(dx, dy, dz));
        }
      }
    }
    return positions;
  }
}
