package cn.onboard.android.app.core.util;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.cache.Cache;
import cn.onboard.android.app.cache.CacheDao;
import cn.onboard.android.app.common.DataHandleUtil;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by XingLiang on 14-4-21.
 */
public abstract class OnboardService {

    public final static String BASE_URL = "http://192.168.100.31:8080/api";

    protected RestTemplate restTemplate;

    protected CacheDao cacheDao;

    public OnboardService() {
        restTemplate = getRestTemplateUsingJacksonConverter();
        cacheDao = AppContext.getDaoSession().getCacheDao();
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
}
