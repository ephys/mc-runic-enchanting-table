package dev.ephys.runicrepository;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RunicRepository.MODID)
public class AnvilEvents {
  @SubscribeEvent
  public static void onAnvilRepairOrRenameNoAnvilDamage(AnvilRepairEvent event) {
    if (!Config.anvilRepairNoDamage && !Config.anvilRenameNoDamage) {
      return;
    }

    ItemStack first = event.getLeft();
    ItemStack second = event.getRight();
    ItemStack result = event.getOutput();

    var resultEnchants = EnchantmentHelper.getEnchantments(result);
    var firstEnchants = EnchantmentHelper.getEnchantments(first);
    var secondEnchants = EnchantmentHelper.getEnchantments(second);

    boolean isRename = second.isEmpty()
      && resultEnchants.equals(firstEnchants);

    boolean isRepair = !second.isEmpty()
      && (resultEnchants.equals(firstEnchants)
      || resultEnchants.equals(secondEnchants));

    if ((isRename && Config.anvilRenameNoDamage)
      || (isRepair && Config.anvilRepairNoDamage)) {
      event.setBreakChance(0);
    }
  }
}
