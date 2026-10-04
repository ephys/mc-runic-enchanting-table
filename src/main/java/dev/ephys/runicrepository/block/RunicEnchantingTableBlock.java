package dev.ephys.runicrepository.block;

import dev.ephys.runicrepository.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

// most of this comes from EnchantmentTableBlock
public class RunicEnchantingTableBlock extends BaseEntityBlock {
  protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

  public RunicEnchantingTableBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }

  @Override
  public boolean useShapeForLightOcclusion(BlockState state) {
    return true;
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    super.animateTick(state, level, pos, random);
    if (random.nextInt(4) == 0) {
      level.addParticle(ParticleTypes.ENCHANT,
        pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D,
        random.nextDouble() - 0.5D, -random.nextDouble(), random.nextDouble() - 0.5D);
    }
  }

  @Override
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new RunicEnchantingTableBlockEntity(pos, state);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    return createTickerHelper(type, ModBlockEntities.RUNIC_ENCHANTING_TABLE.get(), RunicEnchantingTableBlockEntity::tick);
  }

  @Override
  public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
    if (!state.is(newState.getBlock())) {
      if (level.getBlockEntity(pos) instanceof Container container) {
        Containers.dropContents(level, pos, container);
        level.updateNeighbourForOutputSignal(pos, this);
      }
    }

    super.onRemove(state, level, pos, newState, isMoving);
  }

  @Override
  public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    if (level.isClientSide) {
      return InteractionResult.SUCCESS;
    }

    MenuProvider provider = state.getMenuProvider(level, pos);
    if (provider != null && player instanceof ServerPlayer serverPlayer) {
      NetworkHooks.openScreen(serverPlayer, provider, pos);
    }

    return InteractionResult.CONSUME;
  }

  @Nullable
  @Override
  public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (blockEntity instanceof RunicEnchantingTableBlockEntity table) {
      Component name = ((Nameable) blockEntity).getDisplayName();
      return new SimpleMenuProvider((containerId, playerInv, p) ->
        new dev.ephys.runicrepository.menu.RunicEnchantingTableMenu(containerId, playerInv, table), name);
    }

    return null;
  }

  @Override
  public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    if (!stack.hasCustomHoverName()) {
      return;
    }

    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (blockEntity instanceof RunicEnchantingTableBlockEntity table) {
      table.setCustomName(stack.getHoverName());
    }
  }

  @Override
  public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
    return false;
  }
}
