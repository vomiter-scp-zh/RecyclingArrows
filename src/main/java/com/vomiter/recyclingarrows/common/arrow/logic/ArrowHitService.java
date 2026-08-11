package com.vomiter.recyclingarrows.common.arrow.logic;

import com.vomiter.recyclingarrows.Config;
import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.data.ArrowDropDataManager;
import com.vomiter.recyclingarrows.common.arrow.data.HitOctant;
import com.vomiter.recyclingarrows.common.arrow.data.IArrowRecordHolder;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrow;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrowStack;
import com.vomiter.recyclingarrows.common.arrow.platform.IEntityArrowStorageAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ArrowHitService {
    private static final double MOTION_EPSILON = 1.0E-6;

    private final IEntityArrowStorageAccess storageAccess;

    public ArrowHitService(IEntityArrowStorageAccess storageAccess) {
        this.storageAccess = storageAccess;
    }

    public static void addArrow(EntityHitResult hit, AbstractArrow arrow, ItemStack pickUpItem) {
        // Only arrows that a survival player could retrieve should become embedded arrows.
        // This excludes creative-only and non-pickup projectiles.
        if (Config.disableUnpickableArrowRecycling() && arrow.pickup != AbstractArrow.Pickup.ALLOWED) {
            return;
        }

        Entity entity = hit.getEntity();
        if (entity instanceof LivingEntity living) {
            if (living.isAlive()) {
                RecyclingArrows.ARROW_HIT_SERVICE.recordArrowHit(arrow, living, hit);
                syncArrowStorage(living);
            } else {
                StoredArrow storedArrow = ArrowItemResolver.resolve(pickUpItem);

                if (storedArrow == null) {
                    living.spawnAtLocation(pickUpItem);
                    return;
                }

                List<ItemStack> drops = ArrowDropDataManager.INSTANCE.resolveDrops(storedArrow, living.getRandom());
                for (ItemStack stack : drops) {
                    if (!stack.isEmpty()) {
                        living.spawnAtLocation(stack);
                    }
                }
            }
        }
    }

    public void recordArrowHit(AbstractArrow arrow, LivingEntity target, EntityHitResult hit) {
        if (arrow == null || target == null || hit == null) {
            return;
        }

        StoredArrow stored = ArrowItemResolver.resolve(arrow);
        RecyclingArrows.LOGGER.debug("resolved arrow = {}", stored);
        if (stored == null) {
            return;
        }

        HitOctant octant = resolveOctant(target, arrow);
        RecyclingArrows.LOGGER.debug(
                "resolved octant = {}, arrowPos = {}, arrowMotion = {}, targetCenter = {}",
                octant, arrow.position(), arrow.getDeltaMovement(), target.getBoundingBox().getCenter()
        );

        IArrowRecordHolder holder = storageAccess.get(target);
        List<StoredArrowStack> stacks = holder.getArrows();

        for (StoredArrowStack stack : stacks) {
            if (sameArrow(stack.getArrow(), stored)) {
                stack.addArrow(octant);
                return;
            }
        }

        holder.addArrow(new StoredArrowStack(stored, List.of(octant)));
    }

    public List<ItemStack> getArrowDrops(LivingEntity target) {
        var stacks = storageAccess.get(target).getArrows().stream();
        List<ItemStack> list = new ArrayList<>();
        stacks.forEach(
                stack -> {
                    for (int i = 0; i < stack.getCount(); i++) {
                        List<ItemStack> drops = ArrowDropDataManager.INSTANCE.resolveDrops(stack.getArrow(), target.getRandom());
                        list.addAll(drops);
                    }
                }
        );
        return compact(list);
    }

    private static HitOctant resolveOctant(LivingEntity target, AbstractArrow arrow) {
        AABB box = target.getBoundingBox();
        Vec3 center = box.getCenter();
        Vec3 motion = arrow.getDeltaMovement();

        boolean east;
        boolean up;
        boolean south;

        if (motion.lengthSqr() > MOTION_EPSILON) {
            Vec3 dir = motion.normalize();
            Vec3 localDir = toEntityLocalHorizontal(dir, target.yBodyRot);

            east = localDir.x < 0.0D;
            up = localDir.y < 0.0D;
            south = localDir.z > 0.0D;
        } else {
            Vec3 pos = arrow.position();
            Vec3 offset = pos.subtract(center);
            Vec3 localOffset = toEntityLocalHorizontal(offset, target.yBodyRot);

            east = localOffset.x > 0.0D;
            up = localOffset.y > 0.0D;
            south = localOffset.z < 0.0D;
        }

        return HitOctant.fromSigns(east, up, south);
    }

    private static Vec3 toEntityLocalHorizontal(Vec3 world, float bodyYawDegrees) {
        double yaw = Math.toRadians(bodyYawDegrees);
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);

        double localX = world.x * cos + world.z * sin;
        double localZ = -world.x * sin + world.z * cos;

        return new Vec3(localX, world.y, localZ);
    }

    private static boolean sameArrow(StoredArrow a, StoredArrow b) {
        return Objects.equals(a.itemId(), b.itemId()) && Objects.equals(a.tag(), b.tag());
    }

    public StoredArrow removeArrow(LivingEntity target) {
        if (target == null) {
            return null;
        }

        StoredArrow removed = storageAccess.get(target).removeArrow();

        if (removed != null) {
            syncArrowStorage(target);
        }

        return removed;
    }

    public StoredArrow removeArrow(LivingEntity target, StoredArrow arrow) {
        if (target == null || arrow == null) {
            return null;
        }

        StoredArrow removed = storageAccess.get(target).removeArrow(arrow);

        if (removed != null) {
            syncArrowStorage(target);
        }

        return removed;
    }

    public StoredArrow removeArrow(LivingEntity target, ItemStack arrowStack) {
        if (target == null || arrowStack == null || arrowStack.isEmpty()) {
            return null;
        }

        StoredArrow arrow = ArrowItemResolver.resolve(arrowStack);

        if (arrow == null) {
            return null;
        }

        return removeArrow(target, arrow);
    }

    private static void syncArrowStorage(LivingEntity target) {
        target.level()
                .getEntitiesOfClass(ServerPlayer.class, target.getBoundingBox().inflate(64))
                .forEach(serverPlayer -> RecyclingArrows.arrowSyncService.syncToPlayer(target, serverPlayer));
    }

    public static List<ItemStack> compact(List<ItemStack> input) {
        List<ItemStack> result = new ArrayList<>();

        for (ItemStack stack : input) {
            if (stack.isEmpty()) continue;

            ItemStack remaining = stack.copy();

            // 嘗試合併到已存在的 stack
            for (ItemStack existing : result) {
                if (ItemStack.isSameItemSameTags(existing, remaining)) {
                    int transferable = Math.min(
                            existing.getMaxStackSize() - existing.getCount(),
                            remaining.getCount()
                    );

                    if (transferable > 0) {
                        existing.grow(transferable);
                        remaining.shrink(transferable);

                        if (remaining.isEmpty()) break;
                    }
                }
            }

            // 如果還有剩，建立新 stack（可能需要切多份）
            while (!remaining.isEmpty()) {
                int split = Math.min(remaining.getMaxStackSize(), remaining.getCount());
                ItemStack newStack = remaining.copy();
                newStack.setCount(split);
                result.add(newStack);
                remaining.shrink(split);
            }
        }

        return result;
    }
}
