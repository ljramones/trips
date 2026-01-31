package com.teamgannon.trips.measure;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.management.MBeanServer;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

@Slf4j
@Component
public class MetricManagement {

    private RuntimeMXBean runtimeMXBean;
    private OperatingSystemMXBean operatingSystemMXBean;
    private CompilationMXBean compilationMXBean;
    private ThreadMXBean threadMXBean;
    private List<GarbageCollectorMXBean> garbageCollectorMXBeanList;

    private MBeanServer mbs;

    private String host;
    private long pid;
    private String jvmVersion;
    private String specVendor;

    private long startTime;

    private int numberOfProcessors;

    private String osName;
    private String osVersion;

    private String machineArch;

    /**
     * Initialize the metrics management / environment snapshot.
     */
    @PostConstruct
    private void initialize() {
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.compilationMXBean = ManagementFactory.getCompilationMXBean();
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.garbageCollectorMXBeanList = ManagementFactory.getGarbageCollectorMXBeans();

        // Runtime attributes
        this.jvmVersion = runtimeMXBean.getVmVersion();
        this.specVendor = runtimeMXBean.getSpecVendor();
        this.pid = runtimeMXBean.getPid();
        this.startTime = runtimeMXBean.getStartTime();

        // runtimeMXBean.getName() is typically "pid@hostname"
        String name = runtimeMXBean.getName();
        int at = name.indexOf('@');
        this.host = (at >= 0 && at + 1 < name.length()) ? name.substring(at + 1) : name;

        // OS attributes
        this.numberOfProcessors = operatingSystemMXBean.getAvailableProcessors();
        this.osName = operatingSystemMXBean.getName();
        this.machineArch = operatingSystemMXBean.getArch();
        this.osVersion = operatingSystemMXBean.getVersion();

        this.mbs = ManagementFactory.getPlatformMBeanServer();

        log.info("Initialized metrics environment: pid={}, host={}, jvmVersion={}, os={} {} ({})",
                pid, host, jvmVersion, osName, osVersion, machineArch);
    }

    public long getProcessPid() {
        return pid;
    }

    public String getProcessHost() {
        return host;
    }

    public String getJvmVersion() {
        return jvmVersion;
    }

    public String getSpecVendor() {
        return specVendor;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getUptime() {
        return runtimeMXBean.getUptime();
    }

    public int getNumberOfProcessors() {
        return numberOfProcessors;
    }

    // Optional getters if you want them later
    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getMachineArch() {
        return machineArch;
    }
}
