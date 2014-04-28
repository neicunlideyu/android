package cn.onboard.android.app.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.onboard.api.dto.Attachment;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;
import java.util.Locale;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.R;
import cn.onboard.android.app.bean.AttachmentIconType;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.BitmapManager;
import cn.onboard.android.app.common.UIHelper;

public class AttachmentListViewAdapter extends BaseAdapter {
    private final List<Attachment> listItems;// 数据集合
    private final LayoutInflater listContainer;// 视图容器
    private final int itemViewResource;// 自定义项视图源
    private final BitmapManager bmpManager;
    private boolean simplified;

    static class ListItemView { // 自定义控件集合
        public ImageView face;
        public TextView title;
        public TextView author;
        public TextView date;
        public Button btn_download;
    }

    /**
     * 实例化Adapter
     *
     * @param context
     * @param attachments
     * @param resource
     */
    public AttachmentListViewAdapter(Context context, List<Attachment> attachments, int resource, boolean simplified) {
        this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
        this.itemViewResource = resource;
        this.listItems = attachments;
        this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(context.getResources(),
                R.drawable.widget_dface_loading));
        this.simplified = simplified;
    }

    public int getCount() {
        return listItems.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int arg0) {
        return 0;
    }

    private final View.OnClickListener imageClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            UIHelper.showImageDialog(v.getContext(), (String) v.getTag());
        }
    };

    /**
     * ListView Item设置
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        // 自定义视图
        ListItemView listItemView = null;
        final Attachment attachment = listItems.get(position);

        if (convertView == null) {
            // 获取list_item布局文件的视图
            convertView = listContainer.inflate(this.itemViewResource, null);

            listItemView = new ListItemView();
            // 获取控件对象
            listItemView.face = (ImageView) convertView.findViewById(R.id.attachment_listitem_icon);
            listItemView.title = (TextView) convertView.findViewById(R.id.attachment_listitem_title);
            listItemView.author = (TextView) convertView.findViewById(R.id.attachment_listitem_author);
            listItemView.date = (TextView) convertView.findViewById(R.id.attachment_listitem_date);
            if(simplified){
                listItemView.author.setVisibility(View.GONE);
                listItemView.date.setVisibility(View.GONE);

            }
            listItemView.btn_download = (Button) convertView.findViewById(R.id.button_download);

            listItemView.btn_download.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    AppContext appContext = (AppContext) v.getContext();
                    appContext.downloadAttachmentByAttachmentId(attachment.getId(),
                            attachment.getName(), attachment.getCompanyId(), attachment.getProjectId());
                }
            });

            // 设置控件集到convertView
            convertView.setTag(listItemView);
        } else {
            listItemView = (ListItemView) convertView.getTag();
        }

        // 设置文字和图片
        if (attachment.getContentType().contains("image")) {
            String attachmentImageURL = URLs.ATTACHMENT_IMAGE_HTTP;
            attachmentImageURL = attachmentImageURL.replaceAll("companyId", attachment.getCompanyId() + "").replaceAll("projectId", attachment.getProjectId() + "").replaceAll("attachmentId", attachment.getId() + "");
            bmpManager.loadBitmap(attachmentImageURL, listItemView.face);
            listItemView.face.setTag(attachmentImageURL);
            listItemView.face.setOnClickListener(imageClickListener);

        } else {
            listItemView.face.setImageDrawable(convertView.getResources()
                    .getDrawable(AttachmentIconType.getAttachmentTypeIconResourceId(attachment.getName(),
                            attachment.getContentType())));
        }


        listItemView.title.setText(attachment.getName());
        listItemView.title.setTag(attachment);// 设置隐藏参数(实体类)
        listItemView.author.setText(attachment.getCreatorName());
        listItemView.date.setText(new PrettyTime(new Locale("zh")).format(attachment.getCreated()));

        return convertView;
    }
}