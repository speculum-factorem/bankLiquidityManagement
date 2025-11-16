package com.bank.config.event;

import com.bank.config.metrics.ConfigServerMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigServerEventListener {

    private final ConfigServerMetrics configServerMetrics;

    @EventListener
    public void handleConfigRefresh(ConfigRefreshEvent event) {
        log.info("Configuration refreshed for application: {}, profile: {}",
                event.getApplication(), event.getProfile());

        configServerMetrics.recordConfigRefresh(event.getApplication(), event.getProfile());
    }

    @EventListener
    public void handleGitOperation(GitOperationEvent event) {
        log.debug("Git operation performed: {}, success: {}, duration: {}ms",
                event.getOperation(), event.isSuccess(), event.getDuration());

        configServerMetrics.recordGitOperation(event.getOperation(), event.isSuccess(), event.getDuration());
    }

    // Вспомогательные классы событий
    public static class ConfigRefreshEvent {
        private final String application;
        private final String profile;

        public ConfigRefreshEvent(String application, String profile) {
            this.application = application;
            this.profile = profile;
        }

        public String getApplication() { return application; }
        public String getProfile() { return profile; }
    }

    public static class GitOperationEvent {
        private final String operation;
        private final boolean success;
        private final long duration;

        public GitOperationEvent(String operation, boolean success, long duration) {
            this.operation = operation;
            this.success = success;
            this.duration = duration;
        }

        public String getOperation() { return operation; }
        public boolean isSuccess() { return success; }
        public long getDuration() { return duration; }
    }
}