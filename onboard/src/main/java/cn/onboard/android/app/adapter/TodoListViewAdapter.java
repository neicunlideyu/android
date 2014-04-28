package cn.onboard.android.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.onboard.api.dto.Todo;

import org.joda.time.format.DateTimeFormat;

import java.util.List;

import cn.onboard.android.app.R;
import cn.onboard.android.app.ui.EditTodo;
import cn.onboard.android.app.ui.EditTodolist;
import cn.onboard.android.app.ui.NewTodo;
import cn.onboard.android.app.ui.fragment.TodoFragment.EditType;
import cn.onboard.android.app.ui.fragment.TodoFragment.Item;
import cn.onboard.android.app.ui.fragment.TodoFragment.Type;

/**
 * Created by xuchen on 14-4-18.
 */
public class TodoListViewAdapter extends BaseAdapter implements SectionIndexer {
    private final List<Item> listItems;// 数据集合
    private final LayoutInflater listContainer;// 视图容器
    private Context context;
    private final Item[] sections;
    private Fragment fragment;

    /**
     * 实例化Adapter
     *
     * @param context
     * @param data
     */
    public TodoListViewAdapter(Context context, Fragment fragment,List<Item> data, int sectionSize) {
        this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
        this.listItems = data;
        this.sections = new Item[sectionSize];
        this.context = context;
        this.fragment = fragment;
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

//            if (convertView == null) {
        listItemView = new ListItemView();
        if (item.getType() == Type.TODOLIST.value()) {
            convertView = listContainer
                    .inflate(R.layout.todolist_item, null);
            listItemView.name = (TextView) convertView.findViewById(R.id.todolist_titile);
            listItemView.name.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, EditTodolist.class);
                    intent.putExtra("companyId", item.todolist.getCompanyId());
                    intent.putExtra("projectId", item.todolist.getProjectId());
                    intent.putExtra("todolistId", item.todolist.getId());
                    intent.putExtra("todolistName", item.todolist.getName());
                    fragment.startActivityForResult(intent, EditType.CREATE.value());


                }
            });
            listItemView.newTodo = (ImageView) convertView.findViewById(R.id.new_todo_button);
            listItemView.newTodo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context,
                            NewTodo.class);
                    intent.putExtra("companyId", item.todolist.getCompanyId());
                    intent.putExtra("projectId", item.todolist.getProjectId());
                    intent.putExtra("todolistId", item.todolist.getId());
                    fragment.startActivityForResult(intent, 0);

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
                    intent.putExtra("companyId", item.todolist.getCompanyId());
                    intent.putExtra("projectId", item.todolist.getProjectId());
                    intent.putExtra("todoId", ((Todo) item.identifiable).getId());
                    fragment.startActivityForResult(intent, EditType.UPDATE.value());
                }
            });

            listItemView.completeTodo = (CheckBox) convertView.findViewById(R.id.todo_complete_checkBox);
            listItemView.completeTodo.setChecked(false);
            listItemView.completeTodo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (isChecked)
//                        new CompleteTodolistTask().execute(((Todo) item.identifiable).getId());
                }
            });
            if (((Todo) item.identifiable).getCompleted() != null && ((Todo) item.identifiable).getCompleted()) {
                listItemView.completeTodo.setChecked(true);
                listItemView.completeTodo.setClickable(false);
                listItemView.name.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                listItemView.name.setTextColor(Color.RED);
            }

        }
        // 设置控件集到convertView
        convertView.setTag(listItemView);
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
