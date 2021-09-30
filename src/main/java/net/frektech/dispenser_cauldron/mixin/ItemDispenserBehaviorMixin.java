package net.frektech.dispenser_cauldron.mixin;

import net.frektech.dispenser_cauldron.Main;
import net.frektech.dispenser_cauldron.Main.CauldronLevel;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemDispenserBehavior.class)
public abstract class ItemDispenserBehaviorMixin {

	@Inject(at = @At("HEAD"), method = "dispenseSilently", cancellable = true)
	public void CauldronMixin(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        World world = pointer.getWorld();
        if (world.isClient) {
            return;
        }

        if (pointer.getBlockState().getBlock() != Blocks.DISPENSER) {
            return;
        }

        DispenserBlockEntity dispenser = (DispenserBlockEntity)pointer.getBlockEntity();
        BlockPos pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
        BlockState block_state = world.getBlockState(pos);
        Block block = block_state.getBlock();
        Item item = stack.getItem();

        if (!(block instanceof AbstractCauldronBlock cauldron)) {
            return;
        }

        // Handles buckets on full cauldrons
        if (item == Items.BUCKET) {
            if (Main.GetCauldronLevel(block_state) != CauldronLevel.FULL) {
                return;
            }

            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            ItemStack bucket = new ItemStack(Main.CauldronToBucket.getOrDefault(block_state.getBlock(), Items.AIR));

            if (HandleDispense(stack, bucket, dispenser, cir)) {
                customDispenseSilently(pointer, bucket);
            }
        
        // Handles empty bottles on partly or full cauldrons
        } else if (item == Items.GLASS_BOTTLE) {
            if (!Main.CauldronToBottle.containsKey(block)) {
                return;
            }

            world.setBlockState(pos, Main.NewCauldronBottleLevel(block, block_state, false));
            ItemStack bottle = Main.CauldronToBottle.get(block);
            bottle.setCount(1);

            if (HandleDispense(stack, bottle, dispenser, cir)) {
                customDispenseSilently(pointer, bottle);
            }

        // Fills an cauldron with the bucket contents regardless of cauldron type and level (Vanilla Mechanic)
        } else if (Main.BucketToCauldron.containsKey(item)) {
            cir.setReturnValue(new ItemStack(Items.BUCKET));
            world.setBlockState(pos, (BlockState) Main.BucketToCauldron.get(item).get("block_state"));

        // Fills cauldron by 1 with filled bottle
        } else if (Main.ValidBottle(stack)) {
            if (Main.GetCauldronLevel(block_state) == CauldronLevel.FULL) {
                return;
            }

            Block cauldron_type = (Block) Main.BottleToCauldron.get(stack.getNbt().getString("Potion")).get("block");
            world.setBlockState(pos, Main.NewCauldronBottleLevel(cauldron_type, block_state, true));
            if (HandleDispense(stack, new ItemStack(Items.GLASS_BOTTLE), dispenser, cir)) {
                customDispenseSilently(pointer, new ItemStack(Items.GLASS_BOTTLE));
            }
        }
    }

    public boolean HandleDispense(ItemStack stack, ItemStack stack2, DispenserBlockEntity dispenser, CallbackInfoReturnable<ItemStack> cir) {
        stack.decrement(1);
        if (stack.isEmpty()) {
            cir.setReturnValue(stack2);

        } else {
            if (dispenser.addToFirstFreeSlot(stack2) < 0) {
                cir.setReturnValue(stack);
                return true;
            }

            cir.setReturnValue(stack);
        }

        return false;
    }

    public ItemStack customDispenseSilently(BlockPointer pointer, ItemStack stack) {
        BlockPos block_pos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
        BlockPos dispenser_pos = pointer.getPos();

        double velocity_x = block_pos.getX() - dispenser_pos.getX();
        double velocity_y = block_pos.getY() - dispenser_pos.getY();
        double velocity_z = block_pos.getZ() - dispenser_pos.getZ();

        double offset_x = 0;
        double offset_y = 0;
        double offset_z = 0;

        if (velocity_x != 0) { velocity_x /= 1.5; offset_x = 0.2; }
        if (velocity_y != 0) { velocity_y /= 1.5; offset_y = 0.2; }
        if (velocity_z != 0) { velocity_z /= 1.5; offset_z = 0.2; }
        
        World world = pointer.getWorld();
        ItemEntity entity = new ItemEntity(world, pointer.getX() + offset_x, pointer.getY() + offset_y, pointer.getZ() + offset_z, stack, velocity_x, velocity_y, velocity_z);
        world.spawnEntity(entity);
        
        return stack;
    }
}