package com.github.huwenwen.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huwenwen
 * @since 17/1/9
 */
public class ResourceBean {

  private String name;
  private String url;
  private int grade;
  private int power;
  private String parentName;
  private boolean isNoParent;
  private Map<String, String> parentOtherProps = new HashMap<>();
  private Map<String, String> customProps = new HashMap<>();

  public Map<String, String> getCustomProps() {
    return customProps;
  }

  public void setCustomProps(Map<String, String> customProps) {
    this.customProps = customProps;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getGrade() {
    return grade;
  }

  public void setGrade(int grade) {
    this.grade = grade;
  }

  public int getPower() {
    return power;
  }

  public void setPower(int power) {
    this.power = power;
  }

  public String getParentName() {
    return parentName;
  }

  public void setParentName(String parentName) {
    this.parentName = parentName;
  }

  public boolean isNoParent() {
    return isNoParent;
  }

  public void setNoParent(boolean noParent) {
    isNoParent = noParent;
  }

  public Map<String, String> getParentOtherProps() {
    return parentOtherProps;
  }

  public void setParentOtherProps(Map<String, String> parentOtherProps) {
    this.parentOtherProps = parentOtherProps;
  }
}
