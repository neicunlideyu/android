package cn.onboard.android.app.core.todo;

import com.onboard.api.dto.Todo;

import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.core.util.OnboardService;

/**
 * Created by XingLiang on 14-4-25.
 */
public class TodoService extends OnboardService {

    private final static String GET_CALENDER_TODO_URI = "/%d/todos/from/%d/to/%d";

    private final static String GET_TODO_BY_ID_URI = "/%d/projects/%d/todos";

    private final static String CREATE_TODO_URI = "/%d/projects/%d/todos";

    private final static String TODO_URI = "/%d/projects/%d/todos/%d";

    public TodoService(AppContext appContext) {
        super(appContext);
    }

    public List<Todo> getCalendarTodos(int companyId, long startTime, long endTime) throws RestClientException{
        String uri = String.format(GET_CALENDER_TODO_URI, companyId, startTime, endTime);

        return Arrays.asList(getForObjectWithCookie(uri, Todo[].class));
    }

    public Todo getTodoById(int companyId, int projectId, int todoId) throws RestClientException{
        String uri = String.format(TODO_URI, companyId, projectId, todoId);

        return getForObjectWithCookie(uri, Todo.class);
    }

    public Todo createTodo(Todo todo) throws RestClientException{
        String uri = String.format(CREATE_TODO_URI, todo.getCompanyId(), todo.getProjectId());

        return postForObjectWithCookie(uri, todo, Todo.class);
    }

    public Todo updateTodo(Todo todo) throws RestClientException {
        String uri = String.format(TODO_URI, todo.getCompanyId(), todo.getProjectId(), todo.getId());

        return putForObjectWithCookie(uri, todo, Todo.class);
    }

}
