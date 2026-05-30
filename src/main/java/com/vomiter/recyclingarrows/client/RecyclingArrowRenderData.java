package com.vomiter.recyclingarrows.client;

import com.vomiter.recyclingarrows.common.arrow.data.StoredArrowStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public record RecyclingArrowRenderData(
        LivingEntity entity,
        UUID uuid,
        int tickCount,
        double xSize,
        double ySize,
        double zSize,
        List<StoredArrowStack> arrows
) {
    public static RecyclingArrowRenderData from(LivingEntity entity, List<StoredArrowStack> arrows) {
        AABB box = entity.getBoundingBox();

        return new RecyclingArrowRenderData(
                entity,
                entity.getUUID(),
                entity.tickCount,
                box.getXsize(),
                box.getYsize(),
                box.getZsize(),
                List.copyOf(arrows)
        );
    }
}