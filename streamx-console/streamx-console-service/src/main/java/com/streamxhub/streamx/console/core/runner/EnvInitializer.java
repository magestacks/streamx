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

package com.streamxhub.streamx.console.core.runner;

import static com.streamxhub.streamx.common.enums.StorageType.LFS;

import com.streamxhub.streamx.common.conf.CommonConfig;
import com.streamxhub.streamx.common.conf.ConfigConst;
import com.streamxhub.streamx.common.conf.InternalConfigHolder;
import com.streamxhub.streamx.common.conf.InternalOption;
import com.streamxhub.streamx.common.conf.Workspace;
import com.streamxhub.streamx.common.enums.StorageType;
import com.streamxhub.streamx.common.fs.FsOperator;
import com.streamxhub.streamx.common.util.AssertUtils;
import com.streamxhub.streamx.common.util.SystemPropertyUtils;
import com.streamxhub.streamx.console.base.util.WebUtils;
import com.streamxhub.streamx.console.core.entity.FlinkEnv;
import com.streamxhub.streamx.console.core.service.SettingService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author benjobs
 */
@Order
@Slf4j
@Component
public class EnvInitializer implements ApplicationRunner {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SettingService settingService;

    private final Map<StorageType, Boolean> initialized = new ConcurrentHashMap<>(2);

    private final FileFilter fileFilter = p -> !".gitkeep".equals(p.getName());

    private static final Pattern PATTERN_FLINK_SHIMS_JAR = Pattern.compile(
        "^streamx-flink-shims_flink-(1.12|1.13|1.14|1.15)_(2.11|2.12)-(.*).jar$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String appHome = WebUtils.getAppHome();
        if (appHome == null) {
            throw new ExceptionInInitializerError("[StreamX] System initialization check failed," +
                " The system initialization check failed. If started local for development and debugging," +
                " please ensure the -Dapp.home parameter is clearly specified," +
                " more detail: http://www.streamxhub.com/docs/user-guide/development");
        }

        // init InternalConfig
        initInternalConfig(context.getEnvironment());
        // overwrite system variable HADOOP_USER_NAME
        String hadoopUserName = InternalConfigHolder.get(CommonConfig.STREAMX_HADOOP_USER_NAME());
        overrideSystemProp(ConfigConst.KEY_HADOOP_USER_NAME(), hadoopUserName);
        // initialize local file system resources
        storageInitialize(LFS);
    }

    private void initInternalConfig(Environment springEnv) {
        // override config from spring application.yaml
        InternalConfigHolder
            .keys()
            .stream()
            .filter(springEnv::containsProperty)
            .forEach(key -> {
                InternalOption config = InternalConfigHolder.getConfig(key);
                assert config != null;
                InternalConfigHolder.set(config, springEnv.getProperty(key, config.classType()));
            });

        String mvnRepository = settingService.getMavenRepository();
        if (StringUtils.isNotEmpty(mvnRepository)) {
            InternalConfigHolder.set(CommonConfig.MAVEN_REMOTE_URL(), mvnRepository);
        }

        String mvnAuthUser = settingService.getMavenAuthUser();
        if (StringUtils.isNotEmpty(mvnAuthUser)) {
            InternalConfigHolder.set(CommonConfig.MAVEN_AUTH_USER(), mvnAuthUser);
        }

        String mvnAuthPassword = settingService.getMavenAuthPassword();
        if (StringUtils.isNotEmpty(mvnAuthPassword)) {
            InternalConfigHolder.set(CommonConfig.MAVEN_AUTH_PASSWORD(), mvnAuthPassword);
        }

        InternalConfigHolder.log();
    }

    private void overrideSystemProp(String key, String defaultValue) {
        String value = context.getEnvironment().getProperty(key, defaultValue);
        log.info("initialize system properties: key:{}, value:{}", key, value);
        SystemPropertyUtils.set(key, value);
    }

    /**
     * @param storageType
     */
    public synchronized void storageInitialize(StorageType storageType) {

        final String mkdirLog = "storage initialize, now mkdir [{}] starting ...";

        if (initialized.get(storageType) == null) {
            FsOperator fsOperator = FsOperator.of(storageType);
            Workspace workspace = Workspace.of(storageType);

            // 1. prepare workspace dir
            if (storageType.equals(LFS)) {
                String localDist = workspace.APP_LOCAL_DIST();
                if (!fsOperator.exists(localDist)) {
                    log.info(mkdirLog, localDist);
                    fsOperator.mkdirs(localDist);
                }
            }

            String appUploads = workspace.APP_UPLOADS();
            if (!fsOperator.exists(appUploads)) {
                log.info(mkdirLog, appUploads);
                fsOperator.mkdirs(appUploads);
            }

            String appWorkspace = workspace.APP_WORKSPACE();
            if (!fsOperator.exists(appWorkspace)) {
                log.info(mkdirLog, appWorkspace);
                fsOperator.mkdirs(appWorkspace);
            }

            String appBackups = workspace.APP_BACKUPS();
            if (!fsOperator.exists(appBackups)) {
                log.info(mkdirLog, appBackups);
                fsOperator.mkdirs(appBackups);
            }

            String appSavePoints = workspace.APP_SAVEPOINTS();
            if (!fsOperator.exists(appSavePoints)) {
                log.info(mkdirLog, appSavePoints);
                fsOperator.mkdirs(appSavePoints);
            }

            String appJars = workspace.APP_JARS();
            if (!fsOperator.exists(appJars)) {
                log.info(mkdirLog, appJars);
                fsOperator.mkdirs(appJars);
            }

            // 2. upload jar.
            // 2.1) upload client jar
            File client = WebUtils.getAppClientDir();
            AssertUtils.checkArgument(
                client.exists() && client.listFiles().length > 0,
                client.getAbsolutePath().concat(" is not exists or empty directory ")
            );

            String appClient = workspace.APP_CLIENT();
            fsOperator.mkCleanDirs(appClient);

            for (File file : client.listFiles(fileFilter)) {
                log.info("load client:{} to {}", file.getName(), appClient);
                fsOperator.upload(file.getAbsolutePath(), appClient);
            }

            // 2.2) upload plugin jar.
            String appPlugins = workspace.APP_PLUGINS();
            fsOperator.mkCleanDirs(appPlugins);

            File plugins = WebUtils.getAppPluginsDir();
            for (File file : plugins.listFiles(fileFilter)) {
                log.info("load plugin:{} to {}", file.getName(), appPlugins);
                fsOperator.upload(file.getAbsolutePath(), appPlugins);
            }

            // 2.3) upload shims jar
            File[] shims = WebUtils.getAppLibDir()
                .listFiles(pathname -> pathname.getName().matches(PATTERN_FLINK_SHIMS_JAR.pattern()));

            AssertUtils.checkArgument(
                shims != null && shims.length > 0,
                "streamx-flink-shims jar not exist"
            );

            String appShims = workspace.APP_SHIMS();
            fsOperator.delete(appShims);

            for (File file : shims) {
                Matcher matcher = PATTERN_FLINK_SHIMS_JAR.matcher(file.getName());
                if (matcher.matches()) {
                    String version = matcher.group(1);
                    String shimsPath = appShims.concat("/flink-").concat(version);
                    fsOperator.mkdirs(shimsPath);
                    log.info("load shims:{} to {}", file.getName(), shimsPath);
                    fsOperator.upload(file.getAbsolutePath(), shimsPath);
                }
            }

            // 2.4) create maven local repository dir

            String localMavenRepo = workspace.MAVEN_LOCAL_DIR();
            if (FsOperator.lfs().exists(localMavenRepo)) {
                FsOperator.lfs().mkdirs(localMavenRepo);
            }

            initialized.put(storageType, Boolean.TRUE);
        }
    }

    public void checkFlinkEnv(StorageType storageType, FlinkEnv flinkEnv) throws IOException {
        String flinkLocalHome = flinkEnv.getFlinkHome();
        if (flinkLocalHome == null) {
            throw new ExceptionInInitializerError("[StreamX] FLINK_HOME is undefined,Make sure that Flink is installed.");
        }
        Workspace workspace = Workspace.of(storageType);
        String appFlink = workspace.APP_FLINK();
        FsOperator fsOperator = FsOperator.of(storageType);
        if (!fsOperator.exists(appFlink)) {
            log.info("checkFlinkEnv, now mkdir [{}] starting ...", appFlink);
            fsOperator.mkdirs(appFlink);
        }
        File flinkLocalDir = new File(flinkLocalHome);
        if (Files.isSymbolicLink(flinkLocalDir.toPath())) {
            flinkLocalDir = flinkLocalDir.getCanonicalFile();
        }
        String flinkName = flinkLocalDir.getName();
        String flinkHome = appFlink.concat("/").concat(flinkName);
        if (!fsOperator.exists(flinkHome)) {
            log.info("{} is not exists,upload beginning....", flinkHome);
            fsOperator.upload(flinkLocalHome, flinkHome, false, true);
        }
    }

}
