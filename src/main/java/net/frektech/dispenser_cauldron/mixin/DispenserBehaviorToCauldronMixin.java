package net.frektech.dispenser_cauldron.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/block/dispenser/DispenserBehavior$8")
public abstract class DispenserBehaviorToCauldronMixin extends ItemDispenserBehavior {
    @Inject(at = @At("HEAD"), method = "dispenseSilently(Lnet/minecraft/util/math/BlockPointer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", cancellable = true)
    private void fillCauldronFromBucket(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        World world = pointer.getWorld();
        if (world.isClient) {
            return;
        }
            
        BlockPos pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
        Block block = world.getBlockState(pos).getBlock();
        if (block != Blocks.CAULDRON) {
            return;
        }

        if (stack.getItem() == Items.LAVA_BUCKET) {
            cir.setReturnValue(new ItemStack(Items.BUCKET));
            world.setBlockState(pos, Blocks.LAVA_CAULDRON.getDefaultState());

        } else if (stack.getItem() == Items.WATER_BUCKET) {
            cir.setReturnValue(new ItemStack(Items.BUCKET));
            world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
        }
    }
}
