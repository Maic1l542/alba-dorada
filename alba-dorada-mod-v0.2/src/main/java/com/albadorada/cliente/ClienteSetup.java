package com.albadorada.cliente;

import com.albadorada.AlbaDoradaMod;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Registro de lo que solo existe en el cliente (el que juega): el modelo y la capa de alas. */
@Mod.EventBusSubscriber(modid = AlbaDoradaMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClienteSetup {

    @SubscribeEvent
    public static void registrarModelos(EntityRenderersEvent.RegisterLayerDefinitions evento) {
        evento.registerLayerDefinition(AlasModel.CAPA, AlasModel::crearCapa);
    }

    @SubscribeEvent
    public static void agregarCapas(EntityRenderersEvent.AddLayers evento) {
        for (String piel : evento.getSkins()) {
            EntityRenderer<? extends Player> renderizador = evento.getSkin(piel);
            if (renderizador instanceof PlayerRenderer jugadorRenderer) {
                jugadorRenderer.addLayer(new AlasLayer(jugadorRenderer, evento.getEntityModels()));
            }
        }
    }
}
