package com.ilovepc.project_home.config.security.vo;

import lombok.Data;
import lombok.ToString;
import org.springframework.mobile.device.DevicePlatform;

@Data
@ToString
public class UserAgent {
    private DevicePlatform deviceType;
    private String deviceId;
    private String appVersion;
    private String phoneVersion;
    private String appProvider;
    private int iAppVersion;
    private String serviceType;
    private String smtpToken;
}
