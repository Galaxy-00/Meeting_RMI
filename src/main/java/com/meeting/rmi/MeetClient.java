package com.meeting.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import com.meeting.rmi.meeting.MeetDefine;

/**
 * MeetClient 客户端
 * 
 * @author GZ
 * @version 0.1
 */
public class MeetClient {
    static String serverName = "Meeting"; // RMI服务名称
    static String serverUrl; // 服务器地址
    static int port; // 服务端口号

    /**
     * 启动主程序
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        new MeetClient().client(args); // 启动客户端
    }

    /**
     * 客户端主逻辑
     * 
     * @param args 命令行参数
     */
    public void client(String[] args) {
        try {
            String lookupStr = String.format("rmi://%s:%s/%s", args[0], args[1], serverName); // RMI服务URL
            MeetDefine meet = (MeetDefine) Naming.lookup(lookupStr); // RMI查找服务
            // 登录前显示的辅助信息
            String helpInfoBeforeLogin = "RMI Menu:\n "
                    + "\t1.register\n\t\targuments: java [clientname] [servername] [portnumber] register [username] [password]\n"
                    + "\t2.login\n\t\targumnets: java[clientname] [servername] [portnumber] login [username] [password]\n"
                    + "\t3.add\n\t\targuments: java [clientname] [servername] [portnumber] add [username] [password] [otherusername] [start] [end] [title]\n"
                    + "\t4.query\n\t\targuments: java [clientname] [servername] [portnumber] query [username] [password] [start] [end]\n"
                    + "\t5.delete\n\t\targuments: java [clientname] [servername] [portnumber] delete [username] [password] [meetingid]\n"
                    + "\t6.clear\n\t\targuments: java [clientname] [servername] [portnumber] clear [username] [password]\n"
                    + "\t7.help\n\t\targuments: java [clientname] [servername] [portnumber] help";
            // 登陆后显示的辅助信息
            String helpInfoAfterLogin = "RMI Menu:\n "
                    + "\t1.add\n\t\targuments: add [otherusername] [start] [end] [title]\n"
                    + "\t2.query\n\t\targuments: query [start] [end]\n"
                    + "\t3.delete\n\t\targuments: delete [meetingid]\n" + "\t4.clear\n\t\targuments: clear\n"
                    + "\t5.help\n\t\targuments: help\n" + "\t6.quit\n\t\targuments: quit";

            /**
             * 处理用户注册和登录操作
             */
            String res = "";
            String cmd = args[2];
            if (!cmd.equals("help")) { // 非help命令
                /**
                 * 根据命令选择执行方法
                 */
                String param = ""; // 构造命令参数
                for (int i = 3; i < args.length; i++) {
                    param += args[i] + " ";
                }
                switch (cmd) {
                    case "register":
                        res = meet.userRegister(param);
                        break;
                    case "login":
                        res = meet.userLogin(param);
                        break;
                    case "add":
                        res = meet.addMeeting(param);
                        break;
                    case "query":
                        res = meet.queryMeeting(param);
                        break;
                    case "delete":
                        res = meet.deleteMeeting(param);
                        break;
                    case "clear":
                        res = meet.clearMeeting(param);
                        break;
                    default:
                        res = "Unsupported Command!";
                }
            } else {
                res = helpInfoBeforeLogin;
            }
            System.out.println(res);

            // 登录, 注册未成功, 或未执行注册或登录操作 退出
            if (!res.startsWith("Login Success") && !res.startsWith("Register Success")) {
                return;
            }

            /**
             * 登录成功, 循环等待用户操作
             */
            String userName = args[3];
            String password = args[4];
            System.out.println(helpInfoAfterLogin);
            System.out.println("Please Input Command:");
            Scanner sc = new Scanner(System.in); // 用于获取用户标准输入
            boolean running_flag = true;
            while (running_flag) {
                System.out.print(">");
                String msg = sc.nextLine(); // 获取用户输入的命令
                String[] msgs = msg.split(" "); // 处理输入的命令
                String msgCmd = msgs[0];
                String msgParam = userName + " " + password + " "; // 构造函数传入的参数
                for (int i = 1; i < msgs.length; i++) {
                    msgParam += msgs[i] + " ";
                }
                switch (msgCmd) { // 根据命令选择方法执行
                    case "add":
                        res = meet.addMeeting(msgParam);
                        break;
                    case "query":
                        res = meet.queryMeeting(msgParam);
                        break;
                    case "delete":
                        res = meet.deleteMeeting(msgParam);
                        break;
                    case "clear":
                        res = meet.clearMeeting(msgParam);
                        break;
                    case "help":
                        res = helpInfoAfterLogin;
                        break;
                    case "quit":
                        res = "bye!";
                        running_flag = false;
                        break;
                    default:
                        res = "Unsupported Command!";
                }
                System.out.println(res);
            }
            sc.close(); // 关闭输入流
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Invaild Command Format! \nType java com.meeting.rmi.MeetClient <ip> <port> help for Help");
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            System.err.print(e.getMessage());
        }
    }
}
