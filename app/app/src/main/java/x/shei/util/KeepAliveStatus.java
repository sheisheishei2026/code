package x.shei.util;

/**
 * 保活状态类
 */
public class KeepAliveStatus {
    public final boolean isBatteryOptimizationIgnored;
    public final boolean isBackgroundRunningAllowed;
    public final boolean isAutoStartEnabled;
    public final boolean isAccessibilityServiceEnabled;

    public KeepAliveStatus(boolean isBatteryOptimizationIgnored,
                          boolean isBackgroundRunningAllowed,
                          boolean isAutoStartEnabled,
                          boolean isAccessibilityServiceEnabled) {
        this.isBatteryOptimizationIgnored = isBatteryOptimizationIgnored;
        this.isBackgroundRunningAllowed = isBackgroundRunningAllowed;
        this.isAutoStartEnabled = isAutoStartEnabled;
        this.isAccessibilityServiceEnabled = isAccessibilityServiceEnabled;
    }
}
