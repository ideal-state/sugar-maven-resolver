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

import java.io.Closeable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;
import team.idealstate.sugar.logging.Log;
import team.idealstate.sugar.maven.exception.MavenException;
import team.idealstate.sugar.maven.resolver.api.Dependency;
import team.idealstate.sugar.maven.resolver.api.DependencyResolver;
import team.idealstate.sugar.maven.resolver.api.DependencyScope;
import team.idealstate.sugar.maven.resolver.api.LocalRepository;
import team.idealstate.sugar.maven.resolver.api.MavenResolver;
import team.idealstate.sugar.maven.resolver.api.RemoteRepository;
import team.idealstate.sugar.maven.resolver.api.RepositoryPolicy;
import team.idealstate.sugar.maven.resolver.api.ResolvedArtifact;
import team.idealstate.sugar.maven.resolver.api.exception.MavenResolutionException;
import team.idealstate.sugar.validate.Validation;
import team.idealstate.sugar.validate.annotation.NotNull;

final class ApacheMavenResolver implements MavenResolver, Closeable {

    private static final Set<DependencyScope> DEFAULT_RESOLVING_SCOPES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(DependencyScope.COMPILE, DependencyScope.RUNTIME)));

    private final LocalRepository localRepository;
    private final List<RemoteRepository> remoteRepositories;
    private final DependencyResolver dependencyResolver;
    private final org.eclipse.aether.repository.LocalRepository apacheLocalRepository;
    private final List<org.eclipse.aether.repository.RemoteRepository> apacheRemoteRepositories;
    private final RepositorySystem system = new RepositorySystemSupplier().get();

    public ApacheMavenResolver(
            @NotNull LocalRepository localRepository,
            @NotNull List<RemoteRepository> remoteRepositories,
            @NotNull DependencyResolver dependencyResolver) {
        Validation.notNull(localRepository, "Local repository must not be null.");
        Validation.notNull(remoteRepositories, "Remote repositories must not be null.");
        Validation.notNull(dependencyResolver, "Dependency resolver must not be null.");
        this.localRepository = localRepository;
        this.apacheLocalRepository = asApacheLocalRepository(getLocalRepository());
        this.remoteRepositories = remoteRepositories;
        try {
            this.apacheRemoteRepositories = asApacheRemoteRepositories(getRemoteRepositories());
        } catch (MalformedURLException e) {
            throw new MavenException(e);
        }
        this.dependencyResolver = dependencyResolver;
    }

    @NotNull
    private static List<ResolvedArtifact> asResolvedArtifacts(@NotNull List<ArtifactResult> artifactResults) {
        if (artifactResults.isEmpty()) {
            return Collections.emptyList();
        }
        List<ResolvedArtifact> result = new ArrayList<>(artifactResults.size());
        for (ArtifactResult artifactResult : artifactResults) {
            for (Exception exception : artifactResult.getExceptions()) {
                throw new MavenResolutionException(exception);
            }
            Artifact response = artifactResult.getArtifact();
            if (response == null) {
                throw new MavenResolutionException(String.format(
                        "Failed to resolution dependency '%s'.",
                        artifactResult.getRequest().getArtifact()));
            }
            result.add(new ApacheResolvedArtifact(response));
        }
        return result;
    }

    @NotNull
    private static List<org.eclipse.aether.graph.Dependency> asApacheDependencies(
            @NotNull List<Dependency> dependencies, @NotNull Set<DependencyScope> dependencyScopes) {
        if (dependencies.isEmpty()) {
            return Collections.emptyList();
        }
        List<org.eclipse.aether.graph.Dependency> result = new ArrayList<>(dependencies.size());
        for (Dependency dependency : dependencies) {
            DependencyScope scope = dependency.getScope();
            if (!dependencyScopes.contains(scope)) {
                continue;
            }
            result.add(new org.eclipse.aether.graph.Dependency(
                    new org.eclipse.aether.artifact.DefaultArtifact(
                            dependency.getGroupId(),
                            dependency.getArtifactId(),
                            dependency.getClassifier(),
                            dependency.getExtension(),
                            dependency.getVersion()),
                    scope.getActualName()));
        }
        return result;
    }

    @NotNull
    private static List<org.eclipse.aether.repository.RemoteRepository> asApacheRemoteRepositories(
            @NotNull List<RemoteRepository> repositories) throws MalformedURLException {
        if (repositories.isEmpty()) {
            return Collections.emptyList();
        }
        List<org.eclipse.aether.repository.RemoteRepository> result = new ArrayList<>(repositories.size());
        for (RemoteRepository repository : repositories) {
            Set<RepositoryPolicy> policies = repository.getPolicies();
            result.add(new org.eclipse.aether.repository.RemoteRepository.Builder(
                            repository.getName(),
                            "default",
                            repository.getUrl().toURL().toString())
                    .setReleasePolicy(new org.eclipse.aether.repository.RepositoryPolicy(
                            true,
                            org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_NEVER,
                            org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_WARN))
                    .setSnapshotPolicy(new org.eclipse.aether.repository.RepositoryPolicy(
                            true,
                            policies.contains(RepositoryPolicy.ALWAYS_UPDATE)
                                            && !policies.contains(RepositoryPolicy.NEVER_UPDATE)
                                    ? org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_ALWAYS
                                    : org.eclipse.aether.repository.RepositoryPolicy.UPDATE_POLICY_NEVER,
                            org.eclipse.aether.repository.RepositoryPolicy.CHECKSUM_POLICY_WARN))
                    .build());
        }
        return result;
    }

    @NotNull
    @Override
    public LocalRepository getLocalRepository() {
        return localRepository;
    }

    @NotNull
    @Override
    public List<RemoteRepository> getRemoteRepositories() {
        return remoteRepositories.isEmpty() ? Collections.emptyList() : new ArrayList<>(remoteRepositories);
    }

    @NotNull
    @Override
    public DependencyResolver getDependencyResolver() {
        return dependencyResolver;
    }

    @NotNull
    @Override
    public List<ResolvedArtifact> resolve(
            @NotNull List<Dependency> dependencies, @NotNull DependencyScope... dependencyScopes)
            throws MavenResolutionException {
        Validation.notNull(dependencies, "Dependencies must not be null.");
        Validation.notNull(dependencyScopes, "Dependency scopes must not be null.");
        List<org.eclipse.aether.graph.Dependency> apacheDependencies = asApacheDependencies(
                dependencies,
                dependencyScopes.length == 0
                        ? DEFAULT_RESOLVING_SCOPES
                        : new HashSet<>(Arrays.asList(dependencyScopes)));
        if (apacheDependencies.isEmpty()) {
            return Collections.emptyList();
        }
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, apacheLocalRepository));
        session.setTransferListener(new TransferLog());
        session.setReadOnly();
        List<org.eclipse.aether.repository.RemoteRepository> apacheRemoteRepositories =
                system.newResolutionRepositories(session, this.apacheRemoteRepositories);

        DependencyResult dependencyResult;
        try {
            dependencyResult = system.resolveDependencies(
                    session,
                    new DependencyRequest(
                            new CollectRequest(
                                    (org.eclipse.aether.graph.Dependency) null,
                                    apacheDependencies,
                                    apacheRemoteRepositories),
                            null));
        } catch (DependencyResolutionException e) {
            throw new MavenResolutionException(e);
        }
        return asResolvedArtifacts(dependencyResult.getArtifactResults());
    }

    @NotNull
    private org.eclipse.aether.repository.LocalRepository asApacheLocalRepository(@NotNull LocalRepository repository) {
        return new org.eclipse.aether.repository.LocalRepository(repository.getLocation());
    }

    @Override
    public void close() {
        system.shutdown();
    }

    private static class TransferLog extends AbstractTransferListener {

        @Override
        public void transferStarted(TransferEvent event) {
            TransferResource resource = event.getResource();
            Log.info(String.format("Downloading '%s'...", resource.getRepositoryUrl() + resource.getResourceName()));
        }

        @Override
        public void transferSucceeded(TransferEvent event) {
            TransferResource resource = event.getResource();
            Log.info(String.format("Downloaded '%s'.", resource.getRepositoryUrl() + resource.getResourceName()));
        }

        @Override
        public void transferFailed(TransferEvent event) {
            TransferResource resource = event.getResource();
            Log.debug(() -> String.format(
                    "Failed to download '%s'.", resource.getRepositoryUrl() + resource.getResourceName()));
        }
    }
}
