package cn.onboard.android.app.core.activity;

import com.onboard.api.dto.Activity;

import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.core.util.OnboardService;

/**
 * Created by XingLiang on 14-4-28.
 */
public class ActivityService extends OnboardService {

    private final static String GET_ACTIVITY_BY_COMPANY_URI = "/%d/activities?page=%d";

    private final static String GET_ACTIVITY_BY_USER_URI = "/%d/users/%d/activities?page=%d";

    public ActivityService(AppContext appContext) {
        super(appContext);
    }

    /**
     * 分页获取一个公司的回顾
     * @param companyId
     * @param page
     * @return
     * @throws RestClientException
     */
    public List<Activity> getActivitiesByCompany(int companyId, int page) throws RestClientException{
        String uri = String.format(GET_ACTIVITY_BY_COMPANY_URI, companyId, page);

        return Arrays.asList(getForObjectWithCookie(uri, Activity[].class));
    }

    /**
     * 分页获取一个用户的Activity
     * @param companyId
     * @param userId
     * @param page
     * @return
     * @throws RestClientException
     */
    public List<Activity> getActivitiesByCompanyIdByUserId(int companyId, int userId, int page) throws RestClientException {
        String uri = String.format(GET_ACTIVITY_BY_USER_URI, companyId, userId, page);

        return Arrays.asList(getForObjectWithCookie(uri, Activity[].class));
    }
}
