package backup;

public class Process {
    public int id;
    public int size;
    public boolean isAllocated;

    public Process(int id, int size) {
        this.id = id;
        this.size = size;
        this.isAllocated = false;
    }

    public Process copy() {
        Process copy = new Process(this.id, this.size);
        copy.isAllocated = this.isAllocated;
        return copy;
    }
}
