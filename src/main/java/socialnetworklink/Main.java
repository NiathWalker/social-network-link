package socialnetworklink;

import arc.Core;
import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.mod.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;

public class Main extends Plugin {
    private final HashMap<String, String> configValues = new HashMap<>();

    @Override
    public void init() {
        Events.on(EventType.ServerLoadEvent.class, e -> {
            File configPath = new File(Core.settings.getDataDirectory().child("mods/social-network-link.properties").toString());
            try {
                if (!configPath.exists()) {
                    if (configPath.createNewFile()) {
                        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configPath), StandardCharsets.UTF_8))) {
                            StringBuilder data = new StringBuilder();
                            data.append("social-network-link-indicate=https://discord.com/invite/").append("\n");
                            data.append("social-network-link-at-join=true").append("\n");
                            data.append("social-network-link-at-join-for-admins=false").append("\n");
                            data.append("social-network-link-command-toggle=true").append("\n");
                            data.append("social-network-link-command-name=link").append("\n");
                            data.append("social-network-link-command-description=Get a link to social network.");

                            writer.write(data.toString());
                        }
                    }
                }
            } catch (IOException ex) {
                Log.err(String.format("An error occurred while creating the settings file: %s", ex.getMessage()));
            }

            Properties properties = new Properties();
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configPath), StandardCharsets.UTF_8)) {
                properties.load(reader);

                configValues.put("social-network-link-indicate", properties.getProperty("social-network-link-indicate"));
                configValues.put("social-network-link-at-join", properties.getProperty("social-network-link-at-join"));
                configValues.put("social-network-link-at-join-for-admins", properties.getProperty("social-network-link-at-join-for-admins"));
                configValues.put("social-network-link-command-toggle", properties.getProperty("social-network-link-command-toggle"));
                configValues.put("social-network-link-command-name", properties.getProperty("social-network-link-command-name"));
                configValues.put("social-network-link-command-description", properties.getProperty("social-network-link-command-description"));
            } catch (IOException ex) {
                Log.err(String.format("An error occurred while reading the settings file: %s", ex.getMessage()));
            }
        });

        Events.on(EventType.PlayerJoin.class, e -> {
            if (configValues.get("social-network-link-at-join").equals("true")) {
                if (!e.player.admin) {
                    Call.openURI(e.player.con, configValues.get("social-network-link-indicate"));
                } else {
                    if (configValues.get("social-network-link-at-join-for-admins").equals("true")) {
                        Call.openURI(e.player.con, configValues.get("social-network-link-indicate"));
                    }
                }
            }
        });
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        if (configValues.get("social-network-link-command-toggle").equals("true")) {
            handler.<Player>register(configValues.get("social-network-link-command-name"),
                    configValues.get("social-network-link-command-description"), (arg, player) ->
                            Call.openURI(player.con, configValues.get("social-network-link-indicate")));
        }
    }
}
