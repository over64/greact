package com.over64.greact.dom;

import com.greact.model.JSNativeAPI;
import com.greact.model.async;

@JSNativeAPI public class HtmlElement extends Node {
    public static class Style {
        public String color;
        public String border;
        public String padding;
        public String margin;
        public String maxWidth;
        public String display;
        public String cursor;
    }

    // HTML Global Attributes
    public String id;
    public String className;
    public String innerText;
    public String lang;
    public Style style = new Style();

    public Object dependsOn;


    // HTML Event Attributes
    public static class Event<T> {
        public T target;
    }

    @FunctionalInterface
    public interface ChangeHandler<T extends HtmlElement> {
        void handle(Event<T> ev);
    }

    @FunctionalInterface
    public interface MouseEventHandler<T extends HtmlElement> {
        @async void handle(Event<T> ev);
    }

    public enum Key {UP, ENTER, ESC}

    public interface KeyHandler {
        void handle(Key key);
    }

    public KeyHandler onkeyup;
}
