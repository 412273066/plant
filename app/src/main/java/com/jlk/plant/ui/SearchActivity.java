package com.jlk.plant.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jlk.plant.R;
import com.jlk.plant.adapter.ListCateAdapter;
import com.jlk.plant.adapter.ListPlantAdapter;
import com.jlk.plant.app.AppInterface;
import com.jlk.plant.base.BaseFragmentActivity;
import com.jlk.plant.models.Plant;
import com.jlk.plant.models.requestmodels.SearchPlantRequest;
import com.jlk.plant.models.returnmodels.GetPlantListReturn;
import com.jlk.plant.utils.L;
import com.jlk.plant.utils.OkHttpUtils;
import com.jlk.plant.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;


public class SearchActivity extends BaseFragmentActivity {

    private final String tag = "SearchActivity";

    private EditText edit_keyword;
    private ImageView img_search;

    private RecyclerView mRecyclerView;

    ListPlantAdapter mAdapter;

    @Override
    public void setActivityContext() {
        mContext = this;
    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_search);
    }

    @Override
    public void initViews() {

        findViewById(R.id.back).setVisibility(View.VISIBLE);

        edit_keyword = (EditText) findViewById(R.id.edit_keyword);
        img_search = (ImageView) findViewById(R.id.img_search);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mRecyclerView.setHasFixedSize(true);
        // 设置item间隔
//        mRecyclerView.addItemDecoration(new SpacesItemDecoration(0, 0, 0, 0));
        //创建并设置Adapter
        mAdapter = new ListPlantAdapter();
        mAdapter.setOnItemClickListener(new ListCateAdapter.OnItemClickListener<Plant>() {

            @Override
            public void onItemClick(int position, Plant data) {
                Bundle mBundle = new Bundle();
                mBundle.putString("img", data.getImg());
                mBundle.putString("feature", data.getPlantFeature());
                mBundle.putString("habit", data.getPlantHabit());
                mBundle.putString("info", data.getPlantInfo());
                mBundle.putString("name", data.getPlantName());
                mBundle.putString("use", data.getPlantUse());

                startActivityAnim(mBundle, DetailPlantActivity.class);

            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void initListeners() {
        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = edit_keyword.getText().toString().trim();
                if (StringUtils.isEmpty(keyword)) {
                    showToast("请输入植物关键字!");
                } else {
                    doSearch(keyword);
                }
            }
        });


    }

    private void doSearch(String keyword) {
        SearchPlantRequest request = new SearchPlantRequest(keyword);
        String json = new Gson().toJson(request);

        OkHttpUtils client = new OkHttpUtils(mContext, json, AppInterface.SEARCHPLANT);

        client.setOnHttpPostListener(new OkHttpUtils.OnHttpPostListener() {
            @Override
            public void onPostSuccessListener(String json) {
                try {
                    L.i("返回" + AppInterface.GETPLANTLIST + ":" + json);
                    Gson gson = new Gson();
                    final GetPlantListReturn result = gson.fromJson(json, GetPlantListReturn.class);


                    ArrayList<Plant> newData = (ArrayList<Plant>) result.getList();
                    if (newData == null || newData.size() == 0) {
                        showToast(result.getMsg());
                        mAdapter.removeAllData();
                    } else {
                        mAdapter.resetData(newData);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "接口出错，开发人员正在修复中。", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPostFailListener(IOException e) {

            }

            @Override
            public void onPrePostListener() {

            }
        });


        client.doPost();

    }


    @Override
    public void initData() {


    }

    @Override
    public String getTitleName() {
        return null;
    }


}
