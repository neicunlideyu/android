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

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.onboard.api.dto.Todolist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.R;
import cn.onboard.android.app.ui.EditTodo;


public class TodoFragment extends Fragment implements OnClickListener,MenuItem.OnMenuItemClickListener {
    private int companyId;

    private int projectId;

    private List<Item> data;

    private ListViewNewsAdapter listViewNewsAdapter;

    public TodoFragment() {
        setRetainInstance(true);
    }

    public TodoFragment(int companyId, int projectId) {
        this();
        this.companyId = companyId;
        this.projectId = projectId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final LinearLayout lv = (LinearLayout) inflater.inflate(
                R.layout.todo_list, null);
        data = new ArrayList<Item>();
        listViewNewsAdapter = new ListViewNewsAdapter(getActivity().getApplicationContext(), data, 0);
        HashMap<String, Integer> idMap = new HashMap<String, Integer>();
        idMap.put("projectId", projectId);
        idMap.put("companyId", companyId);
        new InitDataTask().execute(idMap);
//        Project project = (Project) getActivity();
//
//        project.getSupportActionBar().setLogo(R.drawable.frame_logo_news);
//        project.getSupportActionBar().setTitle("任务列表");
//        project.setCreateString("新建任务列表");
//        project.setPopupListener(this);
//        project.invalidateOptionsMenu();

        return lv;


    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return true;
    }

    public class InitDataTask extends AsyncTask<HashMap<String,Integer > ,Void ,List<Todolist>>{

        @Override
        protected List<Todolist> doInBackground(HashMap<String, Integer>... hashMaps) {
            AppContext ac = (AppContext) getActivity().getApplication();
            List<Todolist> todolists = new ArrayList<Todolist>();
            try {
                todolists = ac
                        .getTodoListsByProjectId(companyId, projectId);
            } catch (AppException e) {
                e.printStackTrace();
            }
            return todolists;
        }

        @Override
        protected  void onPostExecute (List<Todolist> todolists){
            List<Item> itemList = new ArrayList<Item>();
            int sectionPosition = 0, listPosition = 0;
            ListView pinnedSectionListView = (ListView) getActivity().findViewById(
                    R.id.todolist);
            pinnedSectionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent,
                                        View view, int position, long id) {
                    Item item = (Item) listViewNewsAdapter.getItem(position);
                    if (item != null) {
//                                Toast.makeText(getActivity(), item.identifiable.getType(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            for (int i = 0; i < todolists.size(); i++) {
                Item todolistItem = new Item(todolists.get(i), sectionPosition, listPosition++, todolists.get(i).getName(),Type.TODOLIST.value());
                itemList.add(todolistItem);
                int num_of_todos = todolists.get(i).getTodos() == null ? 0 : todolists.get(i).getTodos().size();
                for (int j = 0; j < num_of_todos; j++) {
                    Item todoItem = new Item(todolists.get(i).getTodos().get(j), sectionPosition, listPosition++, todolists.get(i).getTodos().get(j).getContent(),Type.TODO.value());
                    itemList.add(todoItem);
                }
                sectionPosition++;
            }
            data.addAll(itemList);
            listViewNewsAdapter = new ListViewNewsAdapter(getActivity().getApplicationContext(), data, todolists.size());
            pinnedSectionListView.setAdapter(listViewNewsAdapter);
        }
    }

    @Override
    public void onClick(View view) {

    }

    private static class ListViewNewsAdapter extends BaseAdapter implements SectionIndexer {
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
                if (item.getType()==Type.TODOLIST.value()) {
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
            return listItems.get(position).getType()== Type.TODOLIST.value()?1:0;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = listItems.get(position);


            // 自定义视图
            ListItemView listItemView = null;

            if (convertView == null) {
                listItemView = new ListItemView();
                if (item.getType()== Type.TODOLIST.value()) {
                    convertView = listContainer
                            .inflate(R.layout.todolist_item, null);
                    listItemView.name = (TextView) convertView.findViewById(R.id.todolist_titile);
                    listItemView.newTodo = (ImageView) convertView.findViewById(R.id.new_todo_button);
                    listItemView.newTodo.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View view) {
                            Context context = view.getContext();
                            Intent intent = new Intent(context,
                                    EditTodo.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);

                        }
                    });

                    convertView.setAlpha(1);


                } else if (item.getType()==Type.TODO.value()) {
                    convertView = listContainer
                            .inflate(R.layout.todo_item, null);
                    listItemView.name = (TextView) convertView.findViewById(R.id.todo_text);
                    listItemView.completeTodo = (CheckBox) convertView.findViewById(R.id.todo_complete_checkBox);
                    listItemView.completeTodo.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View view) {

                        }
                    });
                }
                // 设置控件集到convertView
                convertView.setTag(listItemView);
            } else {
                listItemView = (ListItemView) convertView.getTag();
            }
            listItemView.name.setText(item.getText());
            return convertView;
        }

        static class ListItemView { // 自定义控件集合
            public TextView name;
            public CheckBox completeTodo;
            public ImageView newTodo;
        }
    }

    static class Item {

        private Object identifiable;
        private int sectionPosition;
        private int listPosition;
        private String text;
        private int type;

        public Item(Object identifiable, int sectionPosition, int listPosition, String text, int type) {
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
}