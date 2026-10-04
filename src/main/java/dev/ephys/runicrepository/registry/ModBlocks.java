package dev.ephys.runicrepository.registry;

import dev.ephys.runicrepository.RunicRepository;
import dev.ephys.runicrepository.block.RunicEnchantingTableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

public class ModBlocks {
  public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RunicRepository.MODID);

  public static final RegistryObject<Block> RUNIC_ENCHANTING_TABLE = BLOCKS.register("runic_enchanting_table",
    () -> new RunicEnchantingTableBlock(BlockBehaviour.Properties.of()
      .mapColor(MapColor.COLOR_RED)
      .instrument(Objects.requireNonNull(NoteBlockInstrument.BASEDRUM))
      .requiresCorrectToolForDrops()
      .strength(5.0F, 1200.0F)
      .lightLevel((state) -> 7)));
}

