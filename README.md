# Project Name

limelink-aos-sdk

## Getting Started

This section guides you on how to set up and run this project locally.

### Prerequisites


1. API Permissions and Consent:
• Internet Permission: To call external APIs, you need to declare the INTERNET permission in the AndroidManifest.xml file.
```xml
<uses-permission android:name="android.permission.INTERNET" />
```
• Privacy Law and User Consent: If the data collected through external API calls is considered personal information, explicit user consent may be required. For example, under GDPR in Europe or the Personal Information Protection Act in Korea, users must be clearly informed about what data is being collected and how it will be used, and they must give their consent.

2. Network Security Configuration:
   • Use HTTPS Instead of HTTP: It is recommended to use HTTPS for external API calls to ensure security.
   • Network Security Configuration: Starting from API 28 (Android 9.0 Pie), Cleartext (HTTP) is blocked by default. Explicitly configure the network security settings.
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config">
</application>
```
Create the res/xml/network_security_config.xml file:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">limelink.org</domain>
        <domain includeSubdomains="true">deep.limelink.org</domain>
    </domain-config>
</network-security-config>
```

By adhering to the above guidelines, Android developers should face minimal issues when integrating and using an SDK for calling external APIs.


# SDK Integration Guide

This guide provides essential steps and best practices for integrating our SDK, which includes making external API calls. Please ensure you follow these instructions carefully to avoid any issues.