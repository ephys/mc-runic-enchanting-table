package dev.ephys.runicrepository.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantmentLibraryTest {

    @Test
    void twoLevelOnesCombineToLevelTwo() {
        assertEquals(2, EnchantmentLibrary.combineLevels(List.of(1, 1), 5));
    }

    @Test
    void threeLevelOnesCombineToLevelTwo() {
        // 1 + 1 = 2, then 2 + 1 = 2
        assertEquals(2, EnchantmentLibrary.combineLevels(List.of(1, 1, 1), 5));
    }

    @Test
    void twoLevelOnesAndALevelTwoCombineToLevelThree() {
        // 1 + 1 = 2, then 2 + 2 = 3
        assertEquals(3, EnchantmentLibrary.combineLevels(List.of(1, 1, 2), 5));
    }

    @Test
    void fourLevelOnesCombineToLevelThree() {
        // (1 + 1 = 2) and (1 + 1 = 2), then 2 + 2 = 3 -- combining in encounter order instead of
        // always merging the two lowest levels would incorrectly cap this at level 2.
        assertEquals(3, EnchantmentLibrary.combineLevels(List.of(1, 1, 1, 1), 5));
    }

    @Test
    void singleLevelIsReturnedUnchanged() {
        assertEquals(1, EnchantmentLibrary.combineLevels(List.of(1), 5));
    }

    @Test
    void resultIsCappedAtMaxLevel() {
        // Unbreaking-like max level of 3: four level-1s would otherwise reach level 3 and beyond
        // when combined further, but must be capped.
        assertEquals(3, EnchantmentLibrary.combineLevels(List.of(1, 1, 1, 1, 1, 1, 1, 1), 3));
    }

    @Test
    void orderOfInputDoesNotMatter() {
        assertEquals(3, EnchantmentLibrary.combineLevels(List.of(2, 1, 1), 5));
        assertEquals(3, EnchantmentLibrary.combineLevels(List.of(1, 2, 1), 5));
    }

    @Test
    void differingLevelsKeepTheHigherWhenNotEqual() {
        assertEquals(5, EnchantmentLibrary.combineLevels(List.of(3, 5), 5));
    }
}
