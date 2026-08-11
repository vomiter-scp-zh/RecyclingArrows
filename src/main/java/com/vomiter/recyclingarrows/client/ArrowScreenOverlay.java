package com.vomiter.recyclingarrows.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ArrowScreenOverlay {
    private static final int ICON_SIZE = 16;
    private static final int GAP = 2;

    public static void drawArrowIndicator(
            Minecraft minecraft,
            GuiGraphics graphics,
            ItemStack arrowStack,
            int count,
            int centerX,
            int y
    ) {
        Component countText = Component.literal("× " + count);
        int textWidth = minecraft.font.width(countText);
        int totalWidth = ICON_SIZE + GAP + textWidth;

        int iconX = centerX - totalWidth / 2;
        int textX = iconX + ICON_SIZE + GAP;

        graphics.renderItem(arrowStack, iconX, y);

        graphics.drawString(
                minecraft.font,
                countText,
                textX,
                y + (ICON_SIZE - minecraft.font.lineHeight) / 2,
                0xCCCCCC,
                false
        );
    }
}