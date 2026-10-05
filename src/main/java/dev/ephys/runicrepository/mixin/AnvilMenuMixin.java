package dev.ephys.runicrepository.mixin;

import dev.ephys.runicrepository.Config;
import dev.ephys.runicrepository.util.AnvilCost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {
  @Shadow
  @org.spongepowered.asm.mixin.Final
  private DataSlot cost;

  private AnvilMenuMixin(MenuType<?> type, int id, Inventory inv, ContainerLevelAccess access) {
    super(type, id, inv, access);
  }

  @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
  private void runic$blockBookEnchanting(CallbackInfo ci) {
    if (!Config.anvilBlockBookEnchanting) {
      return;
    }

    ItemStack left = this.inputSlots.getItem(0);
    ItemStack right = this.inputSlots.getItem(1);
    if (!left.is(Items.ENCHANTED_BOOK) && right.is(Items.ENCHANTED_BOOK)
      && !EnchantedBookItem.getEnchantments(right).isEmpty()) {
      this.resultSlots.setItem(0, ItemStack.EMPTY);
      this.cost.set(0);
      this.broadcastChanges();
      ci.cancel();
    }
  }

  // lift the "Too Expensive!" cap
  @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40))
  private int runic$noCap(int value) {
    return Config.anvilMaxCost;
  }

  @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getBaseRepairCost()I"))
  private int runic$noBaseRepairCost(ItemStack stack) {
    return Config.anvilNoPriorWorkPenalty ? 0 : stack.getBaseRepairCost();
  }

  @Inject(method = "calculateIncreasedRepairCost", at = @At("HEAD"), cancellable = true)
  private static void runic$noRepairCostIncrease(int cost, CallbackInfoReturnable<Integer> cir) {
    if (Config.anvilNoPriorWorkPenalty) {
      cir.setReturnValue(0);
    }
  }

  @Inject(method = "createResult", at = @At("TAIL"))
  private void runic$stableCost(CallbackInfo ci) {


    ItemStack result = this.resultSlots.getItem(0);
    if (result.isEmpty()) {
      return;
    }

    ItemStack first = this.inputSlots.getItem(0);
    ItemStack second = this.inputSlots.getItem(1);

    var resultEnchants = EnchantmentHelper.getEnchantments(result);
    var firstEnchants = EnchantmentHelper.getEnchantments(first);
    var secondEnchants = EnchantmentHelper.getEnchantments(second);

    boolean isRename = second.isEmpty()
      && resultEnchants.equals(firstEnchants);

    if (isRename) {
      if (Config.anvilRenameXpCost != -1) {
        this.cost.set(Config.anvilRenameXpCost);
        this.broadcastChanges();
      }

      return;
    }

    boolean isRepair = !second.isEmpty()
      && (resultEnchants.equals(firstEnchants)
      || resultEnchants.equals(secondEnchants));

    if (isRepair) {
      if (Config.anvilRepairXpCost != -1) {
        this.cost.set(Config.anvilRepairXpCost);
        this.broadcastChanges();
      }

      return;
    }

    // merge
    if (!Config.anvilMergeStableCost) {
      return;
    }

    int mergeCost = AnvilCost.getEnchantCost(resultEnchants, firstEnchants, secondEnchants, true);
    this.cost.set((int) Math.round(mergeCost * Config.anvilMergeCostMultiplier));
    this.broadcastChanges();
  }

  @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
  private void runic$allowFree(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
    if (Config.anvilMergeStableCost) {
      cir.setReturnValue(hasItem && (player.getAbilities().instabuild || player.experienceLevel >= this.cost.get()));
    }
  }
}
