package net.ec.elytracomponent.component;

import com.mojang.serialization.Codec;
import net.ec.elytracomponent.ElytraComponentMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 注册本模组的所有 DataComponentType。
 */
public class ModComponents {

    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ElytraComponentMod.MODID);

    /**
     * 鞘翅组件数据：存储安装在胸甲上的鞘翅信息
     */
    public static final Supplier<DataComponentType<ElytraComponent>> ELYTRA_COMPONENT =
            COMPONENTS.register("elytra_component", () ->
                    DataComponentType.<ElytraComponent>builder()
                            .persistent(ElytraComponent.CODEC)        // 持久化存储
                            .networkSynchronized(ElytraComponent.STREAM_CODEC) // 网络同步
                            .build()
            );

    /**
     * 标记当前胸甲是否可以飞行（安装鞘翅组件后设置为 true）
     */
    public static final Supplier<DataComponentType<Boolean>> CAN_ELYTRA_FLY =
            COMPONENTS.register("can_elytra_fly", () ->
                    DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build()
            );

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
