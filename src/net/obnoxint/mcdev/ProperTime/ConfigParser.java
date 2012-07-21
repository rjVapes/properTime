package net.obnoxint.mcdev.ProperTime;

import java.io.File;

import org.bukkit.World;
import org.bukkit.configuration.Configuration;

public class ConfigParser {

    public enum Origin {
        FOLLOW,
        PRESET,
        SPECIFIC
    }

    class Conf {

        double factordawn = 1;
        double factorday = 1;
        double factordusk = 1;
        double factornight = 1;
        World followWorld = null;
        double followWorldDelay = 0;
        Origin origin = null;
        int perma = -1;
        World w;

        Conf(World w) {
            this.w = w;
        }
    };

    private Configuration conf = null;
    private Conf[] configs = null;
    private ProperTimeFixTime[] fixTimes;

    private final ProperTime plugin;

    public ConfigParser(ProperTime plugin) {
        this.plugin = plugin;

        configs = new Conf[plugin.getServer().getWorlds().size()];
        fixTimes = new ProperTimeFixTime[plugin.getServer().getWorlds().size()];

        for (int i = 0; i < configs.length; i++) {
            configs[i] = new Conf(plugin.getServer().getWorlds().get(i));
        }

        this.conf = plugin.getConfig();

        if ((new File("plugins" + File.separator + "ProperTime"
                + File.separator + "config.yml").exists())) {
            parseConfigFile();
        } else {
            generateNewConfigFile();
        }

        for (int i = 0; i < configs.length; i++) {
            if (configs[i].origin != Origin.SPECIFIC
                    && configs[i].origin != Origin.PRESET) {
                plugin.getLogger().info("Applying default config on world " + configs[i].w.getName());
            }
            fixTimes[i] = new ProperTimeFixTime(plugin, configs[i]);
        }
    }

    public ProperTimeFixTime[] getFixTimes() {
        return this.fixTimes;
    }

    private void generateNewConfigFile() {
        for (int i = 0; i < plugin.getServer().getWorlds().size(); i++) {
            String worldName = plugin.getServer().getWorlds().get(i).getName().toLowerCase();
            conf.set("propertime." + worldName + ".timespeedday", 1.0);
            conf.set("propertime." + worldName + ".timespeednight", 1.0);
            conf.set("propertime." + worldName + ".timespeeddusk", 1.0);
            conf.set("propertime." + worldName + ".timespeeddawn", 1.0);
            conf.set("propertime." + worldName + ".perma", "none");
        }
        plugin.saveConfig();
        plugin.getLogger().info("New configuration file created");
    }

    private void parseConfigFile() {

        java.util.Set<String> worlds = null;

        if ((!conf.contains("propertime")) || conf.getConfigurationSection("propertime").getKeys(false).size() == 0) {
            generateNewConfigFile();
            return;
        } else {
            worlds = conf.getConfigurationSection("propertime").getKeys(false);
        }

        for (String curWorld : worlds) {

            // System.out.println("now: " + curWorld);

            Conf c = null;
            for (int i = 0; i < configs.length; i++) 
            {
                if (configs[i].w.getName().equalsIgnoreCase(curWorld)) 
                {
                    c = configs[i];
                    plugin.getLogger().info("World set to: " + curWorld + ".");
                }
            }

            if (c == null) {
                plugin.getLogger().warning("World " + curWorld + "not found!");
                continue;
            }

            // System.out.println("propertime." + curWorld + ".followworld");
            if (conf.contains("propertime." + curWorld + ".preset")) {
                String preset = conf.getString("propertime." + curWorld + ".preset");
                boolean found = false;
                for (Preset p : Preset.values()) {
                    if (p.name().equalsIgnoreCase(preset)) {
                        found = true;
                        c.factorday = p.getFactorday();
                        c.factornight = p.getFactornight();
                        c.factordusk = p.getFactordusk();
                        c.factordawn = p.getFactordawn();
                        c.perma = p.getPerma();
                        plugin.getLogger().info("Preset \"" + preset + "\" applyed to " + curWorld);
                        c.origin = Origin.PRESET;
                    }
                }
                if (!found) {
                    plugin.getLogger().warning("Preset \"" + preset + "\" not found. Applying default.");
                }

            } else if (conf.contains("propertime." + curWorld + ".followworld")) {
                String followWorld = conf.getString("propertime." + curWorld + ".followworld");
                World worldFollowed = plugin.getServer().getWorld(followWorld);
                if (worldFollowed == null) {
                    plugin.getLogger().warning("World " + followWorld + "not found");
                }
                c.followWorld = worldFollowed;
                c.origin = Origin.FOLLOW;
                c.followWorldDelay = conf.getDouble("propertime." + curWorld + ".followworlddelay", 0.0);
                plugin.getLogger().info("World " + curWorld + " will follow " + followWorld + "'s time with a delay of " + c.followWorldDelay);
            } else {
                c.origin = Origin.SPECIFIC;
                c.factorday = conf.getDouble("propertime." + curWorld + ".timespeedday", 1.0);
                c.factornight = conf.getDouble("propertime." + curWorld + ".timespeednight", 1.0);
                c.factordusk = conf.getDouble("propertime." + curWorld + ".timespeeddusk", 1.0);
                c.factordawn = conf.getDouble("propertime." + curWorld + ".timespeeddawn", 1.0);

                String perma = conf.getString("propertime." + curWorld + ".perma", "-1");
                if (perma.equalsIgnoreCase("day")) {
                    c.perma = 6000;
                } else if (perma.equalsIgnoreCase("night")) {
                    c.perma = 18000;
                } else if (perma.equalsIgnoreCase("none")) {
                    c.perma = -1;
                } else {
                    c.perma = Integer.parseInt(perma);
                }
                plugin.getLogger().info("World: " + curWorld + " - SpeedDay: " + c.factorday);
                plugin.getLogger().info("World: " + curWorld + " - SpeedNight: " + c.factornight);
                plugin.getLogger().info("World: " + curWorld + " - SpeedDusk: " + c.factordusk);
                plugin.getLogger().info("World: " + curWorld + " - SpeedDawn: " + c.factordawn);
                plugin.getLogger().info("World: " + curWorld + " - Perma: " + c.perma);
            }

        }

        for (int i = 0; i < configs.length; i++) {

            if (configs[i].origin != Origin.SPECIFIC
                    && configs[i].origin != Origin.PRESET) {
                plugin.getLogger().info("Applying default config on world " + configs[i].w.getName());
                configs[i].perma = -1;
                configs[i].factorday = 1;
                configs[i].factornight = 1;
                configs[i].factordawn = 1;
                configs[i].factordusk = 1;
            }

            fixTimes[i] = new ProperTimeFixTime(plugin, configs[i]);
        }
    }

}