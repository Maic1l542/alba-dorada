package com.albadorada;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(AlbaDoradaMod.MOD_ID)
public class AlbaDoradaMod {
    public static final String MOD_ID = "albadorada";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<MobEffect> EFECTOS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> PESTANAS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<Item> GRIMORIO = ITEMS.register("grimorio_cinco_hojas",
            () -> new GrimorioItem(new Item.Properties().stacksTo(1).fireResistant()));

    public static final RegistryObject<MobEffect> EFECTO_DEMONIO =
            EFECTOS.register("forma_demonio", () -> new EfectoForma(0x2A1A3A));
    public static final RegistryObject<MobEffect> EFECTO_ESPIRITU =
            EFECTOS.register("forma_espiritu", () -> new EfectoForma(0xFFE08A));
    public static final RegistryObject<MobEffect> EFECTO_ECLIPSE =
            EFECTOS.register("forma_eclipse", () -> new EfectoForma(0xFFB000));

    public static final RegistryObject<CreativeModeTab> PESTANA = PESTANAS.register("alba_dorada",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.albadorada"))
                    .icon(() -> new ItemStack(GRIMORIO.get()))
                    .displayItems((parametros, salida) -> salida.accept(GRIMORIO.get()))
                    .build());

    public AlbaDoradaMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        EFECTOS.register(bus);
        PESTANAS.register(bus);
    }
}
