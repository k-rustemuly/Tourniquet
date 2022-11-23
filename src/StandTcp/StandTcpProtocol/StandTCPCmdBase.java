package StandTcp.StandTcpProtocol;

import struct.StructClass;
import struct.StructField;
import java.time.LocalDateTime;

public class StandTCPCmdBase extends TCPCmdStruct {
    protected static final byte Loc_Begin = 0;
    protected static final byte Loc_Temp = 1;
    protected static final byte Loc_Command = 2;
    protected static final byte Loc_Address = 3;
    protected static final byte Loc_DoorAddr = 4;
    protected static final byte Loc_Len = 5;
    protected static final byte Loc_Data = 7;

    @StructClass
    public static class RDoorPara
    {
        @StructField(order = 0)
        public byte OpenTimeL;
        @StructField(order = 1)
        public byte OutTime;
        @StructField(order = 2)
        public byte DoublePath;
        @StructField(order = 3)
        public byte ToolongAlarm;
        @StructField(order = 4)
        public byte OpenTimeH;
        @StructField(order = 5)
        public byte AlarmMask;
        @StructField(order = 6)
        public short AlarmTime;
        @StructField(order = 7)
        public byte MCards;
        @StructField(order = 8)
        public byte MCardsInOut;
    }

    @StructClass
    public static class RTimeZone //
    {
        @StructField(order = 0)
        public byte Index;
        @StructField(order = 1)
        public byte FrmHour;
        @StructField(order = 2)
        public byte FrmMinute;
        @StructField(order = 3)
        public byte ToHour;
        @StructField(order = 4)
        public byte ToMinute;
        @StructField(order = 5)
        public byte Week;
        @StructField(order = 6)
        public byte Indetify;
        @StructField(order = 7)
        public byte EndYear;
        @StructField(order = 8)
        public byte EndMonth;
        @StructField(order = 9)
        public byte EndDay;
        @StructField(order = 10)
        public byte Group;
    }
    @StructClass
    public static class RTCPHead{
        @StructField(order = 0)
        public byte stx;
        @StructField(order = 1)
        public byte rand;
        @StructField(order = 2)
        public byte cmd;
        @StructField(order = 3)
        public byte addr;
        @StructField(order = 4)
        public byte door;
        @StructField(order = 5)
        public short lenL;
        @StructField(order = 6)
        public short lenH;

    }
    @StructClass
    public static class RTCPHeartStatus {
        @StructField(order = 0)
        public byte stx;
        @StructField(order = 1)
        public byte temp;
        @StructField(order = 2)
        public byte cmd;
        @StructField(order = 3)
        public byte addr;
        @StructField(order = 4)
        public byte door;
        @StructField(order = 5)
        public short len;
        @StructField(order = 6)
        public byte N1;

        @StructField(order = 7)
        public byte year;
        @StructField(order = 8)
        public byte month;
        @StructField(order = 9)
        public byte day;
        @StructField(order = 10)
        public byte hour;
        @StructField(order = 11)
        public byte minute;
        @StructField(order = 12)
        public byte second;

        @StructField(order = 13)
        public byte DoorStatus;
        @StructField(order = 14)
        public byte CardNumInPack;
        @StructField(order = 15)
        public byte DirPass;
        @StructField(order = 16)
        public byte SystemOption;
        @StructField(order = 17)
        public byte ControlType;
        @StructField(order = 18)
        public byte RelayOut;

        @StructField(order = 19)
        public byte[] N3 = new byte[4];
        @StructField(order = 20)
        public byte Output;
        @StructField(order = 21)
        public byte Ver;
        @StructField(order = 22)
        public short OEMCODE;  //28
        @StructField(order = 23)
        public byte[] Serial = new byte[6];  // 34
        @StructField(order = 24)
        public short Input;

        @StructField(order = 25)
        public int IndexCmd;
        @StructField(order = 26)
        public byte CmdOK;  //31 41   14 + 27    27+6
    }

    @StructClass
    public static class RTCPAlarmEvent {
        @StructField(order = 0)
        public byte[] Head = new byte[7];
        @StructField(order = 1)
        public byte second;
        @StructField(order = 2)
        public byte minute;
        @StructField(order = 3)
        public byte hour;
        @StructField(order = 4)
        public byte day;
        @StructField(order = 5)
        public byte month;
        @StructField(order = 6)
        public byte year;

        @StructField(order = 7)
        public byte Event;
        @StructField(order = 8)
        public byte Door;
        @StructField(order = 9)
        public byte hasEvent;
        @StructField(order = 10)
        public byte index;  // 17
    }

    @StructClass
    public static class RTCPCardEvent {
        @StructField(order = 0)
        public byte[] Head = new byte[7];

        @StructField(order = 1)
        public int Card;
        @StructField(order = 2)
        public byte second;
        @StructField(order = 3)
        public byte minute;
        @StructField(order = 4)
        public byte hour;
        @StructField(order = 5)
        public byte day;
        @StructField(order = 6)
        public byte month;
        @StructField(order = 7)
        public byte year;

        @StructField(order = 8)
        public byte Event;
        @StructField(order = 9)
        public byte Door;
        @StructField(order = 10)
        public byte hasEvent;
        @StructField(order = 11)
        public byte index; // 14 + 7  = 21
    }

    //防潜返记录 card  Anti passback
    @StructClass
    public static class RTCPCardStatus {
        @StructField(order = 0)
        public byte[] Head = new byte[7];
        @StructField(order = 1)
        public short CardIndex;
        @StructField(order = 2)
        public byte AntiPassBackValue;
        @StructField(order = 3)
        public byte index;
        @StructField(order = 4)
        public byte hasEvent;//12
    }

    protected static RDoorPara BuildDoorPara(DoorPara data) {
        RDoorPara rs = new RDoorPara();
        rs.AlarmMask = data.AlarmMask;
        rs.OpenTimeL = (byte) (data.OpenTime & 0xff);
        rs.OutTime = data.OutTime;
        rs.DoublePath = TAcsTool.Bool2Byte(data.DoublePath);
        rs.ToolongAlarm = TAcsTool.Bool2Byte(data.ToolongAlarm);
        rs.OpenTimeH = (byte) ((data.OpenTime >> 8) & 0xff);
        rs.AlarmMask = data.AlarmMask;
        rs.AlarmTime = data.AlarmTime;
        rs.MCards = data.MCards;
        rs.MCardsInOut = data.MCardsInOut;
        return rs;
    }

    protected static RTimeZone BuildTimeZonePara(TimeZone data) {
        RTimeZone rs = new RTimeZone();
        rs.Index = data.Index;
        rs.FrmHour = data.FrmHour;
        rs.FrmMinute = data.FrmMinute;
        rs.ToHour = data.ToHour;
        rs.ToMinute = data.ToMinute;
        rs.Week = data.Week;

        if(data.Holiday)
            rs.Week  |= (byte)0x80;

        rs.Indetify = data.Indetify;
        if(data.APB)
            rs.Indetify |= (byte)0x80;

        rs.EndYear = (byte) (data.EndDate.getYear() - 2000);
        rs.EndMonth = (byte) data.EndDate.getMonth().getValue();
        rs.EndDay = (byte) data.EndDate.getDayOfMonth();
        rs.Group = data.Group;
        return rs;
    }

    // 基本函数
    protected static short SetBufCommand(byte command, byte[] Buffer) {
        //  LastCmd = command;
        Buffer[Loc_Begin] = 0x02;
        Buffer[Loc_Command] = command;
        Buffer[Loc_DoorAddr] = 1;
        Buffer[Loc_Len] = 0;
        Buffer[Loc_Len + 1] = 0;
        Buffer[Loc_Address] = (byte) 0xff;
        return Loc_Data;
    }

    protected static void SetBufAddr(byte Addr, byte[] Buffer) {
        Buffer[Loc_Address] = Addr;
    }

    protected static void SetBufDoorAddr(byte ADoorAddr, byte[] Buffer) {
        Buffer[Loc_DoorAddr] = ADoorAddr;
    }

    protected static short PutBuf(byte AData, short DataLen, byte[] Buffer) {
        Buffer[DataLen] = AData;
        DataLen++;
        return DataLen;
    }

    protected static short PutBufDate(LocalDateTime AData, short DataLen, byte[] Buffer) {
        if (AData.getYear() >= 2000)
            DataLen = PutBuf((byte) ((AData.getYear() - 2000) & 0xff), DataLen, Buffer);
        else
            DataLen = PutBuf((byte) (AData.getYear() & 0xff), DataLen, Buffer);
        DataLen = PutBuf((byte) (AData.getMonthValue()), DataLen, Buffer);
        DataLen = PutBuf((byte) (AData.getDayOfMonth()), DataLen, Buffer);
        return DataLen;
    }

    protected static void PutBufHourMinute(LocalDateTime AData, short DataLen, byte[] Buffer) {
        PutBuf((byte) AData.getHour(), DataLen, Buffer);
        PutBuf((byte) AData.getMinute(), DataLen, Buffer);
    }

    protected static short PutBufCard(int card, short DataLen, byte[] Buffer) {
        DataLen = PutBuf((byte) ((card) & 0xff), DataLen, Buffer);
        DataLen = PutBuf((byte) ((card >> 8) & 0xff), DataLen, Buffer);
        DataLen = PutBuf((byte) ((card >> 16) & 0xff), DataLen, Buffer);
        DataLen = PutBuf((byte) ((card >> 24) & 0xff), DataLen, Buffer);
        return DataLen;
    }

    protected static int GetBufPin4(String AData) {
        int i, len;
        byte[] p = new byte[8];
        byte[] v = new byte[4];

        if (AData == null || AData.isEmpty()) AData = "";
        byte[] ap = AData.getBytes();

        len = ap.length;
        for (i = 0; i < 8; i++) p[i] = (byte) 0xFF;

        if (len > 8) len = 8;
        for (i = 0; i < len; i++)
            p[i] = (byte) (ap[i] - 0x30);

        for (i = 0; i < 4; i++)
            v[i] = (byte) (((p[i * 2] << 4) & 0xF0) + (p[i * 2 + 1] & 0x0F));

        int value = (int) v[0] + (int) (v[1] << 8) + (int) (v[2] << 16) + (int) (v[3] << 24);
        return value;
    }

    protected static short PutBufPin2(String AData, short DataLen, byte[] Buffer) {
        if (AData == null || AData.isEmpty()) AData = "0";
        int vPin = Integer.valueOf(AData);

        DataLen = PutBuf((byte) ((vPin >> 8) & 0xff), DataLen, Buffer);
        DataLen = PutBuf((byte) (vPin & 0xff), DataLen, Buffer);
        return DataLen;
    }

    protected static short PutBufPin4(String AData, short DataLen, byte[] Buffer) {
        int v = GetBufPin4(AData);
        DataLen = PutBuf((byte) (v & 0xff), DataLen, Buffer);
        DataLen = PutBuf((byte) ((v >> 8) & 0xff), DataLen, Buffer);
        DataLen = PutBuf((byte) ((v >> 16) & 0xff), DataLen, Buffer);
        DataLen = PutBuf((byte) ((v >> 24) & 0xff), DataLen, Buffer);
        return DataLen;
    }

    protected static short PutBufNameString(String AData, short Maxlen, short DataLen, byte[] Buffer) {
        int len;
        if (AData == null || AData.isEmpty()) AData = "";
        byte[] Value;
        try {
            Value = AData.getBytes("GB2312");
        } catch (Exception e) {
            Value = AData.getBytes();
        }

        len = Value.length;
        if (len > Maxlen) len = Maxlen;
        System.arraycopy(Value, 0, Buffer, DataLen, len);

        DataLen += Maxlen;
        return DataLen;
    }

    protected static short BuildCS(short DataLen, byte[] Buffer) {
        int i, datalen;
        byte OutBufferCS = 0;
        datalen = DataLen - Loc_Data;
        Buffer[Loc_Len] = (byte) (datalen & 0xFF);
        Buffer[Loc_Len + 1] = (byte) ((datalen >> 8) & 0xFF);

        for (i = 0; i < DataLen; i++)
            OutBufferCS = (byte) (OutBufferCS ^ Buffer[i]);

        Buffer[DataLen] = OutBufferCS;
        Buffer[DataLen + 1] = 0x03;
        DataLen += (short) 2;
        return DataLen;
    }

    // 接收数据校验
    private static Boolean CheckCs(byte[] buff, int loc) {
        int i;
        if (buff[loc] != 0x02) return false;
        if (buff[loc + 3] == 0x03)
            buff[loc + 3] = (byte) (0x03 + loc);

        int Bufferlen = (buff[Loc_Len + 1 + loc] &0xff)  + ( buff[Loc_Len + 0 + loc] & 0xff)* 256 + Loc_Data + 2;
        if (Bufferlen > buff.length) return false;
        if (buff[Bufferlen - 1 + loc] != 0x03) return false;

        Boolean result = false;
        byte cs = 0;
        int len = Bufferlen - 2;
        for (i = 0; i < len; i++) {
            cs ^= buff[i + loc];
        }
        result = (cs == buff[Bufferlen + loc - 2]);
        return result;
    }

    public static Boolean CheckRxDataCS(byte[] buffRX) {
        if (buffRX.length < 4) return false;
        boolean re = CheckCs(buffRX, 0);
        return re;
    }

    public static Boolean CheckRxCmdAck(byte[] buffRX, byte LastCmd, boolean CheckAck) {
        if (buffRX.length < 9) return false;
        if (CheckAck)
            return (LastCmd == buffRX[2] && buffRX[7] == 0x06);
        else
            return (LastCmd == buffRX[2]);
    }

    // 转换 卡信息
    protected static CardData MakeCardData(CardData1Door data) {
        CardData value = new CardData();
        value.FunctionOption = data.FunctionOption;
        value.Index = data.Index;
        value.Name = data.Name;
        value.Pin = data.Pin;
        value.CardNo = data.CardNo;
        value.TZ1 = (byte) (data.TZ & 0x0ff);
        value.TZ2 = (byte) ((data.TZ >> 8) & 0x0ff);
        value.Status = data.Status;
        value.EndTime = data.EndTime;
        return value;
    }

    protected static CardData MakeCardData(CardData2Door data) {
        CardData value = new CardData();
        value.FunctionOption = data.FunctionOption;
        value.Index = data.Index;
        value.Name = data.Name;
        value.Pin = data.Pin;
        value.CardNo = data.CardNo;
        value.TZ1 = (byte) (data.TZ1 & 0x0ff);
        value.TZ2 = (byte) ((data.TZ1 >> 8) & 0x0ff);
        value.TZ3 = (byte) (data.TZ2 & 0x0ff);
        value.TZ4 = (byte) ((data.TZ2 >> 8) & 0x0ff);
        value.Status = data.Status;
        value.EndTime = data.EndTime;
        return value;
    }

    protected static CardData MakeCardData(CardData4Door data) {
        CardData value = new CardData();
        value.FunctionOption = data.FunctionOption;
        value.Index = data.Index;
        value.Name = data.Name;
        value.Pin = data.Pin;
        value.CardNo = data.CardNo;
        value.TZ1 = data.TZ1;
        value.TZ2 = data.TZ2;
        value.TZ3 = data.TZ3;
        value.TZ4 = data.TZ4;
        value.Status = data.Status;
        value.EndTime = data.EndTime;
        return value;
    }
}
