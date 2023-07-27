package cc.stormworth.hcf.util;

import net.minecraft.server.v1_7_R4.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class BossBar {

	public static final double maxHealth = 200.0d;
	private static double health = maxHealth;
	private static int counter = -1;
	
	public static void setCounter(int counter) {
		BossBar.counter = counter;
		health = maxHealth;
	}
	
	public static int getCounter() {
		return counter;
	}
	
	public static void display(String text) {
		if(counter > -1) {
			health -= (maxHealth / counter);
			display(text, health);
		} else {
			display(text, maxHealth);
		}
	}
	
	public static void display(String text, double health) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			display(player, text, health);
		}
	}
	
	public static void display(Player player, String text) {
		if(counter > -1) {
			health -= (maxHealth / counter);
			display(player, text, health);
		} else {
			display(player, text, maxHealth);
		}
	}
	
	public static void display(Player player, String text, double health) {
		try {

			text = ChatColor.translateAlternateColorCodes('&', text);

			for(ChatColor color : ChatColor.values()) {
				text = text.replace(color.toString(), color + "" + ChatColor.BOLD);
			}

			text = text.replace("%NAME%", player.getName());
			DataWatcher watcher = new DataWatcher((Entity) null);
			watcher.a(2, text);
			watcher.a(6, (float) (health <= 0 ? 0.1 : health));
			watcher.a(10, text);
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();

			getField(packet.getClass(), "a").set(packet, 1234);
			getField(packet.getClass(), "b").set(packet, (byte) EntityType.ENDER_DRAGON.getTypeId());
			getField(packet.getClass(), "c").set(packet, (int) Math.floor(player.getLocation().getBlockX() * 32.0D));
			getField(packet.getClass(), "d").set(packet, (int) Math.floor(-500 * 32.0D));
			getField(packet.getClass(), "e").set(packet, (int) Math.floor(player.getLocation().getBlockZ() * 32.0D));
			getField(packet.getClass(), "f").set(packet, (byte) 0);
			getField(packet.getClass(), "g").set(packet, (byte) 0);
			getField(packet.getClass(), "h").set(packet, (byte) 0);
			getField(packet.getClass(), "i").set(packet, (byte) 0);
			getField(packet.getClass(), "j").set(packet, (byte) 0);
			getField(packet.getClass(), "k").set(packet, (byte) 0);
			getField(packet.getClass(), "l").set(packet, watcher);

			CraftPlayer craftPlayer = (CraftPlayer) player;
			craftPlayer.getHandle().playerConnection.sendPacket(packet);

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void remove() {
		for(Player player : Bukkit.getOnlinePlayers()) {
			remove(player);
		}
	}
	
	public static void remove(Player player) {
		if(player.getTicksLived() >= (20 * 5)) {
			try {
				DataWatcher watcher = new DataWatcher((Entity) null);
				watcher.a(0, (byte) 0x20);
				watcher.a(2, " ");
				watcher.a(6, (float) 200);
				watcher.a(10, " ");
				watcher.a(11, (byte) 1);
				PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata();
				getField(metaPacket.getClass(), "a").set(metaPacket, 1234);
				getField(PacketPlayOutEntityMetadata.class, "b").set(metaPacket, watcher.c());
				PacketPlayOutEntityDestroy despawnPacket = new PacketPlayOutEntityDestroy();
				getField(despawnPacket.getClass(), "a").set(despawnPacket, new int [] {1234});
				CraftPlayer craftPlayer = (CraftPlayer) player;
				craftPlayer.getHandle().playerConnection.sendPacket(metaPacket);
				craftPlayer.getHandle().playerConnection.sendPacket(despawnPacket);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static Field getField(Class<?> clazz, String fieldName) {
		Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
        } catch(Exception e) {
			e.printStackTrace();
		}
        return field;
    }
}
