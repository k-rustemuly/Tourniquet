package StandTcp.StandTcpController;

import StandTcp.StandTcpProtocol.StandTCPCmd;
import StandTcp.StandTcpProtocol.TAcsTool;

import StandTcp.StandTcpProtocol.TCPCmdStruct;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.time.LocalTime;
import java.util.*;


public class StandTCPControllerManager implements CardStatusInterface {

    ArrayList<TCPController> list = new ArrayList<>();
    protected LogCmdInterface LogShow;

    public TCPFirstLinkHandler LinkHandler() {
        return new TCPFirstLinkHandler(this);
    }

    public StandTCPControllerManager(LogCmdInterface log) {
        LogShow = log;
    }

    private void Print(byte[] buf, String CmdName) {
        String str = TAcsTool.Bytes2Hex(buf);
        System.out.println(LocalTime.now().toString() + " " + CmdName + " = " + str);
    }

    public void SetCardStatus(String Serial, int group, short Index, byte status) {
        for (TCPController a : list) {
            if (a.Group == group)
                if (!a.SerialNo.equals(Serial)) {
                    a.pullCmd.AddPullCmd(true, StandTCPCmd.SetCardStatus(Index, status));
                }
        }
    }

    public TCPController GetController() {
        for (TCPController a : list) {
            if (a.WorkStatus != TCPWorkStatus.Close) {
                return a;
            }
        }
        return null;
    }

    public void AddControl(String SerialNo, String name) {
        TCPController acs = new TCPController(this, LogShow);
        acs.SerialNo = SerialNo;
        acs.Name = name;
        list.add(acs);
    }

    public TCPController TcpLinkFirst(byte[] data, ChannelHandlerContext ctx) {
        if (data == null) return null;
        if (!StandTCPCmd.CheckRxDataCS(data)) return null;
        if (data[2] != (byte) 0x56) return null;

        TCPCmdStruct.RHeartStatus HeartStatus = StandTCPCmd.HeartBuf2Struct(data);
        String serial = HeartStatus.SerialNo;

        for (TCPController a : list) {
            if (a.SerialNo.equals(serial)) {
                a.ChannelCtx = ctx;
                a.HeartStatus = HeartStatus;
                return a;
            }
        }
        return null;
    }
}

class TCPFirstLinkHandler extends SimpleChannelInboundHandler<Object> {
    StandTCPControllerManager allControllers;

    public TCPFirstLinkHandler(StandTCPControllerManager value) {
        allControllers = value;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        int len = in.readableBytes();
        if (len <= 0) return;

        byte[] data = TAcsTool.ByteBufToBytes(in);
        allControllers.LogShow.ContrlSendCmdHex(data);

        TCPController acs = allControllers.TcpLinkFirst(data, ctx);
        if (acs == null) return;

        TCPLinkHandle handleAcs = new TCPLinkHandle(acs);
        acs.AnswerHeart(true, 0,true );
        ctx.pipeline().removeFirst();
        ctx.pipeline().addLast(handleAcs);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
       // cause.printStackTrace();
        ctx.close();
    }
}

class TCPLinkHandle extends SimpleChannelInboundHandler<Object> {
    private TCPController Controller;

    public TCPLinkHandle(TCPController value) {
        Controller = value;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        byte[] data = TAcsTool.ByteBufToBytes(in);
        Controller.LogShow.ContrlSendCmdHex(data);

        if (Controller == null) return;

        byte[] cmdbuf = Controller.TcpLink(data, ctx);

        if (cmdbuf == null) return;

        ByteBuf buf = Unpooled.wrappedBuffer(cmdbuf);

        Controller.LogShow.SoftSendCmdHex(cmdbuf);
        ctx.writeAndFlush(buf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.pipeline().removeFirst();
        ctx.close();
    }
}



