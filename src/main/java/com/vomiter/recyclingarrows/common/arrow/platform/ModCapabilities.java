package com.vomiter.recyclingarrows.common.arrow.platform;

import com.vomiter.recyclingarrows.common.arrow.data.IArrowRecordHolder;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class ModCapabilities {
    public static final Capability<IArrowRecordHolder> ENTITY_ARROW_STORAGE =
            CapabilityManager.get(new CapabilityToken<>() {});

    private ModCapabilities() {
    }
}