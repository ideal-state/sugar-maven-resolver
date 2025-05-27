package team.idealstate.sugar.maven.resolver;

import team.idealstate.sugar.maven.resolver.api.MavenResolver;
import team.idealstate.sugar.maven.resolver.api.MavenResolverConfiguration;
import team.idealstate.sugar.maven.resolver.spi.MavenResolverFactory;

public final class ApacheMavenResolverFactory implements MavenResolverFactory {
    @Override
    public MavenResolver create(MavenResolverConfiguration configuration) {
        return new ApacheMavenResolver(configuration.getLocalRepository(), configuration.getRemoteRepositories(), new ApacheDependencyResolver());
    }
}
