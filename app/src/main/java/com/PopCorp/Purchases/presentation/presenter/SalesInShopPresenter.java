package com.PopCorp.Purchases.presentation.presenter;

import android.view.View;

import com.PopCorp.Purchases.data.analytics.AnalyticsTrackers;
import com.PopCorp.Purchases.data.callback.FavoriteRecyclerCallback;
import com.PopCorp.Purchases.data.comparator.CategoryComparator;
import com.PopCorp.Purchases.data.mapper.SaleTOListItemMapper;
import com.PopCorp.Purchases.data.model.Category;
import com.PopCorp.Purchases.data.model.ListItem;
import com.PopCorp.Purchases.data.model.Sale;
import com.PopCorp.Purchases.data.model.Shop;
import com.PopCorp.Purchases.data.utils.ErrorManager;
import com.PopCorp.Purchases.data.utils.PreferencesManager;
import com.PopCorp.Purchases.domain.interactor.CategoryInteractor;
import com.PopCorp.Purchases.domain.interactor.ListItemInteractor;
import com.PopCorp.Purchases.domain.interactor.SaleInteractor;
import com.PopCorp.Purchases.domain.interactor.ShoppingListInteractor;
import com.PopCorp.Purchases.presentation.view.moxy.SalesInShopView;
import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@InjectViewState
public class SalesInShopPresenter extends MvpPresenter<SalesInShopView> implements FavoriteRecyclerCallback<Sale> {

    private CategoryInteractor categoryInteractor = new CategoryInteractor();
    private SaleInteractor interactor = new SaleInteractor();
    private ShoppingListInteractor listInteractor = new ShoppingListInteractor();
    private ListItemInteractor listItemInteractor = new ListItemInteractor();

    private Shop currentShop;

    private ArrayList<Sale> objects = new ArrayList<>();

    private ArrayList<Category> favoriteCategories = new ArrayList<>();
    private ArrayList<Category> allCategories = new ArrayList<>();
    private ArrayList<Category> filterCategories = new ArrayList<>();

    private String currentFilter = "";
    private int filterPosition = 0;

    public SalesInShopPresenter() {
        getViewState().showProgress();
    }

    public void setCurrentShop(Shop shop) {
        if (shop != null && currentShop == null) {
            currentShop = shop;
            loadCategories();
        }
    }

    public void loadCategories() {
        categoryInteractor.getData()
                .subscribe(new Observer<List<Category>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AnalyticsTrackers.getInstance().sendError(e);
                        ErrorManager.printStackTrace(e);
                        if (allCategories.size() == 0) {
                            getViewState().showError(e);
                        } else {
                            getViewState().showSnackBar(e);
                        }
                    }

                    @Override
                    public void onNext(List<Category> categories) {
                        if (categories.size() == 0) {
                            getViewState().showCategoriesEmpty();
                        } else {
                            if (allCategories.size() == 0) {
                                allCategories.addAll(categories);
                                for (Category category : categories) {
                                    if (category.isFavorite()) {
                                        favoriteCategories.add(category);
                                    }
                                }
                                if (favoriteCategories.size() == 0) {
                                    getViewState().showFavoriteCategoriesEmpty();
                                } else {
                                    loadData();
                                }
                            }
                        }
                    }
                });
    }

    public void loadData() {
        int regionId = Integer.valueOf(PreferencesManager.getInstance().getRegionId());
        int[] shops = new int[]{currentShop.getId()};
        int[] categories = new int[favoriteCategories.size()];
        int[] types = new int[favoriteCategories.size()];
        for (int i = 0; i < favoriteCategories.size(); i++) {
            categories[i] = favoriteCategories.get(i).getId();
            types[i] = favoriteCategories.get(i).getType();
        }
        interactor.getData(regionId, shops, categories, types)
                .subscribe(new Observer<List<Sale>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getViewState().refreshing(false);
                        AnalyticsTrackers.getInstance().sendError(e);
                        ErrorManager.printStackTrace(e);
                        if (objects.size() == 0) {
                            getViewState().showError(e);
                        } else {
                            getViewState().showSnackBar(e);
                        }
                    }

                    @Override
                    public void onNext(List<Sale> sales) {
                        getViewState().refreshing(false);
                        if (sales.size() == 0) {
                            getViewState().showSalesEmpty();
                        } else {
                            objects.clear();
                            objects.addAll(sales);
                            initFilters();
                            getViewState().showData();
                            getViewState().filter(currentFilter);
                            showTapTarget();
                        }
                    }
                });
    }

    private void initFilters() {
        boolean added = false;
        for (Sale sale : objects) {
            if (!filterCategories.contains(sale.getCategory())) {
                filterCategories.add(sale.getCategory());
                added = true;
            }
        }
        Collections.sort(filterCategories, new CategoryComparator());
        if (added && filterPosition != 0) {
            for (Category category : filterCategories) {
                if (category.getName().equals(currentFilter)) {
                    filterPosition = filterCategories.indexOf(category) + 1;
                }
            }
        }
        if (filterCategories.size() > 1 && !currentFilter.startsWith("query")) {
            getViewState().showSpinner();
            getViewState().selectSpinner(filterPosition);
        }
    }

    public void onRefresh() {
        if (favoriteCategories.size() != 0) {
            getViewState().refreshing(true);
            loadData();
        }
    }

    public void selectCategories() {
        if (allCategories.size() == 0) {
            getViewState().showProgress();
            loadCategories();
        } else {
            getViewState().showCategoriesForSelectingFavorites(allCategories);
        }
    }

    public void onFavoriteCategoriesSelected(List<Category> categories) {
        favoriteCategories.addAll(categories);
        getViewState().showProgress();
        loadData();
    }

    @Override
    public void onItemClicked(View view, Sale item) {
        getViewState().showSales(view, item);
    }

    @Override
    public void onItemLongClicked(View view, Sale item) {

    }

    @Override
    public void onEmpty() {
        if (currentFilter.startsWith("query")) {
            getViewState().showEmptyForSearch(currentFilter.replace("query=", ""));
        }
    }

    @Override
    public void onEmpty(int stringRes, int drawableRes, int buttonRes, View.OnClickListener listener) {

    }

    @Override
    public void onEmpty(String string, int drawableRes, int buttonRes, View.OnClickListener listener) {

    }


    public ArrayList<String> getFilterStrings() {
        ArrayList<String> names = new ArrayList<>();
        for (Category category : filterCategories) {
            names.add(category.getName());
        }
        return names;
    }

    public void onFilter(int position) {
        filterPosition = position;
        if (position == 0) {
            currentFilter = "";
        } else {
            currentFilter = filterCategories.get(position - 1).getName();
        }
        getViewState().filter(currentFilter);
        getViewState().selectSpinner(position);
    }

    public void tryAgain() {
        getViewState().showProgress();
        loadData();
    }

    public ArrayList<Sale> getObjects() {
        return objects;
    }

    public String getTitle() {
        return currentShop.getName();
    }

    public void search(String query) {
        if (query.isEmpty()) {
            getViewState().showSpinner();
            currentFilter = "";
        } else {
            getViewState().hideSpinner();
            currentFilter = "query=" + query;
        }
        getViewState().showData();
        getViewState().filter(currentFilter);
    }

    public void showTapTarget() {
        /*if (filterCategories.size() > 1 && !currentFilter.startsWith("query")) {
            if (!PreferencesManager.getInstance().isTapTargetForSalesFilterByCategsShown()) {
                getViewState().showTapTargetForFilter();
                PreferencesManager.getInstance().putTapTargetForSalesByCategsFilter(true);
                return;
            }
        }
        if (!PreferencesManager.getInstance().isTapTargetForSalesSearchShown()) {
            getViewState().showTapTargetForSalesSearch();
            PreferencesManager.getInstance().putTapTargetForSalesSearch(true);
            return;
        }
        if (!PreferencesManager.getInstance().isTapTargetForSalesFavoriteShown()) {
            getViewState().showTapTargetForSalesFavorite();
            PreferencesManager.getInstance().putTapTargetForSalesFavorite(true);
            return;
        }*/
    }

    @Override
    public void onFavoriteClicked(Sale sale) {
        if (sale.isFavorite()){
            listItemInteractor.removeWithSaleIdFromList(listInteractor.getDefaultList().getId(), sale.getId());
        } else {
            ListItem item = SaleTOListItemMapper.getListItem(sale);
            item.setListId(listInteractor.getDefaultList().getId());
            listItemInteractor.addItem(item);
        }
        sale.setFavorite(!sale.isFavorite());
    }

    public void refreshFavorites() {
        interactor.refreshFavorites(objects)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        AnalyticsTrackers.getInstance().sendError(e);
                        ErrorManager.printStackTrace(e);
                    }

                    @Override
                    public void onNext(Boolean edited) {
                        if (edited){
                            getViewState().update();
                        }
                    }
                });
    }
}
