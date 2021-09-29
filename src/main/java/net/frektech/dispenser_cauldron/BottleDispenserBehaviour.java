package net.frektech.dispenser_cauldron;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BottleDispenserBehaviour extends ItemDispenserBehavior {
    @Override
    public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        World world = pointer.getWorld();
        if (world.isClient) {
            super.dispenseSilently(pointer, stack);
            stack.decrement(1);
            return stack;
        }

        BlockPos pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
        BlockState block_state = world.getBlockState(pos);
        if (stack.getItem() == Items.GLASS_BOTTLE) {
            if (block_state.getBlock() == Blocks.WATER_CAULDRON) {
                ItemStack water_bottle = new ItemStack(Items.POTION);
                NbtCompound nbt_tags = new NbtCompound();
                nbt_tags.putString("Potion", "minecraft:water");
                water_bottle.setNbt(nbt_tags);

                Integer water_level = (Integer)block_state.get(Properties.LEVEL_3);
                if (water_level > 1) {
                    world.setBlockState(pos, block_state.with(Properties.LEVEL_3, water_level - 1));

                } else {
                    world.setBlockState(pos, Blocks.CAULDRON.getDefaultState()); 
                }
                
                stack.decrement(1);
    
                if (stack.isEmpty()) {
                    return water_bottle;
    
                } else {
                    if (((DispenserBlockEntity)pointer.getBlockEntity()).addToFirstFreeSlot(water_bottle) < 0) {
                        super.dispenseSilently(pointer, water_bottle);
                    }
    
                    return stack;
                }
            }

        } else if (stack.getItem() == Items.POTION) {
            if (block_state.getBlock() == Blocks.WATER_CAULDRON) {
                Integer water_level = (Integer)block_state.get(Properties.LEVEL_3);
                if (water_level == 3) {
                    super.dispenseSilently(pointer, stack); // It's already full, abort.
                    stack.decrement(1);
                    return stack;

                } else {
                    world.setBlockState(pos, block_state.with(Properties.LEVEL_3, water_level + 1));
                }

            } else if (block_state.getBlock() == Blocks.CAULDRON) {
                world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState());    

            } else {
                super.dispenseSilently(pointer, stack);
                stack.decrement(1);
                return stack;
            }

            stack.decrement(1);
            if (stack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
    
            } else {
                if (((DispenserBlockEntity)pointer.getBlockEntity()).addToFirstFreeSlot(new ItemStack(Items.GLASS_BOTTLE)) < 0) {
                    return new ItemStack(Items.GLASS_BOTTLE);
                }
    
                return stack;
            }
        }

        return stack;
    }
}
