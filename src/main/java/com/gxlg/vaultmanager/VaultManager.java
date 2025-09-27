package com.gxlg.vaultmanager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaultManager implements ClientModInitializer {

    public static final String MOD_ID = "vault-manager";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Memory MEMORY = Memory.init();

    @Override
    public void onInitializeClient() {
        LOGGER.info("Hello from Vault Manager!");

        ClientPlayConnectionEvents.JOIN.register((n, p, c) -> {
            if (c.isInSingleplayer()) {
                IntegratedServer server = c.getServer();
                if (server == null) return;
                Worker.setCurrentWorld(server.getSavePath(WorldSavePath.ROOT).getParent().getFileName().toString());
            } else {
                ServerInfo info = n.getServerInfo();
                if (info == null) return;
                Worker.setCurrentWorld(info.address);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((n, c) -> Worker.setCurrentWorld(null));

        ClientCommandRegistrationCallback.EVENT.register((l, d) -> l
                .register(ClientCommandManager.literal("vault-manager")
                        .then(ClientCommandManager.literal("clear").executes(Worker::resetCurrentWorldCommand))
                        .then(ClientCommandManager.literal("near").executes(Worker::queryNearestCommand))
                )
        );
    }
}