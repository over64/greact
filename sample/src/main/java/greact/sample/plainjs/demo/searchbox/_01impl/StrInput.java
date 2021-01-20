package greact.sample.plainjs.demo.searchbox._01impl;

import greact.sample.plainjs.demo.searchbox._00base._01Input;

public class StrInput extends _01Input<String> {
    public StrInput() {super("text");}

    public StrInput label(String lbl) {
        this._label = lbl;
        return this;
    }

    @Override protected String parseValueOpt(String src) {
        return src;
    }
}