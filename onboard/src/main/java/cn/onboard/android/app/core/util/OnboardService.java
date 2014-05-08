package cn.onboard.android.app.core.util;

import com.onboard.api.dto.Discussion;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.cache.Cache;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.cache.CacheDao;
import cn.onboard.android.app.common.DataHandleUtil;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by XingLiang on 14-4-21.
 */
public abstract class OnboardService {

    public final static String BASE_URL = "http://192.168.100.31:8080/api";

    //public final static String BASE_URL = "http://onboard.cn/api";

    private String cookie;

    protected RestTemplate restTemplate;

    protected CacheDao cacheDao;

    protected AppContext appContext;

    public OnboardService(AppContext appContext) {
        restTemplate = getRestTemplateUsingJacksonConverter();
        cacheDao = AppContext.getDaoSession().getCacheDao();
        this.appContext = appContext;
        cookie = appContext.getProperty("cookie");
    }

    protected String getUrl(String uri) {
        return String.format("%s%s", BASE_URL, uri);
    }

    protected <T> T getForObjectWithCookie(String uri, Class<T> returnType) throws RestClientException{
        HttpEntity<?> httpEntity = new HttpEntity<Object>(getHeaderWithCookie());
        return restTemplate.exchange(getUrl(uri), HttpMethod.GET, httpEntity, returnType).getBody();
    }

    protected <T> T postForObjectWithCookie(String uri, T object, Class<T> returnType) throws RestClientException{
        HttpEntity<T> httpEntity = new HttpEntity(object, getHeaderWithCookie());
        return restTemplate.exchange(getUrl(uri), HttpMethod.POST, httpEntity, returnType).getBody();
    }

    protected <T> T putForObjectWithCookie(String uri, T object, Class<T> returnType) throws RestClientException{
        HttpEntity<T> httpEntity = new HttpEntity(object, getHeaderWithCookie());
        return restTemplate.exchange(getUrl(uri), HttpMethod.PUT, httpEntity, returnType).getBody();
    }

    protected <T> T deleteForObjectWithCookie(String uri, Class<T> returnType) throws RestClientException{
        HttpEntity<?> httpEntity = new HttpEntity(getHeaderWithCookie());
        return restTemplate.exchange(getUrl(uri), HttpMethod.DELETE, httpEntity, returnType).getBody();
    }

    public void saveData(String url, Object data) {
        String json = DataHandleUtil.objectToJson(data);
        Cache cache = new Cache();
        cache.setUrl(url);
        cache.setData(json);
        cacheDao.insertOrReplace(cache);
    }

    public <T> T readData(final TypeReference<T> type, String url) {
        QueryBuilder<Cache> qb = cacheDao.queryBuilder();
        qb.where(CacheDao.Properties.Url.eq(url));
        String data = "";
        if (qb.list().size() > 0) {
            data = qb.list().get(0).getData();
        }
        return DataHandleUtil.StringToObject(type, data);
    }

    private HttpHeaders getHeaderWithCookie() {
        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", cookie);

        return headers;
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
}
