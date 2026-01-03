package com.webhookcommander;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class WebhookCommanderClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Client-side initialization (config screen support)
        WebhookCommanderMod.LOGGER.info("Webhook Commander client initialized");
    }
}
