package com.vomiter.recyclingarrows;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;

public class Helpers {
    public static Identifier id(String namespace, String path){
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    public static Identifier id(String path){
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
