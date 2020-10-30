package com.meeting.rmi;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

import com.meeting.rmi.meeting.MeetImp;

/**
 * RMI服务端
 * 
 * @author GZ
 * @version 0.1
 */
public class MeetServer {
    static String serverName = "Meeting"; // RMI服务名称
    static String serverUrl; // 服务RMI IP
    static int port; // 服务运行端口

    /**
     * 服务器运行逻辑
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            /**
             * 启动服务并等待
             */
            serverUrl = args[0]; // 获取服务器运行IP地址
            port = Integer.parseInt(args[1]); // 获取服务运行端口
            new MeetServer().server(); // 启动服务
            while (true) { // 阻塞等待
                Thread.sleep(1000);
            }
        } catch (UnknownHostException | ExportException e) { // 异常处理
            System.err.println("Invalid Host or Invalid Port!");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Usage: java MeetServer [url] [port]");
        } catch (AlreadyBoundException e) {
            System.err.println("Port Already In Use!");
        } catch (RemoteException | MalformedURLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动RMI服务
     * 
     * @throws RemoteException
     * @throws java.rmi.AlreadyBoundException
     * @throws MalformedURLException
     * @throws UnknownHostException
     * @throws ExportException
     */
    public void server() throws RemoteException, java.rmi.AlreadyBoundException, MalformedURLException,
            UnknownHostException, ExportException {
        /**
         * 启动RMI服务
         */
        LocateRegistry.createRegistry(port); // 注册远程对象,向客户端提供远程对象服务。
        String bindStr = String.format("rmi://%s:%d/%s", serverUrl, port, serverName); // 格式化RMI URL
        Naming.bind(bindStr, new MeetImp()); // MeetImp服务绑定该URL
        System.out.println("Server Running!");
    }

}
