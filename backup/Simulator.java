import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Simulator {
    
    // Modern distinct colors for processes
    private static final Color[] PROCESS_COLORS = {
        ModernUI.ACCENT,
        new Color(155, 89, 182),
        new Color(26, 188, 156),
        new Color(230, 126, 34),
        new Color(231, 76, 60),
        new Color(39, 174, 96),
        new Color(142, 68, 173),
        new Color(241, 196, 15),
        new Color(211, 84, 0),
        new Color(46, 204, 113)
    };

    public static AllocationResult firstFit(List<MemoryBlock> blocks, List<Process> processes) {
        AllocationResult result = new AllocationResult();
        result.algorithm = "First Fit";
        result.memoryBlocks = copyBlocks(blocks);
        result.processes = copyProcesses(processes);

        for (Process process : result.processes) {
            boolean allocated = false;
            for (MemoryBlock block : result.memoryBlocks) {
                if (!block.isAllocated && block.remainingSize >= process.size) {
                    block.isAllocated = true;
                    block.processId = process.id;
                    block.internalFragmentation = block.remainingSize - process.size;
                    block.remainingSize = 0;
                    block.color = PROCESS_COLORS[(process.id - 1) % PROCESS_COLORS.length];
                    process.isAllocated = true;
                    result.allocationMap.put(process.id, block.id);
                    allocated = true;
                    break;
                }
            }
            if (!allocated) {
                result.unallocatedProcesses.add(process.id);
            }
        }
        calculateFragmentation(result);
        return result;
    }

    public static AllocationResult bestFit(List<MemoryBlock> blocks, List<Process> processes) {
        AllocationResult result = new AllocationResult();
        result.algorithm = "Best Fit";
        result.memoryBlocks = copyBlocks(blocks);
        result.processes = copyProcesses(processes);

        for (Process process : result.processes) {
            int bestBlockIndex = -1;
            int minWastage = Integer.MAX_VALUE;

            for (int i = 0; i < result.memoryBlocks.size(); i++) {
                MemoryBlock block = result.memoryBlocks.get(i);
                if (!block.isAllocated && block.remainingSize >= process.size) {
                    int wastage = block.remainingSize - process.size;
                    if (wastage < minWastage) {
                        minWastage = wastage;
                        bestBlockIndex = i;
                    }
                }
            }

            if (bestBlockIndex != -1) {
                MemoryBlock bestBlock = result.memoryBlocks.get(bestBlockIndex);
                bestBlock.isAllocated = true;
                bestBlock.processId = process.id;
                bestBlock.internalFragmentation = bestBlock.remainingSize - process.size;
                bestBlock.remainingSize = 0;
                bestBlock.color = PROCESS_COLORS[(process.id - 1) % PROCESS_COLORS.length];
                process.isAllocated = true;
                result.allocationMap.put(process.id, bestBlock.id);
            } else {
                result.unallocatedProcesses.add(process.id);
            }
        }
        calculateFragmentation(result);
        return result;
    }

    private static void calculateFragmentation(AllocationResult result) {
        result.totalExternalFragmentation = 0;
        result.totalInternalFragmentation = 0;
        result.totalMemory = 0;
        result.usedMemory = 0;

        for (MemoryBlock block : result.memoryBlocks) {
            result.totalMemory += block.size;
            if (!block.isAllocated) {
                result.totalExternalFragmentation += block.remainingSize;
            } else {
                result.totalInternalFragmentation += block.internalFragmentation;
                result.usedMemory += (block.size - block.internalFragmentation);
            }
        }
    }

    public static List<MemoryBlock> copyBlocks(List<MemoryBlock> original) {
        List<MemoryBlock> copy = new ArrayList<>();
        for (MemoryBlock block : original) {
            copy.add(block.copy());
        }
        return copy;
    }

    public static List<Process> copyProcesses(List<Process> original) {
        List<Process> copy = new ArrayList<>();
        for (Process process : original) {
            copy.add(process.copy());
        }
        return copy;
    }
}
