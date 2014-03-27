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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.onboard.api.dto.Todo;
import com.onboard.api.dto.Todolist;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.ui.EditTodo;


public class TodoFragment extends Fragment implements MenuItem.OnMenuItemClickListener {
    private Integer companyId;

    private Integer projectId;

    private Integer userId;

    private Date date;

    private List<Item> data;

    private List<Todolist> todolistList;

    private ListViewNewsAdapter listViewAdapter;

    public TodoFragment() {
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
        data = new ArrayList<Item>();
        listViewAdapter = new ListViewNewsAdapter(getActivity().getApplicationContext(), data, 0);
        HashMap<String, Integer> idMap = new HashMap<String, Integer>();
        idMap.put("projectId", projectId);
        idMap.put("companyId", companyId);
        new InitDataTask().execute(idMap);
        return lv;


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
                new newTodolistTask().execute(input.getText().toString());
            }
        }).setNegativeButton("取消", null).show();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK)
            return;
        String todojson = intent.getExtras().getString("todo");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
                Item todoItem = new Item(todolists.get(i).getTodos().get(j), todolists.get(i), sectionPosition, listPosition++, todolists.get(i).getTodos().get(j).getContent(), Type.TODO.value());
                itemList.add(todoItem);
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

        public static Type valueOf(int value) {
            switch (value) {
                case 0:
                    return TODOLIST;
                case 1:
                    return TODO;
                default:
            }
            return null;
        }

        public int value() {
            return this.value;
        }
    }

    private class ListViewNewsAdapter extends BaseAdapter implements SectionIndexer {
        private Context context;// 运行上下文
        private List<Item> listItems;// 数据集合
        private LayoutInflater listContainer;// 视图容器
        private int itemViewResource;// 自定义项视图源

        private Item[] sections;

        /**
         * 实例化Adapter
         *
         * @param context
         * @param data
         */
        public ListViewNewsAdapter(Context context, List<Item> data, int sectionSize) {
            this.context = context;
            this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
            this.listItems = data;
            this.sections = new Item[sectionSize];
            int index = 0;
            for (Item item : data) {
                if (item.getType() == Type.TODOLIST.value()) {
                    sections[index++] = item;
                }
            }
        }

        @Override
        public Item[] getSections() {
            return sections;
        }

        @Override
        public int getPositionForSection(int section) {
            if (section >= sections.length) {
                section = sections.length - 1;
            }
            return sections[section].listPosition;
        }

        @Override
        public int getSectionForPosition(int position) {
            if (position >= getCount()) {
                position = getCount() - 1;
            }
            return listItems.get(position).sectionPosition;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return listItems.get(position).getType() == Type.TODOLIST.value() ? 1 : 0;
        }

        public int getCount() {
            return listItems.size();
        }

        @Override
        public Object getItem(int i) {
            return listItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        /**
         * ListView Item设置
         */
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Item item = listItems.get(position);


            // 自定义视图
            ListItemView listItemView = null;

            if (convertView == null) {
                listItemView = new ListItemView();
                if (item.getType() == Type.TODOLIST.value()) {
                    convertView = listContainer
                            .inflate(R.layout.todolist_item, null);
                    listItemView.name = (TextView) convertView.findViewById(R.id.todolist_titile);
                    listItemView.newTodo = (ImageView) convertView.findViewById(R.id.new_todo_button);
                    listItemView.newTodo.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            Context context = view.getContext();
                            Intent intent = new Intent(context,
                                    EditTodo.class);
                            intent.putExtra("companyId", companyId);
                            intent.putExtra("projectId", projectId);
                            intent.putExtra("todolistId", item.todolist.getId());
                            intent.putExtra("editType", EditTodo.EditType.CREATE);
                            startActivityForResult(intent, EditTodo.EditType.CREATE.value());

                        }
                    });

                    convertView.setAlpha(1);


                } else if (item.getType() == Type.TODO.value()) {
                    convertView = listContainer
                            .inflate(R.layout.todo_item, null);
                    listItemView.name = (TextView) convertView.findViewById(R.id.todo_text);
                    listItemView.assigneeName = (TextView) convertView.findViewById(R.id.todo_assign_person);
                    if (((Todo) item.identifiable).getAssigneeId() != null)
                        listItemView.assigneeName.setText(((Todo) item.identifiable).getAssignee().getName());
                    listItemView.dueDate = (TextView) convertView.findViewById(R.id.todo_assign_date);
                    if (((Todo) item.identifiable).getDueDate() != null) {
                        String dateString = DateTimeFormat.forPattern("yyyy-MM-dd").print(((Todo) item.identifiable).getDueDate().getTime());
                        listItemView.dueDate.setText(dateString);
                    }
                    listItemView.name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Context context = view.getContext();
                            Intent intent = new Intent(context,
                                    EditTodo.class);
                            intent.putExtra("companyId", companyId);
                            intent.putExtra("projectId", projectId);
                            intent.putExtra("todolistId", item.todolist.getId());
                            intent.putExtra("todoId", ((Todo) item.identifiable).getId());
                            intent.putExtra("editType", EditTodo.EditType.UPDATE.value());
                            startActivityForResult(intent, EditTodo.EditType.UPDATE.value());
                        }
                    });

                    listItemView.completeTodo = (CheckBox) convertView.findViewById(R.id.todo_complete_checkBox);
                    listItemView.completeTodo.setChecked(false);
                    listItemView.completeTodo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked)
                                new CompleteTodolistTask().execute(((Todo) item.identifiable).getId());
                        }
                    });
                }
                // 设置控件集到convertView
                convertView.setTag(listItemView);
            } else {
                listItemView = (ListItemView) convertView.getTag();

                if (item.getType() == Type.TODO.value()) {
                    if (((Todo) item.identifiable).getCompleted()) {
                        listItemView.completeTodo.setChecked(true);
                        listItemView.completeTodo.setClickable(false);
                        listItemView.name.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                        listItemView.name.setTextColor(Color.RED);
                    }
                    if (((Todo) item.identifiable).getAssigneeId() != null)
                        listItemView.assigneeName.setText(((Todo) item.identifiable).getAssignee().getName());
                    if (((Todo) item.identifiable).getDueDate() != null) {
                        String dateString = DateTimeFormat.forPattern("yyyy-MM-dd").print(((Todo) item.identifiable).getDueDate().getTime());
                        listItemView.dueDate.setText(dateString);
                    }

                }
            }
            listItemView.name.setText(item.getText());
            return convertView;
        }

        class ListItemView { // 自定义控件集合
            public TextView name;
            public CheckBox completeTodo;
            public ImageView newTodo;
            public TextView assigneeName;
            public TextView dueDate;
        }
    }

    static class Item {

        private Object identifiable;
        private Todolist todolist;
        private int sectionPosition;
        private int listPosition;
        private String text;
        private int type;

        public Item(Object identifiable, Todolist todolist, int sectionPosition, int listPosition, String text, int type) {
            this.todolist = todolist;
            this.identifiable = identifiable;
            this.sectionPosition = sectionPosition;
            this.listPosition = listPosition;
            this.text = text;
            this.type = type;
        }

        public int getSectionPosition() {
            return sectionPosition;
        }

        public int getListPosition() {
            return listPosition;
        }

        public Object getIdentifiable() {
            return identifiable;
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

    public class newTodolistTask extends AsyncTask<String, Void, Todolist> {

        @Override
        protected Todolist doInBackground(String... todolistTitle) {
            Todolist todolist = new Todolist();
            todolist.setName(todolistTitle[0]);
            todolist.setCompanyId(companyId);
            todolist.setProjectId(projectId);
            AppContext ac = (AppContext) getActivity().getApplication();
            try {
                todolist = ac
                        .createTodolist(todolist);
            } catch (AppException e) {
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
                todo = ac
                        .updateTodo(todo);
            } catch (AppException e) {
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

    public class InitDataTask extends AsyncTask<HashMap<String, Integer>, Void, List<Todolist>> {

        @Override
        protected List<Todolist> doInBackground(HashMap<String, Integer>... hashMaps) {
            AppContext ac = (AppContext) getActivity().getApplication();
            List<Todolist> todolists = new ArrayList<Todolist>();
            try {
                if (projectId != null) {
                    todolists = ac
                            .getTodoListsByProjectId(companyId, projectId);
                } else if (userId != null)
                    todolists = ac.getTodoListsByUserId(companyId, userId);
                else if (date != null) {
                    todolists = ac.getTodoListsByDate(companyId, date);

                }
                todolistList = todolists;
            } catch (AppException e) {
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
            listViewAdapter = new ListViewNewsAdapter(getActivity().getApplicationContext(), data, todolists.size());
            pinnedSectionListView.setAdapter(listViewAdapter);
        }
    }
}