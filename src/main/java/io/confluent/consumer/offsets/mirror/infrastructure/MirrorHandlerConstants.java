package io.confluent.consumer.offsets.mirror.infrastructure;

public class MirrorHandlerConstants {
  private MirrorHandlerConstants() {
  }

  public static final String BASE_ENDPOINT_PACKAGE = "io.confluent.consumer.offsets.web.endpoint";

  public static final String RECORD_METER_REGISTRY_NAME = "mirroring";

  public static final String SOCKET_ADDRESS_SYSTEM_PROPERTY = "socket-address";
  public static final String SOCKET_ADDRESS_DEFAULT_VALUE = "3131";

  public static final String IDLE_STATE_TIMEOUT_SYSTEM_PROPERTY = "idle-state-timeout-secs";
  public static final String IDLE_STATE_TIMEOUT_DEFAULT_VALUE = "300";

  public static final String CONSOLE_REPORTER_PERIOD_SYSTEM_PROPERTY = "console-reporter-period-secs";
  public static final String CONSOLE_REPORTER_PERIOD_DEFAULT_VALUE = "60";

  public static final String MIRROR_BREAKER_WORKING_MODE_SYSTEM_PROPERTY = "mirror-breaker-working-mode";
  public static final String MIRROR_BREAKER_WORKING_MODE_DEFAULT_VALUE = "NORMAL";

  public static final String SUCCESS_RESPONSE = "Success";
}
