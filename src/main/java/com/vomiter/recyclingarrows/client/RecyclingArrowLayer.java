package com.vomiter.recyclingarrows.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.data.HitOctant;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrow;
import com.vomiter.recyclingarrows.common.arrow.data.StoredArrowStack;
import com.vomiter.recyclingarrows.common.arrow.platform.EntityArrowStorageAccess;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class RecyclingArrowLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final double LOOK_JITTER = 0.36D;
    private static final double MIN_DIR_LEN_SQR = 1.0E-6D;

    private static final double MIN_Y_SIZE_FOR_VERTICAL_OFFSET = 1D;
    private final EntityRenderDispatcher entityRenderDispatcher;

    public RecyclingArrowLayer(RenderLayerParent<T, M> parent, EntityRenderDispatcher entityRenderDispatcher) {
        super(parent);
        this.entityRenderDispatcher = entityRenderDispatcher;
    }

    @Override
    public void render(@NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer,
                       int packedLight,
                       @NotNull T entity,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTick,
                       float ageInTicks,
                       float netHeadYaw,
                       float headPitch) {

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
                        i
                );
                globalIndex++;
            }
        }
    }

    private void renderSingleArrow(PoseStack poseStack,
                                   MultiBufferSource buffer,
                                   int packedLight,
                                   T entity,
                                   float partialTick,
                                   StoredArrow storedArrow,
                                   HitOctant octant,
                                   int globalIndex,
                                   int stackIndex) {
        Item item = ForgeRegistries.ITEMS.getValue(storedArrow.itemId());
        if (!(item instanceof ArrowItem arrowItem)) {
            return;
        }

        ItemStack arrowStack = new ItemStack(item);
        if (storedArrow.tag() != null) {
            arrowStack.setTag(storedArrow.tag().copy());
        }

        AbstractArrow arrowEntity = arrowItem.createArrow(entity.level(), arrowStack, entity);
        if (arrowEntity == null) {
            return;
        }

        RandomSource random = RandomSource.create(makeSeed(entity.getUUID(), storedArrow, globalIndex, stackIndex));

        //HitOctant renderOctant = mapRecordedOctantToRenderOctant(octant);
        HitOctant renderOctant = (octant);
        Vec3 offset = computeOctantOffset(entity, renderOctant, random);

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
        arrowEntity.setXRot(pitch);
        arrowEntity.yRotO = -yaw;
        arrowEntity.xRotO = pitch;

        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);

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

    private Vec3 computeOctantOffset(T entity, HitOctant octant, RandomSource random) {
        var box = entity.getBoundingBox();
        var factor = 0.7d;

        double baseX = box.getXsize() * 0.25D * octant.xSign() * factor;
        double baseY = box.getYsize() * 0.25D * octant.ySign() * factor;
        double baseZ = box.getZsize() * 0.25D * octant.zSign() * factor;

        double jitterX = (random.nextDouble() - 0.5D) * box.getXsize() * 0.12D;
        double jitterY = (random.nextDouble() - 0.5D) * box.getYsize() * 0.12D;
        double jitterZ = (random.nextDouble() - 0.5D) * box.getZsize() * 0.12D;

        if(box.getYsize() < MIN_Y_SIZE_FOR_VERTICAL_OFFSET){
            baseY = 0.5;
            jitterY = 0;
        }

        return new Vec3(baseX + jitterX, baseY + jitterY, baseZ + jitterZ);
    }

    private Vec3 computeArrowLookDirection(T entity, Vec3 insertPos, RandomSource random) {
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

    private long makeSeed(UUID uuid, StoredArrow arrow, int globalIndex, int stackIndex) {
        long seed = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
        seed = 31L * seed + arrow.itemId().hashCode();
        seed = 31L * seed + (arrow.tag() == null ? 0 : arrow.tag().hashCode());
        seed = 31L * seed + globalIndex;
        seed = 31L * seed + stackIndex;
        return seed;
    }

    /**
     * 這不是幾何意義上的完整 opposite。
     * 目前依實測結果，render 時需要反轉 Y / Z，X 保持原樣。
     */
    private static HitOctant mapRecordedOctantToRenderOctant(HitOctant octant) {
        return HitOctant.fromSigns(
                octant.xSign() > 0,
                octant.ySign() < 0,
                octant.zSign() < 0
        );
    }
}