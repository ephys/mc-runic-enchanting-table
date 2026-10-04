package dev.ephys.runicrepository.registry;

import dev.ephys.runicrepository.RunicRepository;
import dev.ephys.runicrepository.block.RunicEnchantingTableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
    DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RunicRepository.MODID);

  public static final RegistryObject<BlockEntityType<RunicEnchantingTableBlockEntity>> RUNIC_ENCHANTING_TABLE =
    BLOCK_ENTITIES.register("runic_enchanting_table", () -> BlockEntityType.Builder.of(
      RunicEnchantingTableBlockEntity::new, ModBlocks.RUNIC_ENCHANTING_TABLE.get()).build(null));
}
