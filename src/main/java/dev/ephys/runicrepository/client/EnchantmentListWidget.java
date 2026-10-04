package dev.ephys.runicrepository.client;

import dev.ephys.runicrepository.menu.RunicEnchantingTableMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class EnchantmentListWidget extends AbstractWidget {
  private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("runicrepository", "textures/gui/container/runic_enchanting_table.png");

  private static final int ROW_WIDTH = 102;
  private static final int ROW_HEIGHT = 19;
  private static final int ROW_U = 0;
  private static final int ROW_ENABLED_V = 199;
  private static final int ROW_DISABLED_V = 218;
  private static final int ROW_SELECTED_V = 237;

  private static final int VIEWPORT_WIDTH = 103;
  private static final int VIEWPORT_HEIGHT = 57;

  private static final int TRACK_LOCAL_X = 103;
  private static final int TRACK_WIDTH = 6;
  private static final int HANDLE_HEIGHT = 27;
  private static final int HANDLE_NORMAL_U = 223;
  private static final int HANDLE_HOVER_U = 229;
  private static final int HANDLE_V = 1;

  private static final int ARROW_WIDTH = 10;
  private static final int ARROW_HEIGHT = 15;
  private static final int RIGHT_ARROW_U = 177;
  private static final int RIGHT_ARROW_V = 29;
  private static final int LEFT_ARROW_U = 177;
  private static final int LEFT_ARROW_V = 48;
  private static final int ARROW_HOVER_U_OFFSET = 12;
  private static final int ARROW_DISABLED_U_OFFSET = 24;

  private static final int LEFT_ARROW_LOCAL_X = 79;
  private static final int LEVEL_LOCAL_CENTER_X = 81;
  private static final int RIGHT_ARROW_LOCAL_X = 90;
  private static final int ARROW_LOCAL_Y = (ROW_HEIGHT - ARROW_HEIGHT) / 2;
  private static final int NAME_LOCAL_X = 4;
  private static final int NAME_MAX_WIDTH = LEFT_ARROW_LOCAL_X - NAME_LOCAL_X - 2;

  private List<RunicEnchantingTableMenu.ApplicableEnchantment> entries = List.of();
  private Map<ResourceLocation, Integer> selection = Map.of();
  private BiConsumer<ResourceLocation, Integer> onLevelChanged;
  private Function<ResourceLocation, Integer> costDeltaForNextLevel;

  private double scrollAmount;
  private boolean draggingScrollbar;

  @Nullable
  private Component hoveredTooltip;

  public EnchantmentListWidget(int x, int y) {
    super(x, y, VIEWPORT_WIDTH + TRACK_WIDTH, VIEWPORT_HEIGHT, Component.empty());
  }

  public void updateEntries(List<RunicEnchantingTableMenu.ApplicableEnchantment> entries,
                            Map<ResourceLocation, Integer> selection,
                            BiConsumer<ResourceLocation, Integer> onLevelChanged,
                            Function<ResourceLocation, Integer> costDeltaForNextLevel) {
    this.entries = entries;
    this.selection = selection;
    this.onLevelChanged = onLevelChanged;
    this.costDeltaForNextLevel = costDeltaForNextLevel;

    double max = getMaxScroll();
    if (this.scrollAmount > max) {
      this.scrollAmount = max;
    }
  }

  @Nullable
  public Component getHoveredTooltip() {
    return hoveredTooltip;
  }

  private double getMaxScroll() {
    return Math.max(0, entries.size() * (double) ROW_HEIGHT - VIEWPORT_HEIGHT);
  }

  @Override
  protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    hoveredTooltip = null;

    int left = this.getX();
    int top = this.getY();

    graphics.enableScissor(left, top, left + VIEWPORT_WIDTH, top + VIEWPORT_HEIGHT);
    for (int i = 0; i < entries.size(); i++) {
      int rowTop = top - (int) scrollAmount + i * ROW_HEIGHT;
      if (rowTop + ROW_HEIGHT < top || rowTop > top + VIEWPORT_HEIGHT) {
        continue;
      }

      renderRow(graphics, entries.get(i), rowTop, left, mouseX, mouseY);
    }
    graphics.disableScissor();

    double maxScroll = getMaxScroll();
    if (maxScroll > 0) {
      int trackX = left + TRACK_LOCAL_X;
      int handleY = top + (int) Math.round((VIEWPORT_HEIGHT - HANDLE_HEIGHT) * (scrollAmount / maxScroll));
      boolean hoveringHandle = mouseX >= trackX && mouseX < trackX + TRACK_WIDTH && mouseY >= handleY && mouseY < handleY + HANDLE_HEIGHT;
      int u = (hoveringHandle || draggingScrollbar) ? HANDLE_HOVER_U : HANDLE_NORMAL_U;
      graphics.blit(TEXTURE, trackX, handleY, u, HANDLE_V, TRACK_WIDTH, HANDLE_HEIGHT);
    }
  }

  private MutableComponent getEnchantmentName(Enchantment enchantment, int level) {
      MutableComponent mutablecomponent = Component.translatable(enchantment.getDescriptionId());

      if (level > 0 && (level != 1 || enchantment.getMaxLevel() != 1)) {
        mutablecomponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + level));
      }

      return mutablecomponent;
  }

  private void renderRow(GuiGraphics graphics, RunicEnchantingTableMenu.ApplicableEnchantment applicable, int rowTop, int left, int mouseX, int mouseY) {
    Font font = Minecraft.getInstance().font;
    boolean selectable = applicable.isSelectable();
    int level = selection.getOrDefault(applicable.id(), 0);

    int rowV = !selectable ? ROW_DISABLED_V : level > 0 ? ROW_SELECTED_V : ROW_ENABLED_V;
    graphics.blit(TEXTURE, left, rowTop, ROW_U, rowV, ROW_WIDTH, ROW_HEIGHT);

    int titleColor = !selectable ? 0x707070 : level > 0 ? 0xFFFFA0 : 0xFFFFFF;
    Component name = getEnchantmentName(applicable.enchantment(), level);
    String fullName = name.getString();
    String trimmed = trimToWidth(font, fullName, NAME_MAX_WIDTH);
    boolean nameTrimmed = !trimmed.equals(fullName);
    graphics.drawString(font, trimmed, left + NAME_LOCAL_X, rowTop + (ROW_HEIGHT - 8) / 2, titleColor, false);

    boolean inViewport = mouseOver(mouseX, mouseY, left, getY(), VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
    boolean rowHovered = inViewport && mouseOver(mouseX, mouseY, left, rowTop, ROW_WIDTH, ROW_HEIGHT);

    if (!selectable) {
      if (rowHovered) {
        hoveredTooltip = Component.translatable("gui.runicrepository.incompatible_with",
          getEnchantmentName(applicable.incompatibleWith(), 0)).withStyle(ChatFormatting.RED);
      }
      return;
    }

    boolean leftEnabled = level > 0;
    boolean rightEnabled = level < applicable.maxLevel();

    int leftArrowX = left + LEFT_ARROW_LOCAL_X;
    int rightArrowX = left + RIGHT_ARROW_LOCAL_X;
    int arrowY = rowTop + ARROW_LOCAL_Y;

    boolean hoveringLeft = inViewport && mouseOver(mouseX, mouseY, leftArrowX, arrowY, ARROW_WIDTH, ARROW_HEIGHT);
    boolean hoveringRight = inViewport && mouseOver(mouseX, mouseY, rightArrowX, arrowY, ARROW_WIDTH, ARROW_HEIGHT);

    drawArrow(graphics, LEFT_ARROW_U, LEFT_ARROW_V, leftArrowX, arrowY, leftEnabled, hoveringLeft && leftEnabled);
    drawArrow(graphics, RIGHT_ARROW_U, RIGHT_ARROW_V, rightArrowX, arrowY, rightEnabled, hoveringRight && rightEnabled);

    if (rightEnabled && hoveringRight && costDeltaForNextLevel != null) {
      int delta = costDeltaForNextLevel.apply(applicable.id());
      hoveredTooltip = Component.translatable("gui.runicrepository.cost_delta", delta > 0 ? "+" + delta : delta).withStyle(delta > 0 ? ChatFormatting.WHITE : ChatFormatting.GREEN);
    } else if (nameTrimmed && rowHovered) {
      hoveredTooltip = name;
    }
  }

  private void drawArrow(GuiGraphics graphics, int baseU, int v, int x, int y, boolean enabled, boolean hovered) {
    int u = baseU + (!enabled ? ARROW_DISABLED_U_OFFSET : hovered ? ARROW_HOVER_U_OFFSET : 0);
    graphics.blit(TEXTURE, x, y, u, v, ARROW_WIDTH, ARROW_HEIGHT);
  }

  private boolean mouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
    return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
  }

  private String trimToWidth(Font font, String text, int maxWidth) {
    if (font.width(text) <= maxWidth) {
      return text;
    }

    String ellipsis = "...";
    if (font.width(ellipsis) > maxWidth) {
      return ellipsis;
    }

    // Grow the kept prefix/suffix one character at a time, alternating sides,
    // so the omitted characters are removed from the middle of the string.
    int prefixEnd = 0;
    int suffixStart = text.length();
    boolean growPrefix = true;
    while (prefixEnd < suffixStart) {
      int nextPrefixEnd = growPrefix ? prefixEnd + 1 : prefixEnd;
      int nextSuffixStart = growPrefix ? suffixStart : suffixStart - 1;
      if (nextPrefixEnd > nextSuffixStart) {
        break;
      }

      String candidate = text.substring(0, nextPrefixEnd) + ellipsis + text.substring(nextSuffixStart);
      if (font.width(candidate) > maxWidth) {
        break;
      }

      prefixEnd = nextPrefixEnd;
      suffixStart = nextSuffixStart;
      growPrefix = !growPrefix;
    }

    return text.substring(0, prefixEnd) + ellipsis + text.substring(suffixStart);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button != 0 || !this.active || !this.visible) {
      return false;
    }

    int left = this.getX();
    int top = this.getY();

    double maxScroll = getMaxScroll();
    if (maxScroll > 0) {
      int trackX = left + TRACK_LOCAL_X;
      int handleY = top + (int) Math.round((VIEWPORT_HEIGHT - HANDLE_HEIGHT) * (scrollAmount / maxScroll));
      if (mouseX >= trackX && mouseX < trackX + TRACK_WIDTH) {
        if (mouseY >= handleY && mouseY < handleY + HANDLE_HEIGHT) {
          draggingScrollbar = true;
        } else if (mouseY >= top && mouseY < top + VIEWPORT_HEIGHT) {
          // Jump to the clicked position within the track.
          double ratio = (mouseY - top - HANDLE_HEIGHT / 2.0) / (VIEWPORT_HEIGHT - HANDLE_HEIGHT);
          scrollAmount = Math.max(0, Math.min(maxScroll, ratio * maxScroll));
          draggingScrollbar = true;
        }
        return true;
      }
    }

    if (mouseX < left || mouseX >= left + VIEWPORT_WIDTH || mouseY < top || mouseY >= top + VIEWPORT_HEIGHT) {
      return false;
    }

    int localY = (int) (mouseY - top + scrollAmount);
    int index = localY / ROW_HEIGHT;
    if (index < 0 || index >= entries.size()) {
      return false;
    }

    RunicEnchantingTableMenu.ApplicableEnchantment applicable = entries.get(index);
    if (!applicable.isSelectable()) {
      return true;
    }

    int rowTop = top - (int) scrollAmount + index * ROW_HEIGHT;
    int leftArrowX = left + LEFT_ARROW_LOCAL_X;
    int rightArrowX = left + RIGHT_ARROW_LOCAL_X;
    int arrowY = rowTop + ARROW_LOCAL_Y;

    int level = selection.getOrDefault(applicable.id(), 0);

    if (mouseOver((int) mouseX, (int) mouseY, leftArrowX, arrowY, ARROW_WIDTH, ARROW_HEIGHT)) {
      if (level > 0 && onLevelChanged != null) {
        onLevelChanged.accept(applicable.id(), level - 1);
      }
      return true;
    }

    if (mouseOver((int) mouseX, (int) mouseY, rightArrowX, arrowY, ARROW_WIDTH, ARROW_HEIGHT)) {
      if (level < applicable.maxLevel() && onLevelChanged != null) {
        onLevelChanged.accept(applicable.id(), level + 1);
      }
      return true;
    }

    return true;
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (draggingScrollbar) {
      double maxScroll = getMaxScroll();
      if (maxScroll > 0) {
        double delta = dragY * (maxScroll / (VIEWPORT_HEIGHT - HANDLE_HEIGHT));
        scrollAmount = Math.max(0, Math.min(maxScroll, scrollAmount + delta));
      }
      return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    draggingScrollbar = false;
    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    double maxScroll = getMaxScroll();
    if (maxScroll <= 0) {
      return false;
    }
    scrollAmount = Math.max(0, Math.min(maxScroll, scrollAmount - delta * ROW_HEIGHT));
    return true;
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput output) {
  }
}
