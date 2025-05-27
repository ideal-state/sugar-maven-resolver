/*
 *    Copyright 2025 ideal-state
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package team.idealstate.sugar.maven.resolver;

import java.util.Objects;
import team.idealstate.sugar.maven.resolver.api.Dependency;
import team.idealstate.sugar.maven.resolver.api.DependencyScope;
import team.idealstate.sugar.validate.Validation;
import team.idealstate.sugar.validate.annotation.NotNull;

final class ApacheDependency implements Dependency {

    private final org.eclipse.aether.graph.Dependency dependency;

    public ApacheDependency(@NotNull org.eclipse.aether.graph.Dependency dependency) {
        Validation.notNull(dependency, "Dependency must not be null.");
        this.dependency = dependency;
    }

    @NotNull
    @Override
    public String getGroupId() {
        return dependency.getArtifact().getGroupId();
    }

    @NotNull
    @Override
    public String getArtifactId() {
        return dependency.getArtifact().getArtifactId();
    }

    @NotNull
    @Override
    public String getExtension() {
        return dependency.getArtifact().getExtension();
    }

    @NotNull
    @Override
    public String getClassifier() {
        return dependency.getArtifact().getClassifier();
    }

    @NotNull
    @Override
    public String getVersion() {
        return dependency.getArtifact().getBaseVersion();
    }

    @NotNull
    @Override
    public DependencyScope getScope() {
        return DependencyScope.of(dependency.getScope());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ApacheDependency that = (ApacheDependency) o;
        return Objects.equals(dependency, that.dependency);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dependency);
    }

    @Override
    public String toString() {
        return "ApacheDependency{" + "dependency=" + dependency + '}';
    }
}
