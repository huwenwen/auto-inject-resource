package com.wen.exception;

import java.io.Serializable;

/**
 * @author huwenwen
 * @since 17/1/9
 */
public class InjectResourceException extends Exception implements Serializable {

  public InjectResourceException(String message) {
    super(message);
  }

  public InjectResourceException(String message, Throwable cause) {
    super(message, cause);
  }
}
