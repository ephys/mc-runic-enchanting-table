package dev.ephys.runicrepository.registry;

import dev.ephys.runicrepository.RunicRepository;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RunicRepository.MODID);

  public static final RegistryObject<Item> RUNIC_ENCHANTING_TABLE_ITEM = ITEMS.register("runic_enchanting_table",
    () -> new BlockItem(ModBlocks.RUNIC_ENCHANTING_TABLE.get(), new Item.Properties()));
}
