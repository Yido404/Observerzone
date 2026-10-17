package com.example.observerzone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 管理"禁用侦测器"的区域，数据保存在存档目录下的 observer_zones.json */
public class ZoneManager {

	public record Zone(String name, String dimension,
					   int minX, int minY, int minZ,
					   int maxX, int maxY, int maxZ) {
		public boolean contains(String dim, BlockPos p) {
			return dimension.equals(dim)
					&& p.getX() >= minX && p.getX() <= maxX
					&& p.getY() >= minY && p.getY() <= maxY
					&& p.getZ() >= minZ && p.getZ() <= maxZ;
		}
	}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final List<Zone> ZONES = new CopyOnWriteArrayList<>();
	private static Path file;

	/** 维度 ID 字符串，例如 minecraft:overworld。如果 1.21.11 里这个方法名编译不过，只需改这一处 */
	public static String dimensionId(ServerLevel level) {
		return level.dimension().identifier().toString();
	}

	public static void load(MinecraftServer server) {
		file = server.getWorldPath(LevelResource.ROOT).resolve("observer_zones.json");
		ZONES.clear();
		if (Files.exists(file)) {
			try (Reader r = Files.newBufferedReader(file)) {
				Zone[] arr = GSON.fromJson(r, Zone[].class);
				if (arr != null) ZONES.addAll(Arrays.asList(arr));
			} catch (Exception e) {
				Observerzone.LOGGER.error("读取 observer_zones.json 失败", e);
			}
		}
	}

	public static void unload() {
		ZONES.clear();
		file = null;
	}

	private static void save() {
		if (file == null) return;
		try (Writer w = Files.newBufferedWriter(file)) {
			GSON.toJson(ZONES.toArray(new Zone[0]), w);
		} catch (Exception e) {
			Observerzone.LOGGER.error("保存 observer_zones.json 失败", e);
		}
	}

	public static boolean add(Zone zone) {
		if (get(zone.name()) != null) return false;
		ZONES.add(zone);
		save();
		return true;
	}

	public static boolean remove(String name) {
		boolean removed = ZONES.removeIf(z -> z.name().equals(name));
		if (removed) save();
		return removed;
	}

	public static Zone get(String name) {
		for (Zone z : ZONES) if (z.name().equals(name)) return z;
		return null;
	}

	public static List<Zone> all() {
		return ZONES;
	}

	/** 该位置的侦测器是否处于禁用区域内 */
	public static boolean isDisabled(ServerLevel level, BlockPos pos) {
		if (ZONES.isEmpty()) return false;
		String dim = dimensionId(level);
		for (Zone z : ZONES) if (z.contains(dim, pos)) return true;
		return false;
	}
}
