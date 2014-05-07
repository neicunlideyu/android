package cn.onboard.android.app.core.todo;

import com.onboard.api.dto.Todo;

import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

import cn.onboard.android.app.core.util.OnboardService;

/**
 * Created by XingLiang on 14-4-25.
 */
public class TodoService extends OnboardService {

    private final static String GET_CALENDER_TODO_URI = "/%d/todos/from/%d/to/%d";

    private final static String GET_TODO_BY_ID_URI = "/%d/projects/%d/todos";

    private final static String CREATE_TODO_URI = "/%d/projects/%d/todos";

    private final static String TODO_URI = "/%d/projects/%d/todos/%d";

    public List<Todo> getCalendarTodos(int companyId, long startTime, long endTime) throws RestClientException{
        String uri = String.format(GET_CALENDER_TODO_URI, companyId, startTime, endTime);
        String url = super.getUrl(uri);

        return Arrays.asList(restTemplate.getForObject(url, Todo[].class));
    }

    public Todo getTodoById(int companyId, int projectId, int todoId) throws RestClientException{
        String uri = String.format(GET_TODO_BY_ID_URI, companyId, projectId, todoId);
        String url = super.getUrl(uri);

        return restTemplate.getForObject(url, Todo.class);
    }

    public Todo createTodo(int companyId, int projectId, Todo todo) throws RestClientException{
        String uri = String.format(CREATE_TODO_URI, companyId, projectId);
        String url = super.getUrl(uri);

        return restTemplate.postForObject(url, todo, Todo.class);
    }

    public Todo updateTodo(int companyId, int projectId, int todoId, Todo todo) throws RestClientException {
        String uri = String.format(TODO_URI, companyId, projectId, todoId);
        String url = super.getUrl(uri);

        return todo;
    }

    public void completeDeleteTodo(int companyId, int projectId, int todoId) throws RestClientException {
        String uri = String.format(TODO_URI, companyId, projectId, todoId);
        String url = super.getUrl(uri);

        restTemplate.delete(url);
    }




}
