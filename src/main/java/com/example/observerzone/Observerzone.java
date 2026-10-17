package com.example.observerzone;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Observerzone implements ModInitializer {
	public static final String MOD_ID = "observerzone";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(ZoneManager::load);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> ZoneManager.unload());

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("observerzone")
						// 需要 OP 权限（等级 2）。若这一行编译报错，见回复里的说明
						.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
						.then(Commands.literal("add")
								.then(Commands.argument("name", StringArgumentType.word())
										.then(Commands.argument("from", BlockPosArgument.blockPos())
												.then(Commands.argument("to", BlockPosArgument.blockPos())
														.executes(Observerzone::add)))))
						.then(Commands.literal("remove")
								.then(Commands.argument("name", StringArgumentType.word())
										.suggests((ctx, builder) -> {
											ZoneManager.all().forEach(z -> builder.suggest(z.name()));
											return builder.buildFuture();
										})
										.executes(Observerzone::remove)))
						.then(Commands.literal("list").executes(Observerzone::list))));
	}

	private static int add(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack src = ctx.getSource();
		String name = StringArgumentType.getString(ctx, "name");
		BlockPos a = BlockPosArgument.getBlockPos(ctx, "from");
		BlockPos b = BlockPosArgument.getBlockPos(ctx, "to");
		String dim = ZoneManager.dimensionId(src.getLevel());

		ZoneManager.Zone zone = new ZoneManager.Zone(name, dim,
				Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()),
				Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));

		if (!ZoneManager.add(zone)) {
			src.sendFailure(Component.literal("已存在同名区域: " + name));
			return 0;
		}
		src.sendSuccess(() -> Component.literal("已创建禁用区域 " + describe(zone)), true);
		return 1;
	}

	private static int remove(CommandContext<CommandSourceStack> ctx) {
		String name = StringArgumentType.getString(ctx, "name");
		if (ZoneManager.remove(name)) {
			ctx.getSource().sendSuccess(() -> Component.literal("已删除区域: " + name), true);
			return 1;
		}
		ctx.getSource().sendFailure(Component.literal("找不到区域: " + name));
		return 0;
	}

	private static int list(CommandContext<CommandSourceStack> ctx) {
		var zones = ZoneManager.all();
		if (zones.isEmpty()) {
			ctx.getSource().sendSuccess(() -> Component.literal("当前没有禁用区域"), false);
			return 0;
		}
		for (ZoneManager.Zone z : zones) {
			ctx.getSource().sendSuccess(() -> Component.literal(describe(z)), false);
		}
		return zones.size();
	}

	private static String describe(ZoneManager.Zone z) {
		return String.format("%s [%s] (%d, %d, %d) ~ (%d, %d, %d)", z.name(), z.dimension(),
				z.minX(), z.minY(), z.minZ(), z.maxX(), z.maxY(), z.maxZ());
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
