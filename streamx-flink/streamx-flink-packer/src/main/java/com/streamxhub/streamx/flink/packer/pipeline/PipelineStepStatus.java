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

package com.streamxhub.streamx.flink.packer.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * <pre>
 * Status of per step of building pipeline.
 * state machine:
 * ┌──────────────────────────────────┐
 * │                     ┌─► success  │
 * │ pending ─► running ─┤            │
 * │              │      └─► failure  │
 * │              │             │     │
 * │              │             ▼     │
 * │              └────────► skipped  │
 * └──────────────────────────────────┘
 * </pre>
 *
 * @author Al-assad
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum PipelineStepStatus {

    unknown(0),
    waiting(1),
    running(2),
    success(3),
    failure(4),
    skipped(5);

    private final Integer code;

    PipelineStepStatus(Integer code) {
        this.code = code;
    }

    @JsonCreator
    public static PipelineStepStatus of(Integer code) {
        for (PipelineStepStatus status : PipelineStepStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return unknown;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    public boolean isUnknown() {
        return this == unknown;
    }

}
