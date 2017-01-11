##自动加载权限系统中的资源到数据库
* 适用的javaWeb后台系统: 资源表的设计必须可以用资源名称来唯一区分
* 如果你的系统使用springMvc, 你可以使用[spring版本](https://github.com/huwenwen/auto_inject_resource/tree/spring)

###Getting Start
1. maven 配置
        
        <repositories>
            <repository>
                <id>wenwen</id>
                <url>https://wenwen.bintray.com/mvn-repo/</url>
            </repository>
        </repositories>
        
        <dependency>
            <groupId>com.wen</groupId>
            <artifactId>auto_inject_resource</artifactId>
            <version>0.0.1</version>
        </dependency>
2. 加入配置

        <bean class="com.wen.AutoInjectResource">
            <!-- 数据源 -->
            <property name="dataSource" ref="dataSource"/>
            <!-- @InjectResource所在的package, 可以是多个(用逗号分隔), 一般为controller层 -->
            <property name="controllerPackages" value="com.wen.controller"/>
        </bean>
3. 需要自动保存到数据库的资源,在方法上加

        @InjectResource(name="resourceName", url="/test/123" parentName="resourceParentName")

4. 保存资源到数据库
    
        // 注入
        @Inject
        private AutoInjectResource autoInjectResource;
        
        // 调用方法
        autoInjectResource.saveResource();
    
### 使用文档
1. 配置自己数据库的资源表的表名称以及字段名称
>第一种方式:在classpath路径下加入 auto_inject_resource.properties 文件。如下该文件示例
    
        # 资源表名
        resource.table.name=m_resource
        # 资源表对应url的列名
        table.column.url=RESOURCE_STRING
        # 资源表对应名称的列名
        table.column.name=RESOURCE_NAME
        # 资源表对应排序列名
        table.column.power=SORT_INDEX
        # 资源表对应级别列名
        table.column.grade=GRADE
        # 对应父节点字段列名
        table.column.parent=PARENT_ID
        # 父节点字段的取值来源字段列名
        table.column.parent.source=RESOURCE_ID
>第二种方式:spring属性注入
    
        <bean class="com.wen.AutoInjectResource">
            <property name="tableName" value="m_resource"/>
            <property name="columnUrl" value="RESOURCE_STRING"/>
            <property name="columnName" value="RESOURCE_NAME"/>
            <property name="columnPower" value="SORT_INDEX"/>
            <property name="columnGrade" value="GRADE"/>
            <property name="columnParent" value="PARENT_ID"/>
            <property name="columnParentSource" value="RESOURCE_ID"/>
        </bean>
2. 可以在@InjectResource customProps中 加入一些自定义的数据库字段和对应的值。
    customProps 为数组, 数组中的值的格式为key:value, key为资源表中的字段名,value为这个字段的值
    
        @InjectResource(name = "resourceName", url="abc/123", parentName = "resourceParentName", customProps = {"key1:value1", "key2:value2"})

### 缺陷
* 设计的资源表必须是要以资源名称来唯一区别的
* 资源名称如果修改，下次这个资源还会再次保存在数据库

### DEMO
   我的数据库资源表结构如下
   
        CREATE TABLE `M_RESOURCE` (
         `RESOURCE_ID` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '资源id',
         `RESOURCE_NAME` varchar(128) DEFAULT NULL COMMENT '资源名称',
         `RESOURCE_DESC` varchar(128) DEFAULT NULL COMMENT '资源描述',
         `RESOURCE_TYPE` char(4) DEFAULT '0' COMMENT '资源类型',
         `IS_ENABLED` char(4) DEFAULT '1' COMMENT '是否可用',
         `SHOW_NAV` char(4) DEFAULT '0' COMMENT '是否显示',
         `GRADE` char(4) DEFAULT '0' COMMENT '菜单级别',
         `PRJ_TYPE` char(4) DEFAULT '0',
         `SORT_INDEX` int(11) DEFAULT '0' COMMENT '排序',
         `PARENT_ID` int(11) DEFAULT '0',
         `RESOURCE_STRING` varchar(128) NOT NULL DEFAULT '' COMMENT '资源url',
         `CREATE_USER` varchar(128) DEFAULT NULL,
         `CREATED_AT` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
         `UPDATED_AT` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
         PRIMARY KEY (`RESOURCE_ID`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='资源表';
   
   controller层
   
        @Controller
        @RequestMapping(value = "/test")
        public class TestController {
         /**
         * 一级菜单, 没有父节点
         */
         @RequestMapping(value = "/abc1", method = RequestMethod.GET)
         @InjectResource(name = "ResourceName1", url = "/parent", power = 10, grade = 1, customProps = {"RESOURCE_TYPE:1", "SHOW_NAV:1"})
         public ModelAndView list() {
           // dosomthing
         }
         
         /**
          * 二级菜单, 父节点为上面
          */
         @RequestMapping(value = "/abc2", method = RequestMethod.GET)
         @InjectResource(name = "ResourceName2", parentName="ResourceName1", url = "/children", power = 10, grade = 2, customProps = {"RESOURCE_TYPE:2", "SHOW_NAV:0"})
         public ModelAndView listDetail() {
            // dosomthing
         }
         
  向数据库插入以上资源:
    
        List<Resource> successInsertResourceList = autoInjectResource.saveResource();
  
     
  返回的list为成功向数据库插入的数据
  


