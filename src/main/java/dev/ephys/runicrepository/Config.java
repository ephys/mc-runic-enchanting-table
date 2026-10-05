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

  private static final ForgeConfigSpec.IntValue CEILING_BREAK_COST = BUILDER
    .comment("Flat extra XP level cost added for each enchantment applied above its vanilla maximum level (only reachable with Quark ancient tomes).")
    .defineInRange("ceilingBreakCost", 30, 0, 1000);

  private static final ForgeConfigSpec.BooleanValue ANVIL_REPAIR_NO_DAMAGE = BUILDER
    .comment("Repairing does not damage the anvil.")
    .define("anvilRepairNoDamage", true);

  private static final ForgeConfigSpec.BooleanValue ANVIL_RENAME_NO_DAMAGE = BUILDER
    .comment("Renaming does not damage the anvil.")
    .define("anvilRenameNoDamage", true);

  private static final ForgeConfigSpec.BooleanValue ANVIL_BLOCK_BOOK_ENCHANTING = BUILDER
    .comment("Prevent applying enchanted books to items in the anvil (books can still be combined with each other, and items with each other).")
    .define("anvilBlockBookEnchanting", true);

  static final ForgeConfigSpec SPEC = BUILDER.build();

  public static int bookshelfHorizontalRange;
  public static int bookshelfAboveRange;
  public static int bookshelfBelowRange;
  public static int rescanIntervalTicks;
  public static int lapisCostPerLevel;
  public static int ceilingBreakCost = 30;
  public static volatile boolean anvilRepairNoDamage = true;
  public static volatile boolean anvilRenameNoDamage = true;
  public static volatile boolean anvilBlockBookEnchanting = true;

  @net.minecraftforge.eventbus.api.SubscribeEvent
  static void onLoad(final ModConfigEvent event) {
    bookshelfHorizontalRange = BOOKSHELF_HORIZONTAL_RANGE.get();
    bookshelfAboveRange = BOOKSHELF_ABOVE_RANGE.get();
    bookshelfBelowRange = BOOKSHELF_BELOW_RANGE.get();
    rescanIntervalTicks = RESCAN_INTERVAL_TICKS.get();
    lapisCostPerLevel = LAPIS_COST_PER_LEVEL.get();
    ceilingBreakCost = CEILING_BREAK_COST.get();
    anvilRepairNoDamage = ANVIL_REPAIR_NO_DAMAGE.get();
    anvilRenameNoDamage = ANVIL_RENAME_NO_DAMAGE.get();
    anvilBlockBookEnchanting = ANVIL_BLOCK_BOOK_ENCHANTING.get();
  }
}
