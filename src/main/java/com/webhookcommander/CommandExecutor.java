package com.webhookcommander;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CommandExecutor {

    public record CommandResult(boolean success, String message, int resultCode) {
    }

    public static CommandResult execute(String command) {
        MinecraftServer server = WebhookCommanderMod.getServer();
        if (server == null) {
            return new CommandResult(false, "Server not available", 0);
        }

        // Remove leading slash if present
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        final String finalCommand = command;
        AtomicReference<CommandResult> resultRef = new AtomicReference<>();
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Execute on the main server thread
        server.execute(() -> {
            try {
                CommandManager commandManager = server.getCommandManager();

                // Create a command source with server permissions
                ServerCommandSource source = server.getCommandSource()
                        .withSilent()
                        .withLevel(4); // Max permission level

                // Execute the command using the command dispatcher
                // In 1.21.4, executeWithPrefix returns void, so we use the dispatcher directly
                int result;
                try {
                    result = commandManager.getDispatcher().execute(finalCommand, source);
                    resultRef.set(new CommandResult(true, "Command executed successfully", result));
                } catch (Exception e) {
                    // Command parsing or execution failed
                    resultRef.set(new CommandResult(false, "Command failed: " + e.getMessage(), 0));
                }
            } catch (Exception e) {
                WebhookCommanderMod.LOGGER.error("Failed to execute command: {}", finalCommand, e);
                resultRef.set(new CommandResult(false, "Execution failed: " + e.getMessage(), 0));
            } finally {
                future.complete(null);
            }
        });

        // Wait for the command to complete (with timeout)
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            return new CommandResult(false, "Command execution timed out", 0);
        }

        CommandResult result = resultRef.get();
        if (result == null) {
            return new CommandResult(false, "Unknown error during execution", 0);
        }

        WebhookCommanderMod.LOGGER.info("Executed command '{}' with result: {}", finalCommand, result.resultCode());
        return result;
    }
}
