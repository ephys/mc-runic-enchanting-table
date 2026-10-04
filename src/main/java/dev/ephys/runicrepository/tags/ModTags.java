package dev.ephys.runicrepository.tags;

import dev.ephys.runicrepository.RunicRepository;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class ModTags {
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
