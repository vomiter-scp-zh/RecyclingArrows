package com.vomiter.recyclingarrows.common.arrow.platform;

import com.vomiter.recyclingarrows.common.arrow.data.ArrowRecordHolder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.NotNull;

public enum ArrowRecordHolderSerializer implements IAttachmentSerializer<ArrowRecordHolder> {
    INSTANCE;

    @Override
    public ArrowRecordHolder read(@NotNull IAttachmentHolder holder, @NotNull ValueInput input) {
        return null;
    }

    @Override
    public boolean write(ArrowRecordHolder attachment, @NotNull ValueOutput output) {
        return false;
    }

}