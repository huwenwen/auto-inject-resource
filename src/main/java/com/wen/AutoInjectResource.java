package com.wen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author huwenwen
 * @since 17/1/10
 */
public class AutoInjectResource {

  private static final Logger logger = LoggerFactory.getLogger(AutoInjectResource.class);

  private CustomRequestMappingHandlerMapping customRequestMappingHandlerMapping;
  private JdbcTemplate jdbcTemplate;
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
  public void saveResource() throws InjectResourceException {
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
    List<Resource> newResourceList = getNewResource();
    List<Resource> nextList = insertResource(newResourceList, 1);
    if (!nextList.isEmpty()) {
      insertResource(newResourceList, 2);
    }
  }

  /**
   * 获取数据库不存在的资源list
   *
   * @return
   */
  public List<Resource> getNewResource() {
    List<Resource> resourceList = customRequestMappingHandlerMapping.getResourceList();
    List<Resource> newResourceList = new ArrayList<>();
    // 查询数据库所有资源
    StringBuilder sql = new StringBuilder();
    sql.append("select ");
    sql.append(columnName);
    sql.append(" from ");
    sql.append(tableName);
    List<String> originNameList = jdbcTemplate.queryForList(sql.toString(), String.class);
    resourceList.stream().forEach(a -> {
      if (!originNameList.contains(a.getName())) {
        newResourceList.add(a);
      }
    });
    return newResourceList;
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
      throws InjectResourceException {
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
          parentId =
              jdbcTemplate.queryForObject(sb.toString(), String.class, resource.getParentName());
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
        } catch (IncorrectResultSizeDataAccessException e) {
          logger.error("Column [{}] in the table [{}] must be unique", columnName, tableName, e);
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
        jdbcTemplate.update(sql.toString(), params.toArray());
      } catch (Exception e) {
        logger.error("execute sql [{}] ;An error occurred", sql.toString(), e);
        throw new InjectResourceException("execute sql [" + sql.toString() + "] ;An error occurred",
            e);
      }
    }
    return nextInsertList;
  }

  public void setCustomRequestMappingHandlerMapping(
      CustomRequestMappingHandlerMapping customRequestMappingHandlerMapping) {
    this.customRequestMappingHandlerMapping = customRequestMappingHandlerMapping;
  }

  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
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

  public void setColumnParent(String columnParent) {
    this.columnParent = columnParent;
  }
}
