package co.ke.xently.common.headers;

import co.ke.xently.common.headers.validators.EpochTimestampValidator;
import co.ke.xently.common.headers.validators.RegexValidator;
import co.ke.xently.common.utils.ElapsedTimeManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "xently.api.headers.validation")
public record HeaderValidationProperties(Set<HeaderRule> headers) {
    private static final Set<HeaderRule> DEFAULT_HEADERS = Set.of(
            HeaderRule.builder().headerName("X-FeatureCode")
                    .required(false)
                    .build(),
            HeaderRule.builder().headerName("X-FeatureName")
                    .build(),
            HeaderRule.builder().headerName("X-ServiceCode")
                    .build(),
            HeaderRule.builder().headerName("X-ServiceName")
                    .build(),
            HeaderRule.builder().headerName("X-ServiceSubCategory")
                    .required(false)
                    .build(),
            HeaderRule.builder().headerName("X-MinorServiceVersion")
                    .validator(new RegexValidator(Pattern.compile("v?\\d+(.\\d+){0,2}", Pattern.CASE_INSENSITIVE)))
                    .build(),
            HeaderRule.builder().headerName("X-ChannelCategory")
                    .build(),
            HeaderRule.builder().headerName("X-ChannelCode")
                    .build(),
            HeaderRule.builder().headerName("X-ChannelName")
                    .build(),
            HeaderRule.builder().headerName("X-RouteCode")
                    .required(false)
                    .build(),
            HeaderRule.builder().headerName(ElapsedTimeManager.TIMESTAMP_HEADER)
                    .required(false)
                    .validator(new EpochTimestampValidator())
                    .build(),
            HeaderRule.builder().headerName("X-ServiceMode")
                    .required(false)
                    .build(),
            HeaderRule.builder().headerName("X-SubscriberEvents")
                    .required(false)
                    .build(),
            HeaderRule.builder().headerName("X-CallBackURL")
                    .required(false)
                    .validator(new RegexValidator("^https?://.+..+"))
                    .build()
    );

    @NonNull
    @Override
    public Set<HeaderRule> headers() {
        Set<HeaderRule> headers = new HashSet<>(Objects.requireNonNullElse(this.headers, Set.of()));
        headers.addAll(DEFAULT_HEADERS);
        return headers;
    }
}