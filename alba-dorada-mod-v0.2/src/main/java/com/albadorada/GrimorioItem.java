package com.albadorada;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** El Grimorio de Cinco Hojas: clic derecho lanza, agachado + clic derecho cambia de hechizo. */
public class GrimorioItem extends Item {

    public GrimorioItem(Properties propiedades) {
        super(propiedades);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level nivel, Player jugador, InteractionHand mano) {
        ItemStack objeto = jugador.getItemInHand(mano);
        if (!nivel.isClientSide && jugador instanceof ServerPlayer sp) {
            if (jugador.isShiftKeyDown()) {
                Magia.siguiente(sp);
            } else {
                Magia.lanzar(sp);
            }
        }
        return InteractionResultHolder.sidedSuccess(objeto, nivel.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack objeto) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack objeto, @Nullable Level nivel, List<Component> texto, TooltipFlag bandera) {
        texto.add(Component.translatable("tooltip.albadorada.grimorio1").withStyle(ChatFormatting.GOLD));
        texto.add(Component.translatable("tooltip.albadorada.grimorio2").withStyle(ChatFormatting.GRAY));
    }
}
