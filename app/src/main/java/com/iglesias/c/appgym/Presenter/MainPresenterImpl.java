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
        if (!msj.toLowerCase().contains("id:")) {
            String msjR = iterator.getMsj(msj);
            if (!msjR.isEmpty())
                view.showErrorLoginDialog(msjR);
        } else {
            view.compareId(msj.split(":")[1]);
        }
    }
}