package com.gxlg.vaultmanager;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Worker {
    private static String currentWorld = null;
    private static String lastChecked = null;

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        HitResult hit = client.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;
        BlockHitResult blockHit = (BlockHitResult) hit;
        ClientWorld world = client.world;
        if (world == null || currentWorld == null) return;
        BlockPos pos = blockHit.getBlockPos();
        Block block = world.getBlockState(pos).getBlock();
        String posString = getPosString(pos);

        if (posString.equals(lastChecked)) return;
        lastChecked = posString;
        if (block != Blocks.VAULT) return;

        if (getVaults(true).contains(posString)) {
            player.sendMessage(Text.literal("Already opened").formatted(Formatting.DARK_PURPLE), true);
        } else {
            player.sendMessage(Text.literal("Not yet opened").formatted(Formatting.GREEN), true);
        }
    }

    private static String getPosString(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
    private static BlockPos posFromString(String pos) {
        String[] c = pos.split(",", 3);
        return new BlockPos(Integer.parseInt(c[0]), Integer.parseInt(c[1]), Integer.parseInt(c[2]));
    }

    public static void setCurrentWorld(@Nullable String name) {
        currentWorld = name;
    }

    private static Set<String> getVaults(boolean directSave) {
        Set<String> vaults;
        if (!VaultManager.MEMORY.vaults.containsKey(currentWorld)) {
            vaults = new HashSet<>();
            VaultManager.MEMORY.vaults.put(currentWorld, vaults);
            if (directSave) VaultManager.MEMORY.save();
        } else {
            vaults = VaultManager.MEMORY.vaults.get(currentWorld);
        }
        return vaults;
    }

    public static boolean addOpenVault(BlockPos pos) {
        String posString = getPosString(pos);
        boolean s = getVaults(false).add(posString);
        VaultManager.MEMORY.save();
        return s;
    }

    /* -------------------------------------------------------------------------------------------------------------- */

    public static int resetCurrentWorldCommand(CommandContext<FabricClientCommandSource> ctx) {
        getVaults(false).clear();
        VaultManager.MEMORY.save();

        ctx.getSource().sendFeedback(Text.literal("Vaults cleared for this world!").formatted(Formatting.DARK_PURPLE));
        return 0;
    }

    public static int queryNearestCommand(CommandContext<FabricClientCommandSource> ctx) {
        FabricClientCommandSource source = ctx.getSource();
        ClientPlayerEntity player = source.getPlayer();
        if (player.clientWorld.getRegistryKey() != World.OVERWORLD) {
            source.sendError(Text.literal("Vaults only spawn in the Overworld!").formatted(Formatting.RED));
            return 1;
        }

        Set<String> vaults = getVaults(false);
        if (vaults.isEmpty()) {
            source.sendError(Text.literal("No vaults have been opened yet!").formatted(Formatting.RED));
            return 1;
        }

        BlockPos cmp = player.getBlockPos();
        BlockPos pos = getVaults(false).stream().map(Worker::posFromString).min(Comparator.comparingInt(a -> a.getManhattanDistance(cmp))).orElseThrow();
        String distance = new DecimalFormat("#0.00").format(Math.sqrt(pos.getSquaredDistance(cmp)));
        ctx.getSource().sendFeedback(Text.literal("The nearest open vault is at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] (" + distance + " block away)").formatted(Formatting.DARK_PURPLE));
        return 0;
    }
}
