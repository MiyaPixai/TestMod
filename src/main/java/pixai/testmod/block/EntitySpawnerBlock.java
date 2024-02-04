package pixai.testmod.block;

import pixai.testmod.TestMod;
import pixai.testmod.blockentity.EntitySpawnBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntitySpawnerBlock extends Block implements EntityBlock {
    private static BooleanProperty POWERED = BlockStateProperties.POWERED;

    public EntitySpawnerBlock(Properties property) {
        super(property);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(POWERED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        EntitySpawnBlockEntity ent = ((EntitySpawnBlockEntity) level.getBlockEntity(pos));
        if (ent != null) {
            ent.use(level, pos, player);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos newPos, Block block, BlockPos oldPos, boolean isMoving) {
        if (level.isClientSide())
            return;

        boolean hasSignal = level.hasNeighborSignal(newPos);
        EntitySpawnBlockEntity ent = ((EntitySpawnBlockEntity) level.getBlockEntity(newPos));

        if (ent != null && state.getValue(POWERED) != hasSignal) {
            ent.setPoweredState(hasSignal);
            level.setBlockAndUpdate(newPos,state.setValue(POWERED,hasSignal));
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == TestMod.TEST_SPAWNER_BLOCK_ENTITY.get()) {
            if (state.getValue(POWERED)) {
                return level.isClientSide() ? null : (level0, pos0, state0, blockEntity) -> ((EntitySpawnBlockEntity) blockEntity).tick(level0, pos0, state0, blockEntity);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TestMod.TEST_SPAWNER_BLOCK_ENTITY.get().create(pos, state);
    }
}
