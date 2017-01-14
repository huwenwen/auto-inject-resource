package com.github.huwenwen.annontation;

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
  String name() default "";

  /**
   * 资源url
   *
   * @return
   */
  String url() default "";

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

  String[] customProps() default {};
}
