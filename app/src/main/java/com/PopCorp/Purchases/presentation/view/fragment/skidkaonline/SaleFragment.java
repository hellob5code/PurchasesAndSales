package com.PopCorp.Purchases.presentation.view.fragment.skidkaonline;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.PopCorp.Purchases.R;
import com.PopCorp.Purchases.data.callback.SaleChildCallback;
import com.PopCorp.Purchases.data.callback.SaleMainCallback;
import com.PopCorp.Purchases.data.model.skidkaonline.Sale;
import com.PopCorp.Purchases.data.model.skidkaonline.SaleComment;
import com.PopCorp.Purchases.presentation.common.MvpAppCompatFragment;
import com.PopCorp.Purchases.presentation.presenter.factory.ViewPagerSkidkaonlinePresenterFactory;
import com.PopCorp.Purchases.presentation.presenter.params.provider.SkidkaonlineSaleParamsProvider;
import com.PopCorp.Purchases.presentation.presenter.skidkaonline.SalePresenter;
import com.PopCorp.Purchases.presentation.view.moxy.skidkaonline.SaleView;
import com.arellomobile.mvp.presenter.InjectPresenter;

import java.util.List;

public class SaleFragment extends MvpAppCompatFragment implements SaleMainCallback, SaleView, SkidkaonlineSaleParamsProvider {

    public static final String CURRENT_SALE = "current_sale";

    @InjectPresenter(factory = ViewPagerSkidkaonlinePresenterFactory.class, presenterId = "SalePresenter")
    SalePresenter presenter;

    private int saleId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        saleId = getArguments().getInt(CURRENT_SALE);
        super.onCreate(savedInstanceState);
        presenter.setSale(saleId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sale, container, false);
    }

    @Override
    public void showInfo(Sale sale) {

    }

    @Override
    public void showFragmentComments(Sale sale) {
        FragmentManager fragmentManager = getChildFragmentManager();
        Fragment fragment = new SaleCommentsFragment();
        Bundle args = new Bundle();
        args.putInt(SaleFragment.CURRENT_SALE, sale.getId());
        fragment.setArguments(args);
        ((SaleChildCallback) fragment).setParent(this);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, fragment, fragment.getClass().getSimpleName())
                .commit();
    }

    @Override
    public void showFragmentInfo(Sale sale) {
        FragmentManager fragmentManager = getChildFragmentManager();
        Fragment fragment = new SaleInfoFragment();
        Bundle args = new Bundle();
        args.putInt(SaleFragment.CURRENT_SALE, sale.getId());
        fragment.setArguments(args);
        ((SaleChildCallback) fragment).setParent(this);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, fragment, fragment.getClass().getSimpleName())
                .commit();
    }

    @Override
    public void showComments(List<SaleComment> comments) {

    }

    @Override
    public void showComments() {
        presenter.showComments();
    }

    @Override
    public void showInfo() {
        presenter.showInfo();
    }

    @Override
    public void openSameSale(View view, int saleId) {

    }

    @Override
    public String getSaleId(String presenterId) {
        return String.valueOf(saleId);
    }
}