package com.albadorada;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AlbaDoradaMod.MOD_ID)
public class EventosJugador {

    /** La primera vez que Percy entra al mundo recibe su grimorio. */
    @SubscribeEvent
    public static void alEntrar(PlayerEvent.PlayerLoggedInEvent evento) {
        if (!(evento.getEntity() instanceof ServerPlayer jugador)) {
            return;
        }
        CompoundTag datos = Magia.datos(jugador);
        if (!datos.getBoolean("recibido")) {
            datos.putBoolean("recibido", true);
            jugador.addItem(new ItemStack(AlbaDoradaMod.GRIMORIO.get()));
            jugador.sendSystemMessage(Component
                    .literal("Capitán Percy, líder del Escuadrón Alba Dorada: tu grimorio de cinco hojas te espera. "
                            + "Clic derecho lanza el hechizo; agachado + clic derecho cambia de hechizo.")
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    /** Al morir se conservan los datos de la magia (el maná se rellena y las formas terminan). */
    @SubscribeEvent
    public static void alClonar(PlayerEvent.Clone evento) {
        CompoundTag viejo = evento.getOriginal().getPersistentData();
        if (viejo.contains(Magia.RAIZ, 10)) {
            evento.getEntity().getPersistentData().put(Magia.RAIZ, viejo.getCompound(Magia.RAIZ).copy());
        }
        if (evento.isWasDeath()) {
            Magia.reiniciarTrasMorir(evento.getEntity());
        }
    }

    @SubscribeEvent
    public static void cadaTick(TickEvent.PlayerTickEvent evento) {
        if (evento.phase != TickEvent.Phase.END || evento.side.isClient()) {
            return;
        }
        if (evento.player instanceof ServerPlayer jugador) {
            Magia.tick(jugador);
        }
    }
}
