package greact.sample.plainjs;

import com.over64.greact.GReact;
import com.over64.greact.dom.HTMLNativeElements;
import com.over64.greact.dom.HTMLNativeElements.button;
import com.over64.greact.dom.HTMLNativeElements.div;
import com.over64.greact.dom.HTMLNativeElements.h1;
import com.over64.greact.dom.HtmlElement;
import com.over64.greact.model.components.Component;

public class HW implements Component {

    int nUsers = 1;

    @Override
    public void mount(HtmlElement dom) {
//        GReact.mount(dom, new div() {{
//            new h1() {{ innerText = "GReact users: " + nUsers; }};
//
//            if(nUsers > 10)
//                new h1() {{ innerText = "too much users: " + nUsers; }};
//
//            new button() {{
//                innerText = "increment";
//                onclick = () -> {
//                    nUsers += 1;
//                    GReact.effect(nUsers);
//                };
//            }};
//        }});

        GReact.mount(dom, new div() {{
            new h1() {{
                innerText = "GReact users: " + nUsers;
            }};
            new button() {{
                innerText = "increment";
                onclick = () -> {
                    nUsers += 1;
                    GReact.effect(nUsers);
                };
            }};
        }});
    }
}