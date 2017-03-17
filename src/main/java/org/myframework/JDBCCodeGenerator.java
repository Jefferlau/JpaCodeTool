package org.myframework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.texen.util.PropertiesUtil;
import org.myframework.codeutil.CodeGenerator;
import org.myframework.codeutil.Column;
import org.myframework.util.StringUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDBCCodeGenerator {
    private final static Log log = LogFactory.getLog(JDBCCodeGenerator.class);

    private static Connection getConnection() throws Exception {
        Properties properties = new PropertiesUtil().load("jdbc.properties");

        Properties localProperties = new Properties();
        localProperties.put("remarksReporting", "true");//注意这里
        localProperties.put("user", properties.getProperty("jdbc.username"));
        localProperties.put("password", properties.getProperty("jdbc.password"));

        // orcl为数据库的SID
        Class.forName(properties.getProperty("jdbc.driver")).newInstance();
        Connection conn = DriverManager.getConnection(properties.getProperty("jdbc.url"), localProperties);
        System.out.println(properties);
        return conn;
    }

    public static List<Column> getLsColumns(String tableName) throws Exception {
        Connection conn = getConnection();
        List<Column> lsColumns = new ArrayList<Column>(10);
        PreparedStatement stmt = conn.prepareStatement("select * from " + tableName + " where 1=0 ");
        ResultSet resultSet = stmt.executeQuery();
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int n = rsmd.getColumnCount();
        for (int i = 1; i <= n; i++) {
            String colName = rsmd.getColumnName(i);
            String fieldName = StringUtil.toBeanPatternStr(colName);
            String dataType = rsmd.getColumnClassName(i);
            if ("java.math.BigDecimal".equals(dataType) && rsmd.getScale(i) == 0)
                dataType = "Long";
            if ("oracle.sql.CLOB".equals(dataType))
                dataType = "String";
            Column column = new Column();
            column.setName(colName);
            column.setJavaName(fieldName);
            column.setDataType(dataType.endsWith("Timestamp") ? "java.util.Date" : dataType);
            column.setPrecision(String.valueOf(rsmd.getPrecision(i)));
            column.setScale(String.valueOf(rsmd.getScale(i)));
            column.setLength(String.valueOf(rsmd.getColumnDisplaySize(i)));
            column.setNullable(String.valueOf(1 == rsmd.isNullable(i)));

            // 获取列注释
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getColumns(null, null, tableName, null);
            while (rs.next()) {
                if (colName.equals(rs.getString("COLUMN_NAME"))) {
                    String comments = rs.getString("REMARKS");
                    column.setComments(StringUtil.asString(comments));
                }

            }
            // 获取主键列
            ResultSet rs2 = dbmd.getPrimaryKeys(null, null, tableName);
            while (rs2.next()) {
                if (colName.equals(rs2.getString("COLUMN_NAME")))
                    column.setColumnKey("TRUE");
            }
            log.debug("------------------" + column + "---------------------");
            log.debug("<td><spring:message code=\"" + StringUtil.toBeanPatternStr(tableName) + "." + column.getJavaName() + "\" />"
                    + "</td> <td><input name=\"" + column.getJavaName() + "\" type=\"text\" id=\"" + column.getJavaName() + "\" ltype=\"text\" validate=\"{required:true,minlength:3,maxlength:10}\" />" + "</td> ");
            lsColumns.add(column);
        }
        return lsColumns;
    }

    public static List<String> getTableNames(Properties properties) throws Exception {
        Connection conn = getConnection();
        List<String> tableNames = new ArrayList<String>();

        String url = properties.getProperty("jdbc.url");
        String schema = null;

        if (url.contains("jdbc:oracle")) {
            schema = properties.getProperty("jdbc.username").toUpperCase();
        } else if (url.contains("jdbc:mysql")) {
            String patternStr = "\\d+/(\\w+)";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                schema = matcher.group(0);
                schema = matcher.group(1);
            } else {
                throw new RuntimeException("解析不到 MySQL 数据库名称！");
            }
        } else {
            schema = "%";
        }

        DatabaseMetaData meta = conn.getMetaData();
        ResultSet resultSet = meta.getTables(null, schema, "%", new String[]{"TABLE"});

        while (resultSet.next()) {
            tableNames.add(resultSet.getString(3));
            System.out.println(resultSet.getString(3) + " " + resultSet.getString(5));
        }

        return tableNames;
    }

    /**
     * 代码生成工具调用
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {


        Properties properties = new PropertiesUtil().load("jdbc.properties");

        //代码内容--
        String tableName = properties.getProperty("tableName");

        if (tableName.contains(",")) {
            String[] tables = tableName.split(",");
            for (String table : tables) {
                generatorSingleTable(properties, table.trim().toUpperCase());
            }
        } else if (tableName.contains("%")) {
            List<String> tables = getTableNames(properties);
            for (String table : tables) {
                generatorSingleTable(properties, table.trim().toUpperCase());
            }
        } else {
            generatorSingleTable(properties, tableName.toUpperCase());
        }

    }

    private static void generatorSingleTable(Properties properties, String tableName) throws Exception {
        CodeGenerator code = new CodeGenerator();
        code.setTablePrefix(properties.getProperty("tablePrefix"));
        code.setPkgPrefix(properties.getProperty("pkgPrefix"));
        code.setDomainPkg(properties.getProperty("domainPkg"));
        code.setDaoPkg(properties.getProperty("daoPkg"));
        code.setDaoSuffix(properties.getProperty("daoSuffix"));
        code.setModule(properties.getProperty("module"));
        code.setSubModule(properties.getProperty("subModule"));
        code.setTableName(tableName);
        // 代码存放目录
        code.setJavaSource(properties.getProperty("javaSource"));
        code.setResources(properties.getProperty("resources"));
        code.setWebapp(properties.getProperty("webapp"));

        // 数据库读取配置信息
        code.initCodeTool(getLsColumns(tableName));
        code.createCodeByConf();
    }


}
