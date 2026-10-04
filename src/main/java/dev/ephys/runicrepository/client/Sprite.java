package dev.ephys.runicrepository.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public record Sprite(int u, int v, int width, int height) {
  public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("runicrepository", "textures/gui/container/runic_enchanting_table.png");
  public static final int TEXTURE_SIZE = 256;

  public void blit(GuiGraphics graphics, int x, int y) {
    graphics.blit(TEXTURE, x, y, u, v, width, height);
  }
}
