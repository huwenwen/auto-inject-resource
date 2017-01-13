package com.wen;

import com.wen.exception.InjectResourceException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huwenwen
 * @since 17/1/11
 */
public class SimpleJdbcTemplate {

  protected DataSource dataSource;

  private Connection getConn(DataSource dataSource) throws SQLException {
    return dataSource.getConnection();
  }

  /**
   * 获得list结果
   *
   * @param sql
   * @return
   * @throws SQLException
   */
  public List<String> getList(String sql, int columnNum, Object... arguments) throws SQLException {
    List<String> resultList = new ArrayList<>();
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet resultSet = null;
    try {
      conn = getConn(dataSource);
      ps = conn.prepareStatement(sql);
      if(arguments.length > 0){
        for (int i = 0; i < arguments.length; i++) {
          ps.setObject(i+1, arguments[i]);
        }
      }
      resultSet = ps.executeQuery();
      while (resultSet.next()){
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < columnNum; i++) {
          temp.append(resultSet.getString(i + 1));
        }
        resultList.add(temp.toString());
      }
    } finally {
      if(resultSet != null){
        resultSet.close();
      }
      if(ps != null){
        ps.close();
      }
      if(conn != null){
        conn.close();
      }
    }
    return resultList;
  }

  /**
   * 获得单一结果字符串
   *
   * @param sql
   * @param arguments
   * @return
   * @throws SQLException
   * @throws InjectResourceException
   */
  public String getString(String sql, Object... arguments)
      throws SQLException, InjectResourceException {
    String result = null;
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet resultSet = null;
    try {
      conn = getConn(dataSource);
      ps = conn.prepareStatement(sql);
      if(arguments.length > 0){
        for (int i = 0; i < arguments.length; i++) {
          ps.setObject(i+1, arguments[i]);
        }
      }
      resultSet = ps.executeQuery();
      if(resultSet != null && resultSet.getRow() > 1){
        throw new InjectResourceException("this sql [" + sql + "] expectation 1 result but not 2 more。 parameter is" + arguments);
      }
      if(resultSet.next()){
        result = resultSet.getString(1);
      }
    } finally {
      if(resultSet != null){
        resultSet.close();
      }
      if(ps != null){
        ps.close();
      }
      if(conn != null){
        conn.close();
      }
    }
    return result;
  }

  /**
   * 更新
   *
   * @param sql
   * @param arguments
   * @return
   * @throws SQLException
   */
  public int update(String sql, Object... arguments) throws SQLException {
    int lines = 0;
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = getConn(dataSource);
      ps = conn.prepareStatement(sql);
      if(arguments.length > 0){
        for (int i = 0; i < arguments.length; i++) {
          ps.setObject(i+1, arguments[i]);
        }
      }
      lines = ps.executeUpdate();
    } finally {
      if(ps != null){
        ps.close();
      }
      if(conn != null){
        conn.close();
      }
    }
    return lines;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }
}
