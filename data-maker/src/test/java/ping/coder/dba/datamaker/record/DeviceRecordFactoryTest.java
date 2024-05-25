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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ping.coder.dba.datamaker.model.DeviceRecord;

import java.util.HashSet;


/**
 * @Description @TODO
 * @Author Ping
 * @Date 2024/5/22 1:03
 **/
public class DeviceRecordFactoryTest {

    @Test
    void testNewRecord() throws JsonProcessingException {
        DeviceRecordFactory factory = DeviceRecordFactory.builder().models(100).build();
        System.out.println("**********************JSON STARTED********************************");
        System.out.println(new ObjectMapper().writeValueAsString(factory.newRecord()));
        System.out.println("**********************JSON ENDED**********************************");
        for (int i = 0; i < 3; i++) {
            DeviceRecord r = factory.newRecord();
            System.out.println(i+" : "+r);
        }

        HashSet<String> typeSet = new HashSet<String>();
        HashSet<String> idSet = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            DeviceRecord deviceRecord = factory.newRecord();
            typeSet.add(deviceRecord.getDeviceType());
            idSet.add(deviceRecord.getDeviceId());
        }
        Assertions.assertTrue(typeSet.size() <= 100, "type size was "+typeSet.size());
        Assertions.assertTrue(idSet.size() >= 100, "id size was "+idSet.size());
    }

}