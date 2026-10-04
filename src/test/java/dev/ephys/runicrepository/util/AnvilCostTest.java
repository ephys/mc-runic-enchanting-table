package dev.ephys.runicrepository.util;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnvilCostTest {

  private static final ToDoubleFunction<String> RARITY = key -> switch (key.split("_")[0]) {
    case "common" -> 1;
    case "uncommon" -> 2;
    case "rare" -> 4;
    case "veryrare" -> 8;
    default -> throw new IllegalArgumentException(key);
  };

  private static final Predicate<String> CURSE = key -> key.startsWith("curse");

  private static int cost(Map<String, Integer> finalE, Map<String, Integer> firstE, Map<String, Integer> secondE, boolean sameObject) {
    return AnvilCost.getEnchantCost(finalE, firstE, secondE, sameObject, RARITY, CURSE);
  }

  @Test
  void newCommonEnchantFromBookCostsLevelTimesRarity() {
    // level 1 common enchant, brand new from a book: 1 * 1 = 1
    assertEquals(1, cost(Map.of("common", 1), Map.of(), Map.of("common", 1), false));
  }

  @Test
  void newUncommonEnchantScalesWithRarityAndLevel() {
    // level 3 uncommon enchant, brand new from a book: 3 * 2 = 6
    assertEquals(6, cost(Map.of("uncommon", 3), Map.of(), Map.of("uncommon", 3), false));
  }

  @Test
  void untouchedExistingEnchantStillAddsHalfRarityCost() {
    // Item already has level-1 common "unbreaking", book adds level-1 common "sharpness".
    // unbreaking (untouched): 1 * max(1/2, 1) = 1
    // sharpness (new): 1 * 1 = 1
    // total = 2
    Map<String, Integer> first = Map.of("common_unbreaking", 1);
    Map<String, Integer> second = Map.of("common_sharpness", 1);
    Map<String, Integer> fin = Map.of("common_unbreaking", 1, "common_sharpness", 1);
    assertEquals(2, cost(fin, first, second, false));
  }

  @Test
  void singleCurseDiscountsTotalCostByTenPercent() {
    // level-1 common enchant (cost 1) + a curse (no direct cost): 1 * (1 - 0.1) = 0.9 -> truncated to 0 -> floored to minimum 1
    Map<String, Integer> second = Map.of("common_sharpness", 1, "curse_of_vanishing", 1);
    assertEquals(1, cost(second, Map.of(), second, false));
  }

  @Test
  void multipleCursesStackTheDiscount() {
    // very_rare level 5 (cost 40) with two curses: 40 * (1 - 0.2) = 32
    Map<String, Integer> second = Map.of("veryrare_x", 5, "curse_of_vanishing", 1, "curse_of_binding", 1);
    assertEquals(32, cost(second, Map.of(), second, false));
  }

  @Test
  void repairingWithSameObjectAndNoChangeIsFree() {
    Map<String, Integer> same = Map.of("common_x", 1);
    assertEquals(0, cost(same, same, Map.of(), true));
  }

  @Test
  void combiningSameLevelOnSameObjectLevelsUp() {
    // Two level-1 common enchants combined on the same object type level up to level 2: 2 * 1 = 2
    assertEquals(2, cost(Map.of("common_x", 2), Map.of("common_x", 1), Map.of("common_x", 1), true));
  }

  @Test
  void sameObjectCombineNeverHalvesCostEvenForSideOnlyEnchants() {
    // Combining two of the same item type: "rare_shared" is on both sides, untouched (free);
    // "common_onlyFirst" is only on the first item, untouched; "common_onlyBook" is only on
    // the book. Unlike fusing with a book (isSameObject = false), same-object combines never
    // halve the cost for enchantments that only came from one side: 1 (onlyFirst) + 1 (onlyBook) = 2.
    Map<String, Integer> first = Map.of("rare_shared", 1, "common_onlyFirst", 1);
    Map<String, Integer> second = Map.of("rare_shared", 1, "common_onlyBook", 1);
    Map<String, Integer> fin = Map.of("rare_shared", 1, "common_onlyFirst", 1, "common_onlyBook", 1);
    assertEquals(2, cost(fin, first, second, true));
  }

  @Test
  void costNeverGoesBelowOneWhenAnyNonCurseEnchantIsPresent() {
    Map<String, Integer> second = Map.of("common_x", 1, "curse_of_vanishing", 1, "curse_of_binding", 1);
    assertEquals(1, cost(second, Map.of(), second, false));
  }

  @Test
  void libraryEnchantCostOverloadMatchesGenericCoreForRealEnchantments() {
    TestEnchantment unbreaking = new TestEnchantment(Enchantment.Rarity.COMMON, false);
    TestEnchantment sharpness = new TestEnchantment(Enchantment.Rarity.COMMON, false);

    Map<Enchantment, Integer> existing = Map.of(unbreaking, 1);
    Map<Enchantment, Integer> selected = Map.of(sharpness, 1);

    // Same scenario as untouchedExistingEnchantStillAddsHalfRarityCost: 1 + 1 = 2
    assertEquals(2, AnvilCost.getEnchantCost(existing, selected));
  }

  @Test
  void libraryEnchantCostOverloadReturnsZeroForEmptySelection() {
    assertEquals(0, AnvilCost.getEnchantCost(Map.of(), Map.of()));
  }

  /**
   * Minimal concrete {@link Enchantment} for tests; the constructor only stores plain fields.
   */
  private static class TestEnchantment extends Enchantment {
    private final boolean curse;

    TestEnchantment(Rarity rarity, boolean curse) {
      super(rarity, EnchantmentCategory.BREAKABLE, new EquipmentSlot[0]);
      this.curse = curse;
    }

    @Override
    public boolean isCurse() {
      return curse;
    }
  }
}
