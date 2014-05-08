/*
 * Copyright (C) 2013 Sergej Shafarenka, halfbit.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.onboard.android.app.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;
import com.onboard.api.dto.Todo;
import com.onboard.api.dto.Todolist;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.adapter.TodoListViewAdapter;
import cn.onboard.android.app.core.todo.TodoService;
import cn.onboard.android.app.core.todo.TodolistService;


public class TodoFragment extends Fragment implements MenuItem.OnMenuItemClickListener {
    private Integer companyId;

    private Integer projectId;

    private Integer userId;

    private Date date;

    private TodoService todoService;

    private TodolistService todolistService;

    private List<Item> data;

    private List<Todolist> todolistList;

    private TodoListViewAdapter listViewAdapter;

    private TodoFragment() {
        setRetainInstance(true);
    }

    public TodoFragment(Integer companyId, Integer projectId, Integer userId, Date date) {
        this();
        this.companyId = companyId;
        this.projectId = projectId;
        this.userId = userId;
        this.date = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final LinearLayout lv = (LinearLayout) inflater.inflate(
                R.layout.todo_list, null);
        initService();
        data = new ArrayList<Item>();
        new InitDataTask().execute();
        return lv;


    }

    private void initService() {
        AppContext appContext = (AppContext)getActivity().getApplicationContext();
        this.todolistService = new TodolistService(appContext);
        this.todoService = new TodoService(appContext);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        final EditText input = new EditText(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        new AlertDialog.Builder(getActivity()).setTitle("请输入任务列表标题").setView(input).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                new SaveTodolistTask().execute(input.getText().toString());
            }
        }).setNegativeButton("取消", null).show();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK)
            return;
        String type = intent.getExtras().getString("type");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (type != null && type.contains("todolist")) {
            String todolistJson = intent.getExtras().getString("todolist");
            Todolist todolist = new Todolist();
            try {
                todolist = mapper.readValue(todolistJson, Todolist.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Todolist t : todolistList) {
                if (t.getId().equals(todolist.getId())) {
                    t.setName(todolist.getName());
                    data.clear();
                    data.addAll(handleData(todolistList));
                    listViewAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }
        String todojson = intent.getExtras().getString("todo");
        Todo todo = new Todo();
        try {
            todo = mapper.readValue(todojson, Todo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        switch (requestCode) {//请求标记
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    for (Todolist todolist : todolistList)
                        if (todo.getTodolistId().equals(todolist.getId())) {
                            todolist.getTodos().add(todo);
                            break;
                        }
                    //do something
                    break;
                }
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    for (Todolist todolist : todolistList) {
                        if (todo.getTodolistId().equals(todolist.getId())) {
                            for (Todo t : todolist.getTodos()) {
                                if (t.getId().equals(todo.getId())) {
                                    int index = todolist.getTodos().indexOf(t);
                                    todolist.getTodos().set(index, todo);
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
        }

        data.clear();
        data.addAll(handleData(todolistList));
        listViewAdapter.notifyDataSetChanged();
    }

    List<Item> handleData(List<Todolist> todolists) {
        List<Item> itemList = new ArrayList<Item>();
        int sectionPosition = 0, listPosition = 0;
        for (int i = 0; i < todolists.size(); i++) {
            Item todolistItem = new Item(todolists.get(i), todolists.get(i), sectionPosition, listPosition++, todolists.get(i).getName(), Type.TODOLIST.value());
            itemList.add(todolistItem);
            int num_of_todos = todolists.get(i).getTodos() == null ? 0 : todolists.get(i).getTodos().size();
            for (int j = 0; j < num_of_todos; j++) {
                if (!(todolists.get(i).getTodos().get(j).getCompleted()) && !(todolists.get(i).getTodos().get(j).getDeleted())) {
                    Item todoItem = new Item(todolists.get(i).getTodos().get(j), todolists.get(i), sectionPosition, listPosition++, todolists.get(i).getTodos().get(j).getContent(), Type.TODO.value());
                    itemList.add(todoItem);
                }
            }
            sectionPosition++;
        }
        return itemList;
    }

    public enum Type {
        TODOLIST(0), TODO(1);

        private int value = 0;

        private Type(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }
    }


    public static class Item {

        public final Object identifiable;
        public final Todolist todolist;
        public final int sectionPosition;
        public final int listPosition;
        public final String text;
        public final int type;

        public Item(Object identifiable, Todolist todolist, int sectionPosition, int listPosition, String text, int type) {
            this.todolist = todolist;
            this.identifiable = identifiable;
            this.sectionPosition = sectionPosition;
            this.listPosition = listPosition;
            this.text = text;
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public int getType() {
            return type;

        }

        @Override
        public String toString() {
            return text.substring(0, 1) + "..";
        }
    }

    private class SaveTodolistTask extends AsyncTask<String, Void, Todolist> {

        @Override
        protected Todolist doInBackground(String... todolistTitle) {
            Todolist todolist = new Todolist();
            todolist.setName(todolistTitle[0]);
            todolist.setCompanyId(companyId);
            todolist.setProjectId(projectId);
            AppContext ac = (AppContext) getActivity().getApplication();
            try {
                todolist = todolistService
                        .createTodolist(todolist);
            } catch (RestClientException e) {
                e.printStackTrace();
            }
            return todolist;
        }

        @Override
        protected void onPostExecute(Todolist todolist) {

            todolistList.add(todolist);
            data.clear();
            data.addAll(handleData(todolistList));
            listViewAdapter.notifyDataSetChanged();
            getActivity().findViewById(R.id.data_empty).setVisibility(View.GONE);
        }
    }

    public class CompleteTodolistTask extends AsyncTask<Integer, Void, Todo> {

        @Override
        protected Todo doInBackground(Integer... todoId) {
            Todo todo = new Todo();
            todo.setId(todoId[0]);
            todo.setCompanyId(companyId);
            todo.setProjectId(projectId);
            todo.setCompleted(true);
            AppContext ac = (AppContext) getActivity().getApplication();
            try {
                todo = todoService
                        .updateTodo(todo);
            } catch (RestClientException e) {
                e.printStackTrace();
            }
            return todo;
        }

        @Override
        protected void onPostExecute(Todo todo) {
            for (Todolist todolist : todolistList) {
                if (todolist.getId().equals(todo.getTodolistId())) {
                    for (Todo t : todolist.getTodos()) {
                        if (t.getId().equals(todo.getId())) {
                            t.setCompleted(true);
                            break;
                        }
                    }
                }
            }
            data.clear();
            data.addAll(handleData(todolistList));
            listViewAdapter.notifyDataSetChanged();
        }
    }

    private class InitDataTask extends AsyncTask<Void, Void, List<Todolist>> {

        @Override
        protected List<Todolist> doInBackground(Void... params) {
            List<Todolist> todolists = new ArrayList<Todolist>();
            try {
                if (projectId != null) {
                    todolists = todolistService
                            .getTodoListByProjectId(companyId, projectId);
                } else if (userId != null)
                    todolists = todolistService
                            .getTodoListByCompanyIdByUserId(companyId, userId);
                else if (date != null) {
                    todolists = todolistService
                            .getTodoListByCompanyIdByDate(companyId, date);

                }
                todolistList = todolists;
            } catch (RestClientException e) {
                e.printStackTrace();
            }
            return todolists;
        }

        @Override
        protected void onPostExecute(List<Todolist> todolists) {
            List<Item> itemList = new ArrayList<Item>();
            ListView pinnedSectionListView = (ListView) getActivity().findViewById(
                    R.id.todolist);
            pinnedSectionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent,
                                        View view, int position, long id) {

                }
            });
            data.addAll(handleData(todolists));
            if (data.size() == 0) {
                getActivity().findViewById(R.id.data_empty).setVisibility(View.VISIBLE);
            }
            listViewAdapter = new TodoListViewAdapter(getActivity().getApplicationContext(),TodoFragment.this, data, todolists.size());
            pinnedSectionListView.setAdapter(listViewAdapter);
            getActivity().findViewById(R.id.progress_bar).setVisibility(View.GONE);

        }
    }

    public enum EditType {
        CREATE(0), UPDATE(1);

        private int value = 0;

        private EditType(int value) {
            this.value = value;
        }

        public static EditType valueOf(int value) {
            switch (value) {
                case 0:
                    return CREATE;
                case 1:
                    return UPDATE;
                default:
            }
            return null;
        }

        public int value() {
            return this.value;
        }
    }

}