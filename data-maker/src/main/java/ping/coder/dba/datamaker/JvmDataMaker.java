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

import com.google.common.base.Strings;
import ping.coder.dba.datamaker.pubsub.PublisherWriter;

import java.io.IOException;

/**
 * @Description @TODO
 * @Author Ping
 * @Date 2024/5/22 11:55
 **/
public class JvmDataMaker extends DataMaker{

    private final Writer writer;

    protected JvmDataMaker(String recordFactoryClassName, Writer writer, Options options) {
        super(recordFactoryClassName, options);
        this.writer = writer;
    }

    public static void main(String[] args) throws Exception {
        String workerIndex = System.getenv().get("DATA_MAKER.WORKER_INDEX");
        if(workerIndex==null || workerIndex.isEmpty())
            workerIndex = "0";
        String workerNumber = System.getenv().get("DATA_MAKER.WORKER_NUMBER");
        if(workerNumber==null || workerNumber.isEmpty())
            workerNumber = "1";
        String modelNumber = System.getenv().get("DATA_MAKER.MODEL_NUMBER");
        if(modelNumber==null || modelNumber.isEmpty())
            modelNumber = "10";
        String deviceNumber = System.getenv().get("DATA_MAKER.DEVICE_NUMBER");
        if(deviceNumber==null || deviceNumber.isEmpty())
            deviceNumber = "1000";
        String interval = System.getenv().get("DATA_MAKER.INTERVAL");
        if(interval==null || interval.isEmpty())
            interval = "1000";
        String loopNumber = System.getenv().get("DATA_MAKER.LOOP_NUMBER");
        if(loopNumber==null || loopNumber.isEmpty())
            loopNumber = "3";

        Options options = new Options();
        options.setWorkerIndex(Integer.parseInt(workerIndex));
        options.setWorkerNumber(Integer.parseInt(workerNumber));
        options.setInterval(Integer.parseInt(interval));
        options.setModelNumber(Integer.parseInt(modelNumber));
        options.setDeviceNumber(Integer.parseInt(deviceNumber));
        options.setLoopNumber(Integer.parseInt(loopNumber));

        String recordFactoryClass = System.getenv().get("DATA_MAKER.RECORD_FACTORY");
        String projectId = System.getenv().get("DATA_MAKER.PROJECT_ID");
        if(Strings.isNullOrEmpty(projectId))
            throw new IllegalArgumentException("Project id is empty.");
        String topicId = System.getenv().get("DATA_MAKER.TOPIC_ID");
        if(Strings.isNullOrEmpty(topicId))
            throw new IllegalArgumentException("Topic id is empty.");
        Writer writer = new PublisherWriter(projectId, topicId);
        JvmDataMaker maker = new JvmDataMaker(recordFactoryClass, writer, options);
        maker.run();
    }

    @Override
    protected void makeRecord(Object record) {
        try {
            writer.open();
            writer.write(record);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            writer.close();
        }
    }
}
