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

package ping.coder.dba.datamaker.record;

import net.datafaker.providers.base.BaseFaker;
import net.datafaker.transformations.Field;
import net.datafaker.transformations.JsonTransformer;
import net.datafaker.transformations.Schema;
import ping.coder.dba.datamaker.model.DeviceModel;
import ping.coder.dba.datamaker.model.DeviceRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description @TODO
 * @Author Ping
 * @Date 2024/5/21 21:47
 **/
public class DeviceRecordFactory implements RecordFactory<DeviceRecord> {
    private static final BaseFaker baseFaker = new BaseFaker();
    private static final Schema<Object, Object> dataSchema = Schema.of(
            Field.field("latitude", () -> baseFaker.random().nextDouble(-90, 90)),
            Field.field("longitude", () -> baseFaker.random().nextDouble(-180, 180)),
            Field.field("production_date", () -> baseFaker.date().past(365 * 5, TimeUnit.DAYS)),
            Field.field("floor", () -> baseFaker.random().nextInt(32) + 1),
            Field.field("room", () -> baseFaker.funnyName().name())
    );
    private static final Schema<Object, Object> sensorSchema = Schema.of(
            Field.field("temperature", () -> baseFaker.random().nextInt(-30, 50)),
            Field.field("humidity", () -> baseFaker.random().nextInt(0, 100)),
            Field.field("occupancy", () -> baseFaker.random().nextInt(0, 100)),
            Field.field("occupied", () -> baseFaker.random().nextInt(1)),
            Field.field("speed", () -> baseFaker.random().nextInt(400))
    );
    private static final Schema<Object, ?> metadataSchema = Schema.of(
            Field.field("signal", () -> baseFaker.random().nextInt(1)),
            Field.compositeField("status", new Field[]{
                    Field.field("battery", () -> baseFaker.random().nextDouble(0,1)),
                    Field.field("network", () -> baseFaker.random().nextDouble(0,1))
            })
    );
    private static final JsonTransformer<Object> transformer = JsonTransformer.builder().build();
    public static final String DEVICE_FORMAT = "D%s-%s-%s";
    public static final String MODEL_FORMAT = "M%08d";
    public static final String DEVICE_NUMBER_FORMAT = "S%08d";
    public static final String VERSION_FORMAT = "ver.%d.%d.%d";
    public static final String DEFAULT_WORKER_ID = "0";
    public static final int DEFAULT_DEVICE_NUMBER = 1000;
    public static final int DEFAULT_MODEL_NUMBER = 10;

    private final String workerId;
    private final List<DeviceModel> modelList;
    private final int deviceNumber;
    private final String numberFormat;

    private DeviceRecordFactory(String workerId, List<DeviceModel> modelList, int deviceNumber, String numberFormat) {
        this.workerId = workerId;
        this.modelList = modelList;
        this.deviceNumber = deviceNumber;
        this.numberFormat = numberFormat;
    }

    @Override
    public DeviceRecord newRecord() {
        DeviceModel model = modelList.get(baseFaker.random().nextInt(modelList.size()));
        DeviceRecord record = new DeviceRecord();
        record.setDeviceId(buildDeviceId(workerId, model.getDeviceModelId(), String.format(numberFormat, baseFaker.random().nextInt(deviceNumber))));
        record.setDeviceType(model.getDeviceType());
        record.setVersion(model.getVersion());
        record.setRetry(baseFaker.random().nextInt(5));
        record.setTimestamp(baseFaker.date().past(5, TimeUnit.HOURS));
        record.setDeviceData(model.getDeviceData());
        record.setSensorMetadata(model.getSensorMetadata());
        record.setSensorData(transformer.generate(sensorSchema, 1));
        return record;
    }

    private static DeviceModel createDeviceMode(int index, String modelFormat){
        DeviceModel model = new DeviceModel();
        model.setDeviceModelId(String.format(modelFormat, index));
        model.setDeviceType(baseFaker.brand().car());
        model.setVersion(buildVersion());
        model.setDeviceData(transformer.generate(dataSchema,1));
        model.setSensorMetadata(transformer.generate(metadataSchema, 1));
        return model;
    }

    private static String buildVersion() {
        return String.format(VERSION_FORMAT, baseFaker.random().nextInt(2), baseFaker.random().nextInt(2), baseFaker.random().nextInt(6));
    }

    private static String buildDeviceId(String workerId, String deviceModelId, String deviceNumber){
        return String.format(DEVICE_FORMAT, workerId, deviceModelId, deviceNumber);
    }

    public static class DeviceRecordFactoryBuilder{
        private String workerId;
        private List<DeviceModel> modelList;
        private int deviceNumber;
        private String numberFormat;
        private DeviceRecordFactoryBuilder(){
        }
        public DeviceRecordFactoryBuilder workerId(String workerId){
            this.workerId = workerId;
            return this;
        }
        public DeviceRecordFactoryBuilder modelList(List<DeviceModel> modelList){
            if(modelList==null || modelList.isEmpty())
                throw new IllegalArgumentException("The size of model should be more than zero.");
            this.modelList = modelList;
            return this;
        }
        public DeviceRecordFactoryBuilder models(int length){
            if(length<=0)
                throw new IllegalArgumentException("The size of model should be more than zero.");
            modelList = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                modelList.add(createDeviceMode(i+1, MODEL_FORMAT));
            }
            return modelList(modelList);
        }
        public DeviceRecordFactoryBuilder deviceNumber(int number){
            if(number<=0)
                throw new IllegalArgumentException("The number of device should be more than zero.");
            this.deviceNumber = number;
            return this;
        }
        public DeviceRecordFactoryBuilder deviceNumberFormat(String format){
            this.numberFormat = format;
            return this;
        }
        public DeviceRecordFactory build(){
            if(modelList==null || modelList.isEmpty())
                models(DEFAULT_MODEL_NUMBER);
            if(workerId == null)
                workerId(DEFAULT_WORKER_ID);
            if(deviceNumber <=0 )
                deviceNumber(DEFAULT_DEVICE_NUMBER);
            if(numberFormat == null || numberFormat.isEmpty())
                deviceNumberFormat(DEVICE_NUMBER_FORMAT);
            return new DeviceRecordFactory(workerId, modelList, deviceNumber, numberFormat);
        }
    }

    public static DeviceRecordFactoryBuilder builder(){
        return new DeviceRecordFactoryBuilder();
    }
}
