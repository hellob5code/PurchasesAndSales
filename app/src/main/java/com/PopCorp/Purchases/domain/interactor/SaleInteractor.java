package com.PopCorp.Purchases.domain.interactor;

import com.PopCorp.Purchases.data.dto.CommentResult;
import com.PopCorp.Purchases.data.model.Sale;
import com.PopCorp.Purchases.data.repository.db.SaleDBRepository;
import com.PopCorp.Purchases.data.repository.net.SaleNetRepository;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SaleInteractor {

    SaleNetRepository netRepository = new SaleNetRepository();
    SaleDBRepository dbRepository = new SaleDBRepository();

    private List<Sale> result;

    public Observable<List<Sale>> getData(int regionId, int[] shops, int[] categories, int[] categoriesTypes) {
        return netRepository.getData(regionId, shops, categories, categoriesTypes)
                .subscribeOn(Schedulers.io())
                .map(sales -> {
                    if (sales.size() != 0) {
                        dbRepository.addAllSales(sales);
                    }
                    return sales;
                })
                .onErrorResumeNext(throwable -> {
                    return dbRepository.getData(regionId, shops, categories, categoriesTypes)
                            .doOnNext(sales -> result = sales)
                            .flatMap(sales -> Observable.error(throwable));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext(throwable -> {
                    return Observable.create(new Observable.OnSubscribe<List<Sale>>() {
                        @Override
                        public void call(Subscriber<? super List<Sale>> subscriber) {
                            subscriber.onNext(result);
                            subscriber.onError(throwable);
                        }
                    }).materialize().observeOn(AndroidSchedulers.mainThread()).<List<Sale>>dematerialize();
                });
    }

    /*public Observable<CommentResult> sendComment(String author, String whom, String text, int regionId, int saleId){
        return netRepository.sendComment(author, whom, text, regionId, saleId);
    }*/

    public Observable<CommentResult> sendComment(String author, String whom, String text, int regionId, int saleId){
        return Observable.create(new Observable.OnSubscribe<CommentResult>() {
            @Override
            public void call(Subscriber<? super CommentResult> subscriber) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                subscriber.onNext(new CommentResult(false, "Error", "", "", 0));
            }
        });
    }

    public Observable<Sale> getSale(int cityId, int saleId){
        return dbRepository.getSale(cityId, saleId)
                .flatMap(sale -> {
                    if (sale == null){
                        return getSaleFromNet(cityId, saleId);
                    }
                    return Observable.just(sale);
                });
    }

    private Observable<Sale> getSaleFromNet(int cityId, int saleId){
        return netRepository.getSale(cityId, saleId)
                .flatMap(sale -> {
                    dbRepository.addSale(sale);
                    return Observable.just(sale);
                });
    }
}
