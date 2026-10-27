package com.albadorada;

import java.util.List;

import org.joml.Vector3f;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Magia del Eclipse Dorado.
 *
 * Los datos de cada jugador se guardan en su "persistent data":
 *   mana, eclipse, seleccion, forma (0 ninguna, 1 demonio, 2 espíritu, 3 eclipse total),
 *   formaTicks, reposo, recarga, recibido.
 */
public final class Magia {
    private Magia() {
    }

    public static final String RAIZ = "alba_dorada";
    public static final int MANA_MAX = 100;
    public static final int ECLIPSE_MAX = 100;

    private static final DustParticleOptions DORADO =
            new DustParticleOptions(new Vector3f(1.0f, 0.78f, 0.15f), 1.2f);

    public enum Hechizo {
        ESPADA_DE_LUZ("Espada de Luz", 10, 0),
        LLUVIA_DE_DESTELLOS("Lluvia de Destellos", 20, 0),
        PASO_DORADO("Paso Dorado", 15, 0),
        PUNO_DEL_ECLIPSE("Puño del Eclipse", 25, 0),
        SELLO_DEL_ECLIPSE("Sello del Eclipse", 30, 0),
        FORMA_DEMONIO("Forma de Demonio", 40, 0),
        FORMA_ESPIRITU("Forma de Espíritu", 40, 0),
        ECLIPSE_TOTAL("Eclipse Total", 60, ECLIPSE_MAX);

        public final String nombre;
        public final int costo;
        public final int eclipseNecesario;

        Hechizo(String nombre, int costo, int eclipseNecesario) {
            this.nombre = nombre;
            this.costo = costo;
            this.eclipseNecesario = eclipseNecesario;
        }

        public boolean esForma() {
            return this == FORMA_DEMONIO || this == FORMA_ESPIRITU || this == ECLIPSE_TOTAL;
        }
    }

    // ------------------------------------------------------------------ datos

    public static CompoundTag datos(Player jugador) {
        CompoundTag raiz = jugador.getPersistentData();
        if (!raiz.contains(RAIZ, 10)) {
            CompoundTag nuevo = new CompoundTag();
            nuevo.putInt("mana", MANA_MAX);
            raiz.put(RAIZ, nuevo);
        }
        return raiz.getCompound(RAIZ);
    }

    public static Hechizo actual(Player jugador) {
        Hechizo[] todos = Hechizo.values();
        return todos[Math.floorMod(datos(jugador).getInt("seleccion"), todos.length)];
    }

    public static void siguiente(ServerPlayer jugador) {
        CompoundTag d = datos(jugador);
        int indice = (d.getInt("seleccion") + 1) % Hechizo.values().length;
        d.putInt("seleccion", indice);
        aviso(jugador, Component.literal("Hechizo: " + Hechizo.values()[indice].nombre)
                .withStyle(ChatFormatting.GOLD));
    }

    public static void reiniciarTrasMorir(Player jugador) {
        CompoundTag d = datos(jugador);
        d.putInt("forma", 0);
        d.putInt("formaTicks", 0);
        d.putInt("reposo", 0);
        d.putInt("recarga", 0);
        d.putInt("mana", MANA_MAX);
    }

    private static void aviso(ServerPlayer jugador, Component mensaje) {
        jugador.displayClientMessage(mensaje, true);
    }

    private static float multiplicador(int forma) {
        return switch (forma) {
            case 1 -> 1.5f;
            case 2 -> 0.8f;
            case 3 -> 2.0f;
            default -> 1.0f;
        };
    }

    // ----------------------------------------------------------------- lanzar

    public static void lanzar(ServerPlayer p) {
        CompoundTag d = datos(p);
        if (d.getInt("recarga") > 0) {
            return;
        }
        Hechizo h = actual(p);
        int forma = d.getInt("forma");

        if (h.esForma()) {
            if (d.getInt("reposo") > 0) {
                aviso(p, Component.literal("Tu cuerpo aún se recupera: " + (d.getInt("reposo") / 20 + 1) + " s")
                        .withStyle(ChatFormatting.RED));
                return;
            }
            if (forma == 3 || (forma != 0 && h != Hechizo.ECLIPSE_TOTAL)) {
                aviso(p, Component.literal("Ya estás transformado.").withStyle(ChatFormatting.RED));
                return;
            }
        }
        if (d.getInt("mana") < h.costo) {
            aviso(p, Component.literal("No tienes suficiente maná.").withStyle(ChatFormatting.RED));
            return;
        }
        if (d.getInt("eclipse") < h.eclipseNecesario) {
            aviso(p, Component.literal("Tu barra de Eclipse no está llena.").withStyle(ChatFormatting.RED));
            return;
        }

        d.putInt("mana", d.getInt("mana") - h.costo);
        d.putInt("recarga", 8);
        if (!h.esForma()) {
            d.putInt("eclipse", Math.min(ECLIPSE_MAX, d.getInt("eclipse") + 8));
        }

        ServerLevel nivel = p.serverLevel();
        float mult = multiplicador(forma);
        switch (h) {
            case ESPADA_DE_LUZ -> espada(p, nivel, mult);
            case LLUVIA_DE_DESTELLOS -> lluvia(p, nivel, mult);
            case PASO_DORADO -> paso(p, nivel);
            case PUNO_DEL_ECLIPSE -> puno(p, nivel, mult);
            case SELLO_DEL_ECLIPSE -> sello(p, nivel);
            case FORMA_DEMONIO -> activarForma(p, nivel, 1, 1200);
            case FORMA_ESPIRITU -> activarForma(p, nivel, 2, 1200);
            case ECLIPSE_TOTAL -> activarForma(p, nivel, 3, 600);
        }
    }

    private static List<LivingEntity> objetivos(ServerPlayer p, ServerLevel nivel, AABB caja) {
        return nivel.getEntitiesOfClass(LivingEntity.class, caja,
                e -> e != p && e.isAlive() && !e.isSpectator());
    }

    // ---------------------------------------------------------------- hechizos

    private static void espada(ServerPlayer p, ServerLevel nivel, float mult) {
        Vec3 mira = p.getLookAngle();
        Vec3 centro = p.position().add(0, 1.0, 0).add(mira.scale(2.0));
        for (LivingEntity e : objetivos(p, nivel, new AABB(centro, centro).inflate(2.2))) {
            e.hurt(p.damageSources().playerAttack(p), 9.0f * mult);
            e.knockback(0.6, -mira.x, -mira.z);
        }
        for (int i = -4; i <= 4; i++) {
            Vec3 dir = mira.yRot((float) Math.toRadians(i * 18));
            Vec3 pos = p.position().add(0, 1.2, 0).add(dir.scale(2.2));
            nivel.sendParticles(DORADO, pos.x, pos.y, pos.z, 4, 0.1, 0.25, 0.1, 0.0);
        }
        nivel.playSound(null, p.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1.0f, 1.2f);
    }

    private static void lluvia(ServerPlayer p, ServerLevel nivel, float mult) {
        Vec3 mira = p.getLookAngle();
        for (int i = 0; i < 5; i++) {
            Vec3 dir = mira.add(
                    (p.getRandom().nextDouble() - 0.5) * 0.30,
                    (p.getRandom().nextDouble() - 0.5) * 0.20,
                    (p.getRandom().nextDouble() - 0.5) * 0.30).normalize();
            rayo(p, nivel, dir, 20.0, 5.0f * mult);
        }
        nivel.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    private static void rayo(ServerPlayer p, ServerLevel nivel, Vec3 dir, double alcance, float dano) {
        Vec3 inicio = p.getEyePosition();
        for (double dist = 1.0; dist <= alcance; dist += 0.6) {
            Vec3 pos = inicio.add(dir.scale(dist));
            BlockPos bloque = BlockPos.containing(pos);
            if (!nivel.getBlockState(bloque).getCollisionShape(nivel, bloque).isEmpty()) {
                break;
            }
            nivel.sendParticles(DORADO, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
            List<LivingEntity> golpeados = objetivos(p, nivel, new AABB(pos, pos).inflate(0.6));
            if (!golpeados.isEmpty()) {
                LivingEntity objetivo = golpeados.get(0);
                objetivo.invulnerableTime = 0;
                objetivo.hurt(p.damageSources().playerAttack(p), dano);
                break;
            }
        }
    }

    private static boolean libre(ServerLevel nivel, BlockPos pos) {
        return nivel.getBlockState(pos).getCollisionShape(nivel, pos).isEmpty();
    }

    private static void paso(ServerPlayer p, ServerLevel nivel) {
        Vec3 dir = p.getLookAngle();
        Vec3 inicio = p.position();
        Vec3 ultimo = inicio;
        for (double dist = 0.5; dist <= 8.0; dist += 0.5) {
            Vec3 pos = inicio.add(dir.scale(dist));
            BlockPos pies = BlockPos.containing(pos);
            if (!libre(nivel, pies) || !libre(nivel, pies.above())) {
                break;
            }
            ultimo = pos;
            nivel.sendParticles(DORADO, pos.x, pos.y + 1.0, pos.z, 3, 0.2, 0.4, 0.2, 0.0);
        }
        p.teleportTo(ultimo.x, ultimo.y, ultimo.z);
        p.fallDistance = 0.0f;
        nivel.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 0.8f, 1.6f);
    }

    private static void puno(ServerPlayer p, ServerLevel nivel, float mult) {
        for (LivingEntity e : objetivos(p, nivel, p.getBoundingBox().inflate(4.0))) {
            e.hurt(p.damageSources().playerAttack(p), 12.0f * mult);
            e.knockback(1.2, p.getX() - e.getX(), p.getZ() - e.getZ());
        }
        for (int i = 0; i < 24; i++) {
            double ang = Math.toRadians(i * 15);
            for (double radio = 1.0; radio <= 4.0; radio += 1.5) {
                nivel.sendParticles(DORADO, p.getX() + Math.cos(ang) * radio, p.getY() + 0.2,
                        p.getZ() + Math.sin(ang) * radio, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        nivel.sendParticles(ParticleTypes.EXPLOSION, p.getX(), p.getY() + 0.5, p.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        nivel.playSound(null, p.blockPosition(), SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS, 0.6f, 1.4f);
    }

    private static void sello(ServerPlayer p, ServerLevel nivel) {
        List<LivingEntity> cerca = nivel.getEntitiesOfClass(LivingEntity.class,
                p.getBoundingBox().inflate(6.0), e -> e.isAlive() && !e.isSpectator());
        for (LivingEntity e : cerca) {
            if (e instanceof Player) {
                e.heal(8.0f);
                e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
            } else {
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0));
            }
        }
        for (int i = 0; i < 36; i++) {
            double ang = Math.toRadians(i * 10);
            nivel.sendParticles(DORADO, p.getX() + Math.cos(ang) * 6.0, p.getY() + 0.2,
                    p.getZ() + Math.sin(ang) * 6.0, 1, 0.0, 0.1, 0.0, 0.0);
        }
        nivel.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    // ------------------------------------------------------------ transformaciones

    private static void activarForma(ServerPlayer p, ServerLevel nivel, int forma, int ticks) {
        CompoundTag d = datos(p);
        d.putInt("forma", forma);
        d.putInt("formaTicks", ticks);
        quitarEfectosForma(p);
        p.addEffect(new MobEffectInstance(efectoDe(forma), ticks, 0, false, false, true));
        if (forma == 3) {
            d.putInt("eclipse", 0);
        }
        nivel.sendParticles(DORADO, p.getX(), p.getY() + 1.0, p.getZ(), 80, 0.8, 1.0, 0.8, 0.1);
        nivel.playSound(null, p.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 1.0f, forma == 1 ? 0.6f : 1.4f);
        String nombre = switch (forma) {
            case 1 -> "Forma de Demonio";
            case 2 -> "Forma de Espíritu";
            default -> "ECLIPSE TOTAL";
        };
        p.sendSystemMessage(Component.literal(nombre + " activada.").withStyle(ChatFormatting.GOLD));
    }

    /** Efecto marcador de cada forma: lo usa el cliente para dibujar las alas. */
    private static MobEffect efectoDe(int forma) {
        return switch (forma) {
            case 1 -> AlbaDoradaMod.EFECTO_DEMONIO.get();
            case 2 -> AlbaDoradaMod.EFECTO_ESPIRITU.get();
            default -> AlbaDoradaMod.EFECTO_ECLIPSE.get();
        };
    }

    private static void quitarEfectosForma(ServerPlayer p) {
        p.removeEffect(AlbaDoradaMod.EFECTO_DEMONIO.get());
        p.removeEffect(AlbaDoradaMod.EFECTO_ESPIRITU.get());
        p.removeEffect(AlbaDoradaMod.EFECTO_ECLIPSE.get());
    }

    private static void terminarForma(ServerPlayer p, CompoundTag d, int formaAnterior) {
        d.putInt("forma", 0);
        d.putInt("formaTicks", 0);
        d.putInt("reposo", formaAnterior == 3 ? 1200 : 600);
        quitarEfectosForma(p);
        p.sendSystemMessage(Component.literal("Tu transformación terminó.").withStyle(ChatFormatting.GRAY));
    }

    // ------------------------------------------------------------------- tick

    public static void tick(ServerPlayer p) {
        CompoundTag d = datos(p);
        int t = p.tickCount;
        int forma = d.getInt("forma");

        if (d.getInt("recarga") > 0) {
            d.putInt("recarga", d.getInt("recarga") - 1);
        }
        if (d.getInt("reposo") > 0) {
            d.putInt("reposo", d.getInt("reposo") - 1);
        }
        if (t % 8 == 0) {
            int regen = (forma == 2 || forma == 3) ? 2 : 1;
            d.putInt("mana", Math.min(MANA_MAX, d.getInt("mana") + regen));
        }

        if (forma != 0) {
            int restante = d.getInt("formaTicks") - 1;
            if (restante <= 0 || !p.hasEffect(efectoDe(forma))) {
                terminarForma(p, d, forma);
            } else {
                d.putInt("formaTicks", restante);
                if (t % 20 == 0) {
                    efectos(p, forma);
                }
                if (forma == 1 && t % 100 == 0 && p.getHealth() > 6.0f) {
                    p.hurt(p.damageSources().magic(), 1.0f);
                }
                if (t % 3 == 0) {
                    aura(p, forma);
                }
            }
        }

        if (t % 10 == 0 && sosteniendoGrimorio(p)) {
            barra(p, d);
        }
    }

    private static void efectos(ServerPlayer p, int forma) {
        if (forma == 1 || forma == 3) {
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, forma == 3 ? 1 : 1, true, false));
        }
        if (forma == 2 || forma == 3) {
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, true, false));
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false));
        }
        if (forma == 3) {
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false));
        }
    }

    private static void aura(ServerPlayer p, int forma) {
        ServerLevel nivel = p.serverLevel();
        double x = p.getX();
        double y = p.getY() + 1.0;
        double z = p.getZ();
        nivel.sendParticles(DORADO, x, y, z, 3, 0.4, 0.7, 0.4, 0.0);
        if (forma == 1 || forma == 3) {
            nivel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 1, 0.4, 0.7, 0.4, 0.01);
        }
        if (forma == 2 || forma == 3) {
            nivel.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.4, 0.7, 0.4, 0.02);
        }
    }

    private static boolean sosteniendoGrimorio(ServerPlayer p) {
        return p.getMainHandItem().is(AlbaDoradaMod.GRIMORIO.get())
                || p.getOffhandItem().is(AlbaDoradaMod.GRIMORIO.get());
    }

    private static void barra(ServerPlayer p, CompoundTag d) {
        Component texto = Component.literal("Maná " + d.getInt("mana") + "/" + MANA_MAX + "   ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal("Eclipse " + d.getInt("eclipse") + "/" + ECLIPSE_MAX + "   ")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(actual(p).nombre).withStyle(ChatFormatting.WHITE));
        aviso(p, texto);
    }
}
