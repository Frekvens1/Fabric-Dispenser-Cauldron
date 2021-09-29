package net.frektech.dispenser_cauldron.mixin;

import net.frektech.dispenser_cauldron.BottleDispenserBehaviour;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin
{
    @Shadow @Final private static Map<Item, DispenserBehavior> BEHAVIORS;

    @Inject(method = "getBehaviorForItem", at = @At("HEAD"), cancellable = true)
    private void getBehaviorForItem(ItemStack stack, CallbackInfoReturnable<DispenserBehavior> cir)
    {
        if ((stack.getItem() == Items.GLASS_BOTTLE)) {
            cir.setReturnValue(new BottleDispenserBehaviour());

        } else if (stack.getItem() == Items.POTION) {
            if (stack.getNbt() == null) {
                return;
            }

            if (!stack.getNbt().getString("Potion").equalsIgnoreCase("minecraft:water")) {
                return;
            }

            cir.setReturnValue(new BottleDispenserBehaviour());
        }
    }
}