package com.example.observerzone.mixin;

import com.example.observerzone.ZoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ObserverBlock.class)
public class ObserverBlockMixin {

	/**
	 * 侦测器的计划刻分两步：未充能时的那一刻负责"点亮"，已充能时的那一刻负责"熄灭"。
	 * 只拦截"点亮"这一步：区域内的侦测器不会发出信号，
	 * 而已经处于充能状态的侦测器仍能正常熄灭，不会卡在常亮状态。
	 */
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void observerzone$blockActivation(BlockState state, ServerLevel level, BlockPos pos,
											  RandomSource random, CallbackInfo ci) {
		if (!state.getValue(BlockStateProperties.POWERED) && ZoneManager.isDisabled(level, pos)) {
			ci.cancel();
		}
	}
}
