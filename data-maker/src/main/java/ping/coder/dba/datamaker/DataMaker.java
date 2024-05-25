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

package ping.coder.dba.datamaker;

import ping.coder.dba.datamaker.record.DeviceRecordFactory;
import ping.coder.dba.datamaker.record.RecordFactory;

/**
 * @Description @TODO
 * @Author Ping
 * @Date 2024/5/21 21:16
 **/
public abstract class DataMaker {

    public static class Options{

        /**
         * worker index, should be less than worker number.
         */
        private int workerIndex;

        /**
         * total worker number, should be more than 1.
         */
        private int workerNumber;

        /**
         * interval of making data, time unit is millisecond.
         */
        private int interval;

        private int modelNumber;

        private int deviceNumber;

        /**
         * If it's zero, should be always looping .
         */
        private int loopNumber;


        public int getWorkerIndex() {
            return workerIndex;
        }

        public void setWorkerIndex(int workerIndex) {
            if(workerIndex < 0)
                throw new IllegalArgumentException("worker index was "+workerIndex);
            this.workerIndex = workerIndex;
        }

        public int getWorkerNumber() {
            return workerNumber;
        }

        public void setWorkerNumber(int workerNumber) {
            if(workerNumber <= 0)
                throw new IllegalArgumentException("worker number was "+workerNumber);
            this.workerNumber = workerNumber;
        }

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            if(interval < 0)
                throw new IllegalArgumentException("interval was "+interval);
            this.interval = interval;
        }

        public int getModelNumber() {
            return modelNumber;
        }

        public void setModelNumber(int modelNumber) {
            this.modelNumber = modelNumber;
        }

        public int getDeviceNumber() {
            return deviceNumber;
        }

        public void setDeviceNumber(int deviceNumber) {
            this.deviceNumber = deviceNumber;
        }

        public int getLoopNumber() {
            return loopNumber;
        }

        public void setLoopNumber(int loopNumber) {
            this.loopNumber = loopNumber;
        }
    }

    protected final Options options;
    protected RecordFactory<?> recordFactory;

    public void run() throws Exception{
        int loopTimes = 0;
        while (true){
            makeRecord(recordFactory.newRecord());
            if(!needLoop(loopTimes))
                break;
            else if(options.interval>0)
                Thread.sleep(options.interval);
        }
    }

    private boolean needLoop(int loopTimes){
        return options.loopNumber == 0 || loopTimes < options.loopNumber;
    }

    protected abstract void makeRecord(Object record);

    protected DataMaker(String recordFactoryClassName, Options options){
        assert options != null;
        this.options = options;
        this.initInternal(recordFactoryClassName);
    }

    private void initInternal(String recordFactoryClassName){
        if(recordFactoryClassName==null || recordFactoryClassName.isEmpty() || DeviceRecordFactory.class.getName().equals(recordFactoryClassName))
            this.recordFactory = buildDeviceRecordFactory();
        else
            throw new IllegalArgumentException(String.format("Error record factory class name is: %s", recordFactoryClassName));
    }

    protected DeviceRecordFactory buildDeviceRecordFactory(){
        return DeviceRecordFactory.builder().workerId(buildWorkerId()).models(options.modelNumber).deviceNumber(options.deviceNumber).build();
    }

    protected String buildWorkerId(){
        int length = options.workerNumber < 10 ? 1 : (int)Math.log10(options.workerNumber) + 1;
        return String.format("%0"+length+"d", options.workerIndex);
    }
}
