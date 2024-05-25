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

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

/**
 * @Description @TODO
 * @Author Ping
 * @Date 2024/5/23 0:04
 **/
public interface Writer {

    void open() throws IOException;
    void close();
    void write(Object record);
}