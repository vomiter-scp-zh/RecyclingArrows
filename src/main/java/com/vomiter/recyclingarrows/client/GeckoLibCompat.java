package com.vomiter.recyclingarrows.client;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GeckoLibCompat {
    static void addLayer(EntityRenderer<?> renderer, EntityRenderDispatcher dispatcher){
        if (renderer instanceof GeoEntityRenderer geoRenderer) {
            geoRenderer.addRenderLayer(new RecyclingArrowGeoLayer<>(geoRenderer, dispatcher));
        }
    }
}
