package net.yourname.alertmod;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class AlertMod implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            
            dispatcher.register(literal("alert")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        sendMenu(player);
                    }
                    return 1;
                })
            );

            dispatcher.register(literal("alert_run")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("1").executes(ctx -> sendTitle(ctx.getSource(), "ВНИМАНИЕ!", "Сервер выключится на техработы через 1 минуту!", Formatting.RED, Formatting.YELLOW)))
                .then(literal("2").executes(ctx -> sendTitle(ctx.getSource(), "ВНИМАНИЕ!", "Срочный перезапуск через 10 секунд!", Formatting.DARK_RED, Formatting.GOLD)))
                .then(literal("3").executes(ctx -> sendTitle(ctx.getSource(), "ОЧИСТКА ДРОПА", "Предметы на земле исчезнут через 30 секунд", Formatting.GOLD, Formatting.GRAY)))
                .then(literal("4").executes(ctx -> sendTitle(ctx.getSource(), "СОХРАНЕНИЕ МИРА", "Все данные успешно сохранены", Formatting.GREEN, Formatting.AQUA)))
                .then(literal("5").executes(ctx -> clearTitle(ctx.getSource())))
            );
        });
    }

    private void sendMenu(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("\n=== ПАНЕЛЬ ОПОВЕЩЕНИЙ ===").formatted(Formatting.BOLD, Formatting.GOLD), false);
        
        player.sendMessage(createButton(" 🛠 1. Техработы (1 мин)", "/alert_run 1", "Отправить: Техработы через 1 минуту", Formatting.RED), false);
        player.sendMessage(createButton(" ⚡ 2. Срочно (10 сек)", "/alert_run 2", "Отправить: Ребус через 10 секунд", Formatting.DARK_RED), false);
        player.sendMessage(createButton(" 🗑 3. Очистка дропа", "/alert_run 3", "Отправить: Очистка дропа через 30 сек", Formatting.GOLD), false);
        player.sendMessage(createButton(" 💾 4. Автосохранение", "/alert_run 4", "Отправить: Сохранение мира", Formatting.GREEN), false);
        player.sendMessage(createButton(" ❌ 5. Очистить экран", "/alert_run 5", "Убрать надписи с экранов игроков", Formatting.GRAY), false);
        
        player.sendMessage(Text.literal("=========================\n").formatted(Formatting.BOLD, Formatting.GOLD), false);
    }

    private Text createButton(String label, String command, String hoverText, Formatting color) {
        return Text.literal("[КЛИК]")
                .formatted(Formatting.BOLD, color)
                .styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hoverText)))
                )
                .append(Text.literal(label).formatted(Formatting.WHITE));
    }

    private int sendTitle(ServerCommandSource source, String titleText, String subtitleText, Formatting titleColor, Formatting subtitleColor) {
        for (ServerPlayerEntity p : source.getServer().getPlayerManager().getPlayerList()) {
            p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(
                    Text.literal(subtitleText).formatted(subtitleColor)
            ));
            p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleS2CPacket(
                    Text.literal(titleText).formatted(titleColor, Formatting.BOLD)
            ));
        }
        source.sendFeedback(() -> Text.literal("Оповещение успешно отправлено!").formatted(Formatting.GREEN), false);
        return 1;
    }

    private int clearTitle(ServerCommandSource source) {
        for (ServerPlayerEntity p : source.getServer().getPlayerManager().getPlayerList()) {
            p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket(true));
        }
        source.sendFeedback(() -> Text.literal("Экран очищен.").formatted(Formatting.YELLOW), false);
        return 1;
    }
}