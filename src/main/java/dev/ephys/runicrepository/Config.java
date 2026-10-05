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
    .comment("Prevent applying enchanted books to items in the anvil in favor of using the enchanting table (books can still be combined with each other, and items with each other).")
    .define("anvilBlockBookEnchanting", true);

  private static final ForgeConfigSpec.IntValue ANVIL_MAX_COST = BUILDER
    .comment("Maximum XP cost for an anvil operation (Vanilla default is 40).")
    .defineInRange("anvilMaxCost", Integer.MAX_VALUE, 0, Integer.MAX_VALUE);

  private static final ForgeConfigSpec.BooleanValue ANVIL_NO_PRIOR_WORK_PENALTY = BUILDER
    .comment("Disable the anvil's cumulative \"prior work\" repair penalty.")
    .define("anvilNoPriorWorkPenalty", true);

  private static final ForgeConfigSpec.IntValue ANVIL_REPAIR_XP_COST = BUILDER
    .comment("Flat XP cost for repairing an item in the anvil. Set to -1 to use the vanilla cost.")
    .defineInRange("anvilRepairXpCost", 0, -1, Integer.MAX_VALUE);

  private static final ForgeConfigSpec.IntValue ANVIL_RENAME_XP_COST = BUILDER
    .comment("Flat XP cost for renaming an item in the anvil. Set to -1 to use the vanilla cost.")
    .defineInRange("anvilRenameXpCost", 0, -1, Integer.MAX_VALUE);

  private static final ForgeConfigSpec.BooleanValue ANVIL_MERGE_STABLE_COST = BUILDER
    .comment("Use the enchanting algorithm of the enchanting library for merging enchantments on items in the anvil (from https://github.com/PCamille/mc-stable_anvil_cost).")
    .define("anvilMergeStableCost", true);

  private static final ForgeConfigSpec.DoubleValue ANVIL_MERGE_COST_MULTIPLIER = BUILDER
    .comment("Multiplier applied to the XP cost of merging enchantments in an anvil (only when anvilMergeStableCost is enabled). 0 makes merging free.")
    .defineInRange("anvilMergeCostMultiplier", 0.0, 0.0, 1000.0);

  private static final ForgeConfigSpec.IntValue FLAT_XP_LEVEL_COST = BUILDER
    .comment("How many XP orbs in an XP level. This config makes each XP level worth a flat amount of XP orbs instead of the vanilla increasing amount. Set to -1 to use the vanilla XP level cost. Around 50 would make reaching level 30 take about the same amount of XP as in vanilla, but in a linear way.")
    .defineInRange("flatXpLevelCost", -1, -1, Integer.MAX_VALUE);

  private static final ForgeConfigSpec.BooleanValue KEEP_XP_AFTER_DEATH = BUILDER
    .comment("Keep your XP after death.")
    .define("keepXpAfterDeath", false);

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
  public static volatile int anvilMaxCost = Integer.MAX_VALUE;
  public static volatile boolean anvilNoPriorWorkPenalty = true;
  public static volatile int anvilRepairXpCost = 0;
  public static volatile int anvilRenameXpCost = 0;
  public static volatile boolean anvilMergeStableCost = true;
  public static volatile double anvilMergeCostMultiplier = 0;
  public static volatile int flatXpLevelCost = -1;
  public static volatile boolean keepXpAfterDeath = false;

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
    anvilMaxCost = ANVIL_MAX_COST.get();
    anvilNoPriorWorkPenalty = ANVIL_NO_PRIOR_WORK_PENALTY.get();
    anvilRepairXpCost = ANVIL_REPAIR_XP_COST.get();
    anvilRenameXpCost = ANVIL_RENAME_XP_COST.get();
    anvilMergeStableCost = ANVIL_MERGE_STABLE_COST.get();
    anvilMergeCostMultiplier = ANVIL_MERGE_COST_MULTIPLIER.get();
    flatXpLevelCost = FLAT_XP_LEVEL_COST.get();
    keepXpAfterDeath = KEEP_XP_AFTER_DEATH.get();
  }
}
