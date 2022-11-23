package StandTcp;

import StandTcp.StandTcpController.LogCmdInterface;
import StandTcp.StandTcpController.StandTCPControllerManager;
import StandTcp.StandTcpController.StandTCPServer;
import StandTcp.StandTcpController.TCPController;
import StandTcp.StandTcpProtocol.TAcsTool;
import StandTcp.StandTcpProtocol.TCPCmdStruct;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        AllClass allObj = new AllClass();
    }
}

class AllClass {
    StandTCPControllerManager AllController;

    public AllClass() {
        DB db = new DB();
        Statement stmt = db.con();
        MainForm form = new MainForm(stmt);
        AllController = new StandTCPControllerManager(form);
        //AllController.AddControl("1F4084","name");
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM loc_device WHERE type_id = 1");
            while (rs.next()) {
//                System.out.println(rs.getString("serial_no").length());
                AllController.AddControl(rs.getString("serial_no"),"name");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ServerTcpRun ServerTcp = new ServerTcpRun(AllController,8001);
        ServerTcp.start();
    }
    class ServerTcpRun implements Runnable {
        private int Port ;
        private Thread t;
        private StandTCPControllerManager allControllers;
        ServerTcpRun(StandTCPControllerManager Server,int port) {
            allControllers = Server;
            Port = port;
        }

        public void run() {
            try {
                StandTCPServer dserver = new StandTCPServer(Port, allControllers);
                dserver.run();
            } catch (Exception e) {
                System.out.println("ServerTcpRun." + e.getMessage());
            }
        }

        public void start() {
            if (t == null) {
                t = new Thread(this, "ServerTcpRun");
                t.start();
            }
        }
    }

    class MainForm extends JFrame implements LogCmdInterface {

        private static final String ENTRY_REPEAT = "6";
        private static final String EXIT_REPEAT = "7";
        private static final String ENTRY = "11";
        private static final String EXIT = "10";
        private static final String READER_ENTRY = "00";
        private static final String READER_EXIT = "01";
        JPanel ToolPanel = null;
        JScrollPane LogPanel = null;
        JTextArea LogText = null;
        JPanel ButtonPanel = null;
        Statement stmt = null;
        JButton ButtonPara;
        JButton ButtonOpenInput, ButtonOpenExit, ButtonClose, ButtonPullOpenDoor, ButtonClear;
        JButton Button_PullAddCard, Button_AddCard, Button_AddCards, Button_ClearCards, Button_AddTZ;
        Integer index = 0;
        String CardNumber = null;
        String name = null;
        LocalDateTime endTime = null;
        HashMap<String,Boolean> privelegeCards = new HashMap<String,Boolean>();
        public MainForm(Statement stmt) {
            this.stmt = stmt;
            setPrivelegeCards();
            AddFormObj();
            AddButtonClick();
        }
        public boolean isHaveInBlackList(String cardNumber){
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM card_black_list WHERE nfc LIKE '"+cardNumber+"'"); //@deprecated
                if (rs.next()) {
                    return true;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return false;
        }

        public void setPrivelegeCards(){
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM loc_user WHERE is_privelege = 1");
                if (rs.next()) {
                    privelegeCards.put(rs.getString("turniket_decimal"), true) ;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        public String getUserIin(String cardNumber){
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM loc_user WHERE turniket_decimal LIKE '"+cardNumber+"'");
                if (rs.next()) {
                    return rs.getString("iin");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        public Integer findIndex(String cardNumber){
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM `loc_pass_tourniquet` WHERE `card` LIKE '"+cardNumber+"'");
                if (rs.next()) {
                    return rs.getInt("id");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return 0;
        }

        public Integer getIndex(String cardNumber){
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM `loc_pass_tourniquet` WHERE `card` LIKE '"+cardNumber+"' AND `end_date_time` <= '"+getCurrentTime()+"' ORDER BY end_date_time ASC LIMIT 1; ");
                if (rs.next()) {
                    return rs.getInt("id");
                }
                rs = stmt.executeQuery("SELECT * FROM `loc_pass_tourniquet` ORDER BY id DESC LIMIT 1;");
                if (rs.next()) {
                    return rs.getInt("id")+1;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return 1;
        }

        public boolean insertCard(){
            try {
                int m = stmt.executeUpdate("INSERT INTO loc_pass_tourniquet(id, card,name,end_date_time) values("+index+",'"+CardNumber+"','"+name+"','"+getEndTime()+"')");
                if(m > 0) return true;
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
            return false;
        }

        public boolean insertEntryAndExit(Integer device_type_id, String user_iin, Integer pass_type_id, String date_time){
            try {
                int m = stmt.executeUpdate("INSERT INTO loc_pass_log(device_type_id, iin, pass_type_id, date_reg, device_reader_id) values("+device_type_id+",'"+user_iin+"',"+pass_type_id+",'"+date_time+"', 1)");
                if(m > 0) return true;
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
            return false;
        }

        public boolean updateCard(){
            try {
                int m = stmt.executeUpdate("UPDATE loc_pass_tourniquet SET card = '"+CardNumber+"', end_date_time = '"+getEndTime()+"' WHERE id ="+index);
                if(m > 0) return true;
            }catch (SQLException e){
                throw new RuntimeException(e);
            }
            return false;
        }

        public void ShowMsg(String Caption, String Msg) {
            String str = TAcsTool.GetNowTime() + " " + Caption + " = " + Msg;
            //System.out.println(str);
            AddLog(str);
        }

        public void ShowCmdHex(String Caption, byte[] data) {
            //String str = TAcsTool.Bytes2Hex(data);
            String str = new String(data);
            str = TAcsTool.GetNowTime() + " " + Caption + " = " + str;
            //System.out.println(str);
            AddLog(str);
        }

        public  void ShowHeartEvent(TCPCmdStruct.RHeartStatus HeartStatus){
            String str = "";
            str += "time:" + HeartStatus.Datetime.toString() + ",  ";
            str += "serial number:" + (HeartStatus.SerialNo) + ",  ";
            str += "input state:" + TAcsTool.Short2Hex(HeartStatus.Input) + ", ";
            str += "index:" + HeartStatus.IndexCmd+", ";
            str += "door status:" + TAcsTool.Bytes2Hex(HeartStatus.DoorStatus) + ",\r\n";
            ShowMsg("Heartbeat data", str);
            AddLog("\r\n");
        }

        public void ShowCardEvent(TCPCmdStruct.RCardEvent CardEvent){
            boolean addToDbEntryAndExit = true;
            String str = "";
            str += "time:" +    CardEvent.Datetime.toString() + ", ";
            String cardNo = CardEvent.CardNo;
            str += "card number:" +  cardNo + ", ";
            String reader = TAcsTool.Bytes2Hex(CardEvent.Reader);
            str += "reader: " + reader+", ";
            String eventType = Integer.toString(CardEvent.EventType);
            str += "event:" + eventType +" " + "\r\n";
            ShowMsg("Receive credit card record", str);
            TCPController Cntrl = AllController.GetController();
            if(eventType.equals(ENTRY) || eventType.equals(EXIT))
            {
//                if(isHaveInBlackList(cardNo)){
//                    //Карточку добавили в черный список. Надо удалить из памяты устройствы
//                    index = findIndex(cardNo);
//                    CardNumber = "0";
//                    endTime = LocalDateTime.now();
//                    updateCard();
//                }
            }
            if(eventType.equals("1") || eventType.equals("3")){
                //System.out.println(cardNo);
                //Карточку не нашли в памяты устройства и ищем в бд
                String iin = getUserIin(cardNo);
                if(iin != null){
                    //Если нашли
                    index = getIndex(cardNo);
                    CardNumber = cardNo;
                    name = "no name";
                    endTime = LocalDateTime.now().plusMonths(3);
                    Button_AddCard.doClick();
                    insertCard();
                    if(reader.equals(READER_ENTRY)){
                        ButtonOpenInput.doClick();
                    }else{
                        ButtonOpenExit.doClick();
                    }
                }else{
                    addToDbEntryAndExit = false;
                }
            }
            if((eventType.equals(ENTRY_REPEAT) || eventType.equals(EXIT_REPEAT)) && privelegeCards.containsKey(cardNo)){
                if(reader.equals(READER_ENTRY)){
                    ButtonOpenInput.doClick();
                }else{
                    ButtonOpenExit.doClick();
                }
            }
            else if(addToDbEntryAndExit){
                String iin = getUserIin(cardNo);
                Integer device_type_id = 1;
                Integer pass_type_id = 2;
                String date_time = Timestamp.valueOf(CardEvent.Datetime).toString();
                if(reader.equals(READER_ENTRY)){
                    pass_type_id = 1;
                }
                if(iin != null)
                    insertEntryAndExit(device_type_id, iin, pass_type_id, date_time);
            }
        }
        public void ShowAlarmEvent(TCPCmdStruct.RAlarmEvent AlarmEvent){
            String str = "";
            str += "time:" +    AlarmEvent.Datetime.toString() + ",";
            str += "event:" + Integer.toString(AlarmEvent.EventType)+ "\r\n";
            ShowMsg("Received alarm record", str);
        }

        public void SoftSendCmdHex(byte[] data) {
            ShowCmdHex("software send", data);
            AddLog("\r\n");
        }

        public void ContrlSendCmdHex(byte[] data) {
            ShowCmdHex("Controller sends", data);
        }

        public void AddLog(String str) {
           // LogText.insert(str + "\r\n", 0);
        }

        private void AddButtonClick() {
            ButtonClear.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    LogText.setText("");
                }
            });

            ButtonOpenExit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TCPController Cntrl = AllController.GetController();
                    boolean re = false;
                    if (Cntrl != null) {
                        re = Cntrl.OpenDoor(false, (byte) 1);
                        AddLog(Cntrl.CmdResult());
                    }
                }
            });

            ButtonOpenInput.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TCPController Cntrl = AllController.GetController();
                    boolean re = false;
                    if (Cntrl != null) {
                        re = Cntrl.OpenDoor(false, (byte) 0);
                        AddLog(Cntrl.CmdResult());
                    }
                }
            });

            ButtonClose.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TCPController Cntrl = AllController.GetController();
                    boolean re = false;
                    if (Cntrl != null) {
                        re = Cntrl.CloseDoor(false,(byte) 0);
                        AddLog(Cntrl.CmdResult());
                    }
                }
            });

            ButtonPullOpenDoor.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TCPController Cntrl = AllController.GetController();
                    if (Cntrl != null) {
                        Cntrl.OpenDoor(true,(byte) 0);
                    }
                }
            });

            Button_AddTZ.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TCPController Cntrl = AllController.GetController();
                    boolean re = false;
                    if (Cntrl != null) {
                        re = Cntrl.AddTimezone(false );
                        AddLog(Cntrl.CmdResult());
                    }
                }
            });

            Button_ClearCards.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TCPController Cntrl = AllController.GetController();
                    if (Cntrl != null) {
                        Cntrl.ClearCards(false );
                        AddLog(Cntrl.CmdResult());
                    } else AddLog("no active controller");
                }
            });

            Button_AddCard.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TCPController Cntrl = AllController.GetController();
                    if (Cntrl != null) {
                        Cntrl.AddCard(index-1, CardNumber, name, endTime, true);
                        AddLog(Cntrl.CmdResult());
                    }
                }
            });

            Button_PullAddCard.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TCPController Cntrl = AllController.GetController();
                    if (Cntrl != null) {
                        TCPCmdStruct.CardData data = new TCPCmdStruct.CardData();
                        data.Index = 0;
                        data.EndTime = LocalDateTime.now().plusYears(1);
                        data.Name = "Name pull";
                        data.TZ1 = 1;
                        data.TZ2 = 1;
                        data.Pin = "888888";
                        data.CardNo = 444855280;

                        Cntrl.PullCmdAddCard(20);

                        Cntrl.PullCmdAddCard(data);
                    }
                }
            });

            Button_AddCards.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TCPController Cntrl = AllController.GetController();
                    if (Cntrl == null) return;

                    TCPCmdStruct.CardData data = new TCPCmdStruct.CardData();
                    data.EndTime = LocalDateTime.now().plusYears(1);
                    data.TZ1 = 1;
                    data.TZ2 = 1;
                    data.TZ3 = 1;
                    data.TZ4 = 1;
                    data.Status = 1;
                    Boolean re;

                    int i;
                    int Len = 100;

                    for (i = 0; i < Len; i++) {
                        data.Index = i;
                        data.CardNo = i + 1 + 2000;
                        data.Pin = Integer.toString(i + 5000);
                        data.Name = "Name" + Integer.toString(i + 1);

                        re = Cntrl.AddCards(i == (Len - 1), data);

                        AddLog("Bulk add card " + Integer.toString(i));

                        if (!re) {
                            AddLog("Failed to add cards in batch " + Integer.toString(i));
                            break;
                        }
                    }

                    AddLog(Cntrl.CmdResult());
                }
            });

            ButtonPara.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TCPController Cntrl = AllController.GetController();
                    if (Cntrl != null) {
                        Cntrl.SetAllPara(true);
                        AddLog(Cntrl.CmdResult());
                    } else AddLog("no active controller");
                }
            });
        }

        private void AddFormObj() {
            ButtonClear = new JButton("clear");
            ToolPanel = new JPanel();
            ToolPanel.add(ButtonClear);
            this.add(ToolPanel, BorderLayout.NORTH);

            LogText = new JTextArea();
            LogPanel = new JScrollPane(LogText);

            ButtonOpenInput = new JButton("open door entry");
            ButtonOpenExit = new JButton("open door exit");
            ButtonClose = new JButton("close the door");
            Button_AddTZ = new JButton("add an opening time");
            Button_ClearCards = new JButton("delete all cards");
            Button_AddCard = new JButton("add a card");
            Button_AddCards = new JButton("Bulk add card");
            Button_PullAddCard = new JButton("Pull plus card");
            ButtonPullOpenDoor = new JButton("Pull open the door");
            ButtonPara = new JButton("Pull update parameters");

            ButtonPanel = new JPanel();
            ButtonPanel.add(ButtonOpenInput);
            ButtonPanel.add(ButtonOpenExit);
            ButtonPanel.add(ButtonClose);
            ButtonPanel.add(Button_AddTZ);
            ButtonPanel.add(ButtonPara);
            ButtonPanel.add(Button_ClearCards);
            ButtonPanel.add(Button_AddCard);
            ButtonPanel.add(Button_AddCards);
            ButtonPanel.add(Button_PullAddCard);
            ButtonPanel.add(ButtonPullOpenDoor);

            this.add(LogPanel);
            this.add(ButtonPanel, BorderLayout.SOUTH);

            this.setLocation(600, 400);
            this.setSize(1200, 750);
            this.setTitle("TCP Controller Communication Protocol Test java");
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setVisible(true);
        }

        public String getCurrentTime(){
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
            return timestamp.toString();
        }

        public String getEndTime(){
            Timestamp timestamp = Timestamp.valueOf(endTime);
            return timestamp.toString();
        }
    }
}