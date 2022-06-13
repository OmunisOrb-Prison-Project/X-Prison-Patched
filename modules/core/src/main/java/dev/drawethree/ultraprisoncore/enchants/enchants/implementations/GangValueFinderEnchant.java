package dev.drawethree.ultraprisoncore.enchants.enchants.implementations;

import dev.drawethree.ultraprisoncore.enchants.UltraPrisonEnchants;
import dev.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import dev.drawethree.ultraprisoncore.gangs.UltraPrisonGangs;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public final class GangValueFinderEnchant extends UltraPrisonEnchantment {

	private int maxAmount;
	private int minAmount;
	private double chance;


	public GangValueFinderEnchant(UltraPrisonEnchants instance) {
		super(instance, 23);
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {

	}

	@Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {
		if (chance * enchantLevel >= ThreadLocalRandom.current().nextDouble(100)) {
			if (!this.plugin.getCore().isModuleEnabled(UltraPrisonGangs.MODULE_NAME)) {
				return;
			}
			int randAmount = minAmount == maxAmount ? minAmount : ThreadLocalRandom.current().nextInt(minAmount, maxAmount);
			plugin.getCore().getGangs().getGangsManager().getPlayerGang(e.getPlayer()).ifPresent(gang -> gang.setValue(gang.getValue() + randAmount));
		}
	}

	@Override
	public void reload() {
		this.minAmount = plugin.getConfig().get().getInt("enchants." + id + ".Min-Value");
		this.maxAmount = plugin.getConfig().get().getInt("enchants." + id + ".Max-Value");
		this.chance = plugin.getConfig().get().getDouble("enchants." + id + ".Chance");
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}