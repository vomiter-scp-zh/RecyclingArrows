package com.vomiter.recyclingarrows.common.arrow.logic;

import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.data.HitOctant;
import com.vomiter.recyclingarrows.common.arrow.data.IArrowRecordHolder;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrow;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrowStack;
import com.vomiter.recyclingarrows.common.arrow.platform.IEntityArrowStorageAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;

public final class ArrowHitService {
    private static final double MOTION_EPSILON = 1.0E-6;

    private final IEntityArrowStorageAccess storageAccess;

    public ArrowHitService(IEntityArrowStorageAccess storageAccess) {
        this.storageAccess = storageAccess;
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

        HitOctant octant = resolveOctant(target.getBoundingBox(), arrow);
        RecyclingArrows.LOGGER.info(
                "resolved octant = {}, arrowPos = {}, arrowMotion = {}, targetCenter = {}",
                octant,
                arrow.position(),
                arrow.getDeltaMovement(),
                target.getBoundingBox().getCenter()
        );

        IArrowRecordHolder holder = storageAccess.get(target);
        List<StoredArrowStack> stacks = holder.getArrows();

        for (StoredArrowStack stack : stacks) {
            if (sameArrow(stack.getArrow(), stored)) {
                stack.addArrow(octant);
                return;
            }
        }

        holder.addArrow(new StoredArrowStack(stored, java.util.List.of(octant)));
    }

    public List<ItemStack> getArrows(LivingEntity target) {
        return storageAccess.get(target).getArrows().stream()
                .map(ArrowItemResolver::build)
                .toList();
    }

    private static HitOctant resolveOctant(AABB box, AbstractArrow arrow) {
        Vec3 center = box.getCenter();
        Vec3 motion = arrow.getDeltaMovement();

        boolean east;
        boolean up;
        boolean south;

        if (motion.lengthSqr() > MOTION_EPSILON) {
            Vec3 dir = motion.normalize();

            // 這裡判斷的是「受擊側」，不是箭目前朝向哪裡
            // 箭朝 +X 飛，表示它是從 WEST 打進來，所以 east = false
            east = dir.x < 0.0D;
            up = dir.y < 0.0D;
            south = dir.z < 0.0D;
        } else {
            Vec3 pos = arrow.position();
            east = pos.x > center.x;
            up = pos.y > center.y;
            south = pos.z > center.z;
        }

        return HitOctant.fromSigns(east, up, south);
    }

    private static boolean sameArrow(StoredArrow a, StoredArrow b) {
        return Objects.equals(a.itemId(), b.itemId())
                && Objects.equals(a.tag(), b.tag());
    }
}