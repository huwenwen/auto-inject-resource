package com.wen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author huwenwen
 * @since 17/1/10
 */
public class ReadConfigUtils {

  private static Log logger = LogFactory.getLog(ReadConfigUtils.class);

  private static Properties p = new Properties();

  static {
    init("auto_inject_resource.properties");
  }

  private static void init(String propertyFileName) {
    InputStream in = null;
    try {
      in = ReadConfigUtils.class.getClassLoader().getResourceAsStream(propertyFileName);
      if (in != null) {
        p.load(in);
      }
    } catch (IOException e) {
      logger.error("load " + propertyFileName + " into Constants error!", e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          logger.error("close " + propertyFileName + " error!", e);
        }
      }
    }
  }

  public static String getProperty(String key) {
    return p.getProperty(key);
  }
}
