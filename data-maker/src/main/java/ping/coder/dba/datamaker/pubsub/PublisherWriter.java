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

package ping.coder.dba.datamaker.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.threeten.bp.Duration;
import ping.coder.dba.datamaker.Writer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description @TODO
 * @Author Ping
 * @Date 2024/5/23 0:01
 **/
public class PublisherWriter implements Writer {

    protected final String projectId;
    protected final String topicId;
    private final ObjectMapper mapper = new ObjectMapper();
    private Publisher publisher;

    public PublisherWriter(String projectId, String topicId) {
        this.projectId = projectId;
        this.topicId = topicId;
    }

    @Override
    public void write(Object record) {
        if(publisher==null)
            throw new IllegalStateException("Publisher isn't initialized.");

        try {
            String message = mapper.writeValueAsString(record);
            ByteString data = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
            // Once published, returns a server-assigned message id (unique within the topic)
            ApiFuture<String> future = publisher.publish(pubsubMessage);
            // Add an asynchronous callback to handle success / failure
            ApiFutures.addCallback(
                    future,
                    new ApiFutureCallback<String>() {
                        @Override
                        public void onFailure(Throwable throwable) {
                            if (throwable instanceof ApiException) {
                                ApiException apiException = ((ApiException) throwable);
                                // details on the API exception
                                System.out.println(apiException.getStatusCode().getCode());
                                System.out.println(apiException.isRetryable());
                            }else{
                                System.out.println(throwable.toString());
                            }
                            System.out.println("Error publishing message : " + message);
                        }
                        @Override
                        public void onSuccess(String messageId) {
                            System.out.println("Published message ID: " + messageId);
                        }
                    },
                    MoreExecutors.directExecutor());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Record can't be transform to Json: "+record.toString(), e);
        }
    }

    @Override
    public void open() throws IOException {
        TopicName topicName = TopicName.of(projectId, topicId);
        Duration initialRetryDelay = Duration.ofMillis(100); // default: 100 ms
        double retryDelayMultiplier = 2.0; // back off for repeated failures, default: 1.3
        Duration maxRetryDelay = Duration.ofSeconds(3); // default : 60 seconds
        Duration initialRpcTimeout = Duration.ofSeconds(5); // default: 5 seconds
        double rpcTimeoutMultiplier = 1.0; // default: 1.0
        Duration maxRpcTimeout = Duration.ofSeconds(10); // default: 600 seconds
        Duration totalTimeout = Duration.ofSeconds(30); // default: 600 seconds

        RetrySettings retrySettings = RetrySettings.newBuilder()
                                            .setInitialRetryDelay(initialRetryDelay)
                                            .setRetryDelayMultiplier(retryDelayMultiplier)
                                            .setMaxRetryDelay(maxRetryDelay)
                                            .setInitialRpcTimeout(initialRpcTimeout)
                                            .setRpcTimeoutMultiplier(rpcTimeoutMultiplier)
                                            .setMaxRpcTimeout(maxRpcTimeout)
                                            .setTotalTimeout(totalTimeout)
                                            .build();

        publisher = Publisher.newBuilder(topicName).setRetrySettings(retrySettings).build();
    }

    @Override
    public void close() {
        if(publisher!=null){
            publisher.shutdown();
            try {
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            publisher = null;
        }
    }

    public static void main(String[] args) throws Exception {
        String projectId = "meitu-pub";
        String topicId = "peace-demo";

        publishWithErrorHandlerExample(projectId, topicId);
    }
    public static void publishWithErrorHandlerExample(String projectId, String topicId)
            throws IOException, InterruptedException {
        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher publisher = null;

        try {
            // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName).build();

            List<String> messages = Arrays.asList("first message", "second message");

            for (final String message : messages) {
                ByteString data = ByteString.copyFromUtf8(message);
                PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

                // Once published, returns a server-assigned message id (unique within the topic)
                ApiFuture<String> future = publisher.publish(pubsubMessage);

                // Add an asynchronous callback to handle success / failure
                ApiFutures.addCallback(
                        future,
                        new ApiFutureCallback<String>() {

                            @Override
                            public void onFailure(Throwable throwable) {
                                if (throwable instanceof ApiException) {
                                    ApiException apiException = ((ApiException) throwable);
                                    // details on the API exception
                                    System.out.println(apiException.getStatusCode().getCode());
                                    System.out.println(apiException.isRetryable());
                                }
                                System.out.println("Error publishing message : " + message);
                                throwable.printStackTrace();
                            }

                            @Override
                            public void onSuccess(String messageId) {
                                // Once published, returns server-assigned message ids (unique within the topic)
                                System.out.println("Published message ID: " + messageId);
                            }
                        },
                        MoreExecutors.directExecutor());
            }
        } finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }
    }
}
