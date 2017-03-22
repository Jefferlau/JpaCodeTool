JPA 逆向工程生成 Java 代码的工具
---

暂时不知道原作者是谁，待找到后添加。本项目在原作基础上修改而来。

**说明**

- src/main/resources/templates       代码生成模板目录
- src/main/resources/jdbc.properties 配置信息文件：数据库连接的信息，代码生成目录，需要生成代码的表
- src/main/resources/templates.cfg   配置文件：使用哪些代码模板，生成的文件所在的目录
- src/main/resources/templates.cfg   可以使用 jdbc.properties 中定义的变量如：tableName ，javaSource等，避免重复配置
- src/main/java/org/myframework/JDBCCodeGenerator.java 是运行入口

jdbc.properties 中 tableName 支持多种配置方式
- 单表，即明确指定表名
- 多表，多个表名以半角逗号","分割
- 默认连接中所有表，给定值"%"

tablePrefix 有两个作用
- 当 tableName 值为"%"时，只处理表名以 tablePrefix 开头的表
- 生成 Entity 类时类名去掉前缀

**注**
目前 tableName 不区分大小写，在 Oracle 下正常，其它数据库尚未验证。

**执行**

```
mvn exec:java
```