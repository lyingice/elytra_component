package net.sc.elytracomponent.mixin;

import net.sc.elytracomponent.component.ElytraComponent;
import net.sc.elytracomponent.component.ModComponents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void elytraComponent$tryStartFallFlying(CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) (Object) this;

        if (!self.onGround() && !self.isFallFlying() && !self.isInWater()
                && !self.hasEffect(MobEffects.LEVITATION)) {

            ItemStack chestStack = self.getItemBySlot(EquipmentSlot.CHEST);
            ElytraComponent component = ModComponents.getComponent(chestStack);

            if (component != null && component.currentDurability() > 0) {
                self.startFallFlying();
                cir.setReturnValue(true);
            }
        }
    }
}
