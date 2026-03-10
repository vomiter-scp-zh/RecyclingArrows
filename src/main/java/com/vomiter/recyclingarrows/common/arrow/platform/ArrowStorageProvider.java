package com.vomiter.recyclingarrows.common.arrow.platform;

import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.data.ArrowRecordHolder;
import com.vomiter.recyclingarrows.common.arrow.data.ArrowRecordHolderCodec;
import com.vomiter.recyclingarrows.common.arrow.data.IArrowRecordHolder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ArrowStorageProvider implements ICapabilitySerializable<CompoundTag> {
    private final ArrowRecordHolder holder = new ArrowRecordHolder();
    private final LazyOptional<IArrowRecordHolder> optional = LazyOptional.of(() -> holder);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        if (cap == ModCapabilities.ENTITY_ARROW_STORAGE) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = ArrowRecordHolderCodec.save(holder);
        RecyclingArrows.LOGGER.debug("serializeNBT: {}", tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ArrowRecordHolderCodec.load(holder, nbt);
    }

    public void invalidate() {
        optional.invalidate();
    }
}