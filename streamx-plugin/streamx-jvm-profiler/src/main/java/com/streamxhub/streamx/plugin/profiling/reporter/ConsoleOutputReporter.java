/*
 * Copyright 2019 The StreamX Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.streamxhub.streamx.plugin.profiling.reporter;

import com.streamxhub.streamx.plugin.profiling.Reporter;
import com.streamxhub.streamx.plugin.profiling.util.AgentLogger;
import com.streamxhub.streamx.plugin.profiling.util.Utils;

import java.util.Map;

public class ConsoleOutputReporter implements Reporter {

    private static final AgentLogger LOGGER = AgentLogger.getLogger(ConsoleOutputReporter.class.getName());

    @Override
    public void report(String profilerName, Map<String, Object> metrics) {
        LOGGER.info(String.format("ConsoleOutputReporter - %s: %s", profilerName, Utils.toJsonString(metrics)));
    }

    @Override
    public void close() {
    }
}
