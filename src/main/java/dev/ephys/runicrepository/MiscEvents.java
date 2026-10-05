package dev.ephys.runicrepository;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RunicRepository.MODID)
public class MiscEvents {
  @SubscribeEvent
  public static void onAnvil$repairOrRenameNoAnvilDamage(AnvilRepairEvent event) {
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

  @SubscribeEvent
  public static void onExperienceDrop(LivingExperienceDropEvent event) {
    if (!Config.keepXpAfterDeath) {
      return;
    }

    if (event.getEntity() instanceof ServerPlayer player) {
      event.setDroppedExperience(0);
    }
  }

  @SubscribeEvent
  public static void onPlayerDeath$keepXp(PlayerEvent.Clone event) {
    if (!Config.keepXpAfterDeath) {
      return;
    }

    if (!event.isWasDeath()) {
      return;
    }

    if (event.getOriginal() instanceof ServerPlayer originalPlayer
      && event.getEntity() instanceof ServerPlayer newPlayer) {
      newPlayer.experienceLevel = originalPlayer.experienceLevel;
      newPlayer.experienceProgress = originalPlayer.experienceProgress;
    }
  }
}
