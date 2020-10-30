package com.meeting.rmi.meeting;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import com.meeting.rmi.database.MySQLRunner;

/**
 * 远程对象实现类 extends UnicastRemoteObject
 * 
 * @author GZ
 * @version 0.1
 */
public class MeetImp extends UnicastRemoteObject implements MeetDefine {

    /**
     * 用于java序列化
     */
    private static final long serialVersionUID = 1L;
    /**
     * 时间标准格式
     */
    String timeFormat = "yyyy-MM-dd-HH:mm:ss";

    /**
     * 显示构造函数
     * 
     * @throws RemoteException
     */
    public MeetImp() throws RemoteException {
        super();
    }

    /**
     * 检查用户密码是否正确
     * 
     * @param userName 用户名字
     * @param password 用户密码
     * @return 匹配返回true, 否则false
     */
    public boolean checkPassword(String userName, String password) {
        /**
         * 构造SQL, params, 执行查询操作
         */
        try {
            String SQL = "SELECT password from user WHERE name = ?";
            Object[] params = new Object[] { userName };
            List<Map<String, Object>> list = MySQLRunner.executeQuery(SQL, params); // 利用MySQLRunner静态方法执行查询
            String rPassword = String.valueOf(list.get(0).get("password")); // 获取该用户的密码
            if (rPassword.equals(password)) { // 查询密码匹配
                return true;
            }
            return false;
        } catch (IndexOutOfBoundsException e) { // 用户不存在
            return false;
        }
    }

    /**
     * 时间戳转换为以format为格式的时间
     * 
     * @param ts     事件戳
     * @param format 时间格式
     * @return 时间戳按格式转换后的时间
     */
    public static String timestamp2Str(String ts, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format); // 根据format创建formatter
        return sdf.format(new Date(Long.parseLong(String.valueOf(ts + "000"))));
    }

    /**
     * 将时间转换为时间戳
     * 
     * @param timeString 时间
     * @param format     时间格式
     * @return 时间按格式转化后得到的时间戳
     * @throws ParseException
     */
    public static String str2Timestamp(String timeString, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format); // 根据format创建formatter
        String ts = ""; // 返回的时间戳
        if (!timeString.equals("")) { // 若没有传入时间, 则返回当前时间的时间戳
            ts = String.valueOf(sdf.parse(timeString).getTime() / 1000); // 除以1000, 以秒为单位
        } else {
            ts = String.valueOf(System.currentTimeMillis() / 1000);
        }
        return ts;
    }

    /**
     * 处理用户注册命令
     * 
     * @param param 命令参数
     * @return 命令执行结果
     * @throws RemoteException
     */
    @Override
    public String userRegister(String param) throws RemoteException {
        String ret = "";
        try {
            /**
             * 解析param, 构造SQL, 执行数据库更新操作
             */
            String[] paramStrings = param.split(" ");
            String userName = paramStrings[0];
            String password = paramStrings[1];

            String SQL = "INSERT INTO user (name, password) VALUES (?, ?);";
            Object[] params = new Object[] { userName, password };
            if (MySQLRunner.executeUpdate(SQL, params)) {
                ret = String.format("Register Success! Welcome, %s!", userName);
            } else {
                ret = "User Already Exits!";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            ret = "Invaild Command Format!\nUsage: java [clientname] [servername] [portnumber] register [username] [password]";
        }

        return ret;
    }

    /**
     * 处理用户登录命令
     * 
     * @param param 命令参数
     * @return 命令执行结果
     * @throws RemoteException
     */
    @Override
    public String userLogin(String param) throws RemoteException {
        String ret = "";
        try {
            /**
             * 解析param, 构造SQL, 执行数据库更新操作
             */
            String[] params = param.split(" ");
            String userName = params[0];
            String password = params[1];

            if (checkPassword(userName, password)) { // 查询用户密码是否正确
                ret = String.format("Login Success! Welcome, %s!", userName);
            } else {
                ret = "Wrong Password or User Not Exits!";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            ret = "Invaild Command Format!\n"
                    + "Usage: \n\tjava [clientname] [servername] [portnumber] login [username] [password]";
        }

        return ret;
    }

    /**
     * 处理添加会议命令
     * 
     * @param param 命令参数
     * @return 命令执行结果
     * @throws RemoteException
     */
    @Override
    public String addMeeting(String param) throws RemoteException {
        String ret = "";
        try {
            /**
             * 解析param, 构造SQL, 执行数据库更新操作
             */
            String[] paramStrings = param.split(" ");
            String userName = paramStrings[0];
            String password = paramStrings[1];
            String otherUserName = paramStrings[2];
            String startTime = paramStrings[3];
            String endTime = paramStrings[4];
            String title = paramStrings[5];
            String tsStartTime = str2Timestamp(startTime, timeFormat);
            String tsEndTime = str2Timestamp(endTime, timeFormat);

            // System.out.println(endTime);
            // System.out.println(tsEndTime);

            if (checkPassword(userName, password)) { // 用户密码是否正确
                if (!otherUserName.equals(userName)) { // 用户不能和自己开会
                    /**
                     * 检测是否存在时间冲突的会议
                     */
                    String QuerySQL = "SELECT * FROM meeting WHERE ((((start_time <= ? AND ? <= end_time) OR (start_time <= ? AND ? <= end_time)) AND (user1_name = ? OR user2_name = ?)) OR (((start_time <= ? AND ? <= end_time) OR (start_time <= ? AND ? <= end_time)) AND (user1_name = ? OR user2_name = ?))) OR "
                            + "((((? <= start_time AND start_time <= ?) OR (? <= end_time AND end_time <= ?)) AND (user1_name = ? OR user2_name = ?)) OR (((? <= start_time AND start_time <= ?) OR (? <= end_time AND end_time <= ?)) AND (user1_name = ? OR user2_name = ?)))"; // 要添加的会议与存在的会议是否有重叠
                    Object[] qparams = new Object[] { tsStartTime, tsStartTime, tsEndTime, tsEndTime, userName,
                            userName, tsStartTime, tsStartTime, tsEndTime, tsEndTime, otherUserName, otherUserName,
                            tsStartTime, tsEndTime, tsStartTime, tsEndTime, userName, userName, tsStartTime, tsEndTime,
                            tsStartTime, tsEndTime, otherUserName, otherUserName };
                    List<Map<String, Object>> list = MySQLRunner.executeQuery(QuerySQL, qparams); // 利用MySQLRunner静态方法执行查询
                    if (list.isEmpty()) { // 不存在时间重叠的会议
                        /**
                         * 插入会议数据
                         */
                        String InsertSQL = "INSERT INTO meeting (title, user1_name, user2_name, start_time, end_time) VALUES (?, ?, ?, ?, ?);";
                        Object[] params = new Object[] { title, userName, otherUserName, tsStartTime, tsEndTime };
                        if (MySQLRunner.executeUpdate(InsertSQL, params)) { // 利用MySQLRunner静态方法进行更新, 成功时返回true, 否则false
                            ret = "Add Meeting Success!";
                        } else {
                            ret = "Add Meeting Failed! (Maybe User2 Not Exits!)";
                        }
                    } else { // 存在时间冲突的会议
                        ret = "Exist Meeting Conflicted!";
                    }
                } else { // 用户1和用户2相同
                    ret = "User1 and User2 must be different!";
                }
            } else { // 密码不正确
                ret = "Wrong Password or User1 Not Exits!";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // e.printStackTrace();
            ret = "Invaild Command Format!\n"
                    + "Usage: \n\tUnlogin: java [clientname] [servername] [portnumber] add [username] [password] [otherusername] [start] [end] [title]\n"
                    + "\tLogin: add [start] [end] [title]";
        } catch (ParseException e) {
            ret = "Invaild Time Format\nFormat: " + timeFormat;
        }

        return ret;
    }

    /**
     * 处理查询会议命令
     * 
     * @param param 命令参数
     * @return 命令执行结果
     * @throws RemoteException
     */
    @Override
    public String queryMeeting(String param) throws RemoteException {
        String ret = "";
        try {
            /**
             * 解析param, 构造SQL, 执行数据库更新操作
             */
            String[] paramStrings = param.split(" ");
            String userName = paramStrings[0];
            String password = paramStrings[1];
            String startTime = paramStrings[2];
            String endTime = paramStrings[3];

            if (checkPassword(userName, password)) { // 查询用户密码是否正确
                String SQL = "SELECT * FROM meeting WHERE ((start_time >= ? AND start_time <= ?) AND (end_time >= ? AND end_time <= ?)) AND (user1_name = ? OR user2_name = ?);"; // 查询在这时间区间内的所有会议
                String tsStartTime = str2Timestamp(startTime, timeFormat);
                String tsEndTime = str2Timestamp(endTime, timeFormat);
                Object[] params = new Object[] { tsStartTime, tsEndTime, tsStartTime, tsEndTime, userName, userName };
                List<Map<String, Object>> list = MySQLRunner.executeQuery(SQL, params); // 利用MySQLRunner静态方法执行查询
                /**
                 * 构造返回信息ret
                 */
                for (int i = 0; i < list.size(); i++) {
                    String id = String.valueOf(list.get(i).get("id"));
                    String title = String.valueOf(list.get(i).get("title"));
                    String user1_name = String.valueOf(list.get(i).get("user1_name"));
                    String user2_name = String.valueOf(list.get(i).get("user2_name"));
                    String start_time = timestamp2Str(String.valueOf(list.get(i).get("start_time")), timeFormat);
                    String end_time = timestamp2Str(String.valueOf(list.get(i).get("end_time")), timeFormat);
                    ret += String.format("ID: %s\ttitle: %s\tuser1: %s\tuser2: %s\tstart time: %s\t end time: %s\n", id,
                            title, user1_name, user2_name, start_time, end_time);
                }
            } else {
                ret = "Wrong Password or User Not Exits!";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            ret = "Invaild Command Format!\n"
                    + "Usage: \n\tUnlogin: java [clientname] [servername] [portnumber] query [username] [password] [start] [end]\n"
                    + "\tLogin: query [start] [end]";
        } catch (ParseException e) {
            ret = "Invaild Time Format\nFormat: " + timeFormat;
        }

        return ret;
    }

    /**
     * 处理删除会议命令
     * 
     * @param param 命令参数
     * @return 命令执行结果
     * @throws RemoteException
     */
    @Override
    public String deleteMeeting(String param) throws RemoteException {
        String ret = "";
        try {
            /**
             * 解析param, 构造SQL, 执行数据库更新操作
             */
            String[] paramStrings = param.split(" ");
            String userName = paramStrings[0];
            String password = paramStrings[1];
            int meetingID = Integer.parseInt(paramStrings[2]);

            if (checkPassword(userName, password)) { // 查询用户密码是否正确
                String SQL = "DELETE FROM meeting WHERE id = ? AND user1_name = ?;";
                Object[] params = new Object[] { meetingID, userName };
                if (MySQLRunner.executeUpdate(SQL, params)) { // 利用MySQLRunner静态方法进行更新, 成功时返回true, 否则false
                    ret = "Delete Meeting Success!";
                } else {
                    ret = "Delete Meeting Failed! (Maybe Meeting Not Exits or Meeting Not Held By " + userName + "!)";
                }
            } else {
                ret = "Wrong Password or User Not Exits!";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            ret = "Invaild Command Format!\n"
                    + "Usage: \n\tUnlogin: java [clientname] [servername] [portnumber] delete [username] [password] [meetingid]\n"
                    + "\tLogin: delete [meetingid]";
        }

        return ret;
    }

    /**
     * 处理清除会议命令
     * 
     * @param param 命令参数
     * @return 命令执行结果
     * @throws RemoteException
     */
    @Override
    public String clearMeeting(String param) throws RemoteException {
        String ret = "";
        try {
            /**
             * 解析param, 构造SQL, 执行数据库更新操作
             */
            String[] paramStrings = param.split(" ");
            String userName = paramStrings[0];
            String password = paramStrings[1];

            if (checkPassword(userName, password)) { // 查询用户密码是否正确
                String SQL = "DELETE FROM meeting WHERE user1_name = ?;";
                Object[] params = new Object[] { userName };
                if (MySQLRunner.executeUpdate(SQL, params)) { // 利用MySQLRunner静态方法进行更新, 成功时返回true, 否则false
                    ret = "Delete All Meeting Success!";
                } else {
                    ret = "Delete All Meeting Failed! (Maybe No Meeting Held!)";
                }
            } else {
                ret = "Wrong Password or User Not Exits!";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            ret = "Invaild Command Format!\n"
                    + "Usage: \n\tUnlogin: java [clientname] [servername] [portnumber] clear [username] [password]\n"
                    + "\tLogin: clear";
        }

        return ret;
    }
}
