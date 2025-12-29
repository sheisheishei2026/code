package x.shei.util;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class LayoutManagerUtils {

    public static int findLastVisiblePosition(RecyclerView.LayoutManager layoutManager) {
        int lastItemPosition = -1;
        if (layoutManager == null) {
            return lastItemPosition;
        }
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            lastItemPosition = linearLayoutManager.findLastVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] lastItemPositionArray = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
            lastItemPosition = findLastPosition(lastItemPositionArray);
        }
        return lastItemPosition;

    }

    public static int findFirstVisiblePosition(RecyclerView.LayoutManager layoutManager) {
        int firstItemPosition = -1;
        if (layoutManager == null) {
            return firstItemPosition;
        }
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] lastItemPositionArray = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null);
            firstItemPosition = findFirstPosition(lastItemPositionArray);
        }
        return firstItemPosition;
    }

    public static int findFirstCompletelyVisiblePosition(RecyclerView.LayoutManager layoutManager) {
        int firstItemPosition = -1;
        if (layoutManager == null) {
            return firstItemPosition;
        }
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            firstItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] lastItemPositionArray = ((StaggeredGridLayoutManager) layoutManager).findFirstCompletelyVisibleItemPositions(null);
            firstItemPosition = findFirstPosition(lastItemPositionArray);
        }
        return firstItemPosition;
    }

    private static int findFirstPosition(int[] array) {
        int position = array.length > 0 ? array[0] : -1;
        for (int cell : array) {
            if (cell < position && cell > -1) {
                position = cell;
            }
        }
        return position;
    }

    private static int findLastPosition(int[] array) {
        int position = -1;
        for (int cell : array) {
            if (cell > position && cell > -1) {
                position = cell;
            }
        }
        return position;
    }
}
