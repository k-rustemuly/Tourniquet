package StandTcp.StandTcpProtocol;

import java.time.LocalDateTime;
import java.util.Arrays;

public final class StandTCPCmd extends StandTCPCmdBase {
    private static byte[] TcpCmdA(byte cmd) {
        short DataLen = 0;
        byte[] bufTX = new byte[32];
        DataLen = SetBufCommand(cmd, bufTX);
        DataLen = BuildCS(DataLen, bufTX);
        return Arrays.copyOf(bufTX, DataLen);
    }

    private static byte[] TcpCmdB(byte cmd, byte door, byte data1, byte data2) {
        short DataLen = 0;
        byte[] bufTX = new byte[32];
        DataLen = SetBufCommand(cmd, bufTX);
        SetBufDoorAddr((byte) (door + 1), bufTX);

        DataLen = PutBuf(data1, DataLen, bufTX);
        DataLen = PutBuf(data2, DataLen, bufTX);

        DataLen = BuildCS(DataLen, bufTX);
        return Arrays.copyOf(bufTX, DataLen);
    }

    private static byte[] TcpCmdDoor(byte cmd, byte door) {
        short DataLen = 0;
        byte[] bufTX = new byte[32];
        DataLen = SetBufCommand(cmd, bufTX);
        SetBufDoorAddr((byte) (door + 1), bufTX);
        DataLen = BuildCS(DataLen, bufTX);
        return Arrays.copyOf(bufTX, DataLen);
    }

    //for Anti passback
    public static byte[] SetCardStatus(short Index, byte status) {
        short DataLen = 0;
        byte[] bufTX = new byte[32];
        DataLen = SetBufCommand((byte) 0x66, bufTX);
        DataLen = PutBuf((byte) (Index & 0xff), DataLen, bufTX);
        DataLen = PutBuf((byte) ((Index >> 8) & 0xFF), DataLen, bufTX);
        DataLen = PutBuf((byte) (status), DataLen, bufTX);
        DataLen = BuildCS(DataLen, bufTX);
        return Arrays.copyOf(bufTX, DataLen);
    }

    public static byte[] AddCard1Door(CardData1Door data) {
        CardData value = MakeCardData(data);
        return AddCard(value);
    }

    public static byte[] AddCard2Door(CardData2Door data) {
        CardData value = MakeCardData(data);
        return AddCard(value);
    }

    public static byte[] AddCard4Door(CardData4Door data) {
        CardData value = MakeCardData(data);
        return AddCard(value);
    }

    // 卡片 Card
    public static byte[] ClearAllCards() {
        return TcpCmdA((byte) 0x17);
    }

    public static byte[] DelCard(short Index) {
        //return TcpCmdB((byte) 0x16, (byte) 0, (byte) (Index & 0xff), (byte) ((Index >> 8) & 0xFF));
        CardData data = new CardData();
        data.Index = Index;
        return AddCard(data);
    }

    public static byte[] AddCard(CardData data) {
        short DataLen = 0;
        byte[] bufTX = new byte[512];
        Boolean isCardorPIN, isEndDate, isShowName;

        isCardorPIN = (data.FunctionOption & 0x30) > 0;
        isEndDate = (data.FunctionOption & 0x01) == 0x01;
        isShowName = (data.FunctionOption & 0x08) == 0x08;

        DataLen = SetBufCommand((byte) 0x62, bufTX);
        DataLen = PutBuf((byte) (data.Index & 0xff), DataLen, bufTX);
        DataLen = PutBuf((byte) ((data.Index >> 8) & 0xff), DataLen, bufTX);
        DataLen = PutBufCard(data.CardNo, DataLen, bufTX);

        if (isCardorPIN) {
            DataLen = PutBufPin4(data.Pin, DataLen, bufTX);
        } else
            DataLen = PutBufPin2(data.Pin, DataLen, bufTX);

        DataLen = PutBuf((data.TZ1), DataLen, bufTX);
        DataLen = PutBuf((data.TZ2), DataLen, bufTX);
        DataLen = PutBuf((data.TZ3), DataLen, bufTX);
        DataLen = PutBuf((data.TZ4), DataLen, bufTX);

        if (isEndDate) {
            DataLen = PutBufDate(data.EndTime, DataLen, bufTX);
            DataLen = PutBuf((byte) (data.EndTime.getHour()), DataLen, bufTX);
            DataLen = PutBuf((byte) (data.EndTime.getMinute()), DataLen, bufTX);
        } else {
            DataLen = PutBuf((byte) 0, DataLen, bufTX);
            DataLen = PutBuf(((byte) 0), DataLen, bufTX);
            DataLen = PutBuf(((byte) 0), DataLen, bufTX);
            DataLen = PutBuf(((byte) 0), DataLen, bufTX);
            DataLen = PutBuf((data.Status), DataLen, bufTX);
        }

        if (isShowName)
            DataLen = PutBufNameString(data.Name, (short) 8, DataLen, bufTX);

        DataLen = BuildCS(DataLen, bufTX);
        return Arrays.copyOf(bufTX, DataLen);
    }

    public static short AddCards(byte[] BufferTX, short DataLen, short PackIndex, byte CardofPack, CardData data) {
        BufferTX[Loc_Command] = (byte) 0x88;
        BufferTX[Loc_Data + 0] = (byte) (((PackIndex + 1) & 0xFF)); //
        BufferTX[Loc_Data + 1] = (byte) ((((PackIndex + 1) >> 8) & 0xFF));
        BufferTX[Loc_Data + 2] = (byte) (CardofPack + 1); //

        if (CardofPack == 0) {
            SetBufCommand((byte) 0x88, BufferTX);
            DataLen = Loc_Data + 3;
            DataLen = PutBuf((byte) (data.Index & 0xFF), DataLen, BufferTX);
            DataLen = PutBuf((byte) ((data.Index >> 8) & 0xFF), DataLen, BufferTX);
        }
        DataLen = SendEmpTcpOne(BufferTX, DataLen, data);
        return DataLen;
    }

    private static short SendEmpTcpOne(byte[] BufferTX, short DataLen, CardData data) {
        Boolean isCardorPIN, isEndDate, isShowName;

        isCardorPIN = (data.FunctionOption & 0x30) > 0;
        isEndDate = (data.FunctionOption & 0x01) == 0x01;
        isShowName = (data.FunctionOption & 0x08) == 0x08;

        DataLen = PutBufCard(data.CardNo, DataLen, BufferTX);
        if (isCardorPIN) {
            DataLen = PutBufPin4(data.Pin, DataLen, BufferTX);
        } else
            DataLen = PutBufPin2(data.Pin, DataLen, BufferTX);

        DataLen = PutBuf((data.TZ1), DataLen, BufferTX);
        DataLen = PutBuf((data.TZ2), DataLen, BufferTX);
        DataLen = PutBuf((data.TZ3), DataLen, BufferTX);
        DataLen = PutBuf((data.TZ4), DataLen, BufferTX);

        if (isEndDate) {
            DataLen = PutBufDate(data.EndTime, DataLen, BufferTX);
            DataLen = PutBuf((byte) (data.EndTime.getHour()), DataLen, BufferTX);
            DataLen = PutBuf((byte) (data.EndTime.getMinute()), DataLen, BufferTX);
        }
        if (isShowName)
            DataLen = PutBufNameString(data.Name, (short) 8, DataLen, BufferTX);
        return DataLen;
    }

    public static byte[] AddCardsEndSendCards(byte[] BufferTX, short DataLen) {
        DataLen = BuildCS(DataLen, BufferTX);
        return Arrays.copyOf(BufferTX, DataLen);
    }

    public static Boolean AddCardsCheckResult(short PackIndex, byte[] BufferRX) {
        if (BufferRX[Loc_Len + 1] > 1) {
            return BufferRX[Loc_Data + 0] * 256 + BufferRX[Loc_Data + 1] == (PackIndex + 1);
        }
        return false;
    }

    public static byte[] Restart() {
        return TcpCmdA((byte) 0x05);
    }

    public static byte[] Reset() {
        return TcpCmdA((byte) 0x04);
    }

    public static byte[] SetTime(LocalDateTime dt) {
        short DataLen = 0;
        byte[] bufTX = new byte[512];
        DataLen = SetBufCommand((byte) 0x07, bufTX);

        DataLen = PutBuf((byte) (dt.getSecond()), DataLen, bufTX);
        DataLen = PutBuf((byte) (dt.getMinute()), DataLen, bufTX);
        DataLen = PutBuf((byte) (dt.getHour()), DataLen, bufTX);
        DataLen = PutBuf((byte) (dt.getDayOfWeek().getValue() + 1), DataLen, bufTX);
        DataLen = PutBuf((byte) (dt.getDayOfMonth()), DataLen, bufTX);
        DataLen = PutBuf((byte) dt.getDayOfMonth(), DataLen, bufTX);

        if (dt.getYear() >= 2000) {
            DataLen = PutBuf((byte) ((dt.getYear() - 2000) & 0xff), DataLen, bufTX);
        } else {
            DataLen = PutBuf((byte) (dt.getYear() & 0xff), DataLen, bufTX);
        }

        DataLen = BuildCS(DataLen, bufTX);
        return Arrays.copyOf(bufTX, DataLen);
    }

    public static byte[] OpenDoor(byte index) {
        return TcpCmdDoor((byte) 0x02C, index);
    }

    public static byte[] CloseDoor(byte index) {
        return TcpCmdDoor((byte) 0x02E, index);
    }

    public static byte[] OpenDoorLong(byte index) {
        return TcpCmdDoor((byte) 0x02D, index);
    }

    public static byte[] LockDoor(byte index, Boolean Lock) {
        return TcpCmdB((byte) 0x2F, index, TAcsTool.Bool2Byte(Lock), TAcsTool.Bool2Byte(Lock));
    }

    public static byte[] SetAlarm(Boolean AClose, Boolean ALong) {
        return TcpCmdB((byte) 0x18, (byte) 0, TAcsTool.Bool2Byte(AClose), TAcsTool.Bool2Byte(ALong));
    }

    public static byte[] SetFire(Boolean AClose, Boolean ALong) {
        return TcpCmdB((byte) 0x19, (byte) 0, TAcsTool.Bool2Byte(AClose), TAcsTool.Bool2Byte(ALong));
    }

    public static byte[] DelTimeZone(byte Door) {
        return TcpCmdDoor((byte) 0x0F, (byte) Door);
    }

    public static byte[] SetPass(byte index, byte Reader, Boolean Pass) {
        short DataLen = 0;
        byte[] bufTX = new byte[32];
        DataLen = SetBufCommand((byte) 0x5A, bufTX);
        SetBufDoorAddr((byte) (index + 1), bufTX);
        DataLen = PutBuf((byte) (0), DataLen, bufTX);
        DataLen = PutBuf((byte) (Reader), DataLen, bufTX);
        DataLen = PutBuf((byte) (0), DataLen, bufTX);
        DataLen = PutBuf((TAcsTool.Bool2Byte(!Pass)), DataLen, bufTX);
        DataLen = BuildCS(DataLen, bufTX);
        return Arrays.copyOf(bufTX, DataLen);
    }

    public static byte[] AddTimeZone(byte Door, TimeZone Data) {
        byte[] BufTX = new byte[512];
        RTimeZone bdata = BuildTimeZonePara(Data);
        byte[] buf = StructToBytes(bdata);
        short DataLen = SetBufCommand((byte) 0x0D, BufTX);
        SetBufDoorAddr((byte) (Door + 1), BufTX);
        System.arraycopy(buf, 0, BufTX, DataLen, buf.length);
        DataLen += buf.length;
        DataLen = BuildCS(DataLen, BufTX);
        return Arrays.copyOf(BufTX, DataLen);
    }

    public static byte[] SendTo485(byte[] value) {
        short DataLen = 0;
        byte[] BufTX = new byte[512];
        DataLen = SetBufCommand((byte) 0xB1, BufTX);
        System.arraycopy(value, 0, BufTX, DataLen, value.length);
        DataLen += value.length;
        DataLen = BuildCS(DataLen, BufTX);
        return Arrays.copyOf(BufTX, DataLen);
    }

    public static byte[] SetControl(byte SystemOption, short FireTime, short AlarmTime, String DuressPIN, byte LockEach) {
        short DataLen = 0;
        byte[] bufTX = new byte[512];

        Boolean isCardorPIN;
        isCardorPIN = (SystemOption & 0x30) > 0;

        DataLen = SetBufCommand((byte) 0x63, bufTX);
        DataLen = PutBuf((byte) (LockEach), DataLen, bufTX);
        DataLen = PutBuf((byte) (FireTime & 0xFF), DataLen, bufTX);
        DataLen = PutBuf((byte) ((FireTime >> 8) & 0xFF), DataLen, bufTX);
        DataLen = PutBuf((byte) (AlarmTime & 0xFF), DataLen, bufTX);
        DataLen = PutBuf((byte) ((AlarmTime >> 8) & 0xFF), DataLen, bufTX);

        if (isCardorPIN) {
            PutBufPin4(DuressPIN, DataLen, bufTX);
        } else
            PutBufPin2(DuressPIN, DataLen, bufTX);

        DataLen = BuildCS(DataLen, bufTX);
        return Arrays.copyOf(bufTX, DataLen);
    }

    public static byte[] SetDoor(byte Door, DoorPara Data) {
        byte[] BufTX = new byte[512];
        RDoorPara bdata = BuildDoorPara(Data);
        byte[] buf = StructToBytes(bdata);
        short DataLen = SetBufCommand((byte) 0x61, BufTX);
        System.arraycopy(buf, 0, BufTX, DataLen, buf.length);
        DataLen += buf.length;
        DataLen = BuildCS(DataLen, BufTX);
        return Arrays.copyOf(BufTX, DataLen);
    }

    public static byte[] DelHoliday() {
        return TcpCmdA((byte) 0x0C);
    }

    public static byte[] AddHoliday(byte Index, LocalDateTime frmdate, LocalDateTime todate) {
        short DataLen = 0;
        byte[] bufTX = new byte[32];
        DataLen = SetBufCommand((byte) 0x09, bufTX);
        DataLen = PutBuf(Index, DataLen, bufTX);
        DataLen = PutBufDate(frmdate, DataLen, bufTX);
        DataLen = PutBufDate(todate, DataLen, bufTX);
        DataLen = BuildCS(DataLen, bufTX);
        return Arrays.copyOf(bufTX, DataLen);
    }

    public static byte[] AckHeart(int indexCmd, short OEMCode, byte[] rec) {
        byte[] bufTX = new byte[512];
        short DataLen;
        DataLen = SetBufCommand((byte) 0x56, bufTX);
        DataLen = PutBuf((byte) ((OEMCode >> 8) & 0xFF), DataLen, bufTX);
        DataLen = PutBuf((byte) (OEMCode & 0xFF), DataLen, bufTX);

        DataLen = PutBuf((byte) (0), DataLen, bufTX);
        DataLen = PutBuf((byte) (0), DataLen, bufTX);

        DataLen = PutBuf((byte) ((indexCmd >> 24) & 0xFF), DataLen, bufTX);
        DataLen = PutBuf((byte) ((indexCmd >> 16) & 0xFF), DataLen, bufTX);
        DataLen = PutBuf((byte) ((indexCmd >> 8) & 0xFF), DataLen, bufTX);
        DataLen = PutBuf((byte) (indexCmd & 0xFF), DataLen, bufTX);

        if (rec != null) {
            System.arraycopy(rec, 0, bufTX, DataLen, rec.length);
            DataLen += rec.length;
        }

        DataLen = BuildCS(DataLen, bufTX);
        return Arrays.copyOf(bufTX, DataLen);
    }

    public static byte[] AnsHistoryEvent(byte Command, byte index) {
        short DataLen = 0;
        byte[] bufTX = new byte[32];
        DataLen = SetBufCommand(Command, bufTX);
        DataLen = PutBuf(index, DataLen, bufTX);
        DataLen = BuildCS(DataLen, bufTX);
        return Arrays.copyOf(bufTX, DataLen);
    }

    public static RHeartStatus HeartBuf2Struct(byte[] buffer) {
        RHeartStatus Event = new RHeartStatus();

        int len = 43;
        if (buffer.length < len) len = buffer.length;

        byte[] head = Arrays.copyOf(buffer, len);

        RTCPHeartStatus Status = new RTCPHeartStatus();
        Status = (RTCPHeartStatus) ByteToStruct(Status, head);

        Event.SerialNo = new String(Status.Serial);
        Event.CardNumInPack = Status.CardNumInPack;
        Event.DoorStatus = Status.DoorStatus;
        Event.Version = Integer.toString(Status.Ver);
        Event.SystemOption = Status.SystemOption;
        Event.OEMCode = Integer.toUnsignedString(Status.OEMCODE & 0xffffffff);
        Event.Datetime = TAcsTool.GetDatetime(Status.second, Status.minute, Status.hour, Status.day, Status.month, Status.year + 2000);
        Event.Version = Integer.toString(Status.Ver);

        Event.Input =  (short) (Status.Input & 0xffff);

        Event.Online = true;
        Event.IndexCmd = (int) (Status.IndexCmd & 0xffffffff);
        Event.CmdOK = Status.CmdOK;
        return Event;
    }

    public static RCardEvent CardEventBuf2Struct(byte[] buffer) {
        RCardEvent Event = new RCardEvent();

        int len = 21;
        if (buffer.length < len) len = buffer.length;
        byte[] event = Arrays.copyOf(buffer, len);

        RTCPCardEvent CardEvent = new RTCPCardEvent();
        CardEvent = (RTCPCardEvent) ByteToStruct(CardEvent, event);
        CardEvent.Card = TAcsTool.reverse(CardEvent.Card);
        Event.CardNo = Integer.toUnsignedString(CardEvent.Card);
        Event.EventType = (byte) (CardEvent.Event & 0x7F);
        Event.Reader = (byte) ((CardEvent.Event & 0x80) >> 7);
        Event.Door = CardEvent.Door;
        Event.ReturnIndex = CardEvent.index;
        Event.Datetime = TAcsTool.GetDatetime(CardEvent.second, CardEvent.minute, CardEvent.hour, CardEvent.day, CardEvent.month, CardEvent.year + 2000);
        return Event;
    }

    public static RAlarmEvent AlarmEventBuf2Struct(byte[] buffer) {
        RAlarmEvent Event = new RAlarmEvent();

        int len = 17;
        if (buffer.length < len) len = buffer.length;
        byte[] event = Arrays.copyOf(buffer, len);

        RTCPAlarmEvent AlarmEvent = new RTCPAlarmEvent();

        AlarmEvent = (RTCPAlarmEvent) ByteToStruct(AlarmEvent, event);
        Event.EventType = (byte) (AlarmEvent.Event & 0x7F);
        Event.Door = (byte) (AlarmEvent.Door & 0x0F);
        Event.ReturnIndex = AlarmEvent.index;
        Event.Datetime = TAcsTool.GetDatetime(AlarmEvent.second, AlarmEvent.minute, AlarmEvent.hour, AlarmEvent.day, AlarmEvent.month, AlarmEvent.year + 2000);
        return Event;
    }

    public static RAcsCardStatus CardStatusBuf2Struct(byte[] buffer) {
        RAcsCardStatus Event = new RAcsCardStatus();

        int len = 12;
        if (buffer.length < len) len = buffer.length;
        byte[] event = Arrays.copyOf(buffer, len);

        RTCPCardStatus CardStatus = new RTCPCardStatus();
        CardStatus = (RTCPCardStatus) ByteToStruct(CardStatus, event);

        Event.CardIndex = CardStatus.CardIndex;
        Event.AntiPassBackValue = CardStatus.AntiPassBackValue;
        Event.ReturnIndex = CardStatus.index;
        return Event;
    }
}
