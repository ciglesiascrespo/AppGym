package com.iglesias.c.appgym.Iterator;

import android.content.Context;

import com.iglesias.c.appgym.Db.DbHandler;
import com.iglesias.c.appgym.Presenter.LoginPresenter;
import com.iglesias.c.appgym.RestApi.Adapter.RestApiAdapter;
import com.iglesias.c.appgym.RestApi.ConstantesRestApi;
import com.iglesias.c.appgym.RestApi.EndPoints;
import com.iglesias.c.appgym.RestApi.Model.ResultLogin;

import retrofit2.Retrofit;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Ciglesias on 21/03/2018.
 */

public class LoginIterator {
    LoginPresenter presenter;
    DbHandler dbHandler;

    public LoginIterator(LoginPresenter presenter, Context context) {
        this.presenter = presenter;
        dbHandler = DbHandler.getInstance(context);
    }

    public void validateUser(String nro) {

        Retrofit retrofit = RestApiAdapter.provideRetrofit();

        retrofit.create(EndPoints.class).login(nro)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<ResultLogin>() {
                               @Override
                               public void onCompleted() {

                               }

                               @Override
                               public void onError(Throwable e) {
                                   e.printStackTrace();
                                   presenter.onErrorLogin();

                               }

                               @Override
                               public void onNext(ResultLogin response) {

                                   if (response.getErrorCode() == ConstantesRestApi.CODE_ERROR) {
                                       presenter.onUserNotValid();
                                   } else {
                                       presenter.onSuccesLogin(response.getInfo());
                                   }

                               }
                           }
                );

    }

    public void validateUserDb(final String nro) {
        Observable.create(new Observable.OnSubscribe<ResultLogin>() {
            @Override
            public void call(Subscriber<? super ResultLogin> subscriber) {
                subscriber.onNext(dbHandler.verificaUsuarioDb(nro));
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ResultLogin>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        presenter.onErrorLogin();
                    }

                    @Override
                    public void onNext(ResultLogin response) {
                        if (response.getErrorCode() == ConstantesRestApi.CODE_ERROR) {
                            presenter.onUserNotValid();
                        } else {
                            presenter.onSuccesLogin(response.getInfo());
                        }

                    }
                });


    }

}
