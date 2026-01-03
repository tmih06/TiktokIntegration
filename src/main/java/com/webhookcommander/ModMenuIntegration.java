package com.webhookcommander;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> createConfigScreen(parent);
    }

    private Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Webhook Commander Config"))
                .setSavingRunnable(ModConfig::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Server Settings Category
        ConfigCategory serverCategory = builder.getOrCreateCategory(Text.literal("Server Settings"));

        serverCategory.addEntry(entryBuilder
                .startIntField(Text.literal("Webhook Port"), ModConfig.getPort())
                .setDefaultValue(8080)
                .setMin(1024)
                .setMax(65535)
                .setTooltip(Text.literal("The port the webhook server listens on (requires restart)"))
                .setSaveConsumer(ModConfig::setPort)
                .build());

        serverCategory.addEntry(entryBuilder
                .startStrField(Text.literal("Auth Token"), ModConfig.getAuthToken())
                .setDefaultValue("")
                .setTooltip(
                        Text.literal("Optional authentication token."),
                        Text.literal("Requests must include: Authorization: Bearer <token>"),
                        Text.literal("Leave empty to disable authentication"))
                .setSaveConsumer(ModConfig::setAuthToken)
                .build());

        // Debug Settings Category
        ConfigCategory debugCategory = builder.getOrCreateCategory(Text.literal("Debug Settings"));

        debugCategory.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Enable Debug Logging"), TiktokCommand.isDebugEnabled())
                .setDefaultValue(false)
                .setTooltip(
                        Text.literal("Enable verbose debug logging in the console"),
                        Text.literal("Shows all incoming webhook requests and responses"))
                .setSaveConsumer(TiktokCommand::setDebugEnabled)
                .build());

        // Info Category
        ConfigCategory infoCategory = builder.getOrCreateCategory(Text.literal("Information"));

        infoCategory.addEntry(entryBuilder
                .startTextDescription(Text.literal(
                        "§bWebhook Commander§r\n\n" +
                                "This mod opens an HTTP server on the configured port.\n\n" +
                                "§eEndpoints:§r\n" +
                                "  POST /execute - Execute a command\n" +
                                "  GET /health - Health check\n\n" +
                                "§eExample Request:§r\n" +
                                "  curl -X POST http://localhost:<port>/execute \\\n" +
                                "    -H \"Content-Type: application/json\" \\\n" +
                                "    -d '{\"command\": \"/say Hello!\"}'\n\n" +
                                "§eIn-game Commands:§r\n" +
                                "  /tiktok debug <true|false>\n" +
                                "  /tiktok status\n" +
                                "  /tiktok test"))
                .build());

        return builder.build();
    }
}
