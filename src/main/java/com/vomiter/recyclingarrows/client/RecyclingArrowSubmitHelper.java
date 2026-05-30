package com.vomiter.recyclingarrows.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vomiter.recyclingarrows.common.arrow.data.HitOctant;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrow;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrowStack;
import com.vomiter.recyclingarrows.common.arrow.logic.IArrowItemAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public final class RecyclingArrowSubmitHelper {
    private static final double LOOK_JITTER = 0.36D;
    private static final double MIN_DIR_LEN_SQR = 1.0E-6D;
    private static final double MIN_Y_SIZE_FOR_VERTICAL_OFFSET = 1.0D;

    private RecyclingArrowSubmitHelper() {
    }

    public static void submit(
            LivingEntityRenderState livingState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        RecyclingArrowRenderData data = RecyclingArrowRenderContexts.ARROWS_DATA.get(livingState);

        if (data == null || data.arrows().isEmpty()) {
            return;
        }
        int globalIndex = 0;

        for (StoredArrowStack stack : data.arrows()) {
            List<HitOctant> octants = stack.getOctants();

            for (int i = 0; i < octants.size(); i++) {
                submitSingleArrow(
                        livingState,
                        poseStack,
                        collector,
                        camera,
                        data,
                        stack.getArrow(),
                        octants.get(i),
                        globalIndex,
                        i
                );
                globalIndex++;
            }
        }
    }

    private static void submitSingleArrow(
            LivingEntityRenderState livingState,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera,
            RecyclingArrowRenderData data,
            StoredArrow storedArrow,
            HitOctant octant,
            int globalIndex,
            int stackIndex
    ) {
        LivingEntity entity = data.entity();

        Item item = storedArrow.stack().getItem();
        if (!(item instanceof IArrowItemAccessor arrowItem)) {
            return;
        }

        ItemStack arrowStack = storedArrow.stack().copy();

        AbstractArrow arrowEntity = arrowItem.recyclingarrows$getArrowEntity(
                entity.level(),
                arrowStack,
                entity
        );

        if (arrowEntity == null) {
            return;
        }

        RandomSource random = RandomSource.create(
                makeSeed(data.uuid(), storedArrow, globalIndex, stackIndex)
        );

        HitOctant renderOctant = octant;
        Vec3 offset = computeOctantOffset(data, renderOctant, random);

        AABB bb = entity.getBoundingBox();
        Vec3 center = bb.getCenter();
        Vec3 insertPos = new Vec3(
                center.x + offset.x,
                center.y + offset.y,
                center.z + offset.z
        );

        Vec3 lookDir = computeArrowLookDirection(entity, insertPos, random);
        float yaw = vecToYaw(lookDir);
        float pitch = vecToPitch(lookDir);

        arrowEntity.setPos(insertPos);
        arrowEntity.xo = insertPos.x;
        arrowEntity.yo = insertPos.y;
        arrowEntity.zo = insertPos.z;
        arrowEntity.xOld = insertPos.x;
        arrowEntity.yOld = insertPos.y;
        arrowEntity.zOld = insertPos.z;

        arrowEntity.tickCount = data.tickCount();

        arrowEntity.setYRot(-yaw);
        arrowEntity.setXRot(-pitch);
        arrowEntity.yRotO = -yaw;
        arrowEntity.xRotO = -pitch;

        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);

        submitArrowEntity(
                arrowEntity,
                livingState.partialTick,
                poseStack,
                collector,
                camera
        );

        poseStack.popPose();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void submitArrowEntity(
            AbstractArrow arrowEntity,
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        EntityRenderer renderer =
                Minecraft.getInstance()
                        .getEntityRenderDispatcher()
                        .getRenderer(arrowEntity);

        EntityRenderState arrowState = (EntityRenderState) renderer.createRenderState();

        renderer.extractRenderState(arrowEntity, arrowState, partialTick);
        renderer.submit(arrowState, poseStack, collector, camera);
    }

    private static Vec3 computeOctantOffset(
            RecyclingArrowRenderData data,
            HitOctant octant,
            RandomSource random
    ) {
        double factor = 0.7D;

        double baseX = data.xSize() * 0.25D * octant.xSign() * factor;
        double baseY = data.ySize() * 0.25D * octant.ySign() * factor;
        double baseZ = data.zSize() * 0.25D * octant.zSign() * factor;

        double jitterX = (random.nextDouble() - 0.5D) * data.xSize() * 0.12D;
        double jitterY = (random.nextDouble() - 0.5D) * data.ySize() * 0.12D;
        double jitterZ = (random.nextDouble() - 0.5D) * data.zSize() * 0.12D;

        if (data.ySize() < MIN_Y_SIZE_FOR_VERTICAL_OFFSET) {
            baseY = 0.5D;
            jitterY = 0.0D;
        }

        return new Vec3(
                baseX + jitterX,
                baseY + jitterY,
                baseZ + jitterZ
        );
    }

    private static Vec3 computeArrowLookDirection(
            LivingEntity entity,
            Vec3 insertPos,
            RandomSource random
    ) {
        Vec3 center = entity.getBoundingBox().getCenter();
        Vec3 dir = center.subtract(insertPos);

        if (dir.lengthSqr() < MIN_DIR_LEN_SQR) {
            dir = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            dir = dir.normalize();
        }

        Vec3 jitter = new Vec3(
                (random.nextDouble() - 0.5D) * LOOK_JITTER,
                (random.nextDouble() - 0.5D) * LOOK_JITTER,
                (random.nextDouble() - 0.5D) * LOOK_JITTER
        );

        Vec3 out = dir.add(jitter);
        if (out.lengthSqr() < MIN_DIR_LEN_SQR) {
            return dir;
        }

        return out.normalize();
    }

    private static float vecToYaw(Vec3 dir) {
        return (float) (Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0D);
    }

    private static float vecToPitch(Vec3 dir) {
        return (float) (-Math.toDegrees(
                Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z))
        ));
    }

    private static long makeSeed(
            UUID uuid,
            StoredArrow arrow,
            int globalIndex,
            int stackIndex
    ) {
        long seed = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
        seed = 31L * seed + arrow.itemId().hashCode();
        seed = 31L * seed + arrow.stack().hashCode();
        seed = 31L * seed + globalIndex;
        seed = 31L * seed + stackIndex;
        return seed;
    }
}