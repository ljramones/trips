package com.teamgannon.trips.measure;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.hardware.CentralProcessor.ProcessorIdentifier;
import oshi.software.os.*;
import oshi.software.os.OperatingSystem.OSVersionInfo;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OshiMeasure {

    private SystemInfo si;
    private HardwareAbstractionLayer hal;

    private GlobalMemory globalMemory;
    private CentralProcessor cpu;

    private OperatingSystem operatingSystem;
    private FileSystem fileSystem;
    private OSVersionInfo osVersionInfo;
    private int currentProcessPid;
    private OSProcess currentOsProcess;

    private String family;
    private String manufacturer;
    private int bitness;
    private int numLogicalProcs;
    private int numPhysicalProcs;

    private ProcessorIdentifier processorIdentifier;
    private ComputerSystem computerSystem;
    private Firmware firmware;
    private List<GraphicsCard> graphicsCards;

    @PostConstruct
    public void initialize() {
        this.si = new SystemInfo();
        this.hal = si.getHardware();

        this.operatingSystem = si.getOperatingSystem();
        this.osVersionInfo = operatingSystem.getVersionInfo();
        this.family = operatingSystem.getFamily();
        this.manufacturer = operatingSystem.getManufacturer();
        this.bitness = operatingSystem.getBitness();
        this.currentProcessPid = operatingSystem.getProcessId();
        this.currentOsProcess = operatingSystem.getProcess(currentProcessPid);

        this.fileSystem = operatingSystem.getFileSystem();

        this.computerSystem = hal.getComputerSystem();

        this.graphicsCards = hal.getGraphicsCards();

        this.firmware = hal.getComputerSystem().getFirmware();

        this.cpu = hal.getProcessor();
        this.numLogicalProcs = cpu.getLogicalProcessorCount();
        this.numPhysicalProcs = cpu.getPhysicalProcessorCount();

        this.processorIdentifier = cpu.getProcessorIdentifier();

        this.globalMemory = hal.getMemory();

        // print out the inventory of this machine
//        log.info(getComputerInventory());

    }

    public String getComputerInventory() {
        int numberProcesses = operatingSystem.getProcessCount();
        int numberThreads = operatingSystem.getThreadCount();
        long availableMemory = globalMemory.getAvailable();
        long totalMemory = globalMemory.getTotal();
        List<OSFileStore> osFileStoreList = fileSystem.getFileStores(true);
        String fileStorage = getFileStorageAsString(osFileStoreList);
        Sensors sensors = hal.getSensors();

        return 
                """
                        \nPlatform Inventory::
                        \tHardware
                        \t\tFirmware:: %s
                        \t\tComputer System:: %s
                        \t\tGraphics Cards:: %s
                        \t\tSensors:: %s
                        \tOperating System:
                        \t\tmanufacturer     = %s
                        \t\tOS name          = %s
                        \t\tversion          = %s
                        \t\tbitness          = %d
                        \t\ttotal num of processes = %,d
                        \t\ttotal num of threads   = %,d
                        \t\tUser Sessions:: %s
                        \t\tthis process
                        \t\t\tthis process id  = %d
                        %s
                        \tCPU:
                        \t\tNumber of Physical Processors = %d
                        \t\tNumber of Logical Processors  = %d
                        \t\tProcessor Arch = %s
                        \tMemory:
                        \t\tAvailable Memory = %s Mb,
                        \t\tTotal Memory     = %s Mb
                        \tFile System: %s
                        """.formatted(
                firmware,
                computerSystem,
                getGraphicsCardsAsString(),
                sensors,
                manufacturer,
                family,
                osVersionInfo,
                bitness,
                numberProcesses,
                numberThreads,
                getUserSessionsAsString(),
                currentProcessPid,
                currentOsProcessAsString(),
                numPhysicalProcs,
                numLogicalProcs,
                processorIdentifier.toString(),
                toMbCommas(availableMemory),
                toMbCommas(totalMemory),
                fileStorage
        );
    }

    public String getUserSessionsAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        List<OSSession> osSessionList = operatingSystem.getSessions();
        int i = 0;
        for (OSSession osSession : osSessionList) {
            stringBuilder.append("\n\t\t\tsession ").append("%d = %s".formatted(i, osSession));
            i++;
        }
        return stringBuilder.toString();
    }

    public List<OSSession> getOsSessions() {
        return operatingSystem.getSessions();
    }

    public Object getGraphicsCardsAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (GraphicsCard graphicsCard : graphicsCards) {
            stringBuilder.append("\n\t\t\tcard ").append("%d = %s".formatted(i, graphicsCard));
            i++;
        }
        return stringBuilder.toString();
    }

    public Sensors getSensors() {
        return hal.getSensors();
    }

    /**
     * get all the graphics cards that this system has available to it.
     *
     * @return the list of graphics cards
     */
    public List<GraphicsCard> getGraphicsCards() {
        return graphicsCards;
    }

    public String getFileStorageAsString(List<OSFileStore> osFileStoreList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (OSFileStore fileStore : osFileStoreList) {
            String name = fileStore.getName();
            String description = fileStore.getDescription();
            long totalSpace = fileStore.getTotalSpace();
            long freeSpace = fileStore.getFreeSpace();
            long usableSpace = fileStore.getUsableSpace();
            stringBuilder.append("\n\t\tName = ").append(name);
            stringBuilder.append("\n\t\t\tDescription = ").append(description);
            stringBuilder.append("\n\t\t\tTotal Space = ").append(toMbCommas(totalSpace)).append(" Mb");
            stringBuilder.append("\n\t\t\tFree Space = ").append(toMbCommas(freeSpace)).append(" Mb");
            stringBuilder.append("\n\t\t\tUsable Space = ").append(toMbCommas(usableSpace)).append(" Mb");
        }
        return stringBuilder.toString();
    }

    ///////////// Memory measures  /////////////////

    /**
     * get the total memory in the system in megabytes
     *
     * @return the total memory
     */
    public long getTotalMemoryInMb() {
        return toMb(globalMemory.getTotal());
    }

    /**
     * get the available memory in megabytes
     *
     * @return the available memory
     */
    public long getAvailableMemoryInMb() {
        return toMb(globalMemory.getAvailable());
    }

    /////////////////  CPU  //////////////////

    /**
     * get number of logical processors
     *
     * @return the number of processing elements available for threads
     */
    public int numberOfLogicalProcessors() {
        return cpu.getLogicalProcessorCount();
    }

    /**
     * get the number of physical processors or cores
     *
     * @return the number of cores
     */
    public int numberOfPhysicalProcessors() {
        return cpu.getPhysicalProcessorCount();
    }

    /**
     * get information on the processor architecture
     *
     * @return the processor architecture (printed as a string gives a simple identification
     */
    public ProcessorIdentifier getProcessorIdentifier() {
        return cpu.getProcessorIdentifier();
    }

    ///////////////// File System /////////////

    /**
     * return only the local file systems
     *
     * @return list of local file systems
     */
    public List<OSFileStore> getFileStores() {
        return fileSystem.getFileStores(true);
    }

    ///////////////// Operating System /////////

    /**
     * get the operating system information
     *
     * @return the os information
     */
    public OSVersionInfo getOperatingSystemInfo() {
        return osVersionInfo;
    }

    /**
     * get the manufacturer
     *
     * @return the manufacturer
     */
    public String getManufacturer() {
        return operatingSystem.getManufacturer();
    }

    /**
     * get the OS name
     *
     * @return OS name
     */
    public String getOsName() {
        return operatingSystem.getFamily();
    }

    public OSProcess getCurrentOsProcess() {
        return currentOsProcess;
    }

    public String currentOsProcessAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        String name = currentOsProcess.getName();
        String currentCommandLine = currentOsProcess.getCommandLine();
        String currentWorkingDir = currentOsProcess.getCurrentWorkingDirectory();
        String path = currentOsProcess.getPath();
        Map<String, String> envVarsList = currentOsProcess.getEnvironmentVariables();
        int priority = currentOsProcess.getPriority();
        double cumLoad = currentOsProcess.getProcessCpuLoadCumulative();
        long startTime = currentOsProcess.getStartTime();
        long upTime = currentOsProcess.getUpTime();
        String state = currentOsProcess.getState().toString();
        long virtualSize = currentOsProcess.getVirtualSize();
        int threadCount = currentOsProcess.getThreadCount();

        stringBuilder.append("\t\t\tname = ").append(name);
        stringBuilder.append("\n\t\t\tcurrentCommandLine = ").append(currentCommandLine);
        stringBuilder.append("\n\t\t\tcurrentWorkingDir = ").append(currentWorkingDir);
        stringBuilder.append("\n\t\t\tpath = ").append(path);

        stringBuilder.append("\n\t\t\tpriority = ").append(priority);
        stringBuilder.append("\n\t\t\tcumLoad = ").append("%.6f".formatted(cumLoad));
        stringBuilder.append("\n\t\t\tstartTime = ").append("%,d".formatted(startTime));
        stringBuilder.append("\n\t\t\tupTime = ").append("%,d".formatted(upTime));
        stringBuilder.append("\n\t\t\tstate = ").append(state);
        stringBuilder.append("\n\t\t\tvirtualSize = ").append("%,d".formatted(virtualSize));
        stringBuilder.append("\n\t\t\tthreadCount = ").append("%,d".formatted(threadCount));

        List<OSThread> osThreadList = currentOsProcess.getThreadDetails();
        for (OSThread osThread : osThreadList) {
            stringBuilder.append(getOsThreadAsString(osThread));
        }
        return stringBuilder.toString();
    }

    public List<OSThread> getOsThreadsForCurrentProcess() {
        return currentOsProcess.getThreadDetails();
    }

    public String getOsThreadAsString(OSThread osThread) {
        StringBuilder stringBuilder = new StringBuilder();
        int id = osThread.getThreadId();
        String state = osThread.getState().toString();
        int priority = osThread.getPriority();
        long startTime = osThread.getStartTime();
        long upTime = osThread.getUpTime();
        double cumulativeLoad = osThread.getThreadCpuLoadCumulative();
        long contextSwitches = osThread.getContextSwitches();

        stringBuilder.append("\n\t\t\t\tthread id = ").append(id);
        stringBuilder.append("\n\t\t\t\t\tstate = ").append(state);
        stringBuilder.append("\n\t\t\t\t\tpriority = ").append(priority);
        stringBuilder.append("\n\t\t\t\t\tstartTime = ").append("%,d".formatted(startTime));
        stringBuilder.append("\n\t\t\t\t\tupTime = ").append("%,d".formatted(upTime));
        stringBuilder.append("\n\t\t\t\t\tcumulativeLoad = ").append("%.6f".formatted(cumulativeLoad));
        stringBuilder.append("\n\t\t\t\t\tcontextSwitches = ").append(contextSwitches);

        return stringBuilder.toString();
    }

    ////////////////// UI Windows  ////////////

    /**
     * get the desktop windows
     *
     * @param visibility true is only visible windows
     * @return the list of windows
     */
    public List<OSDesktopWindow> getWindows(boolean visibility) {
        return operatingSystem.getDesktopWindows(visibility);
    }

    ///////////////// SUPPORT ////////////////

    private long toMb(long value) {
        return value / 1024 / 1024;
    }

    /**
     * convert to a megabyte vale and then turn into a string with commas
     *
     * @param value the value to convert
     * @return the value with commas in megabyte quantity
     */
    private String toMbCommas(long value) {
        long mbValue = toMb(value);
        return "%,d".formatted(mbValue);
    }

}
