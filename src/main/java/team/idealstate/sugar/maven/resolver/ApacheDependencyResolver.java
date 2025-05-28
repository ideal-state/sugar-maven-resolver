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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import team.idealstate.sugar.maven.resolver.api.Dependency;
import team.idealstate.sugar.maven.resolver.api.DependencyResolver;
import team.idealstate.sugar.maven.resolver.api.DependencyScope;
import team.idealstate.sugar.maven.resolver.api.exception.MavenResolutionException;
import team.idealstate.sugar.validate.Validation;
import team.idealstate.sugar.validate.annotation.NotNull;
import team.idealstate.sugar.validate.annotation.Nullable;

final class ApacheDependencyResolver implements DependencyResolver {
    @NotNull
    @Override
    public Dependency resolve(
            @NotNull String groupId,
            @NotNull String artifactId,
            @Nullable String extension,
            @Nullable String classifier,
            @NotNull String version,
            @NotNull DependencyScope scope) {
        Validation.notNullOrBlank(groupId, "Group id must not be null.");
        Validation.notNullOrBlank(artifactId, "Artifact id must not be null.");
        Validation.notNullOrBlank(version, "Version must not be null.");
        return new ApacheDependency(new org.eclipse.aether.graph.Dependency(
                new org.eclipse.aether.artifact.DefaultArtifact(groupId, artifactId, classifier, extension, version),
                scope.getActualName()));
    }

    @NotNull
    @Override
    public List<Dependency> resolvePom(@NotNull InputStream pomInputStream) {
        try {
            List<org.apache.maven.model.Dependency> dependencies =
                    new MavenXpp3Reader().read(pomInputStream).getDependencies();
            if (dependencies.isEmpty()) {
                return Collections.emptyList();
            }
            return dependencies.stream()
                    .map(it -> resolve(
                            it.getGroupId(),
                            it.getArtifactId(),
                            it.getType(),
                            it.getClassifier(),
                            it.getVersion(),
                            DependencyScope.of(it.getScope())))
                    .collect(Collectors.toList());
        } catch (IOException | XmlPullParserException e) {
            throw new MavenResolutionException(e);
        }
    }
}
