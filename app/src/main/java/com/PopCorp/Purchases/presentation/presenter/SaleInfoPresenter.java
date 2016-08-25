package com.PopCorp.Purchases.presentation.presenter;

import com.PopCorp.Purchases.data.callback.CreateEditListCallback;
import com.PopCorp.Purchases.data.dao.ListItemCategoryDAO;
import com.PopCorp.Purchases.data.model.ListItem;
import com.PopCorp.Purchases.data.model.ListItemCategory;
import com.PopCorp.Purchases.data.model.ListItemSale;
import com.PopCorp.Purchases.data.model.Sale;
import com.PopCorp.Purchases.data.model.ShoppingList;
import com.PopCorp.Purchases.data.utils.PreferencesManager;
import com.PopCorp.Purchases.domain.interactor.ListItemInteractor;
import com.PopCorp.Purchases.domain.interactor.SaleInteractor;
import com.PopCorp.Purchases.domain.interactor.ShoppingListInteractor;
import com.PopCorp.Purchases.presentation.view.moxy.SaleInfoView;
import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@InjectViewState
public class SaleInfoPresenter extends MvpPresenter<SaleInfoView> implements CreateEditListCallback {

    private SaleInteractor interactor = new SaleInteractor();
    private ShoppingListInteractor listInteractor = new ShoppingListInteractor();
    private ListItemInteractor listItemInteractor = new ListItemInteractor();
    private ListItemCategoryDAO listItemCategoryDAO = new ListItemCategoryDAO();

    private Sale sale;
    public List<ShoppingList> lists = new ArrayList<>();
    private ArrayList<ShoppingList> selectedLists = new ArrayList<>();

    public void setSale(int saleId) {
        if (sale == null) {
            interactor.getSale(Integer.valueOf(PreferencesManager.getInstance().getRegionId()), saleId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Sale>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Sale result) {
                            sale = result;
                            getViewState().showInfo(sale);
                        }
                    });
        }
    }

    public Sale getSale() {
        return sale;
    }

    public void loadShoppingLists() {
        listInteractor.getData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<ShoppingList>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<ShoppingList> shoppingLists) {
                        if (shoppingLists != null && shoppingLists.size() > 0) {
                            Collections.sort(shoppingLists, PreferencesManager.getInstance().getShoppingListComparator());
                            lists = shoppingLists;
                            getViewState().showListsSelecting(shoppingLists);
                        } else {
                            getViewState().showEmptyLists();
                        }
                    }
                });
    }

    @Override
    public void onListEdited(ShoppingList list, String name, String currency) {

    }

    @Override
    public void addNewList(String name, String currency) {
        Calendar calendar = Calendar.getInstance();
        ShoppingList list = new ShoppingList(-1, name, calendar.getTimeInMillis(), 0, currency);
        listInteractor.addNewShoppingList(list);
        selectedLists.add(list);
        openInputListItem();
    }

    private void openInputListItem() {
        SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy", new Locale("ru"));
        List<ListItemCategory> categories = listItemCategoryDAO.getAllCategories();
        ListItemCategory listItemCategory = null;
        if (categories != null && categories.size() > 0) {
            listItemCategory = categories.get(0);
            for (ListItemCategory category : categories) {
                if (category.getName().contains("Акци")) {
                    listItemCategory = category;
                }
            }
        }
        String count = "1";
        String edizm = "шт ";
        if (sale.getQuantity() != null && !sale.getQuantity().isEmpty()) {
            String[] countSplit = sale.getQuantity().split(" ");
            count = countSplit[0];
            edizm = countSplit[1];
            switch (edizm) {
                case "г":
                    edizm = "кг";
                    count = new BigDecimal(count).divide(new BigDecimal("1000")).toString();
                    break;
                case "мл":
                    edizm = "литр";
                    count = new BigDecimal(count).divide(new BigDecimal("1000")).toString();
                    break;
            }
        }
        String coast = getCoast(sale, count);

        ListItem item = new ListItem(
                -1,
                -1,
                sale.getTitle(),
                count,
                edizm,
                coast,
                listItemCategory,
                sale.getShop().getName(),
                sale.getSubTitle(),
                false,
                false,
                new ListItemSale(-1, sale.getImage(), format.format(new Date(sale.getPeriodStart())), format.format(new Date(sale.getPeriodEnd())))
        );
        long[] ids = new long[selectedLists.size()];
        for (int i = 0; i < selectedLists.size(); i++) {
            ids[i] = selectedLists.get(i).getId();
        }
        getViewState().openInputListItemFragment(item, ids);
    }

    private String getCoast(Sale sale, String count){
        String result = "0";
        if (sale.getCoast() != null && !sale.getCoast().isEmpty()) {
            result = sale.getCoast().split(" ")[0];
        }
        if (sale.getCoastForQuantity() != null && !sale.getCoastForQuantity().isEmpty()) {
            String[] split = sale.getCoastForQuantity().split(" ");
            result = split[split.length - 2];
        } else {
            BigDecimal countDecimal = new BigDecimal(count);
            BigDecimal coastDecimal = new BigDecimal(result);
            result = countDecimal.multiply(coastDecimal).toString();
        }
        return result;
    }

    public void listsSelected(Integer[] which) {
        selectedLists.clear();
        for (int i : which) {
            selectedLists.add(lists.get(i));
        }
        openInputListItem();
    }

    public void onItemsRerurned(ListItem item) {
        for (ShoppingList list : selectedLists){
            item.setListId(list.getId());
            listItemInteractor.addItem(item);
        }
    }
}