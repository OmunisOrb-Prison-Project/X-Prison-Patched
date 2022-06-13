package dev.drawethree.ultraprisoncore.autosell.command;

import dev.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import dev.drawethree.ultraprisoncore.autosell.utils.AutoSellContants;
import me.lucko.helper.Commands;

public class AutoSellCommand {

    private static final String COMMAND_NAME = "autosell";
    private final UltraPrisonAutoSell plugin;

    public AutoSellCommand(UltraPrisonAutoSell plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Commands.create()
                .assertPlayer()
                .assertPermission(AutoSellContants.AUTOSELL_PERMISSION, this.plugin.getAutoSellConfig().getMessage("no_permission_autosell_toggle"))
                .handler(c -> {
                    if (c.args().size() == 0) {
                        this.plugin.getManager().toggleAutoSell(c.sender());
                    }
                }).registerAndBind(this.plugin.getCore(), COMMAND_NAME);
    }
}
