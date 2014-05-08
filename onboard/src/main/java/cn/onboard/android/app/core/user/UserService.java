package cn.onboard.android.app.core.user;

import com.onboard.api.dto.User;

import org.springframework.web.client.RestClientException;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.core.util.OnboardService;

/**
 * Created by XingLiang on 14-5-7.
 */
public class UserService extends OnboardService{

    private final static String GET_USER_BY_PROJECT_URI = "/%d/projects/%d/users";

    public UserService(AppContext appContext) {
        super(appContext);
    }

    public List<User> getUsersByProjectId(int companyId, int projectId) throws RestClientException{
        String uri = String.format(GET_USER_BY_PROJECT_URI, companyId, projectId);

        return Arrays.asList(getForObjectWithCookie(uri, User[].class));
    }
}
