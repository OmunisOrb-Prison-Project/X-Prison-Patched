package me.drawethree.ultraprisoncore.enchants;

import lombok.Getter;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.UltraPrisonModule;
import me.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import me.drawethree.ultraprisoncore.config.FileManager;
import me.drawethree.ultraprisoncore.enchants.api.UltraPrisonEnchantsAPI;
import me.drawethree.ultraprisoncore.enchants.api.UltraPrisonEnchantsAPIImpl;
import me.drawethree.ultraprisoncore.enchants.enchants.UltraPrisonEnchantment;
import me.drawethree.ultraprisoncore.enchants.enchants.implementations.LuckyBoosterEnchant;
import me.drawethree.ultraprisoncore.enchants.gui.DisenchantGUI;
import me.drawethree.ultraprisoncore.enchants.gui.EnchantGUI;
import me.drawethree.ultraprisoncore.enchants.managers.EnchantsManager;
import me.drawethree.ultraprisoncore.mines.UltraPrisonMines;
import me.drawethree.ultraprisoncore.multipliers.UltraPrisonMultipliers;
import me.drawethree.ultraprisoncore.utils.PlayerUtils;
import me.drawethree.ultraprisoncore.utils.compat.MinecraftVersion;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.cooldown.CooldownMap;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;
import org.apache.commons.lang.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class UltraPrisonEnchants implements UltraPrisonModule {


	public static final String MODULE_NAME = "Enchants";
	@Getter
	private static UltraPrisonEnchants instance;

	@Getter
	private UltraPrisonEnchantsAPI api;

	@Getter
	private EnchantsManager enchantsManager;

	@Getter
	private FileManager.Config config;

	@Getter
	private UltraPrisonCore core;

	private HashMap<String, String> messages;
	private List<UUID> disabledLayer = new ArrayList<>();
	private List<UUID> disabledExplosive = new ArrayList<>();
	private CooldownMap<Player> valueCooldown = CooldownMap.create(Cooldown.of(30, TimeUnit.SECONDS));
	private boolean enabled;
	@Getter
	private boolean autoSellModule;
	@Getter
	private boolean minesModule;
	@Getter
	private boolean multipliersModule;

	public UltraPrisonEnchants(UltraPrisonCore UltraPrisonCore) {
		instance = this;
		this.core = UltraPrisonCore;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void reload() {

		this.config.reload();

		this.loadMessages();

		this.autoSellModule = this.core.isModuleEnabled(UltraPrisonAutoSell.MODULE_NAME);
		this.multipliersModule = this.core.isModuleEnabled(UltraPrisonMultipliers.MODULE_NAME);
		this.minesModule = this.core.isModuleEnabled(UltraPrisonMines.MODULE_NAME);

		this.enchantsManager.reload();

		EnchantGUI.reload();
		DisenchantGUI.reload();

		UltraPrisonEnchantment.reloadAll();
	}

	private void loadMessages() {
		messages = new HashMap<>();
		for (String key : getConfig().get().getConfigurationSection("messages").getKeys(false)) {
			messages.put(key, Text.colorize(getConfig().get().getString("messages." + key)));
		}
	}

	@Override
	public void enable() {
		this.enabled = true;
		this.config = this.core.getFileManager().getConfig("enchants.yml").copyDefaults(true).save();
		this.enchantsManager = new EnchantsManager(this);
		this.autoSellModule = this.core.isModuleEnabled(UltraPrisonAutoSell.MODULE_NAME);
		this.multipliersModule = this.core.isModuleEnabled(UltraPrisonMultipliers.MODULE_NAME);
		this.minesModule = this.core.isModuleEnabled(UltraPrisonMines.MODULE_NAME);
		this.api = new UltraPrisonEnchantsAPIImpl(enchantsManager);
		this.loadMessages();
		this.registerCommands();
		this.registerEvents();

		UltraPrisonEnchantment.loadDefaultEnchantments();
	}

	@Override
	public void disable() {
		this.enabled = false;

		for (Player p : Players.all()) {
			p.closeInventory();
		}

	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	private void registerCommands() {


		Commands.create()
				.assertOp()
				.handler(c -> {

					if (c.args().size() == 0) {
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&c/givepickaxe <player> <[enchant1]=[level1],[enchant2]=[level2],...[enchantX]=[levelX]> <pickaxe_name>"));
						return;
					}

					String input = null, name = null;
					Player target = null;

					if (c.args().size() == 1) {
						input = c.rawArg(0);
					} else if (c.args().size() == 2) {
						target = c.arg(0).parseOrFail(Player.class);
						input = c.rawArg(1);
					} else if (c.args().size() == 3) {
						target = c.arg(0).parseOrFail(Player.class);
						input = c.rawArg(1);
						name = StringUtils.join(c.args().subList(2, c.args().size()), " ");
					}

					this.enchantsManager.givePickaxe(target, input, name, c.sender());
				}).registerAndBind(core, "givepickaxe");

		Commands.create()
				.assertOp()
				.handler(c -> {

					if (c.args().size() == 0) {
						PlayerUtils.sendMessage(c.sender(), Text.colorize("&c/givefirstjoinpickaxe <player>"));
						return;
					}

					Player target = c.arg(0).parseOrFail(Player.class);

					target.getInventory().addItem(this.enchantsManager.createFirstJoinPickaxe(target));
					PlayerUtils.sendMessage(c.sender(), Text.colorize("&aYou have given first join pickaxe to &e" + target.getName()));
				}).registerAndBind(core, "givefirstjoinpickaxe");

		Commands.create()
				.assertPlayer()
				.handler(c -> {
					if (LuckyBoosterEnchant.hasLuckyBoosterRunning(c.sender())) {
						PlayerUtils.sendMessage(c.sender(), getMessage("lucky_mode_timeleft").replace("%timeleft%", LuckyBoosterEnchant.getTimeLeft(c.sender())));
					} else {
						PlayerUtils.sendMessage(c.sender(), getMessage("lucky_mode_disabled"));
					}
				}).registerAndBind(core, "luckybooster");

		Commands.create()
				.assertPlayer()
				.handler(c -> {
					ItemStack pickAxe = c.sender().getItemInHand();

					if (pickAxe == null || !this.getCore().isPickaxeSupported(pickAxe.getType())) {
						PlayerUtils.sendMessage(c.sender(), getMessage("no_pickaxe_found"));
						return;
					}

					int pickaxeSlot = this.enchantsManager.getInventorySlot(c.sender(), pickAxe);
					this.core.debug("Pickaxe slot is: " + pickaxeSlot);
					new DisenchantGUI(c.sender(), pickAxe, pickaxeSlot).open();
				}).registerAndBind(core, "disenchant", "dise", "de", "disenchantmenu", "dismenu");

		Commands.create()
				.assertPlayer()
				.handler(c -> {
					ItemStack pickAxe = c.sender().getItemInHand();

					if (pickAxe == null || !this.getCore().isPickaxeSupported(pickAxe.getType())) {
						PlayerUtils.sendMessage(c.sender(), getMessage("no_pickaxe_found"));
						return;
					}

					int pickaxeSlot = this.enchantsManager.getInventorySlot(c.sender(), pickAxe);
					this.core.debug("Pickaxe slot is: " + pickaxeSlot);
					new EnchantGUI(c.sender(), pickAxe, pickaxeSlot).open();
				}).registerAndBind(core, "enchantmenu", "enchmenu");

		Commands.create()
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0) {
						toggleExplosive(c.sender());
					}
				}).registerAndBind(core, "explosive");
		Commands.create()
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0) {
						toggleLayer(c.sender());
					}
				}).registerAndBind(core, "layer");
		Commands.create()
				.assertPlayer()
				.assertPermission("ultraprison.value", this.getMessage("value_no_permission"))
				.handler(c -> {
					if (!valueCooldown.test(c.sender())) {
						PlayerUtils.sendMessage(c.sender(), this.getMessage("value_cooldown").replace("%time%", String.valueOf(valueCooldown.remainingTime(c.sender(), TimeUnit.SECONDS))));
						return;
					}
					ItemStack pickAxe = c.sender().getItemInHand();

					if (pickAxe == null || !this.getCore().isPickaxeSupported(pickAxe.getType())) {
						PlayerUtils.sendMessage(c.sender(), getMessage("value_no_pickaxe"));
						return;
					}

					PlayerUtils.sendMessage(c.sender(), this.getMessage("value_value").replace("%player%", c.sender().getName()).replace("%tokens%", String.format("%,d", this.enchantsManager.getPickaxeValue(pickAxe))));
				}).registerAndBind(core, "value");
	}

	private void toggleLayer(Player sender) {
		if (disabledLayer.contains(sender.getUniqueId())) {
			PlayerUtils.sendMessage(sender, getMessage("layer_enabled"));
			disabledLayer.remove(sender.getUniqueId());
		} else {
			PlayerUtils.sendMessage(sender, getMessage("layer_disabled"));
			disabledLayer.add(sender.getUniqueId());
		}
	}

	private void toggleExplosive(Player sender) {
		if (disabledExplosive.contains(sender.getUniqueId())) {
			PlayerUtils.sendMessage(sender, getMessage("explosive_enabled"));
			disabledExplosive.remove(sender.getUniqueId());
		} else {
			PlayerUtils.sendMessage(sender, getMessage("explosive_disabled"));
			disabledExplosive.add(sender.getUniqueId());
		}
	}

	private void registerEvents() {
		Events.subscribe(PlayerInteractEvent.class)
				.filter(e -> e.getItem() != null && this.getCore().isPickaxeSupported(e.getItem().getType()))
				.filter(e -> (e.getAction() == Action.RIGHT_CLICK_AIR || (e.getAction() == Action.RIGHT_CLICK_BLOCK && this.enchantsManager.isOpenEnchantMenuOnRightClickBlock())))
				.handler(e -> {
					e.setCancelled(true);
					ItemStack pickAxe = e.getItem();
					int pickaxeSlot = this.enchantsManager.getInventorySlot(e.getPlayer(), pickAxe);
					this.core.debug("Pickaxe slot is: " + pickaxeSlot);
					new EnchantGUI(e.getPlayer(), pickAxe, pickaxeSlot).open();
				}).bindWith(core);
		Events.subscribe(BlockBreakEvent.class, EventPriority.HIGHEST)
				.filter(EventFilters.ignoreCancelled())
				.filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && !e.isCancelled() && e.getPlayer().getItemInHand() != null && this.getCore().isPickaxeSupported(e.getPlayer().getItemInHand().getType()))
				.filter(e -> this.enchantsManager.isAllowEnchantsOutside() || WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().anyMatch(region -> region.getId().toLowerCase().startsWith("mine")))
				.handler(e -> {
					enchantsManager.addBlocksBrokenToItem(e.getPlayer(), 1);
					enchantsManager.handleBlockBreak(e, e.getPlayer().getItemInHand());
				}).bindWith(core);
		Events.subscribe(BlockBreakEvent.class, EventPriority.LOWEST)
				.filter(e -> e.getPlayer().getGameMode() == GameMode.SURVIVAL && !e.isCancelled() && e.getPlayer().getItemInHand() != null && this.getCore().isPickaxeSupported(e.getPlayer().getItemInHand().getType()))
				.filter(e -> WorldGuardWrapper.getInstance().getRegions(e.getBlock().getLocation()).stream().noneMatch(region -> region.getId().toLowerCase().startsWith("mine")))
				.filter(e -> this.enchantsManager.hasEnchants(e.getPlayer().getItemInHand()))
				.handler(e -> e.setCancelled(true)).bindWith(core);
		// Switching pickaxes
		Events.subscribe(PlayerItemHeldEvent.class, EventPriority.HIGHEST)
				.handler(e -> {

					ItemStack newItem = e.getPlayer().getInventory().getItem(e.getNewSlot());
					ItemStack previousItem = e.getPlayer().getInventory().getItem(e.getPreviousSlot());

					if (previousItem != null && this.getCore().isPickaxeSupported(previousItem.getType())) {
						this.enchantsManager.handlePickaxeUnequip(e.getPlayer(), previousItem);
					}

					if (newItem != null && this.getCore().isPickaxeSupported(newItem.getType())) {
						this.enchantsManager.handlePickaxeEquip(e.getPlayer(), newItem);
					}

				}).bindWith(core);
		// Dropping pickaxe
		Events.subscribe(PlayerDropItemEvent.class, EventPriority.HIGHEST)
				.handler(e -> {
					if (this.getCore().isPickaxeSupported(e.getItemDrop().getItemStack())) {
						this.enchantsManager.handlePickaxeUnequip(e.getPlayer(), e.getItemDrop().getItemStack());
					}
				}).bindWith(core);
		//First join pickaxe
		Events.subscribe(PlayerJoinEvent.class)
				.filter(e -> !e.getPlayer().hasPlayedBefore() && this.enchantsManager.isFirstJoinPickaxeEnabled())
				.handler(e -> {
					ItemStack firstJoinPickaxe = this.enchantsManager.createFirstJoinPickaxe(e.getPlayer());
					e.getPlayer().getInventory().addItem(firstJoinPickaxe);
				}).bindWith(core);
		//Grindstone disenchanting - disable
		if (MinecraftVersion.atLeast(MinecraftVersion.V.v1_14)) {
			Events.subscribe(InventoryClickEvent.class)
					.filter(e -> e.getInventory() instanceof GrindstoneInventory)
					.handler(e -> {
						ItemStack item1 = e.getInventory().getItem(0);
						ItemStack item2 = e.getInventory().getItem(1);
						if (e.getSlot() == 2 && (this.enchantsManager.isEnchanted(item1) || this.enchantsManager.isEnchanted(item2))) {
							e.setCancelled(true);
						}
					}).bindWith(core);
		}
	}

	public String getMessage(String key) {
		return messages.getOrDefault(key.toLowerCase(), Text.colorize("&cMessage " + key + " not found."));
	}

	public boolean hasLayerDisabled(Player p) {
		return disabledLayer.contains(p.getUniqueId());
	}

	public boolean hasExplosiveDisabled(Player p) {
		return disabledExplosive.contains(p.getUniqueId());
	}

}
