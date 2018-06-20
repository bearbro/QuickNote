package cn.edu.zjut.quicknote.model;

import java.util.List;

public abstract class LoadDataCallBack<T> {
    protected abstract void onSucceed(List<T> list);

}
