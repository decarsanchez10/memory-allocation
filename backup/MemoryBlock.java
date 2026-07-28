package backup;

import java.awt.Color;

public class MemoryBlock {
    public int id;
    public int size;
    public int remainingSize;
    public boolean isAllocated;
    public int processId;
    public int internalFragmentation;
    public Color color;

    public MemoryBlock(int id, int size) {
        this.id = id;
        this.size = size;
        this.remainingSize = size;
        this.isAllocated = false;
        this.processId = -1;
        this.internalFragmentation = 0;
        this.color = null; 
    }

    public MemoryBlock copy() {
        MemoryBlock copy = new MemoryBlock(this.id, this.size);
        copy.remainingSize = this.remainingSize;
        copy.isAllocated = this.isAllocated;
        copy.processId = this.processId;
        copy.internalFragmentation = this.internalFragmentation;
        copy.color = this.color;
        return copy;
    }
}
