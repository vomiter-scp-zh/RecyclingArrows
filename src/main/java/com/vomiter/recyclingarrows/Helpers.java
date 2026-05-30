package com.vomiter.recyclingarrows;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;

public class Helpers {
    public static ResourceLocation id(String namespace, String path){
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ResourceLocation id(String path){
        return id(RecyclingArrows.MOD_ID, path);
    }

    public static EquipmentSlot getHandSlot(InteractionHand hand){
        if(hand.equals(InteractionHand.MAIN_HAND)) return EquipmentSlot.MAINHAND;
        else if (hand.equals(InteractionHand.OFF_HAND)) {
            return EquipmentSlot.OFFHAND;
        }
        return null;
    }
}
