package StandTcp.StandTcpProtocol;

import struct.JavaStruct;

import java.nio.ByteOrder;
import java.time.*;
import java.time.LocalDateTime;

public class TCPCmdStruct {
    public static class DoorPara //
    {
        public short OpenTime = 5;
        public byte OutTime = 5;
        public boolean DoublePath = true;
        public boolean ToolongAlarm = true;
        public byte AlarmMask = 0;
        public short AlarmTime = 10;
        public byte MCards;
        public byte MCardsInOut;
    }

    public static class TimeZone //
    {
        public byte Index = 0;
        public byte FrmHour = 0;
        public byte FrmMinute = 0;
        public byte ToHour = 23;
        public byte ToMinute = 59;
        public byte Week = (byte) 255;
        public byte Indetify = (byte) 0;
        public boolean APB = false;
        public boolean Holiday = true;
        public LocalDate EndDate = LocalDate.now().plusYears(10);
        public byte Group = 0;
    }

    public static class CardData1Door {
        public byte FunctionOption;
        public int Index;
        public String Name, Pin;
        public int CardNo;
        public short TZ;
        public byte Status;
        public LocalDateTime EndTime;
    }

    public static class CardData2Door {
        public byte FunctionOption;
        public int Index;
        public String Name, Pin;
        public int CardNo;
        public short TZ1, TZ2;
        public byte Status;
        public LocalDateTime EndTime;
    }

    public static class CardData4Door {
        public byte FunctionOption;
        public int Index;
        public String Name, Pin;
        public int CardNo;
        public byte TZ1, TZ2, TZ3, TZ4, Status;
        public LocalDateTime EndTime;
    }

    public static class CardData {
        public byte FunctionOption;
        public int Index;
        public String Name, Pin;
        public int CardNo;
        public byte TZ1, TZ2, TZ3, TZ4, Status;
        public LocalDateTime EndTime;
    }

    public static class RHeartStatus {
        public LocalDateTime Datetime;
        public String SerialNo;
        public byte DoorStatus;
        public short Input;
        public Boolean Online;
        public byte TModel;
        public byte SystemOption;
        public byte CardNumInPack;

        public String Version;
        public String OEMCode;
        public int IndexCmd;
        public byte CmdOK;
    }

    public static class RCardEvent {
        public LocalDateTime Datetime;
        public String CardNo;
        public byte Reader;
        public byte Door;
        public byte EventType;
        public int ReturnIndex;
    }

    public static class RAlarmEvent {
        public LocalDateTime Datetime;
        public byte Door;
        public byte EventType;
        public int ReturnIndex;
    }

    public static class RAcsCardStatus {
        public byte EventType;
        public short CardIndex;
        public byte AntiPassBackValue;
        public int ReturnIndex;
    }

    public static byte[] StructToBytes(Object structObj) {
        byte[] bytes;
        try {
            bytes = JavaStruct.pack(structObj, ByteOrder.LITTLE_ENDIAN);
            return bytes;
        } catch (Exception e) {
            return new byte[0];
        }
    }

    protected static Object ByteToStruct(Object obj, byte[] bytes) {
        try {
            JavaStruct.unpack(obj, bytes, ByteOrder.LITTLE_ENDIAN);
            return obj;
        } catch (Exception e) {
            return null;
        }
    }
}

