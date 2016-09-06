package com.PopCorp.Purchases.domain.interactor.skidkaonline;

import com.PopCorp.Purchases.data.model.skidkaonline.Sale;
import com.PopCorp.Purchases.data.repository.db.skidkaonline.SaleDBRepository;
import com.PopCorp.Purchases.data.repository.net.skidkaonline.SaleNetRepository;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SaleInteractor {

    private SaleNetRepository netRepository = new SaleNetRepository();
    private SaleDBRepository dbRepository = new SaleDBRepository();

    private List<Sale> result;

    public Observable<List<Sale>> getData(int cityId, String shopUrl) {
        return netRepository.getData(cityId, shopUrl)
                .subscribeOn(Schedulers.io())
                .map(sales -> {
                    if (sales.size() != 0) {
                        dbRepository.addAllSales(sales);
                    }
                    return sales;
                })
                .onErrorResumeNext(throwable -> {
                    return dbRepository.getData(cityId, shopUrl)
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
