package dev.passarelli.xprison.enchants.model.impl;

import com.cryptomorin.xseries.XPotion;
import dev.passarelli.xprison.enchants.XPrisonEnchants;
import dev.passarelli.xprison.enchants.model.XPrisonEnchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public final class JumpBoostEnchant extends XPrisonEnchantment {
	public JumpBoostEnchant(XPrisonEnchants instance) {
		super(instance, 6);
	}

	@Override
	public void onEquip(Player p, ItemStack pickAxe, int level) {
		if (level == 0) {
			this.onUnequip(p, pickAxe, level);
			return;
		}
		p.addPotionEffect(new PotionEffect(XPotion.JUMP_BOOST.get(), Integer.MAX_VALUE, level - 1, true, true), true);
	}

	@Override
	public void onUnequip(Player p, ItemStack pickAxe, int level) {
		p.removePotionEffect(XPotion.JUMP_BOOST.get());
	}

	@Override
	public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

	}

	@Override
	public double getChanceToTrigger(int enchantLevel) {
		return 100.0;
	}

	@Override
	public void reload() {
		super.reload();
	}

	@Override
	public String getAuthor() {
		return "Drawethree";
	}
}
