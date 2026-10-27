package com.albadorada.cliente;

import com.albadorada.AlbaDoradaMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Modelo 3D de las alas, hecho solo con código: cada ala son 5 plumas (cajas)
 * abiertas en abanico. Se mueve con un aleteo suave.
 */
public class AlasModel<T extends LivingEntity> extends EntityModel<T> {

    public static final ModelLayerLocation CAPA =
            new ModelLayerLocation(new ResourceLocation(AlbaDoradaMod.MOD_ID, "alas"), "principal");

    private static final float[] LARGO = {13.0f, 17.0f, 18.0f, 16.0f, 12.0f};
    private static final float[] ANGULO = {70.0f, 45.0f, 20.0f, -5.0f, -30.0f};

    private final ModelPart izquierda;
    private final ModelPart derecha;

    public AlasModel(ModelPart raiz) {
        this.izquierda = raiz.getChild("izquierda");
        this.derecha = raiz.getChild("derecha");
    }

    public static LayerDefinition crearCapa() {
        MeshDefinition malla = new MeshDefinition();
        PartDefinition raiz = malla.getRoot();
        PartDefinition izq = raiz.addOrReplaceChild("izquierda", CubeListBuilder.create(),
                PartPose.offset(2.0f, 2.0f, 0.0f));
        PartDefinition der = raiz.addOrReplaceChild("derecha", CubeListBuilder.create(),
                PartPose.offset(-2.0f, 2.0f, 0.0f));

        for (int i = 0; i < LARGO.length; i++) {
            float angulo = (float) Math.toRadians(ANGULO[i]);
            izq.addOrReplaceChild("pluma" + i,
                    CubeListBuilder.create().texOffs(0, i * 5)
                            .addBox(0.0f, -1.5f, 0.0f, LARGO[i], 3.0f, 1.0f),
                    PartPose.rotation(0.0f, 0.0f, -angulo));
            der.addOrReplaceChild("pluma" + i,
                    CubeListBuilder.create().texOffs(0, i * 5)
                            .addBox(-LARGO[i], -1.5f, 0.0f, LARGO[i], 3.0f, 1.0f),
                    PartPose.rotation(0.0f, 0.0f, angulo));
        }
        return LayerDefinition.create(malla, 64, 32);
    }

    @Override
    public void setupAnim(T entidad, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float aleteo = (float) Math.sin(ageInTicks * 0.15f) * 0.18f;
        float abierta = 0.45f;
        float inclinacion = 0.0f;
        float bajada = 0.0f;
        if (entidad.isCrouching()) {
            inclinacion = 0.5f;
            bajada = 3.0f;
        }
        izquierda.yRot = -(abierta + aleteo);
        derecha.yRot = abierta + aleteo;
        izquierda.xRot = inclinacion;
        derecha.xRot = inclinacion;
        izquierda.y = 2.0f + bajada;
        derecha.y = 2.0f + bajada;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int luz, int superposicion,
                               float rojo, float verde, float azul, float alfa) {
        izquierda.render(pose, buffer, luz, superposicion, rojo, verde, azul, alfa);
        derecha.render(pose, buffer, luz, superposicion, rojo, verde, azul, alfa);
    }
}
