package com.meeting.rmi.meeting;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI接口
 * 
 * @author GZ
 * @version 0.1
 */
public interface MeetDefine extends Remote {
    /**
     * 处理用户注册命令
     * 
     * @param param 命令参数
     * @return 命令执行结果
     * @throws RemoteException
     */
    public String userRegister(String param) throws RemoteException;

    /**
     * 处理用户登录命令
     * 
     * @param param 命令参数
     * @return 命令执行结果
     * @throws RemoteException
     */
    public String userLogin(String param) throws RemoteException;

    /**
     * 处理添加会议命令
     * 
     * @param param 命令参数
     * @return 命令执行结果
     * @throws RemoteException
     */
    public String addMeeting(String param) throws RemoteException;

    /**
     * 处理查询会议命令
     * 
     * @param param 命令参数
     * @return 命令执行结果
     * @throws RemoteException
     */
    public String queryMeeting(String param) throws RemoteException;

    /**
     * 处理删除会议命令
     * 
     * @param param 命令参数
     * @return 命令执行结果
     * @throws RemoteException
     */
    public String deleteMeeting(String param) throws RemoteException;

    /**
     * 处理清除会议命令
     * 
     * @param param 命令参数
     * @return 命令执行结果
     * @throws RemoteException
     */
    public String clearMeeting(String param) throws RemoteException;
}