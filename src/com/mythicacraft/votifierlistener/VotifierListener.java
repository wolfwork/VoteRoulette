package com.mythicacraft.votifierlistener;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.mythicacraft.votifierlistener.listeners.VoteListener;
import com.mythicacraft.votifierlistener.utils.ConfigAccessor;

public class VotifierListener extends JavaPlugin {

	FileConfiguration newConfig;
	private static final Logger log = Logger.getLogger("VotifierListener");
	public Economy economy = null;
	private boolean vaultEnabled = false;

	public void onDisable() {
		log.info("[VotifierListener] Disabled!");
	}

	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		if(!setupVotifier()) {
			pm.disablePlugin(this);
			return;
		}
		if(setupVault()) {
			vaultEnabled = true;
		}
		loadConfig();
		loadPlayerData();
		pm.registerEvents(new VoteListener(this), this);
		log.info("[VotifierListener] Enabled!");
	}

	private boolean setupVotifier() {
		Plugin votifier =  getServer().getPluginManager().getPlugin("Votifier");
		if (!(votifier != null && votifier instanceof com.vexsoftware.votifier.Votifier)) {
			log.severe("[VotifierListener] Votifier was not found!");
			return false;
		}
		return true;
	}

	private boolean setupVault() {
		Plugin vault =  getServer().getPluginManager().getPlugin("Vault");
		if (vault != null && vault instanceof net.milkbowl.vault.Vault) {
			if(!setupEconomy()) {
				log.warning("[VotifierListener] No plugin to handle cash, cash rewards will NOT be given!");
				return false;
			}
		} else {
			log.warning("[VotifierListener] Vault plugin not found, cash rewards will NOT be given!");
			return false;
		}
		return true;
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	public boolean isVaultEnabled() {
		return vaultEnabled;
	}

	public void loadConfig() {
		PluginManager pm = getServer().getPluginManager();
		try {
			reloadConfig();
			newConfig = this.getConfig();
			newConfig.options().copyDefaults(true);
			saveConfig();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception while loading VotifierListener/config.yml", e);
			pm.disablePlugin(this);
		}
	}

	public void loadPlayerData() {
		PluginManager pm = getServer().getPluginManager();
		String pluginFolder = this.getDataFolder().getAbsolutePath();
		(new File(pluginFolder)).mkdirs();
		String playerFolder = pluginFolder + File.separator + "data";
		(new File(playerFolder)).mkdirs();
		File playerDataFile = new File(playerFolder, "players.yml");
		ConfigAccessor playerData = new ConfigAccessor("players.yml");

		if (!playerDataFile.exists()) {
			try {
				playerDataFile.createNewFile();
				playerData.getConfig().options().header("You do NOT need to touch this file!");
				playerData.getConfig().options().copyHeader();
				playerData.saveConfig();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception while loading VotifierListener/data/players.yml", e);
				pm.disablePlugin(this);
			}
			return;
		} else {
			try {
				playerData.getConfig().options().header("You do NOT need to touch this file!");
				playerData.getConfig().options().copyHeader();
				playerData.reloadConfig();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Exception while loading VotifierListener/config.yml", e);
				pm.disablePlugin(this);
			}
		}

	}
}
