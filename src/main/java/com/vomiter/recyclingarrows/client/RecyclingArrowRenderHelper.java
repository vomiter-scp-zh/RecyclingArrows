package com.vomiter.recyclingarrows.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.data.HitOctant;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrow;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrowStack;
import com.vomiter.recyclingarrows.common.arrow.logic.IArrowItemAccessor;
import com.vomiter.recyclingarrows.common.arrow.platform.EntityArrowStorageAccess;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class RecyclingArrowRenderHelper {
    private static final double LOOK_JITTER = 0.36D;
    private static final double MIN_DIR_LEN_SQR = 1.0E-6D;

    private static final double MIN_Y_SIZE_FOR_VERTICAL_OFFSET = 1D;

    public static void render(@NotNull PoseStack poseStack,
                              @NotNull MultiBufferSource buffer,
                              int packedLight,
                              @NotNull LivingEntity entity,
                              float partialTick,
                              EntityRenderDispatcher entityRenderDispatcher) {
        render(poseStack, buffer, packedLight, entity, partialTick, entityRenderDispatcher, false);
    }

    public static void render(@NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer,
                       int packedLight,
                       @NotNull LivingEntity entity,
                       float partialTick,
                       EntityRenderDispatcher entityRenderDispatcher,
                       boolean isGecko) {

        var holder = EntityArrowStorageAccess.getNullable(entity);
        if (holder == null) return;

        List<StoredArrowStack> stacks = holder.getArrows();
        if (stacks.isEmpty()) return;

        int globalIndex = 0;
        for (StoredArrowStack stack : stacks) {
            List<HitOctant> octants = stack.getOctants();
            for (int i = 0; i < octants.size(); i++) {
                renderSingleArrow(
                        poseStack,
                        buffer,
                        packedLight,
                        entity,
                        partialTick,
                        stack.getArrow(),
                        octants.get(i),
                        globalIndex,
                        i,
                        entityRenderDispatcher,
                        isGecko
                );
                globalIndex++;
            }
        }
    }

    private static void renderSingleArrow(PoseStack poseStack,
                                          MultiBufferSource buffer,
                                          int packedLight,
                                          LivingEntity entity,
                                          float partialTick,
                                          StoredArrow storedArrow,
                                          HitOctant renderOctant,
                                          int globalIndex,
                                          int stackIndex,
                                          EntityRenderDispatcher entityRenderDispatcher,
                                          boolean isGecko) {
        Item item = BuiltInRegistries.ITEM.get(storedArrow.itemId());
        if (!(item instanceof IArrowItemAccessor arrowItem)) {
            return;
        }

        ItemStack arrowStack = new ItemStack(item);
        if (storedArrow.stack() != null) {
            arrowStack = storedArrow.stack();
        }

        AbstractArrow arrowEntity = arrowItem.recyclingarrows$getArrowEntity(entity.level(), arrowStack, entity);
        if (arrowEntity == null) {
            return;
        }

        RandomSource random = RandomSource.create(makeSeed(entity.getUUID(), storedArrow, globalIndex, stackIndex));

        Vec3 offset = computeOctantOffset(entity, mapRecordedOctantToRenderOctant(renderOctant), random);

        prepareArrow(entity, arrowEntity, random, offset);
        poseStack.pushPose();
        var extraOffsetY = entity.getBbHeight() < MIN_Y_SIZE_FOR_VERTICAL_OFFSET? 1.1: 0;
        var geckoOffsetY = isGecko? entity.getBbHeight() * 0.45: 0;
        poseStack.translate(offset.x, offset.y + extraOffsetY + geckoOffsetY, offset.z);

        entityRenderDispatcher.render(
                arrowEntity,
                0.0D,
                0.0D,
                0.0D,
                0.0F,
                partialTick,
                poseStack,
                buffer,
                packedLight
        );

        poseStack.popPose();
    }

    private static void renderSingleArrow(PoseStack poseStack,
                                   MultiBufferSource buffer,
                                   int packedLight,
                                   LivingEntity entity,
                                   float partialTick,
                                   StoredArrow storedArrow,
                                   HitOctant octant,
                                   int globalIndex,
                                   int stackIndex,
                                   EntityRenderDispatcher entityRenderDispatcher) {
        renderSingleArrow(poseStack, buffer, packedLight, entity, partialTick, storedArrow, octant, globalIndex, stackIndex, entityRenderDispatcher, false);
    }

    private static void prepareArrow(LivingEntity entity, AbstractArrow arrowEntity, RandomSource random, Vec3 offset){
        AABB bb = entity.getBoundingBox();
        Vec3 center = bb.getCenter();
        if(entity.tickCount % 120 == 0)
            RecyclingArrows.LOGGER
                    .debug("[RA] Entity = {}, center = {}, ySize = {}", entity, center, bb.getYsize());

        Vec3 insertPos = new Vec3(
                center.x + offset.x,
                center.y + offset.y,
                center.z + offset.z
        );

        Vec3 lookDir = computeArrowLookDirection(entity, insertPos, random);
        float yaw = vecToYaw(lookDir);
        float pitch = vecToPitch(lookDir);

        if(entity.tickCount % 120 == 0)
            RecyclingArrows.LOGGER
                    .debug("[RA] Insert Pos = {}", insertPos);
        arrowEntity.setPos(insertPos);
        arrowEntity.tickCount = entity.tickCount;
        arrowEntity.setYRot(-yaw);
        arrowEntity.setXRot(-pitch);
        arrowEntity.yRotO = -yaw;
        arrowEntity.xRotO = -pitch;

    }

    private static Vec3 computeOctantOffset(LivingEntity entity, HitOctant octant, RandomSource random) {
        var box = entity.getBoundingBox();
        var factor = 0.7d;

        //baseX = distance from center in X axis
        //box.getXsize() * 0.25D = 1/4 x size, so that it would be at quadra point of the axis
        //However, it can cause arrows to be immersed in entity
        //So Math.max(..., xSize * 0.5 - 0.5D)
        double baseX = Math.max(box.getXsize() * 0.25D, box.getXsize() * 0.5D - 1D) * octant.xSign() * factor;
        double baseY = Math.max(box.getXsize() * 0.25D, box.getYsize() * 0.5D - 1D) * octant.ySign() * factor;
        double baseZ = Math.max(box.getXsize() * 0.25D, box.getZsize() * 0.5D - 1D) * octant.zSign() * factor;

        double jitterX = (random.nextDouble() - 0.5D) * box.getXsize() * 0.12D;
        double jitterY = (random.nextDouble() - 0.5D) * box.getYsize() * 0.12D;
        double jitterZ = (random.nextDouble() - 0.5D) * box.getZsize() * 0.12D;

        var offsetY = entity.getBbHeight() < MIN_Y_SIZE_FOR_VERTICAL_OFFSET? -0.5: baseY + jitterY;


        return new Vec3(baseX + jitterX, offsetY, baseZ + jitterZ);
    }

    private static Vec3 computeArrowLookDirection(LivingEntity entity, Vec3 insertPos, RandomSource random) {
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
        return (float) (-Math.toDegrees(Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z))));
    }

    private static long makeSeed(UUID uuid, StoredArrow arrow, int globalIndex, int stackIndex) {
        long seed = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
        seed = 31L * seed + arrow.itemId().hashCode();
        seed = 31L * seed + (arrow.stack() == null ? 0 : arrow.stack().hashCode());
        seed = 31L * seed + globalIndex;
        seed = 31L * seed + stackIndex;
        return seed;
    }

    /**
     * 目前依實測結果，render 時需要反轉 Y
     */
    private static HitOctant mapRecordedOctantToRenderOctant(HitOctant octant) {
        return HitOctant.fromSigns(
                octant.xSign() > 0,
                octant.ySign() < 0,
                octant.zSign() > 0
        );
    }

}
