package org.tangram.view;

public class TargetDescriptor {

    public TargetDescriptor(Object bean, String view, String action) {
        this.bean = bean;
        this.view = view;
        this.action = action;
    } // TargetDescriptor()

    public Object bean;

    public String view;

    public String action;


    public Object getBean() {
        return bean;
    }


    public String getView() {
        return view;
    }


    public String getAction() {
        return action;
    }

} // TargetDescriptor

