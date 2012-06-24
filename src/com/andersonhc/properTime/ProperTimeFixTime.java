package com.andersonhc.properTime;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;


import org.bukkit.World;

import com.andersonhc.properTime.ConfigParser.Conf;


public final class ProperTimeFixTime extends Thread {

    private final class Step implements Callable<Void> {

        @Override
        public Void call() {
            long ctime = world.getTime();
            if (perma >= 0) { /* Permanent time */
                world.setTime(ctime - (ctime % 24000) + perma);
            }
            if (worldFollow != null) { /* World time follow */
                long newTime = worldFollow.getTime() - worldFollowDelay;
                if (newTime < 0) {
                    newTime += plugin.getMcDayLength();
                }
                world.setTime(newTime);
            } else {
                if (ctime < lasttime + 3 * plugin.getDefaultStep() && ctime > lasttime - plugin.getDefaultStep()) {
                    long ntime = lasttime + getStep(lasttime);
                    world.setTime(ntime);
                    plugin.logInfo("Synchronized time on world \"" + world.getName() + "\", diff was " + (ntime - ctime) + ".", true);
                    lasttime = ntime;
                } else { // someone used settime
                    plugin.logInfo("(debug) Apparently someone used setTime, not synchronizing.", true);
                    lasttime = ctime;
                }
            }
            return null;
        }

    }

    private final class ttask extends TimerTask {

        @Override
        public void run() {
            plugin.getServer().getScheduler().callSyncMethod(plugin, new Step());
        }
    }

    private int desiredStepDawn;
    private int desiredStepDay;
    private int desiredStepDusk;
    private int desiredStepNight;
    private long lasttime;
    private int perma = -1;
    private ProperTime plugin;

    private World world;
    private World worldFollow;

    private int worldFollowDelay;

    protected Timer t; // cancel this in onDisable!

    // Constructor
    ProperTimeFixTime(ProperTime plugin, Conf c) {
        this.plugin = plugin;
        desiredStepDawn = (int) (c.factordawn * plugin.getDefaultStep());
        desiredStepDusk = (int) (c.factordusk * plugin.getDefaultStep());
        desiredStepDay = (int) (c.factorday * plugin.getDefaultStep());
        desiredStepNight = (int) (c.factornight * plugin.getDefaultStep());
        world = c.w;
        perma = c.perma;
        worldFollow = c.followWorld;
        worldFollowDelay = (int) c.followWorldDelay;
    }

    @Override
    public void run() {
        t = new Timer();
        t.schedule(new ttask(), plugin.getStepSize() * 1000, plugin.getStepSize() * 1000);
    }

    private int getStep(long a) {
        if ((a % 24000) < 12000) { // day
            if ((a + plugin.getDefaultStep()) % 24000 > 12000) {
                return desiredStepDusk;
            }
            return desiredStepDay;
        } else if ((a % 24000) < 13800) { // sundown
            if ((a + plugin.getDefaultStep()) % 24000 > 13800) {
                return desiredStepNight;
            }
            return desiredStepDusk;
        } else if ((a % 24000) < 22200) { // night
            if ((a + plugin.getDefaultStep()) % 24000 > 22200) {
                return desiredStepDawn;
            }
            return desiredStepNight;
        } else { // sunrise
            if ((a + plugin.getDefaultStep()) % 24000 < 12000) {
                return desiredStepDay;
            }
            return desiredStepDawn;
        }
    }
}