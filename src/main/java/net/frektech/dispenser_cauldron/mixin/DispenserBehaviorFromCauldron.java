package net.frektech.dispenser_cauldron.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/block/dispenser/DispenserBehavior$9")
public abstract class DispenserBehaviorFromCauldron extends ItemDispenserBehavior {
    @Inject(at = @At("HEAD"), method = "dispenseSilently(Lnet/minecraft/util/math/BlockPointer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", cancellable = true)
    private void fillBucketFromCauldron(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        World world = pointer.getWorld();
        if (world.isClient) {
            return;
        }

        if (stack.getItem() != Items.BUCKET) {
            return;
        }

        BlockPos pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
        BlockState block_state = world.getBlockState(pos);
        if (block_state.getBlock() == Blocks.LAVA_CAULDRON) {
            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            stack.decrement(1);

            if (stack.isEmpty()) {
                cir.setReturnValue(new ItemStack(Items.LAVA_BUCKET));

            } else {
                if (((DispenserBlockEntity)pointer.getBlockEntity()).addToFirstFreeSlot(new ItemStack(Items.LAVA_BUCKET)) < 0) {
                    super.dispenseSilently(pointer, new ItemStack(Items.LAVA_BUCKET));
                }

                cir.setReturnValue(stack);
            }

        } else if (block_state.getBlock() == Blocks.WATER_CAULDRON) {
            if (block_state.get(Properties.LEVEL_3) != 3) { // Check if cauldron is full (Lava can only be full)
                return;
            }
    
            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            stack.decrement(1);
    
            if (stack.isEmpty())
            {
                cir.setReturnValue(new ItemStack(Items.WATER_BUCKET));
    
            } else {
                if (((DispenserBlockEntity)pointer.getBlockEntity()).addToFirstFreeSlot(new ItemStack(Items.WATER_BUCKET)) < 0) {
                    super.dispenseSilently(pointer, new ItemStack(Items.WATER_BUCKET));
                }
    
                cir.setReturnValue(stack);
            }

        } else if (block_state.getBlock() == Blocks.POWDER_SNOW_CAULDRON) {
            if (block_state.get(Properties.LEVEL_3) != 3) { // Check if cauldron is full (Lava can only be full)
                return;
            }
    
            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            stack.decrement(1);
    
            if (stack.isEmpty())
            {
                cir.setReturnValue(new ItemStack(Items.POWDER_SNOW_BUCKET));
    
            } else {
                if (((DispenserBlockEntity)pointer.getBlockEntity()).addToFirstFreeSlot(new ItemStack(Items.POWDER_SNOW_BUCKET)) < 0) {
                    super.dispenseSilently(pointer, new ItemStack(Items.POWDER_SNOW_BUCKET));
                }
    
                cir.setReturnValue(stack);
            }
        }
    }
}
