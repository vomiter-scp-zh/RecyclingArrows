package com.vomiter.recyclingarrows;

import net.minecraft.resources.ResourceLocation;

public class Helpers {
    public static ResourceLocation id(String namespace, String path){
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation id(String path){
        return id(RecyclingArrows.MOD_ID, path);
    }
}
