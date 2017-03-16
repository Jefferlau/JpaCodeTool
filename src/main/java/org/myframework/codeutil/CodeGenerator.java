package org.myframework.codeutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.texen.util.PropertiesUtil;
import org.myframework.util.DateUtil;
import org.myframework.util.FileUtil;
import org.myframework.util.StringUtil;

public   class CodeGenerator {

	private final Log log = LogFactory.getLog(getClass());

	CodeTool tool = new CodeTool();

	private String pkgPrefix = "com.jusfoun.fusion";

	private String module = "";

	private String subModule = "region";
	
	private String tablePrefix="";

	private String tableName = "GROUP_REGION_AUTO_CLAIM";

	private String javaSource="D://CODEGEN//java//";

	private String webapp="D://CODEGEN//webapp//";

	private String resources="D://CODEGEN//resources//";

	private String classVar;

	private String className;

	private String keyVar;

	private String keyType;

	private String jspDir;

	private String packageName;


	private void splitColumns( List<Column> lsColumns ) {
		List<Column> lsColumnTemp = new ArrayList<Column>(lsColumns.size());
		List<Column> pkColumns = new ArrayList<Column>(2);
		for (Column column : lsColumns) {
			if ("TRUE".equalsIgnoreCase(column.getColumnKey())) {
				pkColumns.add(column);
			}else{
				lsColumnTemp.add(column);
			}
		}
		tool.put("columnResult", lsColumnTemp);
		tool.put("pkResult", pkColumns);
	}

	public void initCodeTool(List<Column> lsColumns) throws Exception {
		classVar = StringUtil.toBeanPatternStr(tableName.substring(tablePrefix.length()));
		className = StringUtil.firstCharUpperCase(classVar);
		pkgPrefix =pkgPrefix
		+ (StringUtil.isEmpty(module) ? "" : ("." + module))
		+ (StringUtil.isEmpty(subModule) ? "" : ("." + subModule));
		jspDir = (StringUtil.isEmpty(module) ? "" : ("/" + module))
				+ (StringUtil.isEmpty(subModule) ? "" : ("/" + subModule));
		tool.put("tableName", tableName);
		tool.put("className", className);
		tool.put("module", module);
		tool.put("subModule", subModule);
		tool.put("classVar", classVar);
		tool.put("packagePrefix", pkgPrefix);
		tool.put("packageName", packageName);
		tool.put("jspDir", jspDir);

		//把主键和非主键分别放入tool
		splitColumns(  lsColumns);
		List<Column> pkResult = (List<Column>) tool.get("pkResult");
		int pkCnt = pkResult.size();
		if (pkCnt > 1 || pkCnt ==0) {
			log.debug("创建复合主键类：");
			keyType = "PK";
			keyVar =  "pk";
			tool.put("keyType", keyType);
			tool.put("keyVar", keyVar);
		}else if (pkCnt == 1) {
			Column column =   pkResult.get(0);
			keyType =column.getDataType();
			keyVar = column.getJavaName();
			tool.put("keyType", keyType);
			tool.put("keyVar", keyVar);
		} else {
		//	throw new Exception("该表无主键！！！！！！！！");
		}
//		org.apache.commons.lang.StringUtils  stringUtil = new  org.apache.commons.lang.StringUtils();
//		tool.put("stringUtil",stringUtil );

		tool.put("needUpdate",true);
		tool.put("date", DateUtil.getCurrentDay());
		tool.put("author", System.getProperty("user.name"));

	}


	/**
	 *  模型层主键代码生成
	 *
	 * @throws Exception
	 */
	private void createModelPkFile() throws Exception {
		List<Column> pkResult = (List<Column>) tool.get("pkResult");
		int pkCnt = pkResult.size();
		if (pkCnt > 1) {
			log.debug("创建复合主键类：");
			keyType = className + "PK";
			keyVar = classVar + "PK";
			tool.put("keyType", keyType);
			tool.put("keyVar", keyVar);

			String classNameWithPkg = pkgPrefix + ".model.base.Base" + keyType;
			String javaFile = javaSource + "/"
					+ FileUtil.getJavaFileName(classNameWithPkg);
			tool.setTmplFile("BaseModelPK.tmpl");
			tool.setAbsolutePath(javaFile);
			tool.createFileByTmpl();

			classNameWithPkg = pkgPrefix + ".model." + keyType;
			javaFile = javaSource + "/"
					+ FileUtil.getJavaFileName(classNameWithPkg);
			tool.setTmplFile("modelPK.tmpl");
			tool.setAbsolutePath(javaFile);
			tool.createFileByTmpl();
		} else if (pkCnt == 1) {
			Column column =   pkResult.get(0);
			keyType =column.getDataType();
			keyVar = column.getJavaName();
			tool.put("keyType", keyType);
			tool.put("keyVar", keyVar);
		} else {
			throw new Exception("该表无主键！！！！！！！！");
		}

		tool.put("idGetMethod", "get" + StringUtil.firstCharUpperCase(keyVar));

	}

	/**
	 * STEP 1 :模型层代码生成
	 *
	 * @throws Exception
	 */
	public void createModelFile() throws Exception {

		createModelPkFile();

		// 生成文件信息
		String classNameWithPkg = pkgPrefix + ".model.base.Base" + className;
		String javaFile = javaSource + "/"
				+ FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("BaseModel.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();

		classNameWithPkg = pkgPrefix + ".model." + className;
		javaFile = javaSource + "/" + FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("model.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();
	}

	/**
	 * STEP 1 :模型层代码生成
	 *
	 * @throws Exception
	 */
	public void createDomainFile() throws Exception {
		// 生成文件信息


		String classNameWithPkg = pkgPrefix + ".domain." + className;
		String javaFile = javaSource + "/"
				+ FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("Domain.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();
	}

	/**
	 * 2持久层代码生成
	 *
	 * @throws Exception
	 */
	public void createDAOFile() throws Exception {
		String classNameWithPkg = pkgPrefix + ".dao." + className + "DAO";
		String javaFile = javaSource + "/"
				+ FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("DaoInterface.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();

		classNameWithPkg = pkgPrefix + ".dao.hibernate." + className
				+ "DAOHibernate";
		javaFile = javaSource + "/" + FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("DaoImpl.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();
	}

	public void createJPAFile() throws Exception {
		String classNameWithPkg = pkgPrefix + ".dao." + className + "Dao";
		String javaFile = javaSource + "/"
				+ FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("Jpa.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();

	}

	/**
	 * STEP 3 :业务逻辑层代码生成
	 *
	 * @throws Exception
	 */
	public void createBusinessFile() throws Exception {

		String classNameWithPkg = pkgPrefix + ".service." + className
				+ "Manager";
		String javaFile = javaSource + "/"
				+ FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("ManagerInterface.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();

		classNameWithPkg = pkgPrefix + ".service.impl." + className
				+ "ManagerImpl";
		javaFile = javaSource + "/" + FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("ManagerImpl.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();

	}


	public void createServiceFile() throws Exception {

		String classNameWithPkg = pkgPrefix + ".service." + className
				+ "Service";
		String javaFile = javaSource + "/"
				+ FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("ServiceInterface.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();

		classNameWithPkg = pkgPrefix + ".service.impl." + className
				+ "ServiceImpl";
		javaFile = javaSource + "/" + FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("ServiceImpl.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();

	}

	/**
	 * STEP 4 :控制层代码生成
	 *
	 * @throws Exception
	 */
	public void createControlFile() throws Exception {
		// CREATE SPRING CONTROLLER
		String classNameWithPkg = pkgPrefix + ".web.action." + className
				+ "Controller";
		String javaFile = javaSource + "/"
				+ FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("controllerFile.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();

	}

	public void createMVCFile() throws Exception {
		// CREATE SPRING CONTROLLER
		String classNameWithPkg = pkgPrefix + ".mvc." + className
				+ "Controller";
		String javaFile = javaSource + "/"
				+ FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("MvcFile.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();

	}

	/**
	 * STEP 5 :视图层代码生成
	 *
	 * @throws Exception
	 */
	public void createViewFile() throws Exception {
		// 视图层
		tool.setTmplFile("magJsp.tmpl");
		tool.setAbsolutePath(webapp + jspDir + "/init" + className + "Mag.jsp");
		tool.createFileByTmpl();

		tool.setTmplFile("editJsp.tmpl");
		tool.setAbsolutePath(webapp  + jspDir + "/init" + className
				+ "Edit.jsp");
		tool.createFileByTmpl();

		tool.setTmplFile("qryListJsp.tmpl");
		tool.setAbsolutePath(webapp + jspDir + "/qry" + className + "List.jsp");
		tool.createFileByTmpl();

	}

	/**
	 * STEP 6 :生成
	 *
	 * @throws Exception
	 */
	public void createPropFile() throws Exception {
		tool.setTmplFile("prop.tmpl");
		tool.setAbsolutePath(resources + classVar + ".properties");
		tool.createFileByTmpl();
	}

	/**
	 * STEP 7 :单元测试代码生成
	 *
	 * @throws Exception
	 */
	public void createJunitFile() throws Exception {

		String classNameWithPkg = pkgPrefix + ".test." + className + "Test";
		String javaFile = javaSource + "/"
				+ FileUtil.getJavaFileName(classNameWithPkg);
		tool.setTmplFile("jUnitTest.tmpl");
		tool.setAbsolutePath(javaFile);
		tool.createFileByTmpl();
	}


	public void createCodeByConf()throws Exception {
		//step 1
		tool.setTmplDir(CodeTool.RESOURCE_PATH_PREFIX+"templates");
		tool.initVelocity();
		//step 2
		Properties jdbcProperties = new PropertiesUtil().load("jdbc.properties");
		for (Object key  :jdbcProperties.keySet()) {
			tool.put(key.toString(),jdbcProperties.get(key));
		}
		//step 3
		Properties properties = new PropertiesUtil().load("templates.cfg");
		for ( Object file:properties.values()){
			String fileAbsolutePath = file.toString() ;
 			String [] props =fileAbsolutePath.split(";");
			String filePath = tool.getText(props[1].trim());
			String vmFile =  props[2].trim() ;
			tool.setAbsolutePath(filePath);
			tool.setTmplFile(vmFile);
			tool.createFileByTmpl();
		}
	}



	public String getPkgPrefix() {
		return pkgPrefix;
	}

	public void setPkgPrefix(String pkgPrefix) {
		this.pkgPrefix = pkgPrefix;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getSubModule() {
		return subModule;
	}

	public void setSubModule(String subModule) {
		this.subModule = subModule;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setJavaSource(String javaSource) {
		this.javaSource = javaSource;
	}

	public void setWebapp(String webapp) {
		this.webapp = webapp;
	}

	public void setResources(String resources) {
		this.resources = resources;
	}


	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	@Override
	public String toString() {
		return "CodeGenerator [pkgPrefix=" + pkgPrefix + ", module=" + module
				+ ", subModule=" + subModule + ", tableName=" + tableName
				+ ", javaSource=" + javaSource + ", webapp=" + webapp
				+ ", resources=" + resources + ", classVar=" + classVar
				+ ", className=" + className + ", keyVar=" + keyVar
				+ ", keyType=" + keyType + ", jspDir=" + jspDir
				+ ", packageName=" + packageName + "]";
	}

}
