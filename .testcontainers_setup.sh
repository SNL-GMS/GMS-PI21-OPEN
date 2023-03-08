if [ -f "$HOME/.testcontainers.properties" ]; then rm $HOME/.testcontainers.properties || true; fi

echo docker.client.strategy=org.testcontainers.dockerclient.UnixSocketClientProviderStrategy > $HOME/.testcontainers.properties
echo hub.image.name.prefix=${CI_THIRD_PARTY_DOCKER_REGISTRY:-$CI_DOCKER_REGISTRY}/ >> $HOME/.testcontainers.properties
