package team.idealstate.sugar.maven.resolver;

import team.idealstate.sugar.maven.resolver.api.Dependency;
import team.idealstate.sugar.maven.resolver.api.DependencyScope;
import team.idealstate.sugar.validate.Validation;
import team.idealstate.sugar.validate.annotation.NotNull;

import java.util.Objects;

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
        return "ApacheDependency{" +
                "dependency=" + dependency +
                '}';
    }
}
