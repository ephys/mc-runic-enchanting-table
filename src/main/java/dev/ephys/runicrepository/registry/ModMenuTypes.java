package dev.ephys.runicrepository.registry;

import dev.ephys.runicrepository.RunicRepository;
import dev.ephys.runicrepository.block.RunicEnchantingTableBlockEntity;
import dev.ephys.runicrepository.menu.RunicEnchantingTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
  public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, RunicRepository.MODID);

  public static final RegistryObject<MenuType<RunicEnchantingTableMenu>> RUNIC_ENCHANTING_TABLE = MENUS.register(
    "runic_enchanting_table", () -> IForgeMenuType.create((containerId, inv, data) -> {
      BlockPos pos = data.readBlockPos();
      BlockEntity blockEntity = inv.player.level().getBlockEntity(pos);
      Container container = blockEntity instanceof RunicEnchantingTableBlockEntity table
        ? table
        // This should not happen, but at least it won't crash the game if it does.
        // If it happens, the player will just see an empty menu with no functionality.
        : new SimpleContainer(2);

      return new RunicEnchantingTableMenu(containerId, inv, container);
    }));
}
