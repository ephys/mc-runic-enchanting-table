package dev.ephys.runicrepository.tags;

import dev.ephys.runicrepository.RunicRepository;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTags {
  public static class Items {
    /**
     * Items whose stored enchantments are added to the library when found in a bookshelf or lectern.
     */
    public static final TagKey<Item> ENCHANTED_BOOKS = tag("enchanted_books");

    /**
     * Items that raise one enchantment's library level by one, up to one above its hard maximum.
     */
    public static final TagKey<Item> ANCIENT_TOMES = tag("ancient_tomes");

    private static TagKey<Item> tag(String name) {
      return TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(RunicRepository.MODID, name));
    }
  }

  public static class Blocks {
    /**
     * Blocks that can store enchanted books read by the runic enchanting table.
     */
    public static final TagKey<Block> ENCHANTING_BOOKSHELVES = tag("enchanting_bookshelves");

    private static TagKey<Block> tag(String name) {
      return TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), ResourceLocation.fromNamespaceAndPath(RunicRepository.MODID, name));
    }
  }
}
