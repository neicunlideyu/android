package cn.onboard.android.app.bean;

/**
 * Created by xuchen on 14-4-18.
 */
public enum ActivityForResultType {
    TODO_CREATE(0), TODO_UPDATE(1), TODOLIST_UPDATE(2), PIC_FROME_CAMERA(3), PIC_FROM_GALLERY(4);

    private int value = 0;

    private ActivityForResultType(int value) {
        this.value = value;
    }

    public static ActivityForResultType valueOf(int value) {
        switch (value) {
            case 0:
                return TODO_CREATE;
            case 1:
                return TODO_UPDATE;
            case 2:
                return TODOLIST_UPDATE;
            case 3:
                return PIC_FROME_CAMERA;
            case 4:
                return PIC_FROM_GALLERY;
            default:
        }
        return null;
    }

    public int value() {
        return this.value;
    }
}