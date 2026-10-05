package dev.ephys.runicrepository.mixin;

import dev.ephys.runicrepository.Config;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {
  // lift the client-side "Too Expensive!" cap
  @ModifyConstant(method = "renderLabels", constant = @Constant(intValue = 40, ordinal = 0))
  private int runic$noCap(int value) {
    return Config.anvilMaxCost;
  }
}
