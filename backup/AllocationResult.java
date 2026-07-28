import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllocationResult {
    public String algorithm;
    public List<MemoryBlock> memoryBlocks;
    public List<Process> processes;
    public int totalExternalFragmentation;
    public int totalInternalFragmentation;
    public int totalMemory;
    public int usedMemory;
    public Map<Integer, Integer> allocationMap;
    public List<Integer> unallocatedProcesses;

    public AllocationResult() {
        this.memoryBlocks = new ArrayList<>();
        this.processes = new ArrayList<>();
        this.allocationMap = new HashMap<>();
        this.unallocatedProcesses = new ArrayList<>();
    }
}
