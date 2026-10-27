package com.albadorada;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Efecto "marcador" de cada transformación. Sirve para que los demás jugadores
 * (y tu propio cliente) sepan que estás transformado y dibujen las alas.
 */
public class EfectoForma extends MobEffect {

    public EfectoForma(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }
}
