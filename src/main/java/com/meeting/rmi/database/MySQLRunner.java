package com.meeting.rmi.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * MySQL辅助类
 * 
 * @author GX
 * @version 0.2
 */
public class MySQLRunner {

    static String JDBC_DRIVER; // MySQL 8.0 以上版本 - JDBC 驱动名
    static String DB_URL; // 数据库Url
    static String USER; // 数据库用户名
    static String PASS; // 数据库密码

    /**
     * 加载数据库设置与数据
     */
    private static void loadSetting() throws ParserConfigurationException, SAXException, IOException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            String xmlPath = MySQLRunner.class.getResource("/com/meeting/rmi/database/setting.xml").getPath(); // setting.xml在class同目录下
            Document document = db.parse(xmlPath);
            NodeList database = document.getElementsByTagName("database");
            for (int i = 0; i < database.getLength(); i++) {
                JDBC_DRIVER = document.getElementsByTagName("JDBC_DRIVER").item(i).getFirstChild().getNodeValue();
                DB_URL = document.getElementsByTagName("DB_URL").item(i).getFirstChild().getNodeValue();
                USER = document.getElementsByTagName("USER").item(i).getFirstChild().getNodeValue();
                PASS = document.getElementsByTagName("PASSWORD").item(i).getFirstChild().getNodeValue();
            }
        } catch (ParserConfigurationException e) {
            System.err.println("setting.xml Parse Error!");
            throw e;
        } catch (NullPointerException e) {
            System.err.println("setting.xml is Not in the dir of MySQLRunner.class!");
            throw e;
        }
    }

    /**
     * 静态方法获取数据库连接
     * 
     * @return 数据库连接Connection
     */
    public static Connection getConnection() throws ParserConfigurationException, SAXException, IOException {
        Connection conn = null;
        try {
            loadSetting();
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
            // 打开链接, 获取连接
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (Exception e) {
            // e.printStackTrace();
            System.err.println("MySQL connection ERROR!");
        }
        return conn;
    }

    /**
     * 执行增删改操作
     * 
     * @param SQL    构造PrepareStatement的SQL语句, 变量用?代替
     * @param params 变量数组
     * @return 执行成功返回true, 失败返回false
     */
    public static boolean executeUpdate(String SQL, Object[] params) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection(); // 获取连接
            ps = conn.prepareStatement(SQL);
            for (int i = 1; i < params.length + 1; i++) { // 构造PrepareStatement
                ps.setObject(i, params[i - 1]);
            }
            int result = ps.executeUpdate(); // 执行SQL, 返回受到影响的语句数量
            if (result > 0) {
                return true;
            }
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            /**
             * 及时关闭PrepareStatement和数据库连接
             */
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 执行查询操作
     * 
     * @param SQL    构造PrepareStatement的SQL语句, 变量用?代替
     * @param params 变量数组
     * @return 返回List<Map<string, Object>>类型, 存储查询后的结果
     */
    public static List<Map<String, Object>> executeQuery(String SQL, Object[] params) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(); // 用于存储查询后的结果
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection(); // 获取数据库连接
            ps = conn.prepareStatement(SQL); // 构造PrepareStatement
            for (int i = 1; i < params.length + 1; i++) {
                ps.setObject(i, params[i - 1]);
            }
            rs = ps.executeQuery(); // 执行查询操作, 返回结果集ResultSet
            ResultSetMetaData rsmd = rs.getMetaData(); // 获取结果集中的元数据
            while (rs.next()) { // 将结果集中数据存储到list中
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String column_name = rsmd.getColumnName(i);
                    Object column_value = rs.getObject(column_name);
                    if (column_value == null) {
                        column_value = "";
                    }
                    map.put(column_name, column_value);
                }
                list.add(map);
                // System.out.println(map.toString());
            }
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            try {
                /**
                 * 及时关闭ResultSet, PrepareStatement和Connection
                 */
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}