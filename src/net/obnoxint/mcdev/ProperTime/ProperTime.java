package net.obnoxint.mcdev.ProperTime;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public final class ProperTime extends JavaPlugin {

    private int dayLength = 20 * 60; // 20 minutes
    private boolean debug = false;
    private ProperTimeFixTime[] fixTimes;
    private final Logger log = Logger.getLogger("Minecraft");
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

    public boolean isDebug() {
        return this.debug;
    }

    @Override
    public void onDisable() {
        try {
            for (int i = 0; i < fixTimes.length; i++) {
                fixTimes[i].t.cancel();
                fixTimes[i].join();
                logInfo("Thread " + i + " successfully joined.", false);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logInfo("Signing off.", false);
    }

    @Override
    public void onEnable() {
        fixTimes = new ConfigParser(this).getFixTimes();
        for (int i = 0; i < fixTimes.length; i++) {
            fixTimes[i].start();
        }
        logInfo("Initialized.", false);
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    protected void logInfo(String text, Boolean debugMessage) {
        if (debugMessage) {
            if (!isDebug()) {
                return;
            }
        }
        log.info("[" + getDescription().getName() + " v" + getDescription().getVersion() + "] " + text);
    }

    protected void logWarn(String text) {
        log.warning("[" + getDescription().getName() + " v" + getDescription().getVersion() + "] " + text);
    }

}
