package cn.yangjian.im.common.dataconstruct;

import cn.yangjian.im.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

//字典树字符前缀模糊匹配
public class TrieTree {

    //    字典树节点
    private class Node {

        //是否为最后一个字符
        public boolean isEnd = false;

        //如果只是查询，则不需要存储数据
        public char data;

        public Node[] children = new Node[CHILDREN_LENGTH];

    }

    /**
     * 大小写都可保存
     */
    private static final int CHILDREN_LENGTH = 26 * 2;

    private static final char UPPERCASE_STAR = 'A';

    /**
     * 小写就要 -71
     */
    private static final char LOWERCASE_STAR = 'G';

    private Node root;

    public TrieTree() {
        root = new Node();
    }

    /**
     * 写入
     *
     * @param data
     */
    public void insert(String data) {
        this.insert(this.root, data);
    }

    private void insert(Node root, String data) {
        char[] chars = data.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            int index;
            if (Character.isUpperCase(aChar)) {
                index = aChar - UPPERCASE_STAR;
            } else {
                //小写就要 -71
                index = aChar - LOWERCASE_STAR;
            }


            if (index >= 0 && index < CHILDREN_LENGTH) {
                if (root.children[index] == null) {
                    Node node = new Node();
                    root.children[index] = node;
                    root.children[index].data = chars[i];

                }

                //最后一个字符设置标志
                if (i + 1 == chars.length) {
                    root.children[index].isEnd = true;
                }

                //指向下一节点
                root = root.children[index];
            }

        }
    }


    //    根据前缀查找
    public List<String> prefixSearch(String key) {
        List<String> value = new ArrayList<>();
        if (StringUtil.isEmpty(key)) {
            return value;
        }

        char k = key.charAt(0);
        int index;
        if (Character.isUpperCase(k)) {
            index = k - UPPERCASE_STAR;
        } else {
            index = k - LOWERCASE_STAR;

        }
        if (root.children != null && root.children[index] != null) {
            return query(root.children[index], value, key.substring(1), String.valueOf(k));
        }
//        没查到
        return value;
    }

    private List<String> query(Node child, List<String> value, String key, String result) {

        if (child.isEnd && key == null) {
            value.add(result);
        }
        if (StringUtil.isNotEmpty(key)) {
            char ca = key.charAt(0);

            int index;
            if (Character.isUpperCase(ca)) {
                index = ca - UPPERCASE_STAR;
            } else {
                index = ca - LOWERCASE_STAR;

            }

            if (child.children[index] != null) {
                query(child.children[index], value, key.substring(1).equals("") ? null : key.substring(1), result + ca);
            }
        } else {
            for (int i = 0; i < CHILDREN_LENGTH; i++) {
                if (child.children[i] == null) {
                    continue;
                }

                int j;
                if (Character.isUpperCase(child.children[i].data)) {
                    j = UPPERCASE_STAR + i;
                } else {
                    j = LOWERCASE_STAR + i;
                }

                char temp = (char) j;
                query(child.children[i], value, null, result + temp);
            }
        }

        return value;
    }

}
