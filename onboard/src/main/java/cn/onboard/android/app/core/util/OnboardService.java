package cn.onboard.android.app.core.util;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Created by XingLiang on 14-4-21.
 */
public abstract class OnboardService {

    public final static String BASE_URL = "http://onboard.cn/api";

    protected RestTemplate restTemplate;

    public OnboardService() {
        restTemplate = getRestTemplateUsingJacksonConverter();
    }

    protected String getUrl(String uri) {
        return String.format("%s%s", BASE_URL, uri);
    }

    private RestTemplate getRestTemplateUsingJacksonConverter() {
        RestTemplate restTemplate = new RestTemplate();
        MappingJacksonHttpMessageConverter converter = new MappingJacksonHttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        converter.setObjectMapper(objectMapper);

        restTemplate.getMessageConverters().add(converter);

        return restTemplate;
    }
}
