package StandTcp.StandTcpProtocol;

import io.netty.buffer.ByteBuf;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

// 通用公共工具和函数
public final class TAcsTool {

    public static byte Bool2Byte(boolean val) {
        return (val == false) ? (byte) 0 : (byte) 1;
    }

    private static final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public static String Bytes2Hex(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    public static String Bytes2Hex(byte data) {
        String str;
        str = String.valueOf(hexCode[(data >> 4) & 0xF]);
        str += String.valueOf(hexCode[(data & 0xF)]);
        return str;
    }

    public static String Short2Hex(short data){
        String str;
        str = String.valueOf(hexCode[(data >> 12) & 0xF]);
        str += String.valueOf(hexCode[(data >> 8) & 0xF]);
        str += String.valueOf(hexCode[(data >> 4) & 0xF]);
        str += String.valueOf(hexCode[(data & 0xF)]);
        return str;
    }

    public static String Int2Hex(int data) {
        String str;
        str = String.valueOf(hexCode[(data >> 28) & 0xF]);
        str += String.valueOf(hexCode[(data >> 24) & 0xF]);
        str += String.valueOf(hexCode[(data >> 20) & 0xF]);
        str += String.valueOf(hexCode[(data >> 16) & 0xF]);
        str += String.valueOf(hexCode[(data >> 12) & 0xF]);
        str += String.valueOf(hexCode[(data >> 8) & 0xF]);
        str += String.valueOf(hexCode[(data >> 4) & 0xF]);
        str += String.valueOf(hexCode[(data & 0xF)]);
        return str;
    }


    public static int bytesToInt(byte[] data) {
        int x = ((data[0] & 0xff) << 24) |
                ((data[1] & 0xff) << 16) |
                ((data[2] & 0xff) << 8) |
                (data[3] & 0xff);
        return x;
    }

    public static final byte[] bswap32(int x) {
        return new byte[]{
                (byte) (((x << 24) & 0xff000000) >> 24),
                (byte) (((x << 8) & 0x00ff0000) >> 16),
                (byte) (((x >> 8) & 0x0000ff00) >> 8),
                (byte) (((x >> 24) & 0x000000ff))
        };
    }

    public static final int reverse(int x) {
        byte[] a = bswap32(x);
        return  bytesToInt(a);
    }


    public  static  String GetNowTime()
    {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
        return  format.format(now);
    }

    public static void Print(String Cmd) {
        System.out.println(Cmd);
    }

    public  static byte[] ByteBufToBytes(ByteBuf buf) {
        int len = buf.readableBytes();
        byte[] data = new byte[len];
        buf.getBytes(0, data);
        return data;
    }

    public  static LocalDateTime GetDatetime(byte Second, byte Minute, byte Hour, byte Day, byte Month, int Year) {
        try {
            return LocalDateTime.of(Year, Month, Day, Hour, Minute, Second);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}