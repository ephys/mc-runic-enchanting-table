package dev.ephys.runicrepository.util;

import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * XP cost calculation for applying enchantments, ported from the "stable anvil cost" algorithm
 * (https://github.com/PCamille/mc-stable_anvil_cost): cost depends only on the enchantments and
 * their levels (no "prior work" repair-cost penalty), curses reduce the total cost.
 */
public final class AnvilCost {
  private AnvilCost() {
  }


  public static int getEnchantCost(
    Map<Enchantment, Integer> existingEnchantments,
    Map<Enchantment, Integer> selectedEnchantments
  ) {
    if (selectedEnchantments.isEmpty()) {
      return 0;
    }

    Map<Enchantment, Integer> finalEnchantments = new HashMap<>(existingEnchantments);
    finalEnchantments.putAll(selectedEnchantments);
    return getEnchantCost(finalEnchantments, existingEnchantments, selectedEnchantments, false);
  }

  // anvil version, from mc-stable_anvil_cost
  public static int getEnchantCost(
    Map<Enchantment, Integer> finalEnchantments,
    Map<Enchantment, Integer> firstEnchantments,
    Map<Enchantment, Integer> secondEnchantments,
    boolean isSameObject
  ) {
    return getEnchantCost(finalEnchantments, firstEnchantments, secondEnchantments, isSameObject,
      AnvilCost::rarityMultiplier, Enchantment::isCurse);
  }

  private static double rarityMultiplier(Enchantment enchantment) {
    return switch (enchantment.getRarity()) {
      case COMMON -> 1;
      case UNCOMMON -> 2;
      case RARE -> 4;
      case VERY_RARE -> 8;
    };
  }

  /**
   * Generic core of the algorithm, parameterized over the enchantment key type so it can be
   * unit-tested with plain keys instead of real {@link Enchantment} instances.
   *
   * @param finalEnchantments  every enchantment (and its level) that will be present after this
   *                           operation: untouched existing ones plus newly selected ones.
   * @param firstEnchantments  the enchantments already on the target item before this operation.
   * @param secondEnchantments the enchantments being newly added (conceptually "from a book").
   * @param isSameObject       true when combining two instances of the same physical item/stack
   *                           (as on an anvil); always false for the runic table, which always
   *                           applies enchantments "from a book" onto the target item.
   */
  public static <T> int getEnchantCost(Map<T, Integer> finalEnchantments, Map<T, Integer> firstEnchantments,
                                       Map<T, Integer> secondEnchantments, boolean isSameObject,
                                       ToDoubleFunction<T> rarityMultiplier, Predicate<T> isCurse) {
    if (isSameObject && (finalEnchantments.equals(firstEnchantments) || finalEnchantments.equals(secondEnchantments))) {
      // Repair: nothing new was enchanted.
      return 0;
    }

    double cost = 0;
    int nbCurses = 0;

    for (T enchantment : finalEnchantments.keySet()) {
      if (isCurse.test(enchantment)) {
        nbCurses++;
        continue;
      }

      int finalLevel = finalEnchantments.get(enchantment);
      int firstLevel = firstEnchantments.getOrDefault(enchantment, 0);
      int secondLevel = secondEnchantments.getOrDefault(enchantment, 0);
      double rarity = rarityMultiplier.applyAsDouble(enchantment);

      if (firstLevel == secondLevel && finalLevel != firstLevel) {
        // Fusion of two same-level enchants.
        cost += finalLevel * rarity;
      } else if (firstLevel == 0 || secondLevel == 0) {
        // Total new enchant.
        if (isSameObject) {
          cost += finalLevel * rarity;
        } else if (secondLevel == 0) {
          // Already on the item, untouched by this fusion.
          cost += finalLevel * Math.max(rarity / 2, 1);
        } else {
          // Newly added from the book.
          cost += finalLevel * rarity;
        }
      } else {
        // Enchant already on both items being combined.
        cost += (finalLevel - Math.max(Math.min(firstLevel, secondLevel) / 2, 1)) * rarity;
      }
    }

    return Math.max(1, (int) (cost * (1 - 0.1 * nbCurses)));
  }
}
