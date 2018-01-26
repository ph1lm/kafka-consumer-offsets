package io.confluent.consumer.offsets.mirror.infrastructure;


import io.confluent.consumer.offsets.mirror.MirrorBreakerMode;
import lombok.Getter;

@Getter
public class MirrorHandlerProperties {
  private final long idleStateTimeoutSecs;
  private final int consoleReporterPeriod;
  private final int socketAddress;
  private final MirrorBreakerMode mirrorBreakerWorkingMode;

  public MirrorHandlerProperties() {
    this.idleStateTimeoutSecs = Integer
        .parseInt(System.getProperty(MirrorHandlerConstants.IDLE_STATE_TIMEOUT_SYSTEM_PROPERTY,
            MirrorHandlerConstants.IDLE_STATE_TIMEOUT_DEFAULT_VALUE));
    this.consoleReporterPeriod = Integer
        .parseInt(System.getProperty(MirrorHandlerConstants.CONSOLE_REPORTER_PERIOD_SYSTEM_PROPERTY,
            MirrorHandlerConstants.CONSOLE_REPORTER_PERIOD_DEFAULT_VALUE));
    this.socketAddress = Integer
        .parseInt(System.getProperty(MirrorHandlerConstants.SOCKET_ADDRESS_SYSTEM_PROPERTY,
            MirrorHandlerConstants.SOCKET_ADDRESS_DEFAULT_VALUE));
    this.mirrorBreakerWorkingMode = MirrorBreakerMode
        .valueOf(System.getProperty(MirrorHandlerConstants.MIRROR_BREAKER_WORKING_MODE_SYSTEM_PROPERTY,
            MirrorHandlerConstants.MIRROR_BREAKER_WORKING_MODE_DEFAULT_VALUE));
  }
}
