package com.wen.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huwenwen
 * @since 17/1/13
 */
public class CommonUtils {

  /**
   * 将{"key1:value1", "key2:value2"} 形式数组转化成map
   *
   * @param arrays
   * @return
   */
  public static Map<String, String> arrayConvertToMap(String[] arrays){
    Map<String, String> map = new HashMap<>();
    for (String prop : arrays) {
      String[] split = prop.split(":");
      if (split.length == 2) {
        String key = split[0].trim();
        String value = split[1].trim();
        map.put(key, value);
      }
    }
    return map;
  }
}
