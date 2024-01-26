package de.cyb3rko.jabcodereader;

public class ReturnObject {
    private final int status;
    private final byte[] data;

    ReturnObject(int status, byte[] data) {
        this.status = status;
        this.data = data;
    }

    public int getStatus() {
        return this.status;
    }

    public byte[] getData() {
        return this.data;
    }
}
