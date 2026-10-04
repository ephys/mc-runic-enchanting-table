package dev.ephys.runicrepository;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = RunicRepository.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

  private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

  private static final ForgeConfigSpec.IntValue BOOKSHELF_HORIZONTAL_RANGE = BUILDER
    .comment("How far (in blocks, square radius) around the table to look for bookshelves, on the X/Z axes.")
    .defineInRange("bookshelfHorizontalRange", 10, 0, 64);

  private static final ForgeConfigSpec.IntValue BOOKSHELF_ABOVE_RANGE = BUILDER
    .comment("How far (in blocks) at and above the table's Y level to look for bookshelves.")
    .defineInRange("bookshelfAboveRange", 10, 0, 64);

  private static final ForgeConfigSpec.IntValue BOOKSHELF_BELOW_RANGE = BUILDER
    .comment("How far (in blocks) below the table's Y level to look for bookshelves.")
    .defineInRange("bookshelfBelowRange", 10, 0, 64);

  private static final ForgeConfigSpec.IntValue RESCAN_INTERVAL_TICKS = BUILDER
    .comment("How often (in ticks) the table rescans nearby bookshelves while a player has it open.")
    .defineInRange("rescanIntervalTicks", 20, 1, 1200);

  private static final ForgeConfigSpec.IntValue LAPIS_COST_PER_LEVEL = BUILDER
    .comment("How many lapis lazuli are consumed per enchantment level applied.")
    .defineInRange("lapisCostPerLevel", 1, 0, 64);

  static final ForgeConfigSpec SPEC = BUILDER.build();

  public static int bookshelfHorizontalRange;
  public static int bookshelfAboveRange;
  public static int bookshelfBelowRange;
  public static int rescanIntervalTicks;
  public static int lapisCostPerLevel;

  @net.minecraftforge.eventbus.api.SubscribeEvent
  static void onLoad(final ModConfigEvent event) {
    bookshelfHorizontalRange = BOOKSHELF_HORIZONTAL_RANGE.get();
    bookshelfAboveRange = BOOKSHELF_ABOVE_RANGE.get();
    bookshelfBelowRange = BOOKSHELF_BELOW_RANGE.get();
    rescanIntervalTicks = RESCAN_INTERVAL_TICKS.get();
    lapisCostPerLevel = LAPIS_COST_PER_LEVEL.get();
  }
}
