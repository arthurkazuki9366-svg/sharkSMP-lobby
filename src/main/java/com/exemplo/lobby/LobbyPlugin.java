package com.exemplo.lobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class LobbyPlugin extends JavaPlugin {

    private File dataFile;
    private YamlConfiguration data;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        dataFile = new File(getDataFolder(), "data.yml");
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    private String msg(String chave, String padrao) {
        return ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("mensagens." + chave, padrao));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg("so-jogador", "Apenas jogadores podem usar este comando."));
            return true;
        }
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("setlobby")) {
            if (!p.hasPermission("lobby.admin")) {
                p.sendMessage(msg("sem-permissao", "&cVoce nao tem permissao."));
                return true;
            }
            Location l = p.getLocation();
            data.set("local.mundo", l.getWorld().getName());
            data.set("local.x", l.getX());
            data.set("local.y", l.getY());
            data.set("local.z", l.getZ());
            data.set("local.yaw", (double) l.getYaw());
            data.set("local.pitch", (double) l.getPitch());
            try {
                data.save(dataFile);
            } catch (IOException e) {
                getLogger().warning("Nao consegui salvar data.yml: " + e.getMessage());
            }
            p.sendMessage(msg("lobby-definido", "&aLobby definido.")
                    .replace("{mundo}", l.getWorld().getName()));
            return true;
        }

        if (!p.hasPermission("lobby.use")) {
            p.sendMessage(msg("sem-permissao", "&cVoce nao tem permissao."));
            return true;
        }

        Location dest = destino();
        if (dest == null) {
            p.sendMessage(msg("mundo-nao-encontrado", "&cMundo nao encontrado.")
                    .replace("{mundo}", getConfig().getString("mundo", "lobby")));
            return true;
        }

        p.teleport(dest);
        if (getConfig().getBoolean("tocar-som", true)) {
            p.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        }
        p.sendMessage(msg("sucesso", "&aVoce foi para o lobby!"));
        return true;
    }

    // Se /setlobby foi usado, vai para o ponto salvo; senao, para o spawn do mundo configurado
    private Location destino() {
        if (data.isSet("local.mundo")) {
            World w = Bukkit.getWorld(data.getString("local.mundo", ""));
            if (w != null) {
                return new Location(w,
                        data.getDouble("local.x"), data.getDouble("local.y"), data.getDouble("local.z"),
                        (float) data.getDouble("local.yaw"), (float) data.getDouble("local.pitch"));
            }
        }
        World w = Bukkit.getWorld(getConfig().getString("mundo", "lobby"));
        if (w == null) return null;
        return w.getSpawnLocation().add(0.5, 0, 0.5);
    }
}
