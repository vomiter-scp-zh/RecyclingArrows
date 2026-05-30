package com.vomiter.recyclingarrows.common.arrow.platform;

import com.vomiter.recyclingarrows.common.arrow.data.ArrowRecordHolder;
import com.vomiter.recyclingarrows.common.arrow.data.ArrowRecordHolderCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.NotNull;

public enum ArrowRecordHolderSerializer implements IAttachmentSerializer<CompoundTag, ArrowRecordHolder> {
    INSTANCE;

    @Override
    public @NotNull ArrowRecordHolder read(@NotNull IAttachmentHolder iAttachmentHolder, @NotNull CompoundTag compoundTag, HolderLookup.@NotNull Provider provider) {
        var holder = new ArrowRecordHolder();
        ArrowRecordHolderCodec.load(holder, compoundTag, provider);
        return holder;
    }

    @Override
    public @NotNull CompoundTag write(
            @NotNull ArrowRecordHolder holder,
            HolderLookup.@NotNull Provider registries
    ) {
        return ArrowRecordHolderCodec.save(holder, registries);
    }
}