/*
 * Copyright 2014-2019 Netflix, Inc.
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
package nebula.plugin.dependencylock.tasks

import nebula.plugin.dependencylock.ConfigurationsToLockFinder
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.tasks.OutputDirectory

abstract class AbstractMigrateToCoreLocksTask extends DefaultTask {
    String group = 'Locking'

    @OutputDirectory
    File outputLocksDirectory

    Set<String> configurationNames

    void lockSelectedConfigurations() {
        project.gradle.taskGraph.whenReady { TaskExecutionGraph taskGraph ->
            if (project.hasProperty("lockAllConfigurations") && (project.property("lockAllConfigurations") as String).toBoolean()) {
                project.dependencyLocking {
                    it.lockAllConfigurations()
                }
            } else {
                def configurationsToLock = new ConfigurationsToLockFinder(project)
                        .findConfigurationsToLock(getConfigurationNames())
                project.configurations.each {
                    if (configurationsToLock.contains(it.name)) {
                        it.resolutionStrategy.activateDependencyLocking()
                    }
                }
            }
        }
    }

    Collection<Configuration> lockableConfigurations() {
        GenerateLockTask.lockableConfigurations(project, project, getConfigurationNames())
    }
}