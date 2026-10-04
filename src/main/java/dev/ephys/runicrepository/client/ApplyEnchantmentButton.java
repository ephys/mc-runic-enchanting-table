package dev.ephys.runicrepository.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ApplyEnchantmentButton extends AbstractWidget {
  private static final int WIDTH = 20;
  private static final int HEIGHT = 18;

  private static final Sprite BG_ENABLED = new Sprite(212, 29, WIDTH, HEIGHT);
  private static final Sprite BG_DISABLED = new Sprite(212, 48, WIDTH, HEIGHT);
  private static final Sprite BG_HOVERED = new Sprite(233, 48, WIDTH, HEIGHT);

  private static final Sprite ICON = new Sprite(177, 64, 16, 14);

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
    Sprite background = !this.active ? BG_DISABLED : this.isHovered ? BG_HOVERED : BG_ENABLED;

    background.blit(graphics, this.getX(), this.getY());
    ICON.blit(graphics, this.getX() + (WIDTH - ICON.width()) / 2, this.getY() + (HEIGHT - ICON.height()) / 2);
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output) {
    output.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, this.getMessage());
  }
}
