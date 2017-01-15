package com.github.huwenwen;

import com.github.huwenwen.annotation.InjectResource;
import com.github.huwenwen.bean.ResourceBean;
import com.github.huwenwen.util.CommonUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author huwenwen
 * @since 16/7/29.
 */
public class CustomRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

  private final List<ResourceBean> resourceBeanList = new ArrayList<>();
  private boolean filterUrlStartSlash = false;
  private Map<String, String> defaultCustomProps;
  private Map<String, String> defaultParentOtherProps;
  private int noParentGrade = 1;

  @Override
  protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
    RequestMappingInfo mappingForMethod = super.getMappingForMethod(method, handlerType);
    InjectResource annotation = AnnotationUtils.findAnnotation(method, InjectResource.class);
    if (annotation != null) {
      if (mappingForMethod != null) {
        PatternsRequestCondition patternsCondition = mappingForMethod.getPatternsCondition();
        if (patternsCondition != null) {
          Set<String> patterns = patternsCondition.getPatterns();
          for (String url : patterns) {
            if (filterUrlStartSlash && url.startsWith("/")) {
              url = url.substring(1);
            }
            ResourceBean r = new ResourceBean();
            if (annotation.url().equals("")) {
              r.setUrl(url);
            } else {
              r.setUrl(annotation.url());
            }
            if (annotation.name().equals("")) {
              r.setName(mappingForMethod.getName());
            } else {
              r.setName(annotation.name());
            }
            r.setGrade(annotation.grade());
            r.setPower(annotation.power());
            r.setParentName(annotation.parentName());
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
    return mappingForMethod;
  }

  public List<ResourceBean> getResourceBeanList() {
    return resourceBeanList;
  }

  public void setFilterUrlStartSlash(boolean filterUrlStartSlash) {
    this.filterUrlStartSlash = filterUrlStartSlash;
  }

  public void setNoParentGrade(int noParentGrade) {
    this.noParentGrade = noParentGrade;
  }

  public void setDefaultCustomProps(Map<String, String> defaultCustomProps) {
    this.defaultCustomProps = defaultCustomProps;
  }

  public void setDefaultParentOtherProps(Map<String, String> defaultParentOtherProps) {
    this.defaultParentOtherProps = defaultParentOtherProps;
  }
}
