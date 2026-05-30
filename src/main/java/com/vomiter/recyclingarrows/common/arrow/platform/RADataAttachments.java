package com.vomiter.recyclingarrows.common.arrow.platform;

import com.vomiter.recyclingarrows.RecyclingArrows;
import com.vomiter.recyclingarrows.common.arrow.data.ArrowRecordHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class RADataAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RecyclingArrows.MOD_ID);

    public static final Supplier<AttachmentType<ArrowRecordHolder>> ARROW_RECORDS =
            ATTACHMENT_TYPES.register(
                    "arrow_records",
                    () -> AttachmentType
                            .builder(ArrowRecordHolder::new)
                            .serialize(ArrowRecordHolderSerializer.INSTANCE)
                            .build()
            );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }

    private RADataAttachments() {
    }
}