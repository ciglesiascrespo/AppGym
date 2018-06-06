package com.iglesias.c.appgym.Presenter;

import com.iglesias.c.appgym.Iterator.MainIterator;
import com.iglesias.c.appgym.View.MainView;

/**
 * Created by dell on 08/05/2018.
 */

public class MainPresenterImpl {
    MainView view;
    MainIterator iterator;

    public MainPresenterImpl(MainView view) {
        this.view = view;
        iterator = new MainIterator();
    }

    public void receiveMsj(String msj) {
view.showErrorLoginDialog(msj);
        if (!msj.toLowerCase().contains("template:")) {
            if (msj.toLowerCase().contains("r")) {
                view.sendId();
            } else if (msj.substring(0, 1).toLowerCase().equals("n")) {
                view.compareId("-1");
            } else {
                String msjR = iterator.getMsj(msj.substring(0, 1));
                if (!msjR.isEmpty())
                    view.showErrorLoginDialog(msjR);
            }

        } else {
            view.compareId("0");
        }

    }
}
