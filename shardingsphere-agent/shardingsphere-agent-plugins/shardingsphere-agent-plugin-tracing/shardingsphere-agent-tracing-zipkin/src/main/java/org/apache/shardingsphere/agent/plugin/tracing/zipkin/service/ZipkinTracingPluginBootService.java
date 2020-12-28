/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.agent.plugin.tracing.zipkin.service;

import brave.Tracing;
import org.apache.shardingsphere.agent.core.config.PluginConfiguration;
import org.apache.shardingsphere.agent.core.plugin.service.PluginBootService;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

/**
 * Zipkin tracing plugin boot service.
 */
public final class ZipkinTracingPluginBootService implements PluginBootService {
    
    private AsyncZipkinSpanHandler zipkinSpanHandler;
    
    private OkHttpSender sender;
    
    private Tracing tracing;
    
    @Override
    public void start(final PluginConfiguration configuration) {
        sender = OkHttpSender.create(buildHttpPath(configuration));
        zipkinSpanHandler = AsyncZipkinSpanHandler.create(sender);
        tracing = Tracing.newBuilder().localServiceName("shardingsphere-agent").addSpanHandler(zipkinSpanHandler).build();
    }
    
    @Override
    public void close() {
        tracing.close();
        zipkinSpanHandler.close();
        sender.close();
    }
    
    @Override
    public String getType() {
        return "Zipkin";
    }
    
    private String buildHttpPath(final PluginConfiguration configuration) {
        return String.format("http://%s:%s", configuration.getHost(), configuration.getPort());
    }
}
