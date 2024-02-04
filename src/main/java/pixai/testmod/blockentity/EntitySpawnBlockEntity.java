package pixai.testmod.blockentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import pixai.testmod.TestMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;


import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EntitySpawnBlockEntity extends BlockEntity{
    private int tickInterval = 100;
    private int currentTick = 0;
    private boolean isPowered = false;

    private static final String TAG_POWERED = "powered";
    private static final String TAG_INTERVAL = "interval";


    public EntitySpawnBlockEntity(BlockPos pos, BlockState state) {
        super(TestMod.TEST_SPAWNER_BLOCK_ENTITY.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state, BlockEntity ent){
        if (level == null || level.isClientSide() || !isPowered )
            return;

        if(this.currentTick++ % tickInterval == 0) {
            Mob mob = getRandomMobEntityType().create(level);
            if (mob == null)
                return;

            var rand = ThreadLocalRandom.current();
            mob.setPos(this.worldPosition.getX() + rand.nextInt(-5,5),
                    this.worldPosition.getY() + 1,
                    this.worldPosition.getZ() + rand.nextInt(-5,5));
            level.addFreshEntity(mob);
        }
    }

    private EntityType<? extends Mob> getRandomMobEntityType(){
        return Arrays.asList(EntityType.ENDERMAN, EntityType.SHEEP, EntityType.BLAZE, EntityType.BEE, EntityType.FOX)
                .get(new Random().nextInt(5));
    }

    @Nullable
    public BlockState use(Level level, BlockPos pos, Player player) {
        tickInterval = player.isShiftKeyDown() ?
                tickInterval + 5 :
                tickInterval - 5;

        player.displayClientMessage(Component.literal("Interval: " + tickInterval), true);

        setChanged();

        return null;
    }

    public void setPoweredState(boolean hasSignal) {
        isPowered = hasSignal;
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        compound.putBoolean(TAG_POWERED, isPowered);
        compound.putInt(TAG_INTERVAL, tickInterval);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        isPowered = compound.getBoolean(TAG_POWERED);
        tickInterval = compound.getInt(TAG_INTERVAL);
    }
}
