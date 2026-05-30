package com.vomiter.recyclingarrows.common.arrow.platform;

import com.vomiter.recyclingarrows.common.arrow.data.ArrowRecordHolder;
import com.vomiter.recyclingarrows.common.arrow.data.ArrowRecordHolderCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.NotNull;

public enum ArrowRecordHolderSerializer implements IAttachmentSerializer<@NotNull ArrowRecordHolder> {
    INSTANCE;

    @Override
    public @NotNull ArrowRecordHolder read(@NotNull IAttachmentHolder holder, @NotNull ValueInput input) {
        ArrowRecordHolder arrows = new ArrowRecordHolder();
        ArrowRecordHolderCodec.load(arrows, input);
        return arrows;
    }

    @Override
    public boolean write(@NotNull ArrowRecordHolder holder, @NotNull ValueOutput output) {
        if (holder.getArrows().isEmpty()) {
            return false;
        }

        ArrowRecordHolderCodec.save(holder, output);
        return true;
    }
}