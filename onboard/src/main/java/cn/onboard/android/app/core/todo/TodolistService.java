package cn.onboard.android.app.core.todo;

import com.onboard.api.dto.Todolist;

import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.core.util.OnboardService;

/**
 * Created by XingLiang on 14-4-25.
 */
public class TodolistService extends OnboardService {

    private final static String GET_TODOLIST_BY_PROJECT_URI = "/%d/projects/%d/todoLists";

    private final static String GET_TODOLIST_BY_USER_URI = "/%d/users/%d/todoLists";

    private final static String GET_TODOLIST_BY_DATE_URI = "/%d/duedate/%d/todoLists";

    private final static String GET_TODOLIST_BY_ID_URI = "/%d/projects/%d/todoLists/%d";

    public TodolistService(AppContext appContext) {
        super(appContext);
    }
    /**
     * 通过id获取todolist
     * @param companyId
     * @param projectId
     * @param todolistId
     * @return
     * @throws RestClientException
     */
    public Todolist getTodolistById(int companyId, int projectId, int todolistId) throws RestClientException {
        String uri = String.format(GET_TODOLIST_BY_ID_URI, companyId, projectId, todolistId);

        return getForObjectWithCookie(uri, Todolist.class);
    }

    /**
     * 通过projectId获得这个项目的所有todolist
     * @param companyId
     * @param projectId
     * @return
     * @throws RestClientException
     */

    public List<Todolist> getTodoListByProjectId(int companyId, int projectId)throws RestClientException {
        String uri = String.format(GET_TODOLIST_BY_PROJECT_URI, companyId, projectId);

        return Arrays.asList(getForObjectWithCookie(uri, Todolist[].class));
    }

    /**
     * 通过userId获取用户在这个公司下的所有todolist
     * @param companyId
     * @param userId
     * @return
     * @throws RestClientException
     */
    public List<Todolist> getTodoListByCompanyIdByUserId(int companyId, int userId) throws RestClientException {
        String uri = String.format(GET_TODOLIST_BY_USER_URI, companyId, userId);        String url = super.getUrl(uri);

        return Arrays.asList(getForObjectWithCookie(uri, Todolist[].class));
    }

    /**
     * 根据duedate获取todolist
     * @param companyId
     * @param date
     * @return
     * @throws RestClientException
     */

    public List<Todolist> getTodoListByCompanyIdByDate(int companyId, Date date) throws RestClientException {
        String uri = String.format(GET_TODOLIST_BY_DATE_URI, companyId, date.getTime());        String url = super.getUrl(uri);

        return Arrays.asList(getForObjectWithCookie(uri, Todolist[].class));
    }
}
