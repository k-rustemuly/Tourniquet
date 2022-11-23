package StandTcp.StandTcpController;


import StandTcp.StandTcpProtocol.StandTCPCmd;
import StandTcp.StandTcpProtocol.TAcsTool;
import StandTcp.StandTcpProtocol.TCPCmdStruct;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

interface CardStatusInterface {
    void SetCardStatus(String Serial, int group, short Index, byte status);
}


public class TCPController {
    private byte[] BufferTX = new byte[512];
    private byte[] BufferRX = new byte[512];

    protected PullCmd pullCmd = new PullCmd();
    private Object TcpAckStatus = new Object();

    private byte LastCmd = 0;
    public TCPWorkStatus WorkStatus = TCPWorkStatus.Finished;
    protected LogCmdInterface LogShow;
    protected CardStatusInterface SendUpCardStatus = null;
    protected ChannelHandlerContext ChannelCtx = null;
    protected TCPCmdStruct.RHeartStatus HeartStatus;

    public String SerialNo;
    public String Name;
    public int Group;

    public TCPController(CardStatusInterface updateCardStatus, LogCmdInterface logShow) {
        LogShow = logShow;
        SendUpCardStatus = updateCardStatus;
    }

    public String CmdResult() {
        switch (WorkStatus) {
            case Close:
                return "no connection！";

            case Linked:
                return "connected！";

            case Sending:
                return "at work！";

            case OutTime:
                return "time out！";

            case Faild:
                return "fail！";

            case Finished:
                return "success！";
        }
        return "";
    }

    private Boolean CheckStatus() {
        if (WorkStatus == TCPWorkStatus.Close) return false;

        if (ChannelCtx == null) {
            WorkStatus = TCPWorkStatus.Close;
            return false;
        }

        if (!ChannelCtx.channel().isActive()) {
            WorkStatus = TCPWorkStatus.Close;
            return false;
        }

        if (WorkStatus == TCPWorkStatus.Sending) return false;

        WorkStatus = TCPWorkStatus.Sending;
        return true;
    }

    private boolean SendToTcp(byte[] cmdbuf) {
        boolean rs = this.CheckStatus();
        if (!rs) return false;

        ByteBuf buf = Unpooled.wrappedBuffer(cmdbuf);

        LogShow.SoftSendCmdHex(cmdbuf);
        ChannelCtx.writeAndFlush(buf);
        LastCmd = cmdbuf[2];
        return true;
    }

    private boolean BeginWait(long mstime, byte cmd, boolean CheckAck) {
        long futureTime = System.currentTimeMillis() + mstime;
        long remaining = mstime;

        synchronized (TcpAckStatus) {
            try {
                TcpAckStatus.wait(mstime);
                remaining = futureTime - System.currentTimeMillis();

                if (remaining <= 0) {
                    WorkStatus = TCPWorkStatus.OutTime;
                    return false;
                } else {
                    if (StandTCPCmd.CheckRxCmdAck(BufferRX, LastCmd, CheckAck)) {
                        WorkStatus = TCPWorkStatus.Finished;
                    } else {
                        WorkStatus = TCPWorkStatus.Faild;
                    }
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                WorkStatus = TCPWorkStatus.OutTime;
                return false;
            }
        }
    }

    private void OnTcpAckNotify() {
        synchronized (TcpAckStatus) {
            TcpAckStatus.notify();
        }
    }

    public void AnswerHeart(boolean CanPull, int IndexCmd, boolean lastOK) {
        byte[] cmd = null;
        PullCmdData pullcmd;
        int index = 0;

        if (CanPull) {
            pullcmd = pullCmd.GetNextPullCmd(IndexCmd, lastOK);
            if (pullcmd != null) {
                cmd = pullcmd.CmdBytes;
                index = pullcmd.ID;
            }
        }

        byte[] cmdbuf = StandTCPCmd.AckHeart(index, (short) 0, cmd);
        if (cmdbuf != null) {
            ByteBuf buf = Unpooled.wrappedBuffer(cmdbuf);
            ChannelCtx.writeAndFlush(buf);
            LogShow.SoftSendCmdHex(cmdbuf);
            WorkStatus = TCPWorkStatus.Linked;
        }
    }

    private byte[] RevDataController(byte[] data) {
        byte[] cmdbuf = null;

        byte cmd = data[2];
        switch (cmd) {
            case 0x56:
                TCPCmdStruct.RHeartStatus vStatus = StandTCPCmd.HeartBuf2Struct(data);
                if (vStatus != null) {
                    HeartStatus = vStatus;
                    AnswerHeart(true, HeartStatus.IndexCmd, HeartStatus.CmdOK == 1);
                    LogShow.ShowHeartEvent(vStatus);
                } else
                    AnswerHeart(false, 0, false);
                break;

            case 0x53:
                TCPCmdStruct.RCardEvent vCardEvent = StandTCPCmd.CardEventBuf2Struct(data);
                if (vCardEvent != null) {
                    LogShow.ShowCardEvent(vCardEvent);
                    cmdbuf = StandTCPCmd.AnsHistoryEvent(cmd, (byte) vCardEvent.ReturnIndex);
                }
                break;

            case 0x54:
                TCPCmdStruct.RAlarmEvent vAlarmEvent = StandTCPCmd.AlarmEventBuf2Struct(data);
                if (vAlarmEvent != null) {
                    LogShow.ShowAlarmEvent(vAlarmEvent);
                    cmdbuf = StandTCPCmd.AnsHistoryEvent(cmd, (byte) vAlarmEvent.ReturnIndex);
                }
                break;

            case 0x52:
                TCPCmdStruct.RAcsCardStatus vCardStatusEvent = StandTCPCmd.CardStatusBuf2Struct(data);
                if (vCardStatusEvent != null) {
                    cmdbuf = StandTCPCmd.AnsHistoryEvent(cmd, (byte) vCardStatusEvent.ReturnIndex);
                    if (SendUpCardStatus != null)
                        SendUpCardStatus.SetCardStatus(this.SerialNo, this.Group, vCardStatusEvent.CardIndex, vCardStatusEvent.AntiPassBackValue);
                }
                break;

            default:
                OnTcpAckNotify();
                break;
        }
        return cmdbuf;
    }

    protected byte[] TcpLink(byte[] data, ChannelHandlerContext ctx) {
        ChannelCtx = ctx;
        if (data == null) return null;

        if (!StandTCPCmd.CheckRxDataCS(data)) return null;

        int len = data.length;
        if (len > BufferRX.length) len = BufferRX.length;
        System.arraycopy(data, 0, BufferRX, 0, len);

        byte[] cmdbuf = RevDataController(data);

        if (cmdbuf != null) {
            ByteBuf buf = Unpooled.wrappedBuffer(cmdbuf);
            ChannelCtx.writeAndFlush(buf);
            LogShow.SoftSendCmdHex(cmdbuf);
        }
        return null;
    }

    public boolean OpenDoor(boolean pull, byte index) {
        byte[] cmdbuf = StandTCPCmd.OpenDoor((byte) index);

        if (pull) {
            pullCmd.AddPullCmd(true, cmdbuf);
            return true;
        }
        boolean rs = SendToTcp(cmdbuf);
        if (rs)
            rs = BeginWait(100, cmdbuf[2], true);

        return rs;
    }

    public boolean CloseDoor(boolean pull, byte index) {
        byte[] cmdbuf = StandTCPCmd.CloseDoor((byte) 0);

        if (pull) {
            pullCmd.AddPullCmd(true, cmdbuf);
            return true;
        }

        boolean rs = SendToTcp(cmdbuf);
        if (rs)
            rs = BeginWait(100, cmdbuf[2], true);

        return rs;
    }

    public boolean DelTimeZone(boolean pull, byte Door) {
        byte[] cmdbuf = StandTCPCmd.DelTimeZone((byte) Door);
        if (pull) {
            pullCmd.AddPullCmd(false, cmdbuf);
            return true;
        }

        boolean rs = SendToTcp(cmdbuf);
        if (rs)
            rs = BeginWait(100, cmdbuf[2], true);
        return rs;
    }

    public boolean AddTimezone(boolean pull) {
        boolean rs = false;
        TCPCmdStruct.TimeZone data = new TCPCmdStruct.TimeZone();

        data.Index = 0;
        data.FrmHour = 0;
        data.FrmMinute = 0;
        data.ToHour = 23;
        data.ToMinute = 59;
        data.Week = (byte) 0xff;
        data.Indetify = 1;
        data.APB = true;
        data.EndDate = LocalDate.now().plusYears(10);
        data.Group = 0;

        byte[] cmdbuf = StandTCPCmd.AddTimeZone((byte) 0, data);
        if (pull) {
            pullCmd.AddPullCmd(false, cmdbuf);
            return true;
        }

        rs = SendToTcp(cmdbuf);
        if (rs)
            rs = BeginWait(100, cmdbuf[2], true);
        return rs;
    }

    public boolean SetDoor(boolean pull) {
        TCPCmdStruct.DoorPara data = new TCPCmdStruct.DoorPara();
        byte[] cmdbuf = StandTCPCmd.SetDoor((byte) 0, data);

        if (pull) {
            pullCmd.AddPullCmd(false, cmdbuf);
            return true;
        }

        boolean rs = SendToTcp(cmdbuf);
        if (rs)
            rs = BeginWait(100, cmdbuf[2], true);
        return rs;
    }

    public boolean SetControl(boolean pull, short FireTime, short AlarmTime, String DuressPIN, byte LockEach) {
        byte[] cmdbuf = StandTCPCmd.SetControl(HeartStatus.SystemOption, FireTime, AlarmTime, DuressPIN, LockEach);

        if (pull) {
            pullCmd.AddPullCmd(false, cmdbuf);
            return true;
        }

        boolean rs = SendToTcp(cmdbuf);
        if (rs)
            rs = BeginWait(100, cmdbuf[2], true);
        return rs;
    }

    public boolean DelHoliday(boolean pull) {
        byte[] cmdbuf = StandTCPCmd.DelHoliday();
        if (pull) {
            pullCmd.AddPullCmd(false, cmdbuf);
            return true;
        }

        boolean rs = SendToTcp(cmdbuf);
        if (rs)
            rs = BeginWait(150, cmdbuf[2], true);
        return rs;
    }

    public boolean AddHoliday(boolean pull, byte Index, LocalDateTime frmdate, LocalDateTime todate) {
        byte[] cmdbuf = StandTCPCmd.AddHoliday(Index, frmdate, todate);
        if (pull) {
            pullCmd.AddPullCmd(false, cmdbuf);
            return true;
        }

        boolean rs = SendToTcp(cmdbuf);
        if (rs)
            rs = BeginWait(150, cmdbuf[2], true);
        return rs;
    }

    public boolean SetAllPara(boolean pull) {
        boolean rs;

        rs = SetDoor(pull);
        LogShow.ShowMsg("Update gate parameters", String.valueOf(rs));

        rs = SetControl(pull, (short) 100, (short) 10, "ABCD", (byte) 0);
        LogShow.ShowMsg("Update controller parameters", String.valueOf(rs));

        rs = DelTimeZone(pull, (byte) 0);
        LogShow.ShowMsg("delete all opening hours", String.valueOf(rs));

        rs = AddTimezone(pull);
        LogShow.ShowMsg("increase opening hours", String.valueOf(rs));

        rs = DelHoliday(pull);
        LogShow.ShowMsg("delete holiday", String.valueOf(rs));

        rs = AddHoliday(pull, (byte) 1, LocalDateTime.now(), LocalDateTime.now());
        LogShow.ShowMsg("add holiday", String.valueOf(rs));

        return rs;
    }

    public boolean ClearCards(boolean pull) {
        byte[] cmdbuf = StandTCPCmd.ClearAllCards();
        if (pull) {
            pullCmd.AddPullCmd(false, cmdbuf);
            return true;
        }
        boolean rs = SendToTcp(cmdbuf);
        if (rs)
            rs = BeginWait(2000, cmdbuf[2], true);

        return rs;
    }

    public boolean AddCard(Integer index, String CardNo, String name, LocalDateTime endTime, boolean pull) {
        TCPCmdStruct.CardData data = new TCPCmdStruct.CardData();

        data.Index = index;
        data.EndTime = endTime;
        data.Name = name;
        data.Pin = "1234";
        data.TZ1 = 1;
        data.TZ2 = 1;
        data.CardNo = Integer.parseUnsignedInt(CardNo);
        data.FunctionOption = HeartStatus.SystemOption;
        data.Status = 1;
        byte[] cmdbuf = StandTCPCmd.AddCard(data);
        if (pull) {
            pullCmd.AddPullCmd(false, cmdbuf);
            return true;
        }

        boolean rs = SendToTcp(cmdbuf);
        if (rs)
            rs = BeginWait(100, cmdbuf[2], true);

        return rs;
    }

    private short PackIndex = 0;
    private byte CardofPack = 0;
    private short CardsDataLen = 0;

    public Boolean AddCards(Boolean isLastRecord, TCPCmdStruct.CardData data) {
        int CardIndex = data.Index;
        byte CardNumInPack = HeartStatus.CardNumInPack;

        boolean rs;

        if ((CardNumInPack < 15) || (CardNumInPack > 45)) return false;

        PackIndex = (short) (CardIndex / CardNumInPack);
        CardofPack = (byte) (CardIndex % CardNumInPack);

        data.FunctionOption = HeartStatus.SystemOption;
        CardsDataLen = StandTCPCmd.AddCards(this.BufferTX, CardsDataLen, PackIndex, CardofPack, data);

        if (((CardofPack + 1) >= CardNumInPack) || (isLastRecord)) {
            byte[] cmdbuf = StandTCPCmd.AddCardsEndSendCards(BufferTX, CardsDataLen);

            CardsDataLen = 0;
            rs = this.CheckStatus();

            if (!rs) return false;

            rs = SendToTcp(cmdbuf);
            if (rs)
                rs = BeginWait(1800, cmdbuf[2], false);

            if (rs) {
                rs = StandTCPCmd.AddCardsCheckResult(PackIndex, BufferRX);
            }
            return rs;
        }
        return true;
    }


    public void PullCmdAddCard(TCPCmdStruct.CardData data) {
        data.Status = 1;
        data.FunctionOption = HeartStatus.SystemOption;
        byte[] cmdbuf = StandTCPCmd.AddCard(data);
        pullCmd.AddPullCmd(false, cmdbuf);
    }

    public void PullCmdAddCard(int num) {
        int i;
        TCPCmdStruct.CardData data = new TCPCmdStruct.CardData();
        data.FunctionOption = HeartStatus.SystemOption;

        for (i = 0; i < num; i++) {
            data.Index = i;
            data.EndTime = LocalDateTime.now().plusYears(1);
            data.Name = "pname" + Integer.toString(i + 1);
            data.TZ1 = 1;
            data.TZ2 = 1;
            data.TZ3 = 1;
            data.TZ4 = 1;
            data.CardNo = 2000 + i;
            data.Pin = 8000 + Integer.toString(i + 1);

            byte[] cmdbuf = StandTCPCmd.AddCard(data);
            pullCmd.AddPullCmd(false, cmdbuf);
        }
    }
}


enum TCPWorkStatus {
    Close,
    Linked,
    Sending,
    OutTime,
    Faild,
    Finished
}

class PullCmdData {
    public int ID;
    public byte[] CmdBytes;
}

class PullCmd {
    private int IDNow = 0;

    private List<PullCmdData> PullCmdList = new ArrayList<>();

    public void AddPullCmd(boolean priority, byte[] Cmd) {
        PullCmdData value = new PullCmdData();
        value.CmdBytes = Cmd;
        IDNow++;
        value.ID = IDNow;
        if (priority) {
            PullCmdList.add(0, value);
        } else
            PullCmdList.add(value);
    }

    public PullCmdData GetNextPullCmd(int lastid, boolean lastOK) {
        PullCmdData cmd;

        if (lastid > 0 && lastOK) {
            for (int i = 0; i < PullCmdList.size(); i++) {
                cmd = PullCmdList.get(i);
                if (cmd.ID == lastid) {
                    PullCmdList.remove(i);
                    break;
                }
            }
        }

        if (PullCmdList.size() > 0) {
            cmd = PullCmdList.get(0);
            return cmd;
        } else return null;
    }
}
