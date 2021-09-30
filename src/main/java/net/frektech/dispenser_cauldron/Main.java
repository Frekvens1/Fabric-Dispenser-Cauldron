package net.frektech.dispenser_cauldron;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.api.ModInitializer;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

public class Main implements ModInitializer {
	@Override
	public void onInitialize() {
		System.out.println("Dispenser Cauldron v.1.0.4 - Let dispensers use buckets and bottles on cauldrons!");
	}

	public static HashMap<Item, HashMap<String, Object>> BucketToCauldron = new HashMap<>() {
		{
			put(Items.LAVA_BUCKET, new HashMap<String, Object>() {
				{
					put("block", 	   Blocks.LAVA_CAULDRON);
					put("block_state", Blocks.LAVA_CAULDRON.getDefaultState());
					put("has_levels", false);
				}
			});

			put(Items.WATER_BUCKET, new HashMap<String, Object>() {
				{
					put("block", 	   Blocks.WATER_CAULDRON);
					put("block_state", Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
					put("has_levels", true);
				}
			});

			put(Items.POWDER_SNOW_BUCKET, new HashMap<String, Object>() {
				{
					put("block", 	   Blocks.POWDER_SNOW_CAULDRON);
					put("block_state", Blocks.POWDER_SNOW_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
					put("has_levels", true);
				}
			});
		}
	};

	public static HashMap<String, HashMap<String, Object>> BottleToCauldron = new HashMap<>(){
		{
			String itemID = "minecraft:water";
			put(itemID, new HashMap<String, Object>() {
				{
					ItemStack water_bottle = new ItemStack(Items.POTION);
					NbtCompound nbt_tags = new NbtCompound();
					nbt_tags.putString("Potion", itemID);
					water_bottle.setNbt(nbt_tags);

					put("stack", water_bottle);
					put("block", Blocks.WATER_CAULDRON);
				}
			});
		}
	};

	public static HashMap<Block, Item> CauldronToBucket = new HashMap<>(){
		{
			for (Map.Entry<Item, HashMap<String, Object>> entry : BucketToCauldron.entrySet()) {
				put((Block)entry.getValue().get("block"), entry.getKey());
			}
		}
	};

	public static HashMap<Block, ItemStack> CauldronToBottle = new HashMap<>(){
		{
			for (Map.Entry<String, HashMap<String, Object>> entry : BottleToCauldron.entrySet()) {
				put((Block)entry.getValue().get("block"), (ItemStack)entry.getValue().get("stack"));
			}
		}
	};

	public static boolean ValidBottle(ItemStack bottle) {
		if (!bottle.hasNbt()) {
			return false;
		}

		if (!bottle.getNbt().contains("Potion")) {
			return false;
		}

		return BottleToCauldron.containsKey(bottle.getNbt().getString("Potion"));
	}

	public static enum CauldronLevel {
		EMPTY(0), LOW(1), MEDIUM(2), FULL(3);

		private final int value;
		private CauldronLevel(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
	
	public static CauldronLevel GetCauldronLevel(BlockState block_state) {
		Block block = block_state.getBlock();
		if (!CauldronToBucket.containsKey(block)) { return CauldronLevel.EMPTY; }
		if (block == Blocks.CAULDRON) { return CauldronLevel.EMPTY; } 

		HashMap<String, Object> cauldron = BucketToCauldron.get(CauldronToBucket.get(block));
		if ((Boolean) cauldron.get("has_levels") == false) { return CauldronLevel.FULL; }

		switch (block_state.get(Properties.LEVEL_3)) {
			case 3:
				return CauldronLevel.FULL;

			case 2:
				return CauldronLevel.MEDIUM;

			case 1:
				return CauldronLevel.LOW;
			
			default:
				return CauldronLevel.EMPTY;
		}
	}

	public static BlockState NewCauldronBottleLevel(Block cauldron_type, BlockState block_state, Boolean increase) {
		Integer cauldron_level = GetCauldronLevel(block_state).value;
		if (increase) { cauldron_level += 1; } else { cauldron_level -= 1; }
		if (cauldron_level >= 3) { cauldron_level = 3; }
		if (cauldron_level <= 0) { cauldron_level = 0; }

		if (cauldron_level == 0) { return Blocks.CAULDRON.getDefaultState(); }
		return cauldron_type.getDefaultState().with(Properties.LEVEL_3, cauldron_level);
	}
}
