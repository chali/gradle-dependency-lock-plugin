/**
 *
 *  Copyright 2019 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package nebula.plugin.dependencylock.utils

import nebula.plugin.responsible.NebulaIntegTestPlugin
import nebula.test.ProjectSpec
import org.gradle.api.artifacts.Configuration

class GradleVersionUtilsProjectSpec extends ProjectSpec {
    def setup() {
        project.apply plugin: 'java-library'
    }

    def "compile, compileOnly, runtime, testCompile, testCompileOnly, and testRuntime should not be resolved after Gradle 6.0"() {
        when:
        def results = GradleVersionUtils.findAllConfigurationsThatResolveButHaveAlternatives(project)

        then:
        if (GradleVersionUtils.currentGradleVersionIsLessThan('6.0')) {
            assert results.size() == 0
        } else {
            assert results.size() == 6

            Collection<String> configurationNames = results.collect { (it as Configuration).name }
            assert configurationNames.contains('compile')
            assert configurationNames.contains('compileOnly')
            assert configurationNames.contains('runtime')
            assert configurationNames.contains('testCompile')
            assert configurationNames.contains('testCompileOnly')
            assert configurationNames.contains('testRuntime')
        }
    }

    def "facets with similar configurations should not be resolved after Gradle 6.0"() {
        given:
        project.apply plugin: NebulaIntegTestPlugin.class
        project.facets {
            integTest {
                parentSourceSet = 'test'
            }
        }

        when:
        def results = GradleVersionUtils.findAllConfigurationsThatResolveButHaveAlternatives(project)

        then:
        if (GradleVersionUtils.currentGradleVersionIsLessThan('6.0')) {
            assert results.size() == 0
        } else {
            assert results.size() == 9

            Collection<String> configurationNames = results.collect { (it as Configuration).name }
            assert configurationNames.contains('compile')
            assert configurationNames.contains('compileOnly')
            assert configurationNames.contains('runtime')
            assert configurationNames.contains('testCompile')
            assert configurationNames.contains('testCompileOnly')
            assert configurationNames.contains('testRuntime')
            assert configurationNames.contains('integTestCompile')
            assert configurationNames.contains('integTestCompileOnly')
            assert configurationNames.contains('integTestRuntime')
        }
    }
}
