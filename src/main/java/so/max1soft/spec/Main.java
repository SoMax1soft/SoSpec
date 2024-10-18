package so.max1soft.spec;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin implements CommandExecutor {
    private String prefix;
    private String specTitle;
    private String specSubTitle;
    private String specFirstMessage;
    private String specSecondMessage;

    private final Map<UUID, Long> specCooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig(); // Загружает конфигурацию, если она не существует
        loadConfigValues();

        getLogger().info("");
        getLogger().info("§fИвент: §aЗапущен");
        getLogger().info("§fСоздатель: §b@max1soft");
        getLogger().info("§fВерсия: §c1.1");
        getLogger().info("");
        if (getCommand("spec") != null) {
            getCommand("spec").setExecutor(this);
        } else {
            getLogger().warning("Не удалось зарегистрировать команду spec.");
        }
    }

    private void loadConfigValues() {
        prefix = getConfig().getString("prefix", "&4[ПОМОЩНИК-СЛЕЖКИ] ").replace("&", "§");
        specTitle = getConfig().getString("spec-title", "&6Игрок запросил слежку").replace("&", "§");
        specSubTitle = getConfig().getString("spec_sub_title", "&7(Информация в чате)").replace("&", "§");
        specFirstMessage = getConfig().getString("spec_f_message", "&f§fИгрок запросил слежку за игроком§e ").replace("&", "§");
        specSecondMessage = getConfig().getString("spec_s_message", ", вылетаем на помощь!").replace("&", "§");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("spec")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                UUID playerUUID = player.getUniqueId();

                // Проверка на задержку
                if (specCooldowns.containsKey(playerUUID)) {
                    long lastUsed = specCooldowns.get(playerUUID);
                    long currentTime = System.currentTimeMillis();
                    long cooldownTime = 60000; // 1 минута в миллисекундах

                    if (currentTime - lastUsed < cooldownTime) {
                        long timeLeft = (cooldownTime - (currentTime - lastUsed)) / 1000;
                        player.sendMessage(prefix + "Вы не можете использовать команду /spec ещё " + timeLeft + " секунд.");
                        return true;
                    }
                }

                if (args.length > 0) {
                    String targetName = args[0];
                    Player targetPlayer = Bukkit.getPlayer(targetName);

                    if (targetPlayer != null && targetPlayer.isOnline()) {
                        String message = "§fИгрок §a" + player.getName() + "§f запросил слежку за игроком §a" + targetPlayer.getName();
                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                            if (onlinePlayer.hasPermission("spec.moder")) {
                                onlinePlayer.sendTitle(specTitle, specSubTitle, 10, 70, 20);
                                onlinePlayer.sendMessage(prefix + message);
                            }
                        }
                        player.sendMessage(prefix + "Вы запросили слежку за игроком " + targetPlayer.getName() + ".");
                        specCooldowns.put(playerUUID, System.currentTimeMillis());
                    } else {
                        player.sendMessage(prefix + "Игрок с именем " + targetName + " не найден онлайн.");
                    }
                } else {
                    player.sendMessage(prefix + "Использование: /spec <имя игрока>");
                }
            } else {
                sender.sendMessage(prefix + "Эту команду может выполнить только игрок.");
            }
            return true;
        }
        return false;
    }
}
