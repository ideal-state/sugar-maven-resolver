package team.idealstate.sugar.maven.resolver;

import team.idealstate.sugar.maven.resolve.api.MavenResolver;
import team.idealstate.sugar.maven.resolve.api.MavenResolverConfiguration;
import team.idealstate.sugar.maven.resolve.spi.MavenResolverFactory;

public final class ApacheMavenResolverFactory implements MavenResolverFactory {
    @Override
    public MavenResolver create(MavenResolverConfiguration configuration) {
        return new ApacheMavenResolver(configuration.getLocalRepository(), configuration.getRemoteRepositories(), new ApacheDependencyResolver());
    }
}
