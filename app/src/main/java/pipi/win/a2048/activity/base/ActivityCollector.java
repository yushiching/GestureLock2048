package pipi.win.a2048.activity.base;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pip on 2017/7/11.
 */

public class ActivityCollector {
    public static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishAll() {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
    public static void justKeepLast(){
        int len=activities.size();

        for(int i=0;i<len-1;i++){
            activities.get(i).finish();
        }
    }
}
