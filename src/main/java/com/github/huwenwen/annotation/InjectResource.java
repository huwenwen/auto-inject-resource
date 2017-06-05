package com.github.huwenwen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author huwenwen
 * @since 17/1/9
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InjectResource {

  /**
   * 资源名称
   *
   * @return
   */
  String name();

  /**
   * 资源url
   *
   * @return
   */
  String url() default "NULL";

  /**
   * 资源排序
   *
   * @return
   */
  int power() default 99;

  /**
   * 资源级别
   *
   * @return
   */
  int grade() default 1;

  /**
   * 对应的父节点名称
   * @return
   */
  String parentName() default "";

  /**
   * 其他父节点字段和值
   *
   * @return
   */
  String[] parentOtherProps() default {};

  /**
   * 自定义的其他属性
   *
   * @return
   */
  String[] customProps() default {};

  /**
   * 默认的自定义属性是否有效
   * @return
   */
  boolean enableDefaultCustomProps() default true;

  /**
   * 默认的确定父节点的属性是否有效
   * @return
   */
  boolean enableDefaultParentOtherProps() default true;
}
