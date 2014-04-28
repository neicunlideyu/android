package cn.onboard.android.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.onboard.api.dto.User;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.onboard.android.app.R;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.BitmapManager;
import cn.onboard.android.app.ui.Person;

/**
 * Created by xingliang on 14-3-24.
 */
public class EveryoneAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {

    class UserDepartmentName {
        final String userName;
        final int userId;
        final String avatar;
        final String departmentName;

        UserDepartmentName(String departmentName, String username, int userId, String avatar) {
            this.userName = username;
            this.departmentName = departmentName;
            this.userId = userId;
            this.avatar = avatar;
        }
    }

    private final List<UserDepartmentName> users;
    private final int companyId;

    private final int mHeaderResId;
    private final LayoutInflater mInflater;
    private final int mItemResId;
    private final BitmapManager bmpManager;

    public EveryoneAdapter(Context context, Map<String, List<User>> departmentNameUserMap, int companyId, int mHeaderResId, int mItemResId) {
        this.companyId = companyId;
        this.mHeaderResId = mHeaderResId;
        this.mItemResId = mItemResId;
        this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.widget_dface_loading));
        users = new ArrayList<UserDepartmentName>();
        int headPosition = 0;
        for (String departmentName : departmentNameUserMap.keySet()) {
            List<User> departmentUsers = departmentNameUserMap.get(departmentName);
            for (User user : departmentUsers) {
                users.add(new UserDepartmentName(departmentName, user.getName(), user.getId(), user.getAvatar()));
            }
        }
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public long getHeaderId(int position) {
        return getItem(position).departmentName.charAt(0);
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        TextView headView;
        convertView = mInflater.inflate(mHeaderResId, parent, false);
        headView = (TextView) convertView.findViewById(R.id.group_name);
        convertView.setTag(headView);
        UserDepartmentName userDepartmentName = getItem(position);
        headView.setText(userDepartmentName.departmentName);

        return convertView;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public UserDepartmentName getItem(int i) {
        return users.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView itemView;
        view = mInflater.inflate(mItemResId, viewGroup, false);
        itemView = (TextView) view.findViewById(R.id.user_name);
        final UserDepartmentName item = getItem(i);
        itemView.setText(item.userName);
        ImageView face = (ImageView) view.findViewById(R.id.user_face);
        face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context,
                        Person.class);
                intent.putExtra("userId", item.userId);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.putExtra("companyId", companyId);
                context.startActivity(intent);

            }
        });
        String faceURL = URLs.USER_FACE_HTTP + item.avatar;
        bmpManager.loadBitmap(faceURL, face);

        return view;
    }
}
