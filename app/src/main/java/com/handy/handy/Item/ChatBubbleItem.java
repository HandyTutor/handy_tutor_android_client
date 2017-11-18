package com.handy.handy.Item;

/**
 * Created by sekyo on 2017-11-17.
 */

public class ChatBubbleItem {
    private boolean isLeft;
    private String content;

    public ChatBubbleItem(Boolean isLeft, String content){
        this.isLeft = isLeft;
        this.content = content;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public void setIsLeft(boolean left) {
        isLeft = left;
    }

}
