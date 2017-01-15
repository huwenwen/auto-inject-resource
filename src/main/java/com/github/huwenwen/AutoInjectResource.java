package com.github.huwenwen;

import com.github.huwenwen.annotation.InjectResource;
import com.github.huwenwen.bean.ResourceBean;
import com.github.huwenwen.exception.InjectResourceException;
import com.github.huwenwen.util.CommonUtils;
import com.github.huwenwen.util.ReadConfigUtils;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author huwenwen
 * @since 17/1/10
 */
public class AutoInjectResource extends InjectResourceAnnotationsHandler {
  // 自动扫描注解的包
  private String[] basePackages;
  private String tableName = "m_resource";
  private String columnUrl = "url";
  private String columnName = "name";
  private String columnPower = "power";
  private String columnGrade = "grade";
  private String columnParentSource = "id";
  private String columnParent = "parent_id";
  // 确定父节点的字段, 默认加上资源名称字段
  private String[] otherConfirmUniqueColumns = {};
  private Map<String, String> defaultCustomProps;
  private Map<String, String> defaultParentOtherProps;
  // 没有父节点的级别, 默认为 1
  private int noParentGrade = 1;

  /**
   * 自动扫描注入resource
   *
   * @throws InjectResourceException
   */
  public List<ResourceBean> saveResource() throws InjectResourceException{
    try {
      String temp;
      if ((temp = ReadConfigUtils.getProperty("resource.table.name")) != null) {
        tableName = temp;
      }
      if ((temp = ReadConfigUtils.getProperty("table.column.url")) != null) {
        columnUrl = temp;
      }
      if ((temp = ReadConfigUtils.getProperty("table.column.name")) != null) {
        columnName = temp;
      }
      if ((temp = ReadConfigUtils.getProperty("table.column.power")) != null) {
        columnPower = temp;
      }
      if ((temp = ReadConfigUtils.getProperty("table.column.grade")) != null) {
        columnGrade = temp;
      }
      if ((temp = ReadConfigUtils.getProperty("table.column.parent.source")) != null) {
        columnParentSource = temp;
      }
      if ((temp = ReadConfigUtils.getProperty("table.column.parent")) != null) {
        columnParent = temp;
      }
    } catch (Exception e) {
      logger.warn("you don't hava auto_inject_resource.properties file");
    }
    try {
      List<ResourceBean> newResourceBeanList = getNewResource();
      if(newResourceBeanList.isEmpty()){
        return newResourceBeanList;
      }
      List<ResourceBean> nextList = insertResource(newResourceBeanList, 1);
      if (!nextList.isEmpty()) {
        insertResource(nextList, 2);
      }
      return newResourceBeanList;
    } catch (Exception e) {
      throw new InjectResourceException("oh my god!!! some exception had appear", e);
    }
  }

  /**
   * 获取数据库不存在的资源list
   *
   * @return
   */
  public List<ResourceBean> getNewResource()
      throws InjectResourceException, ClassNotFoundException, SQLException {
    List<ResourceBean> resourceBeanList = getAllInjectResource();
    // 校验注入的资源是否有重复的
    checkInjectResourceUnique(resourceBeanList);
    List<ResourceBean> newResourceBeanList = new ArrayList<>();
    // 查询数据库所有资源
    StringBuilder sql = new StringBuilder();
    sql.append("select ");
    sql.append(columnName);
    for (String confirmParentColumn : otherConfirmUniqueColumns) {
      sql.append(",");
      sql.append(confirmParentColumn);
    }
    sql.append(" from ");
    sql.append(tableName);
    List<String> originNameList = getList(sql.toString(), 1 + otherConfirmUniqueColumns.length);
    for (ResourceBean a : resourceBeanList) {
      Map<String, String> customProps = a.getCustomProps();
      StringBuilder temp = new StringBuilder(a.getName());
      for (String confirmParentColumn : otherConfirmUniqueColumns) {
        temp.append(customProps.get(confirmParentColumn));
      }
      if (!originNameList.contains(temp.toString())) {
        newResourceBeanList.add(a);
      }
    }
    return newResourceBeanList;
  }


  /**
   * 校验注入的资源是否唯一 & 校验自定义确认父节点的字段在注解属性中出现
   *
   * @param list
   * @throws InjectResourceException
   */
  private void checkInjectResourceUnique(List<ResourceBean> list) throws InjectResourceException {
    Map<String, String> map = new HashMap<>();
    for (ResourceBean r : list) {
      StringBuilder key = new StringBuilder(r.getName());
      Map<String, String> parentOtherProps = r.getParentOtherProps();
      Map<String, String> customProps = r.getCustomProps();
      for (String confirmParentColumn : otherConfirmUniqueColumns) {
        String s = parentOtherProps.get(confirmParentColumn);
        // 对应没有父节点的不校验这点
        if(r.isNoParent()){
          s = "";
        }
        String custom = customProps.get(confirmParentColumn);
        if(s == null){
          throw new InjectResourceException("you defined otherConfirmUniqueColumns [" + confirmParentColumn + "], but not found in annotation [@InjectResource] property [parentOtherProps]");
        }
        if(custom == null){
          throw new InjectResourceException("you defined otherConfirmUniqueColumns [" + confirmParentColumn + "], but not found in annotation [@InjectResource] property [customProps]");
        }
        key.append(custom);
      }
      if(map.containsKey(key.toString())){
        throw new InjectResourceException("@InjectResource inject resource not unique, the same name is " + r.getName() + ", please check and modify it");
      }
      map.put(key.toString(), "");
    }
  }

  /**
   * 获得所有注解的值
   *
   * @return
   * @throws InjectResourceException
   * @throws ClassNotFoundException
   */
  public List<ResourceBean> getAllInjectResource()
      throws InjectResourceException, ClassNotFoundException {
    if(basePackages == null){
      throw new InjectResourceException("you must setter [basePackages] property");
    }
    List<ResourceBean> resourceBeanList = new ArrayList<>();
    for (String location : basePackages) {
      Set<Class<?>> classes = super.getClasses(location);
      for (Class<?> clazz : classes) {
        for (Method method : clazz.getMethods()) {
          InjectResource annotation = method.getAnnotation(InjectResource.class);
          if(annotation != null){
            ResourceBean r = new ResourceBean();
            r.setName(annotation.name());
            r.setUrl(annotation.url());
            r.setParentName(annotation.parentName());
            r.setPower(annotation.power());
            r.setGrade(annotation.grade());
            // 是否没有父节点
            if(annotation.grade() == noParentGrade){
              r.setNoParent(true);
            }
            Map<String, String> customMap = new HashMap<>();
            Map<String, String> parentMap = new HashMap<>();
            // 默认值
            if(annotation.enableDefaultCustomProps() && defaultCustomProps != null){
              customMap.putAll(defaultCustomProps);
            }
            if(annotation.enableDefaultParentOtherProps() && defaultParentOtherProps != null){
              parentMap.putAll(defaultParentOtherProps);
            }
            // 自定义属性
            customMap.putAll(CommonUtils.arrayConvertToMap(annotation.customProps()));
            parentMap.putAll(CommonUtils.arrayConvertToMap(annotation.parentOtherProps()));
            r.setCustomProps(customMap);
            r.setParentOtherProps(parentMap);
            resourceBeanList.add(r);
          }
        }
      }
    }
    return resourceBeanList;
  }

  /**
   * 插入数据库
   *
   * @param list
   * @param time
   * @return
   * @throws InjectResourceException
   */
  private List<ResourceBean> insertResource(List<ResourceBean> list, int time)
      throws InjectResourceException, SQLException {
    List<ResourceBean> nextInsertList = new ArrayList<>();
    for (ResourceBean resourceBean : list) {
      //  查询父节点信息
      StringBuilder sb = new StringBuilder();
      List<Object> ps = new ArrayList<>();
      sb.append("select ");
      sb.append(columnParentSource);
      sb.append(" from ");
      sb.append(tableName);
      sb.append(" where ");
      sb.append(columnName);
      sb.append(" = ?");
      ps.add(resourceBean.getParentName());
      Map<String, String> parentOtherProps = resourceBean.getParentOtherProps();
      for (String confirmParentColumn : otherConfirmUniqueColumns) {
        sb.append(" and " + confirmParentColumn + " = ?");
        ps.add(parentOtherProps.get(confirmParentColumn));
      }

      String parentId;
      // 没有父节点
      if (resourceBean.isNoParent()) {
        parentId = "0";
      } else {
        try {
          parentId = getString(sb.toString(), ps.toArray());
          if (parentId == null) {
            if (time > 1) {
              // second time throw exception
              throw new InjectResourceException("not found parentId with sql [" + sb.toString() + "] , params is [" + resourceBean.getParentName() + "]");
            }
            // save next to insert
            nextInsertList.add(resourceBean);
            continue;
          }
        } catch (InjectResourceException e) {
          logger.error("Column [" + columnName + "] in the table [" + tableName + "] must be unique", e);
          throw new InjectResourceException("Column [" + columnName + "] in the table [" + tableName + "] must be unique", e);
        }
      }
      StringBuilder sql = new StringBuilder();
      Map<String, String> customPropMap = resourceBean.getCustomProps();
      sql.append("insert into ");
      sql.append(tableName);
      sql.append(" (");
      // add customer prop
      List<Object> params = new ArrayList<>();
      Iterator<Map.Entry<String, String>> iterator = customPropMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<String, String> next = iterator.next();
        sql.append(next.getKey());
        sql.append(",");
        params.add(next.getValue());
      }
      sql.append(columnUrl);
      sql.append(",");
      sql.append(columnName);
      sql.append(",");
      sql.append(columnPower);
      sql.append(",");
      sql.append(columnGrade);
      sql.append(",");
      sql.append(columnParent);
      sql.append(") values (");
      for (int i = 0; i < customPropMap.size(); i++) {
        sql.append("?,");
      }
      sql.append("?,?,?,?,?)");
      params.add(resourceBean.getUrl());
      params.add(resourceBean.getName());
      params.add(resourceBean.getPower());
      params.add(resourceBean.getGrade());
      params.add(parentId);
      try {
        update(sql.toString(), params.toArray());
      } catch (Exception e) {
        logger.error("execute sql [" + sql.toString() + "] ;An error occurred", e);
        throw new InjectResourceException("execute sql [" + sql.toString() + "] ;An error occurred", e);
      }
    }
    return nextInsertList;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public void setColumnUrl(String columnUrl) {
    this.columnUrl = columnUrl;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public void setColumnPower(String columnPower) {
    this.columnPower = columnPower;
  }

  public void setColumnGrade(String columnGrade) {
    this.columnGrade = columnGrade;
  }

  public void setColumnParentSource(String columnParentSource) {
    this.columnParentSource = columnParentSource;
  }

  public void setBasePackages(String[] basePackages) {
    this.basePackages = basePackages;
  }

  public void setColumnParent(String columnParent) {
    this.columnParent = columnParent;
  }

  public void setOtherConfirmUniqueColumns(String[] otherConfirmUniqueColumns) {
    this.otherConfirmUniqueColumns = otherConfirmUniqueColumns;
  }

  public void setDefaultCustomProps(Map<String, String> defaultCustomProps) {
    this.defaultCustomProps = defaultCustomProps;
  }

  public void setDefaultParentOtherProps(Map<String, String> defaultParentOtherProps) {
    this.defaultParentOtherProps = defaultParentOtherProps;
  }

  public void setNoParentGrade(int noParentGrade) {
    this.noParentGrade = noParentGrade;
  }
}
