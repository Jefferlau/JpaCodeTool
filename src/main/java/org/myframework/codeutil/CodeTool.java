package org.myframework.codeutil;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.myframework.util.FileUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CodeTool {
    private String tmplDir = "classpath:templates";
    private String encoding = "UTF-8";
    private String tmplFile;// 锟斤拷锟斤拷
    private String absolutePath;// 锟斤拷锟斤拷
    private Map context = new HashMap();
    boolean isDebug = false;

    public boolean getIsDebug() {
        return isDebug;
    }

    public void put(String key, Object value) {
        context.put(key, value);
    }

    public void put(Map paramMap) {
        context.putAll(paramMap);
    }

    public Object get(String key) {
        return context.get(key);
    }

    public void setIsDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public String getTmplFile() {
        return tmplFile;
    }

    public void setTmplFile(String tmplFile) {
        this.tmplFile = tmplFile;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public Map getContext() {
        return context;
    }

    public void setContext(Map context) {
        this.context = context;
    }

    public void createFileByTmpl() throws Exception {
//		Velocity.addProperty("file.resource.loader.path", getClassPath()
//				+ tmplDir);
        Template template = Velocity.getTemplate(tmplFile, encoding);
        VelocityContext tmplContext = new VelocityContext(context);
        FileUtil.createFile(absolutePath);
        PrintWriter writer = new PrintWriter(
                new FileOutputStream(absolutePath), true);
        template.merge(tmplContext, writer);
        writer.flush();
        writer.close();
        if (isDebug) {
            debug();
        }
    }

    public String getText(String text) throws Exception {
        VelocityContext tmplContext = new VelocityContext(context);
        StringWriter w = new StringWriter();
        Velocity.evaluate(tmplContext, w, super.getClass().getName(), text);
        return w.toString();
    }

    /**
     * 取锟斤拷Class路锟斤拷
     *
     * @return
     */
    public String getTemplatePath() {
        if (tmplDir.startsWith(RESOURCE_PATH_PREFIX)) {
            URL url = this.getClass().getResource("/" + tmplDir.substring(RESOURCE_PATH_PREFIX.length()));
            File file = new File(url.getPath());
            return file.getAbsolutePath();
        }
        return tmplDir;
    }

    public static String RESOURCE_PATH_PREFIX = "classpath:";

    public void setTmplDir(String tmplDir) {
        this.tmplDir = tmplDir;
    }

    public void initVelocity() {
        Properties p = new Properties();
        p.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, getTemplatePath());
        Velocity.init(p);
    }

    public void debug() throws Exception {
//		VelocityEngine.FILE_RESOURCE_LOADER_PATH
//		Velocity.addProperty("file.resource.loader.path", getClassPath()
//				+ tmplDir);
//		Properties p = new Properties();
//		p.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, getTemplatePath() );
//		Velocity.init(p);
        Template template = Velocity.getTemplate(tmplFile, encoding);// 锟斤拷取锟斤拷锟斤拷锟侥硷拷
        VelocityContext tmplContext = new VelocityContext(context); // 锟斤拷取锟斤拷锟教诧拷锟斤拷
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                System.out));
        if (template != null)
            template.merge(tmplContext, writer); // 锟斤拷锟斤拷锟斤拷模锟斤拷锟斤拷傻锟斤拷募锟�
        writer.flush();
        writer.close();
    }

    public CodeTool() {
        super();
    }

    public CodeTool(String tmplFile, String absolutePath) {
        super();
        this.tmplFile = tmplFile;
        this.absolutePath = absolutePath;
    }

    public static void main(String[] args) throws Exception {
//		CodeTool tool = new CodeTool();
//		tool.setTmplFile("tmpl/DAOImpl.tmpl");
//		tool.setAbsolutePath("D:/ABC/abc1.txt");
//		tool.createFileByTmpl();
        System.out.println("111");
    }
}
