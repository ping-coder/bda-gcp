/*
 * Copyright (C) 2024 Ping He
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ping.coder.dba.datamaker.model;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.Date;

/**
 * @Description @TODO
 * @Author Ping
 * @Date 2024/5/21 21:41
 **/
public class DeviceRecord {

    private String deviceId;
    private String deviceType;
    private String version;
    private Date timestamp;
    private int retry;
    /**
     * Json type
     */
    @JsonRawValue
    private String deviceData;
    /**
     * Json type
     */
    @JsonRawValue
    private String sensorData;
    /**
     * Json type
     */
    @JsonRawValue
    private String sensorMetadata;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public String getDeviceData() {
        return deviceData;
    }

    public void setDeviceData(String deviceData) {
        this.deviceData = deviceData;
    }

    public String getSensorData() {
        return sensorData;
    }

    public void setSensorData(String sensorData) {
        this.sensorData = sensorData;
    }

    public String getSensorMetadata() {
        return sensorMetadata;
    }

    public void setSensorMetadata(String sensorMetadata) {
        this.sensorMetadata = sensorMetadata;
    }

    @Override
    public String toString() {
        return "DeviceRecord{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", version='" + version + '\'' +
                ", timestamp=" + timestamp +
                ", retry=" + retry +
                ", deviceData='" + deviceData + '\'' +
                ", sensorData='" + sensorData + '\'' +
                ", sensorMetadata='" + sensorMetadata + '\'' +
                '}';
    }

}
