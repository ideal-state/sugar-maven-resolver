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

import java.io.File;
import org.eclipse.aether.artifact.Artifact;
import team.idealstate.sugar.maven.resolver.api.ResolvedArtifact;
import team.idealstate.sugar.validate.Validation;
import team.idealstate.sugar.validate.annotation.NotNull;

final class ApacheResolvedArtifact implements ResolvedArtifact {

    private final Artifact response;

    public ApacheResolvedArtifact(@NotNull Artifact response) {
        Validation.notNull(response, "Response must not be null.");
        this.response = response;
    }

    @NotNull
    @Override
    public String getActualVersion() {
        return response.getVersion();
    }

    @NotNull
    @Override
    public String getGroupId() {
        return response.getGroupId();
    }

    @NotNull
    @Override
    public String getArtifactId() {
        return response.getArtifactId();
    }

    @NotNull
    @Override
    public String getExtension() {
        return response.getExtension();
    }

    @NotNull
    @Override
    public String getClassifier() {
        return response.getClassifier();
    }

    @NotNull
    @Override
    public String getVersion() {
        return response.getBaseVersion();
    }

    @NotNull
    @Override
    public File getFile() {
        return Validation.requireNotNull(response.getFile(), "File must not be null.");
    }
}
