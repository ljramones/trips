package com.teamgannon.trips.measure;

import com.codahale.metrics.MetricRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.management.MBeanServer;
import java.lang.management.*;
import java.util.List;

@Slf4j
@Component
public class MetricManagement {

    private final MetricRegistry metricRegistry = new MetricRegistry();

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
     * initialize the metrics management
     */
    @PostConstruct
    private void initialize() {
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.compilationMXBean = ManagementFactory.getCompilationMXBean();
        this.threadMXBean = ManagementFactory.getThreadMXBean();

        this.garbageCollectorMXBeanList = ManagementFactory.getGarbageCollectorMXBeans();


        // process the runtime attributes
        this.jvmVersion = runtimeMXBean.getVmVersion();
        this.specVendor = runtimeMXBean.getSpecVendor();
        this.pid = runtimeMXBean.getPid();
        this.startTime = runtimeMXBean.getStartTime();
        this.host = runtimeMXBean.getName().split("@")[1];

        // process the operating system attributes
        this.numberOfProcessors = operatingSystemMXBean.getAvailableProcessors();
        this.osName = operatingSystemMXBean.getName();
        this.machineArch = operatingSystemMXBean.getArch();
        this.osVersion = operatingSystemMXBean.getVersion();

        this.mbs = ManagementFactory.getPlatformMBeanServer();


        log.info("Initialzed metrics contingent");

    }

    /**
     * got the process pid
     *
     * @return the process pid
     */
    public long getProcessPid() {
        return pid;
    }

    /**
     * get the host name
     *
     * @return the host name
     */
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

}
