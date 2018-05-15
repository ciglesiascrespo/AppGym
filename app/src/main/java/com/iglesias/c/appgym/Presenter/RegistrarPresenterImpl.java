package com.iglesias.c.appgym.Presenter;

import com.iglesias.c.appgym.Iterator.RegistraIterator;
import com.iglesias.c.appgym.View.RegistrarView;

/**
 * Created by Ciglesias on 6/05/2018.
 */

public class RegistrarPresenterImpl implements RegistrarPresenter {
    private RegistrarView view;
    private RegistraIterator iterator;
    private String matHuella = "";
    private boolean flagQ = false;

    public RegistrarPresenterImpl(RegistrarView view) {
        this.view = view;
        iterator = new RegistraIterator(this, view.getContextApp());
    }

    public void registrarUsuario(String nro, String id, Boolean flag) {
        if (nro.isEmpty()) {
            view.showErrorLoginDialog("Diligencia un número de documento", false);
        } else if (!flag) {
            view.showErrorLoginDialog("Debe registrar una huella", false);
        } else {
            view.showLoading();
            iterator.registrarUsuario(nro, id);
        }


    }

    @Override
    public void onUserExist() {
        view.hideLoading();
        view.showErrorLoginDialog("El usuario ya se encuentra registrado.", true);
    }

    @Override
    public void onError() {
        view.hideLoading();
        view.showErrorLoginDialog("Error registrando usuario.", true);
    }

    @Override
    public void onSuccess() {
        view.hideLoading();
        view.showErrorLoginDialog("Usuario registrado con éxito.", true);

    }

    public void receiveMsj(String msj) {

        if (!msj.toLowerCase().contains("id:")) {
            String msjResult = iterator.getMsj(msj.substring(0, 1));
            if (!msjResult.isEmpty())
                view.showErrorLoginDialog(msjResult, false);
        }

        if (msj.toLowerCase().contains("q")) {
            flagQ = true;
        }
        if (flagQ) {
           // matHuella += msj;
        }
        if (msj.toLowerCase().contains("}")) {
            matHuella += msj;
            view.setFlagHuella(true);

            view.setId(matHuella.replace("Q", "").replace("}", "").replace("{",""));

            view.showErrorLoginDialog(matHuella.replace("Q", "").replace("}", "").replace("{",""),false);
            matHuella = "";
        }
    }
}
