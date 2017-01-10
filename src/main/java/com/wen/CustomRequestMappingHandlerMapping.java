package com.wen;

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

  private final List<Resource> resourceList = new ArrayList<>();
  private boolean filterUrlStartSlash = false;

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
            Resource r = new Resource();
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
            r.setParentName(annotation.parentName());
            resourceList.add(r);
          }
        }
      }
    }
    return mappingForMethod;
  }

  public List<Resource> getResourceList() {
    return resourceList;
  }

  public void setFilterUrlStartSlash(boolean filterUrlStartSlash) {
    this.filterUrlStartSlash = filterUrlStartSlash;
  }
}
