package team.idealstate.sugar.maven.resolver;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.ArtifactRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import team.idealstate.sugar.maven.resolve.api.ResolvedArtifact;
import team.idealstate.sugar.validate.Validation;
import team.idealstate.sugar.validate.annotation.NotNull;

import java.io.File;

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
