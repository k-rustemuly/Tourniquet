package StandTcp;

import StandTcp.StandTcpProtocol.StandTCPCmd;
import StandTcp.StandTcpProtocol.TAcsTool;
import StandTcp.StandTcpProtocol.TCPCmdStruct;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class StandTCPTest {

    private void Print(byte[] buf, String CmdName) {
        String str = TAcsTool.Bytes2Hex(buf);
        System.out.println(LocalTime.now().toString() + " " + CmdName + " = " + str);
    }

    public void Test() {
        StandTCPCmd cmd = new StandTCPCmd();
        byte[] buffer;

        // Restart
        buffer = cmd.Restart();
        Print(buffer, "Restart");

        //Reset
        buffer = cmd.Reset();
        Print(buffer, "Reset");

        //SetTime
        buffer = cmd.SetTime(LocalDateTime.now());
        Print(buffer, "SetTime");

        //OpenDoor
        buffer = cmd.OpenDoor((byte) 0);
        Print(buffer, "Opendoor");

        //CloseDoor
        buffer = cmd.CloseDoor((byte) 0);
        Print(buffer, "Closedoor");

        //OpenDoorLong
        buffer = cmd.OpenDoorLong((byte) 0);
        Print(buffer, "OpenDoorLong");

        //LockDoor
        buffer = cmd.LockDoor((byte) 0, true);
        Print(buffer, "LockDoor");

        //SetAlarm
        buffer = cmd.SetAlarm(true, false);
        Print(buffer, "SetAlarm");

        //SetFire
        buffer = cmd.SetFire(true, false);
        Print(buffer, "SetFire");

        //SetPass
        buffer = cmd.SetPass((byte) 0, (byte) (0), false);
        Print(buffer, "SetPass");

        //DelTimeZone
        buffer = cmd.DelTimeZone((byte) 0);
        Print(buffer, "DelTimeZone");

        TCPCmdStruct.TimeZone timezone = new TCPCmdStruct.TimeZone();
        timezone.EndDate = LocalDate.now();
        buffer = cmd.AddTimeZone((byte) 0, timezone);
        Print(buffer, "AddTimeZone");

        //SendTo485
        buffer = cmd.SendTo485(new byte[10]);
        Print(buffer, "SendTo485");

        //DelHoliday
        buffer = cmd.DelHoliday();
        Print(buffer, "DelHoliday");

        //AddHoliday
        buffer = cmd.AddHoliday((byte) 0, LocalDateTime.now(), LocalDateTime.now());
        Print(buffer, "AddHoliday");

        //SetCardStatus
        buffer = cmd.SetCardStatus((short) 0, (byte) 0);
        Print(buffer, "SetCardStatus");

        TCPCmdStruct.DoorPara data = new TCPCmdStruct.DoorPara();
        buffer = cmd.SetDoor((byte) 0, data);
        Print(buffer, "SetDoor");

        //SetControl
        buffer = cmd.SetControl((byte) 0, (short) 100, (short) 10, "", (byte) 0);
        Print(buffer, "SetControl");

        //ClearAllCards
        buffer = cmd.ClearAllCards();
        Print(buffer, "ClearAllCards");

        //DelCard
        buffer = cmd.DelCard((short) 0);
        Print(buffer, "DelCard");

        //AddCard1Door
        TCPCmdStruct.CardData1Door d1 = new TCPCmdStruct.CardData1Door();
        d1.FunctionOption = 0;
        d1.Index = 0;
        d1.Name = "Name";
        d1.Pin = "987654";
        d1.CardNo = 123;
        d1.TZ  = 0x0001;
        d1.Status = 1;
        d1.EndTime = LocalDateTime.now();
        buffer = cmd.AddCard1Door(d1);
        Print(buffer, "AddCard1Door");
    }
}
