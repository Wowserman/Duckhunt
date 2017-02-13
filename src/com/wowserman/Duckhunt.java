package com.wowserman;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.wowserman.arena.Arena;
import com.wowserman.arena.ArenaEditor;
import com.wowserman.arena.ArenaStatus;
import com.wowserman.arena.ArenaStorage;
import com.wowserman.arena.Team;
import com.wowserman.bungee.BungeeArena;
import com.wowserman.bungee.BungeeCommunication;
import com.wowserman.classes.Barrager;
import com.wowserman.classes.Cloaker;
import com.wowserman.classes.Cyborg;
import com.wowserman.classes.Freezer;
import com.wowserman.classes.Healer;
import com.wowserman.classes.Pyrotech;
import com.wowserman.classes.Runner;
import com.wowserman.classes.Smoker;
import com.wowserman.commands.DuckhuntExecutor;
import com.wowserman.listeners.BlockBreak;
import com.wowserman.listeners.BlockPlace;
import com.wowserman.listeners.CommandPreprocess;
import com.wowserman.listeners.Damage;
import com.wowserman.listeners.FoodChange;
import com.wowserman.listeners.GamePlayerLeftArena;
import com.wowserman.listeners.Heal;
import com.wowserman.listeners.InventoryClick;
import com.wowserman.listeners.InventoryClose;
import com.wowserman.listeners.ItemDrop;
import com.wowserman.listeners.ItemPickup;
import com.wowserman.listeners.PlayerConnect;
import com.wowserman.listeners.PlayerDisconnect;
import com.wowserman.listeners.PlayerInteract;
import com.wowserman.listeners.PlayerMove;
import com.wowserman.listeners.PlayerSneak;
import com.wowserman.signs.ArenaSign;
import com.wowserman.signs.BungeeArenaSign;
import com.wowserman.signs.SignStorage;

public class Duckhunt extends JavaPlugin {

	public static Plugin plugin;
	public static List<Arena> arenas = new ArrayList<Arena>();
	public static List<BungeeArena> bungeeArenas = new ArrayList<BungeeArena>();
	public static List<BungeeArenaSign> bungeeSigns = new ArrayList<BungeeArenaSign>();
	public static List<ArenaSign> arenaSigns = new ArrayList<ArenaSign>();
	public static Config config;
	public static ArenaStorage arenaStorage;
	public static BookStorage bookStorage;
	public static SignStorage signStorage;
	public static BungeeCommunication bungeeComunication;
	
	public static boolean isPlayerInGame(Player player) {
		for (Arena arena : arenas) {
			if (arena.getPlayers().contains(player)) return true;
			else continue;
		}
		return false;
	}
	
	public static List<Arena> getArenas() {
		return arenas;
	}
	
	public static Arena getArena(Player player) {
		for (Arena a : arenas) {
			if (a.getPlayers().contains(player)) return a; 
		}
		return null;
	}
	
	public static Team getTeam(Player player) {
		Arena arena = getArena(player);
		if (arena != null) return arena.getTeam(player);
		else return Team.None;
	}
	
	public static boolean isInGame(Player player) {
		if (getArena(player)==null) return false;
		else return true;
	}
	
	public static Arena getArenaOfName(String name) {
		for (Arena arena:arenas) 
			if (arena.getName().equalsIgnoreCase(name)) return arena;
		return null;
	}
	
	public static Arena getMainArena() {
		return Duckhunt.getArenaOfName(Config.mainArenaName);
	}
	
	public static List<BungeeArena> getBungeeArenas() {
		return bungeeArenas;
	}
	
	public static BungeeArena getBungeeArena(String arenaName) {
		for (BungeeArena arena:bungeeArenas) 
			if (arena.getArenaName().equalsIgnoreCase(arenaName)) return arena;
		return null;
	}
	
	public static BungeeArena getBungeeArenaOfServerName(String serverName) {
		for (BungeeArena arena:bungeeArenas) 
			if (arena.getServerName().equalsIgnoreCase(serverName)) return arena;
		return new BungeeArena(serverName);
	}
	
	public static boolean isBungeeSign(Location location) {
		for (BungeeArenaSign sign:bungeeSigns) 
			if (Tools.isSameLocation(sign.getLocation(), location)) return true;
		return false;
	}
	
	public static boolean isArenaSign(Location location) {
		for (ArenaSign sign:arenaSigns) {
			if (Tools.isSameLocation(sign.getLocation(), location)) return true;	
		}
		return false;
	}
	
	public static BungeeArenaSign getBungeeArenaSign(Location location) {
		for (BungeeArenaSign sign:bungeeSigns) 
			if (Tools.isSameLocation(sign.getLocation(), location)) return sign;
		return null;
	}
	
	public static ArenaSign getArenaSign(Location location) {
		for (ArenaSign sign:arenaSigns) 
			if (Tools.isSameLocation(sign.getLocation(), location)) return sign;
		return null;
	}
	
	public static boolean isLeaveSign(ArenaSign sign) {
		return sign.isLeaveSign();
	}
	
	@Override
	public void onEnable() {
		Duckhunt.plugin = this;
		Duckhunt.config = new Config();
		Duckhunt.arenaStorage = new ArenaStorage();
		Duckhunt.bookStorage = new BookStorage();
		Duckhunt.signStorage = new SignStorage();
		Duckhunt.bungeeComunication = new BungeeCommunication();
		
		for (String key:arenaStorage.configfile.getKeys(false)) {
			if (Duckhunt.arenaStorage.getArena(key) != null) 
				arenas.add(Duckhunt.arenaStorage.getArena(key));
		}
		
		for (Arena arena:arenas) {
			System.out.print("Booting up " + arena.getName() + "...");
			if (arena.getStatus() != ArenaStatus.Disabled) {
				arena.startRecruiting();
				System.out.print("Sucessfully Booted Up " + arena.getName() + ", Arena is now Recruiting.");
			}
		}
		
		// Load Signs after all of the plugins have loaded up to make sure any MultiVerse worlds load up so we don't get any errors. 
		new BukkitRunnable() {
			@Override
			public void run() {
				
				for (String key:signStorage.configfile.getKeys(false)) {
					signStorage.loadSign(signStorage.configfile.getConfigurationSection(key));
				}				
			}
		}.runTaskLater(this, 10);
		
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new PlayerInteract(), this);
		pm.registerEvents(new InventoryClick(), this);
		pm.registerEvents(new InventoryClose(), this);
		pm.registerEvents(new FoodChange(), this);
		pm.registerEvents(new ItemDrop(), this);
		pm.registerEvents(new ItemPickup(), this);
		pm.registerEvents(new PlayerMove(), this);
		pm.registerEvents(new CommandPreprocess(), this);
		pm.registerEvents(new Damage(), this);
		pm.registerEvents(new ArenaEditor(), this);
		pm.registerEvents(new Runner(), this);
		pm.registerEvents(new Cyborg(), this);
		pm.registerEvents(new Pyrotech(), this);
		pm.registerEvents(new Freezer(), this);
		pm.registerEvents(new Smoker(), this);
		pm.registerEvents(new Healer(), this);
		pm.registerEvents(new Cloaker(), this);
		pm.registerEvents(new Barrager(), this);
		pm.registerEvents(new Heal(), this);
		pm.registerEvents(new PlayerConnect(), this);
		pm.registerEvents(new PlayerDisconnect(), this);
		pm.registerEvents(new GamePlayerLeftArena(), this);
		pm.registerEvents(new BlockBreak(), this);
		pm.registerEvents(new BlockPlace(), this);
		pm.registerEvents(new PlayerSneak(), this);
		
		this.getCommand("duckhunt").setExecutor(new DuckhuntExecutor());
		
		if (!Config.infoMessage) return;
		
		System.out.print("                                                     ");
		System.out.print("•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•");
		System.out.print("                                                     ");
		System.out.print("DuckHunt is a Payed Resource marketed on SpigotMC.org");
		System.out.print("Do not Redistribute or Resell this Plugin in any form");
		System.out.print("Do not Hesitate to ask Questions or related concerns!");
		System.out.print("The Author is always Happy to Help You!");
		System.out.print("                                                     ");
		System.out.print("Created By Peter Larson - 2016");
		System.out.print("You can Contact me through SpigotMC.org");
		System.out.print("or Email me @ wowserman.mc@gmail.com");
		System.out.print("         (disable this message in the config)        ");
		System.out.print("•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•-•");
		System.out.print("                                                     ");
	}
	
	public void onDisable() {
		System.out.print("Stopping Duckhunt...");
		for (Arena arena:arenas) arena.allPlayersLeave();
		Freezer.removeAllIcePillars();
	}
	
}
