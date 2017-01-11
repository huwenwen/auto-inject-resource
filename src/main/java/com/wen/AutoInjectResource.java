package com.wen;

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
  private String[] controllerPackages;
  private String tableName = "m_resource";
  private String columnUrl = "url";
  private String columnName = "name";
  private String columnPower = "power";
  private String columnGrade = "grade";
  private String columnParentSource = "id";
  private String columnParent = "parent_id";

  /**
   * 自动扫描注入resource
   *
   * @throws InjectResourceException
   */
  public List<Resource> saveResource() throws InjectResourceException{
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
      List<Resource> newResourceList = getNewResource();
      if(newResourceList.isEmpty()){
        return newResourceList;
      }
      List<Resource> nextList = insertResource(newResourceList, 1);
      if (!nextList.isEmpty()) {
        insertResource(newResourceList, 2);
      }
      return newResourceList;
    } catch (Exception e) {
      throw new InjectResourceException("oh my god!!! some exception had appear", e);
    }
  }

  /**
   * 获取数据库不存在的资源list
   *
   * @return
   */
  public List<Resource> getNewResource()
      throws InjectResourceException, ClassNotFoundException, SQLException {
    List<Resource> resourceList = getAllInjectResource();
    List<Resource> newResourceList = new ArrayList<>();
    // 查询数据库所有资源
    StringBuilder sql = new StringBuilder();
    sql.append("select ");
    sql.append(columnName);
    sql.append(" from ");
    sql.append(tableName);
    List<String> originNameList = getList(sql.toString());
    resourceList.stream().forEach(a -> {
      if (!originNameList.contains(a.getName())) {
        newResourceList.add(a);
      }
    });
    return newResourceList;
  }

  /**
   * 获得所有注解的值
   *
   * @return
   * @throws InjectResourceException
   * @throws ClassNotFoundException
   */
  public List<Resource> getAllInjectResource()
      throws InjectResourceException, ClassNotFoundException {
    List<Resource> resourceList = new ArrayList<>();
    for (String location : controllerPackages) {
      Set<Class<?>> classes = super.getClasses(location);
      for (Class<?> clazz : classes) {
        for (Method method : clazz.getMethods()) {
          InjectResource annotation = method.getAnnotation(InjectResource.class);
          if(annotation != null){
            Resource r = new Resource();
            r.setName(annotation.name());
            r.setUrl(annotation.url());
            r.setParentName(annotation.parentName());
            r.setPower(annotation.power());
            r.setGrade(annotation.grade());
            // 自定义属性
            String[] props = annotation.customProps();
            if (props.length > 0) {
              Map<String, String> map = new HashMap<>();
              for (String prop : props) {
                String[] split = prop.split(":");
                if (split.length == 2) {
                  String key = split[0].trim();
                  String value = split[1].trim();
                  map.put(key, value);
                }
              }
              r.setCustomProps(map);
            }
            resourceList.add(r);
          }
        }
      }
    }
    return resourceList;
  }

  /**
   * 插入数据库
   *
   * @param list
   * @param time
   * @return
   * @throws InjectResourceException
   */
  private List<Resource> insertResource(List<Resource> list, int time)
      throws InjectResourceException, SQLException {
    List<Resource> nextInsertList = new ArrayList<>();
    for (Resource resource : list) {
      //  查询父节点信息
      StringBuilder sb = new StringBuilder();
      sb.append("select ");
      sb.append(columnParentSource);
      sb.append(" from ");
      sb.append(tableName);
      sb.append(" where ");
      sb.append(columnName);
      sb.append(" = ?");
      String parentId;
      // 没有父节点
      if ("".equals(resource.getParentName())) {
        parentId = "0";
      } else {
        try {
          parentId = getString(sb.toString(), resource.getParentName());
          if (parentId == null) {
            if (time > 1) {
              // second time throw exception
              throw new InjectResourceException(
                  "not found parentId with sql [" + sb.toString() + "] , params is [" + resource
                      .getParentName() + "]");
            }
            // save next to insert
            nextInsertList.add(resource);
            continue;
          }
        } catch (InjectResourceException e) {
          logger.error("Column [" + columnName + "] in the table [" + tableName + "] must be unique", e);
          throw new InjectResourceException(
              "Column [" + columnName + "] in the table [" + tableName + "] must be unique", e);
        }
      }
      StringBuilder sql = new StringBuilder();
      Map<String, String> customPropMap = resource.getCustomProps();
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
      params.add(resource.getUrl());
      params.add(resource.getName());
      params.add(resource.getPower());
      params.add(resource.getGrade());
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

  public void setControllerPackages(String[] controllerPackages) {
    this.controllerPackages = controllerPackages;
  }

  public void setColumnParent(String columnParent) {
    this.columnParent = columnParent;
  }
}
