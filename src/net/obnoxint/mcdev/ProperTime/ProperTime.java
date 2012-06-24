package net.obnoxint.mcdev.ProperTime;

import org.bukkit.plugin.java.JavaPlugin;

public final class ProperTime extends JavaPlugin {

    private static final String LOG_DEBUG_PREFIX = "DEBUG: ";

    private int dayLength = 20 * 60; // 20 minutes
    private ProperTimeFixTime[] fixTimes;
    private int mcDayLength = 24000;
    private int stepSize = 5; // seconds
    private int steps = dayLength / stepSize;
    private int defaultStep = getMcDayLength() / steps;

    public int getDefaultStep() {
        return this.defaultStep;
    }

    public int getMcDayLength() {
        return mcDayLength;
    }

    public int getStepSize() {
        return this.stepSize;
    }

    @Override
    public void onDisable() {
        try {
            for (int i = 0; i < fixTimes.length; i++) {
                fixTimes[i].t.cancel();
                fixTimes[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        fixTimes = new ConfigParser(this).getFixTimes();
        for (int i = 0; i < fixTimes.length; i++) {
            fixTimes[i].start();
        }
    }

    void logDebug(String msg) {
        getLogger().info(LOG_DEBUG_PREFIX + msg);
    }

}
