package net.violetunderscore.stephop.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.violetunderscore.stephop.config.ConfigWrapper;
import net.violetunderscore.stephop.config.StephopConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Redirect(
            method = "onPlayerMove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;isInTeleportationState()Z"
            )
    )
    private boolean alwaysInTeleportationState(ServerPlayerEntity player) {
        if (ConfigWrapper.config.disable_weird_movement_checks) {
            return true;
        } else {
            return player.isInTeleportationState();
        }
    }
}
