package lachlanrobinson.myfirstapp;

/**
 * Created by Lachlan on 13/10/2015.
 */
public class SerialReadData {

    private int numBytesRead = -1;
    private byte buffer[] = new byte[16];

    public SerialReadData(int x, byte[] y) {
        this.numBytesRead = x;
        this.buffer = y;
    }

    public void reset() {
        this.numBytesRead = 0;
        this.buffer = new byte[16];
    }

    public void setNumBytesRead(int x) { this.numBytesRead = x; }

    public void setBuffer(byte[] y) {
        this.buffer = y;
    }

    public int getNumBytesRead() {
        return this.numBytesRead;
    }

    public byte[] getBuffer() { return this.buffer; }

}
