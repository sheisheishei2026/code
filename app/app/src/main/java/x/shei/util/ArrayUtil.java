package x.shei.util;

import android.widget.Adapter;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 数组操作工具类
 * Created by jzj on 16/6/15.
 */
public final class ArrayUtil {
    private ArrayUtil() {

    }

    public interface ITarget<T> {
        boolean isTarget(T t);
    }

    public interface NonNullOperator<T> {
        void operate( T t);
    }

    public interface Visitor<T> {
        boolean accept(T item);
    }

    public static boolean notEmpty(Object[] array) {
        return !isEmpty(array);
    }

    public static boolean notEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean notEmpty(Adapter adapter) {
        return !isEmpty(adapter);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Adapter adapter) {
        return adapter == null || adapter.isEmpty();
    }


    public static boolean isEmpty(Object[] array){
        return array == null || array.length == 0;
    }


    /**
     * 获取collection的size，如果为空则返回0
     */
    public static int sizeof( Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * 获取list的size，如果为空则返回0
     */
    public static int sizeof(  Object[] list) {
        return list == null ? 0 : list.length;
    }

    /**
     * 获取list的item，如果list为空、超出范围，则返回null
     */
    public static <T> T getItem(  List<T> list, int index) {
        return (list == null || index < 0 || index >= list.size()) ? null : list.get(index);
    }

    /**
     * 获取array的item，如果array为空、超出范围，则返回null
     */
    public static <T> T getItem( T[] array, int index) {
        return (array == null || index < 0 || index >= array.length) ? null : array[index];
    }

    /**
     * 将elements添加到collection中
     */
    public static <T> void addAll(  Collection<T> collection,
                                   Collection<? extends T> elements) {
        if (collection != null && ArrayUtil.notEmpty(elements)) {
            collection.addAll(elements);
        }
    }

    public static <T> int indexOf( List<T> list, ITarget<T> target) {
        if (ArrayUtil.notEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                if (target.isTarget(list.get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static <T> int indexOf(  T[] array,  ITarget<T> target) {
        if (ArrayUtil.notEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (target.isTarget(array[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

//    public static <T> List<T> subList(@Nullable List<T> list, int fromIndex, int toIndex) {
//        if (isEmpty(list)) {
//            return list;
//        }
//        int max = list.size();
//        fromIndex = MathUtil.constrain(fromIndex, 0, max);
//        toIndex = MathUtil.constrain(toIndex, 0, max);
//        if (fromIndex > toIndex) {
//            int index = fromIndex;
//            fromIndex = toIndex;
//            toIndex = index;
//        }
//        return list.subList(fromIndex, toIndex);
//    }

    /**
     * 将array所有非空元素加入到一个List中
     */
    public static <T> ArrayList<T> getNonNullElements(T[] array) {
        if (isEmpty(array)) {
            return null;
        }
        ArrayList<T> list = new ArrayList<>();
        for (T t : array) {
            if (t != null) {
                list.add(t);
            }
        }
        return list.isEmpty() ? null : list;
    }

    /**
     * 将list中的所有元素和obj加入一个新的list返回
     */
    public static <T> ArrayList<T> join( Collection<T> list,  T obj) {
        ArrayList<T> result = new ArrayList<>();
        if (list != null) {
            result.addAll(list);
        }
        if (obj != null) {
            result.add(obj);
        }
        return result;
    }

    /**
     * 将所有list中的所有元素加入一个新的list返回
     */
    @SafeVarargs
    public static <T> ArrayList<T> joinLists(Collection<? extends T>... lists) {
        ArrayList<T> result = new ArrayList<>();
        if (lists != null) {
            for (Collection<? extends T> c : lists) {
                result.addAll(c);
            }
        }
        return result;
    }

    /**
     * 处理collections中每个非空元素
     */
    public static <T> void forEachNonNull( Collection<T> collections,
                                            NonNullOperator<? super T> operator) {
        if (notEmpty(collections)) {
            for (T t : collections) {
                if (t != null) {
                    operator.operate(t);
                }
            }
        }
    }

    /**
     * 过滤出符合条件的Item
     */
    @SuppressWarnings("ConstantConditions")
    public static <T> List<T> filter( Collection<T> collection,
                                      Visitor<? super T> visitor) {
        if (isEmpty(collection) || visitor == null) {
            return null;
        }
        ArrayList<T> list = new ArrayList<>();
        for (T t : collection) {
            if (visitor.accept(t)) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 返回两个列表的交集
     *
     * @author mashengchao 2012-2-27 下午3:47:32
     */
    public static <T> List<T> intersect(List<T> list1, List<T> list2) {
        @SuppressWarnings("unchecked")
        List<T> list = new ArrayList<T>(
                (Collection<? extends T>) Arrays.asList(new Object[list1.size()]));

        Collections.copy(list, list1);
        list.retainAll(list2);
        return list;
    }

    public static <T> List<T> asList(T... arr) {
        if (arr == null) {
            return null;
        }
        return new ArrayList<T>(Arrays.asList(arr));
    }

    /**
     * 返回两个列表的并集
     *
     * @author mashengchao 2012-2-27 下午3:53:46
     */
    public static <T> List<T> union(List<T> list1, List<T> list2) {
        @SuppressWarnings("unchecked")
        List<T> list = new ArrayList<T>(
                (Collection<? extends T>) Arrays.asList(new Object[list1.size()]));

        Collections.copy(list, list1);
        if (list2 != null) {
            list.removeAll(list2);
        }
        list.addAll(list2);
        return list;
    }

    /**
     * 返回两个列表的差集
     *
     * @author mashengchao 2012-2-27 下午3:53:33
     */
    public static <T> List<T> diff(List<T> list1, List<T> list2) {
        List<T> unionList = union(list1, list2);
        List<T> intersectList = intersect(list1, list2);
        if (intersectList != null) {
            unionList.removeAll(intersectList);
        }

        return unionList;
    }

    /**
     * 检测一个对象是否在列表中
     *
     * @author mashengchao 2012-4-9 下午3:51:27
     */
    public static <T> boolean inArray(T t, List<T> list) {
        if (t == null || isEmpty(list)) {
            return false;
        }
        return list.contains(t);
    }

    /**
     * 大于等于
     */
    public static boolean greaterEqual(Collection conn, int min) {
        return !isEmpty(conn) && conn.size() >= min;
    }

    /**
     * 小于等于
     */
    public static boolean lessEqual(Collection conn, int max) {
        return isEmpty(conn) || conn.size() <= max;
    }


    /**
     * 获取size
     */
    public static <T> int getSize(List<T> conn) {
        return null == conn ? 0 : conn.size();
    }

    /**
     * 存在一个不为空，返回true
     */
    public static boolean hasOne(Collection... con) {
        if (null == con) {
            return false;
        }
        for (Collection c : con) {
            if (null != c && !c.isEmpty()) {
                return true;
            }
        }
        return false;
    }


}
