package com.gxlg.vaultmanager.mixin;

import com.gxlg.vaultmanager.MultiVersion;
import com.gxlg.vaultmanager.Worker;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VaultBlock;
import net.minecraft.block.enums.VaultState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(ClientPlayerInteractionManager.class)
public class Interaction {
    @Inject(at = @At("RETURN"), method = "interactBlock")
    private void interact(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> info) {
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = player.clientWorld.getBlockState(pos);
        if (state.getBlock() != Blocks.VAULT) return;
        ItemStack stack = player.getStackInHand(hand);

        // update an already opened one: shift + hand click + close enough + vault closed
        boolean close = player.getBlockPos().getSquaredDistance(pos) <= MathHelper.square(4.0);
        if (state.get(VaultBlock.VAULT_STATE) == VaultState.INACTIVE && player.isSneaky() && stack.isEmpty() && close) {
            if (Worker.addOpenVault(pos))
                player.sendMessage(Text.literal("Vault state updated").formatted(Formatting.DARK_PURPLE), true);
            return;
        }

        boolean ominous = state.get(VaultBlock.OMINOUS);
        boolean matchKey = (ominous && stack.getItem() == Items.OMINOUS_TRIAL_KEY) || (!ominous && stack.getItem() == Items.TRIAL_KEY);

        // open the vault with the matching key
        if (MultiVersion.wasKeyUsed(info.getReturnValue()) && matchKey) {
            player.sendMessage(Text.literal("Vault opened").formatted(Formatting.DARK_PURPLE), true);
            Worker.addOpenVault(pos);
        }
    }
}
