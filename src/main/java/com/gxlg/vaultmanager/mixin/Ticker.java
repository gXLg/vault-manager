package com.gxlg.vaultmanager.mixin;

import com.gxlg.vaultmanager.Worker;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(ClientPlayerEntity.class)
public class Ticker {
    @Inject(at = @At("HEAD"), method = "tick")
    private void tick(CallbackInfo info) {
        Worker.tick();
    }
}