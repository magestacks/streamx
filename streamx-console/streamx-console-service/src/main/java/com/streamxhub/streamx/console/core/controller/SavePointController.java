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

package com.streamxhub.streamx.console.core.controller;

import com.streamxhub.streamx.console.base.domain.RestRequest;
import com.streamxhub.streamx.console.base.domain.RestResponse;
import com.streamxhub.streamx.console.base.exception.InternalException;
import com.streamxhub.streamx.console.core.entity.Application;
import com.streamxhub.streamx.console.core.entity.SavePoint;
import com.streamxhub.streamx.console.core.service.ApplicationService;
import com.streamxhub.streamx.console.core.service.SavePointService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author benjobs
 */
@Slf4j
@Validated
@RestController
@RequestMapping("flink/savepoint")
public class SavePointController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private SavePointService savePointService;

    @PostMapping("latest")
    public RestResponse latest(Long appId) {
        SavePoint savePoint = savePointService.getLatest(appId);
        return RestResponse.success(savePoint);
    }

    @PostMapping("history")
    public RestResponse history(SavePoint savePoint, RestRequest request) {
        IPage<SavePoint> page = savePointService.page(savePoint, request);
        return RestResponse.success(page);
    }

    @PostMapping("delete")
    @RequiresPermissions("savepoint:delete")
    public RestResponse delete(Long id) throws InternalException {
        SavePoint savePoint = savePointService.getById(id);
        Application application = applicationService.getById(savePoint.getAppId());
        Boolean deleted = savePointService.delete(id, application);
        return RestResponse.success(deleted);
    }
}
