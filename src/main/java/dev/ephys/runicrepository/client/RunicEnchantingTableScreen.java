package dev.ephys.runicrepository.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.ephys.runicrepository.menu.RunicEnchantingTableMenu;
import dev.ephys.runicrepository.network.NetworkHandler;
import dev.ephys.runicrepository.network.ServerboundApplyEnchantmentPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RunicEnchantingTableScreen extends AbstractContainerScreen<RunicEnchantingTableMenu> {
  private static final ResourceLocation ENCHANTING_BOOK_LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/enchanting_table_book.png");

  private static final int IMAGE_WIDTH = 176;
  private static final int IMAGE_HEIGHT = 177;

  private static final int LIST_X = 59;
  private static final int LIST_Y = 14;

  private static final int APPLY_BUTTON_X = 149;
  private static final int APPLY_BUTTON_Y = 74;

  private static final int PRICE_RIGHT_X = 142;
  private static final int PRICE_Y = 77;
  private static final int PRICE_ROW_HEIGHT = 12; // height of tallest icon (lapis)
  private static final int PRICE_SPACING = 6;
  private static final int ICON_TEXT_GAP = 2;
  private static final int PRICE_MIN_TEXT_WIDTH = 6;

  private static final Sprite BACKGROUND = new Sprite(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

  private static final Sprite XP_OK_ICON = new Sprite(177, 2, 9, 9);
  private static final Sprite XP_KO_ICON = new Sprite(178, 19, 7, 7);
  private static final Sprite LAPIS_KO_ICON = new Sprite(194, 64, 12, 12);
  private static final Sprite LAPIS_OK_ICON = new Sprite(194, 77, 12, 12);

  private static final int GREY_TEXT_COLOR = 0x404040;
  private static final int INSUFFICIENT_TEXT_COLOR = 0xFF6060;

  /**
   * Enchantments currently chosen for the pending "Enchant" action, mapped to their chosen level.
   * An enchantment missing from this map (or mapped to 0) is not selected.
   */
  private final Map<ResourceLocation, Integer> selection = new LinkedHashMap<>();
  private EnchantmentListWidget list;
  private ApplyEnchantmentButton applyButton;
  private boolean hasApplicableEnchantments;

  // Animated book state/model, ported from vanilla EnchantmentScreen#renderBook/#tickBook.
  private final RandomSource random = RandomSource.create();
  private BookModel bookModel;
  private float flip;
  private float oFlip;
  private float flipT;
  private float flipA;
  private float open;
  private float oOpen;
  private ItemStack lastItem = ItemStack.EMPTY;

  public RunicEnchantingTableScreen(RunicEnchantingTableMenu menu, Inventory playerInventory, Component title) {
    super(menu, playerInventory, title);
    this.imageWidth = IMAGE_WIDTH;
    this.imageHeight = IMAGE_HEIGHT;
  }

  @Override
  protected void init() {
    super.init();

    this.bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));

    this.list = new EnchantmentListWidget(this.leftPos + LIST_X, this.topPos + LIST_Y);
    this.addRenderableWidget(this.list);

    this.applyButton = new ApplyEnchantmentButton(this.leftPos + APPLY_BUTTON_X, this.topPos + APPLY_BUTTON_Y, this::onEnchant);
    this.addRenderableWidget(this.applyButton);

    refreshList();
  }

  @Override
  protected void containerTick() {
    super.containerTick();
    refreshList();
    tickBook();
  }

  /**
   * Ported from vanilla EnchantmentScreen#tickBook: page-flip on item change, open/close on activity.
   */
  private void tickBook() {
    ItemStack itemstack = this.menu.getItemToEnchant();
    if (!ItemStack.matches(itemstack, this.lastItem)) {
      this.lastItem = itemstack;

      do {
        this.flipT += (float) (this.random.nextInt(4) - this.random.nextInt(4));
      } while (this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
    }

    this.oFlip = this.flip;
    this.oOpen = this.open;

    if (this.hasApplicableEnchantments) {
      this.open += 0.2F;
    } else {
      this.open -= 0.2F;
    }

    this.open = Mth.clamp(this.open, 0.0F, 1.0F);
    float f1 = (this.flipT - this.flip) * 0.4F;
    f1 = Mth.clamp(f1, -0.2F, 0.2F);
    this.flipA += (f1 - this.flipA) * 0.9F;
    this.flip += this.flipA;
  }

  private void refreshList() {
    if (list == null) {
      return;
    }

    // Drop selections that are no longer valid (item swapped, book removed from a shelf, etc.).
    var applicable = this.menu.getApplicableEnchantments(selection.keySet());
    var validIds = applicable.stream().map(RunicEnchantingTableMenu.ApplicableEnchantment::id).collect(java.util.stream.Collectors.toSet());
    selection.keySet().removeIf(id -> !validIds.contains(id));
    applicable.forEach(a -> selection.computeIfPresent(a.id(), (id, level) -> level > a.currentLevel() ? level : null));

    this.hasApplicableEnchantments = !applicable.isEmpty();
    list.updateEntries(applicable, selection, this::onLevelChanged, this::costDeltaForLevel);

    if (applyButton != null) {
      applyButton.active = !selection.isEmpty() && canAfford(this.menu.computeCost(selection));
    }
  }

  private void onLevelChanged(ResourceLocation id, int level) {
    if (level <= 0) {
      selection.remove(id);
    } else {
      selection.put(id, level);
    }
  }

  private int costDeltaForLevel(ResourceLocation id, int level) {
    int before = this.menu.computeCost(selection).xpLevels();
    Map<ResourceLocation, Integer> bumped = new HashMap<>(selection);
    bumped.put(id, level);
    int after = this.menu.computeCost(bumped).xpLevels();
    return after - before;
  }

  private boolean hasEnoughLapis(RunicEnchantingTableMenu.Cost cost) {
    return this.minecraft.player.getAbilities().instabuild || this.menu.getLapisCount() >= cost.lapis();
  }

  private boolean hasEnoughXp(RunicEnchantingTableMenu.Cost cost) {
    return this.minecraft.player.getAbilities().instabuild || this.minecraft.player.experienceLevel >= cost.xpLevels();
  }

  private boolean canAfford(RunicEnchantingTableMenu.Cost cost) {
    return hasEnoughLapis(cost) && hasEnoughXp(cost);
  }

  private void onEnchant() {
    if (selection.isEmpty()) {
      return;
    }
    NetworkHandler.CHANNEL.sendToServer(new ServerboundApplyEnchantmentPacket(new HashMap<>(selection)));
    selection.clear();
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    int x = this.leftPos;
    int y = this.topPos;

    BACKGROUND.blit(graphics, x, y);
    this.renderBook(graphics, x, y, partialTick);

    if (selection.size() == 0) {
      return;
    }

    var cost = this.menu.computeCost(selection);
    boolean lapisOk = hasEnoughLapis(cost);
    boolean xpOk = hasEnoughXp(cost);

    Sprite lapisIcon = lapisOk ? LAPIS_OK_ICON : LAPIS_KO_ICON;
    Sprite xpIcon = xpOk ? XP_OK_ICON : XP_KO_ICON;

    String lapisText = String.valueOf(cost.lapis());
    String xpText = String.valueOf(cost.xpLevels());

    // Fixed-size slots (sized for the widest icon) so icon changes never shift the layout; the text width is a minimum that grows for large values.
    int lapisSlotWidth = Math.max(LAPIS_OK_ICON.width(), LAPIS_KO_ICON.width());
    int xpSlotWidth = Math.max(XP_OK_ICON.width(), XP_KO_ICON.width());
    int lapisWidth = lapisSlotWidth + ICON_TEXT_GAP + Math.max(this.font.width(lapisText), PRICE_MIN_TEXT_WIDTH);
    int xpWidth = xpSlotWidth + ICON_TEXT_GAP + Math.max(this.font.width(xpText), PRICE_MIN_TEXT_WIDTH);

    // Lapis then XP, side by side, right-aligned on PRICE_RIGHT_X.
    int xpX = x + PRICE_RIGHT_X - xpWidth;
    int lapisX = xpX - PRICE_SPACING - lapisWidth;
    int rowY = y + PRICE_Y;

    drawPrice(graphics, lapisIcon, lapisSlotWidth, lapisText, lapisX, rowY, lapisOk ? GREY_TEXT_COLOR : INSUFFICIENT_TEXT_COLOR);
    drawPrice(graphics, xpIcon, xpSlotWidth, xpText, xpX, rowY, xpOk ? GREY_TEXT_COLOR : INSUFFICIENT_TEXT_COLOR);
  }

  /**
   * Draws an icon followed by its amount, both vertically centered on the price row.
   */
  private void drawPrice(GuiGraphics graphics, Sprite icon, int iconSlotWidth, String text, int x, int rowY, int color) {
    int iconY = rowY + (PRICE_ROW_HEIGHT - icon.height()) / 2;
    icon.blit(graphics, x + (iconSlotWidth - icon.width()) / 2, iconY);
    int textY = rowY + (PRICE_ROW_HEIGHT - 8) / 2;
    graphics.drawString(this.font, text, x + iconSlotWidth + ICON_TEXT_GAP, textY, color, false);
  }

  /**
   * Ported verbatim (same position/animation) from vanilla EnchantmentScreen#renderBook.
   */
  private void renderBook(GuiGraphics graphics, int x, int y, float partialTick) {
    float f = Mth.lerp(partialTick, this.oOpen, this.open);
    float f1 = Mth.lerp(partialTick, this.oFlip, this.flip);
    Lighting.setupForEntityInInventory();
    graphics.pose().pushPose();
    graphics.pose().translate((float) x + 33.0F, (float) y + 31.0F, 100.0F);
    graphics.pose().scale(-40.0F, 40.0F, 40.0F);
    graphics.pose().mulPose(Axis.XP.rotationDegrees(25.0F));
    graphics.pose().translate((1.0F - f) * 0.2F, (1.0F - f) * 0.1F, (1.0F - f) * 0.25F);
    float f3 = -(1.0F - f) * 90.0F - 90.0F;
    graphics.pose().mulPose(Axis.YP.rotationDegrees(f3));
    graphics.pose().mulPose(Axis.XP.rotationDegrees(180.0F));
    float f4 = Mth.clamp(Mth.frac(f1 + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
    float f5 = Mth.clamp(Mth.frac(f1 + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
    this.bookModel.setupAnim(0.0F, f4, f5, f);
    VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(this.bookModel.renderType(ENCHANTING_BOOK_LOCATION));
    this.bookModel.renderToBuffer(graphics.pose(), vertexconsumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    graphics.flush();
    graphics.pose().popPose();
    Lighting.setupFor3DItems();
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);
    renderTooltip(graphics, mouseX, mouseY);

    if (list != null && list.getHoveredTooltip() != null) {
      graphics.renderTooltip(this.font, list.getHoveredTooltip(), mouseX, mouseY);
    }
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    graphics.drawString(this.font, Component.translatable("container.enchant"), 8, 6, 4210752, false);
  }
}
