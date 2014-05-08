package cn.onboard.android.app.core.util;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.bean.URLs;

/**
 * Created by XingLiang on 14-4-21.
 */
public abstract class OnboardService {

    public final static String BASE_URL = "http://192.168.100.31:8080/api";

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
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

        return restTemplate;
    }

    protected HttpHeaders getHeaderWithCookie(AppContext appContext) {
        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", appContext.getProperty("cookie"));

        return headers;
    }
}
