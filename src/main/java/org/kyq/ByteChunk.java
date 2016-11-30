package org.kyq;

import java.util.Iterator;

/**
 * Created by jianghan on 2016/11/18.
 */
public class ByteChunk {

  private static final int CHUNK_LENGTH = 65536;
  private byte[] chunk;
  private int wpos;
  private ByteChunk next;

  public ByteChunk() {
    this.chunk = new byte[CHUNK_LENGTH];
    this.wpos = 0;
  }

  public ByteChunk(byte[] chunk) {
    this.chunk = chunk;
    this.wpos = chunk.length;
  }

  public int write(byte x) {
    int r = 0;
    ByteChunk point = this;
    while (point.next != null) {
      point = point.next;
      r += CHUNK_LENGTH;
    }
    point.chunk[wpos] = x;
    point.wpos++;
    if (point.wpos == CHUNK_LENGTH) {
      point.next = new ByteChunk();
    }
    return r + point.wpos - 1;
  }

  public int write(int x) {
    return writeVarint32(intToZigZag(x));
    /**
     int r = write((byte) (x >>> 24));
     write((byte) (x >>> 16));
     write((byte) (x >>> 8));
     write((byte) (x));
     return r;
     **/
  }

  public int write(long x) {
    return writeVarint64(longToZigZag(x));
    /**
     int r = write((byte) (x >> 56));
     write((byte) (x >> 48));
     write((byte) (x >> 40));
     write((byte) (x >> 32));
     write((byte) (x >> 24));
     write((byte) (x >> 16));
     write((byte) (x >> 8));
     write((byte) (x));
     return r;
     **/
  }

  public int write(double x) {
    long l = Double.doubleToRawLongBits(x);
    return write(l);
  }

  public int write(char x) {
    int r = write((byte) (x >> 8));
    write((byte) (x));
    return r;
  }

  public int write(String str) {
    int r = -1; // TODO
    for (int i = 0; i < str.length(); i++) {
      r = write(str.charAt(i));
    }
    return r;
  }

  public byte getByte(int pos) {
    ByteChunk point = this;
    while (point.wpos <= pos) {
      pos -= point.wpos;
      point = point.next;
    }
    return point.chunk[pos];
  }

  public byte[] getByte(int pos, int size) {
    return new byte[0];
  }

  public Iterator getByteIterator(int pos) {
    return null;
  }

  public int getInt(int pos) {
    return intFromZigZag(readVarint32(pos));
    /**
     return ((getByte(pos) & 0xff) << 24)
     | ((getByte(pos + 1) & 0xff) << 16)
     | ((getByte(pos + 2) & 0xff) << 8)
     | ((getByte(pos + 3) & 0xff));
     **/
  }

  public long getLong(int pos) {
    return longFromZigZag(readVarint64(pos));
    /**
     return (((long) getByte(pos) & 0xff) << 56)
     | (((long) getByte(pos + 1) & 0xff) << 48)
     | (((long) getByte(pos + 2) & 0xff) << 40)
     | (((long) getByte(pos + 3) & 0xff) << 32)
     | (((long) getByte(pos + 4) & 0xff) << 24)
     | (((long) getByte(pos + 5) & 0xff) << 16)
     | (((long) getByte(pos + 6) & 0xff) << 8)
     | (((long) getByte(pos + 7) & 0xff));
     **/
  }

  public double getDouble(int pos) {
    long l = getLong(pos);
    return Double.longBitsToDouble(l);
  }

  public char getChar(int pos) {
    return (char) ((((char) getByte(pos) & 0xff) << 8) | (((char) getByte(pos + 1) & 0xff)));

  }

  public String getString(int pos, int length) {
    char[] chs = new char[length];
    for (int i = 0; i < length; ++i) {
      chs[i] = getChar(pos + i);
    }
    return new String(chs);
  }

  public int size() {
    int r = 0;
    ByteChunk point = this;
    while (point.next != null) {
      point = point.next;
      r += CHUNK_LENGTH;
    }
    return r + point.wpos;
  }

  private long longToZigZag(long l) {
    return (l << 1) ^ (l >> 63);
  }

  private long longFromZigZag(long n) {
    return (n >>> 1) ^ -(n & 1);
  }

  private int intToZigZag(int n) {
    return (n << 1) ^ (n >> 31);
  }

  private int intFromZigZag(int n) {
    return (n >>> 1) ^ -(n & 1);
  }

  private int readVarint32(int pos) {
    int result = 0;
    int shift = 0;
    while (true) {
      byte b = getByte(pos++);
      result |= (b & 0x7f) << shift;
      if ((b & 0x80) != 0x80) break;
      shift += 7;
    }
    return result;
  }

  private long readVarint64(int pos) {
    int shift = 0;
    long result = 0;
    while (true) {
      byte b = getByte(pos++);
      result |= (long) (b & 0x7f) << shift;
      if ((b & 0x80) != 0x80) break;
      shift += 7;
    }
    return result;
  }

  /**
   * Write an i32 as a varint. Results in 1-5 bytes on the wire.
   * TODO: make a permanent buffer like writeVarint64?
   */
  byte[] i32buf = new byte[5];

  private int writeVarint32(int n) {
    int idx = 0;
    while (true) {
      if ((n & ~0x7F) == 0) {
        i32buf[idx++] = (byte) n;
        // writeByteDirect((byte)n);
        break;
        // return;
      } else {
        i32buf[idx++] = (byte) ((n & 0x7F) | 0x80);
        // writeByteDirect((byte)((n & 0x7F) | 0x80));
        n >>>= 7;
      }
    }
    int r = write(i32buf[0]);
    for (int i = 1; i < idx; i++) {
      write(i32buf[i]);
    }
    return r;
  }

  /**
   * Write an i64 as a varint. Results in 1-10 bytes on the wire.
   */
  byte[] varint64out = new byte[10];

  private int writeVarint64(long n) {
    int idx = 0;
    while (true) {
      if ((n & ~0x7FL) == 0) {
        varint64out[idx++] = (byte) n;
        break;
      } else {
        varint64out[idx++] = ((byte) ((n & 0x7F) | 0x80));
        n >>>= 7;
      }
    }
    int r = write(varint64out[0]);
    for (int i = 1; i < idx; i++) {
      write(varint64out[i]);
    }
    return r;
  }

}
