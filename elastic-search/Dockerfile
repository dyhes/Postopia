FROM docker.1ms.run/elasticsearch:8.4.1

# Set environment variables to avoid interactive prompts during plugin installation
ENV ELASTIC_CONTAINER=true
ENV ES_JAVA_OPTS="-Xms512m -Xmx512m"

# Install analysis-ik plugin with better error handling
RUN echo "Installing analysis-ik plugin..." && \
    bin/elasticsearch-plugin install --batch https://get.infini.cloud/elasticsearch/analysis-ik/8.4.1 || \
    # Fallback to alternative source if the first one fails
    bin/elasticsearch-plugin install --batch analysis-ik --verbose || \
    # Output helpful error information if both attempts fail
    (echo "Plugin installation failed. Check plugin compatibility with Elasticsearch 8.4.1" && exit 1)

# Verify plugin installation
RUN bin/elasticsearch-plugin list