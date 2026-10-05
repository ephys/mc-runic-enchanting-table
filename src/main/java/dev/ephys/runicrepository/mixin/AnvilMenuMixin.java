package dev.ephys.runicrepository.mixin;

import dev.ephys.runicrepository.Config;
import dev.ephys.runicrepository.util.AnvilCost;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
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
}
