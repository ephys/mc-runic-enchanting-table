package dev.ephys.runicrepository.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ApplyEnchantmentButton extends AbstractWidget {
  private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("runicrepository", "textures/gui/container/runic_enchanting_table.png");

  private static final int WIDTH = 20;
  private static final int HEIGHT = 18;

  private static final int BG_ENABLED_U = 212, BG_ENABLED_V = 29;
  private static final int BG_DISABLED_U = 212, BG_DISABLED_V = 48;
  private static final int BG_HOVERED_U = 233, BG_HOVERED_V = 48;

  private static final int ICON_U = 177, ICON_V = 64;
  private static final int ICON_WIDTH = 16, ICON_HEIGHT = 14;

  private final Runnable onPress;

  public ApplyEnchantmentButton(int x, int y, Runnable onPress) {
    super(x, y, WIDTH, HEIGHT, Component.translatable("gui.runicrepository.enchant"));
    this.onPress = onPress;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (this.active && this.visible && button == 0 && this.clicked(mouseX, mouseY)) {
      playDownSound(net.minecraft.client.Minecraft.getInstance().getSoundManager());
      onPress.run();
      return true;
    }
    return false;
  }


  @Override
  protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    int u, v;
    if (!this.active) {
      u = BG_DISABLED_U;
      v = BG_DISABLED_V;
    } else if (this.isHovered) {
      u = BG_HOVERED_U;
      v = BG_HOVERED_V;
    } else {
      u = BG_ENABLED_U;
      v = BG_ENABLED_V;
    }

    graphics.blit(TEXTURE, this.getX(), this.getY(), u, v, WIDTH, HEIGHT);
    graphics.blit(TEXTURE, this.getX() + (WIDTH - ICON_WIDTH) / 2, this.getY() + (HEIGHT - ICON_HEIGHT) / 2, ICON_U, ICON_V, ICON_WIDTH, ICON_HEIGHT);
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output) {
    output.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, this.getMessage());
  }
}
