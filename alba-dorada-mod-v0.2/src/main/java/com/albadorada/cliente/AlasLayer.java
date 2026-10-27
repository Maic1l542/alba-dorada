package com.albadorada.cliente;

import com.albadorada.AlbaDoradaMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Dibuja las alas en la espalda del jugador según su transformación. */
public class AlasLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation DEMONIO =
            new ResourceLocation(AlbaDoradaMod.MOD_ID, "textures/entity/alas_demonio.png");
    private static final ResourceLocation ESPIRITU =
            new ResourceLocation(AlbaDoradaMod.MOD_ID, "textures/entity/alas_espiritu.png");
    private static final ResourceLocation ECLIPSE =
            new ResourceLocation(AlbaDoradaMod.MOD_ID, "textures/entity/alas_eclipse.png");

    private final AlasModel<AbstractClientPlayer> modelo;

    public AlasLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> padre,
                     EntityModelSet modelos) {
        super(padre);
        this.modelo = new AlasModel<>(modelos.bakeLayer(AlasModel.CAPA));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int luz, AbstractClientPlayer jugador,
                       float limbSwing, float limbSwingAmount, float parcial, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (jugador.isInvisible()) {
            return;
        }
        ResourceLocation textura;
        if (jugador.hasEffect(AlbaDoradaMod.EFECTO_ECLIPSE.get())) {
            textura = ECLIPSE;
        } else if (jugador.hasEffect(AlbaDoradaMod.EFECTO_DEMONIO.get())) {
            textura = DEMONIO;
        } else if (jugador.hasEffect(AlbaDoradaMod.EFECTO_ESPIRITU.get())) {
            textura = ESPIRITU;
        } else {
            return;
        }

        pose.pushPose();
        pose.translate(0.0f, 0.0f, 0.125f);
        modelo.setupAnim(jugador, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer vertices = buffer.getBuffer(RenderType.entityTranslucent(textura));
        modelo.renderToBuffer(pose, vertices, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f);
        pose.popPose();
    }
}
