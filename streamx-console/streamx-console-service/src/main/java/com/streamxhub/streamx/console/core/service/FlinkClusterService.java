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

package com.streamxhub.streamx.console.core.service;

import com.streamxhub.streamx.console.core.bean.ResponseResult;
import com.streamxhub.streamx.console.core.entity.FlinkCluster;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author benjobs
 */
public interface FlinkClusterService extends IService<FlinkCluster> {

    /**
     * check
     *
     * @param flinkCluster
     * @return
     */
    String check(FlinkCluster flinkCluster);

    ResponseResult create(FlinkCluster flinkCluster);

    ResponseResult delete(FlinkCluster flinkCluster);

    ResponseResult update(FlinkCluster flinkCluster);

    ResponseResult start(FlinkCluster flinkCluster);

    ResponseResult shutdown(FlinkCluster flinkCluster);
}
