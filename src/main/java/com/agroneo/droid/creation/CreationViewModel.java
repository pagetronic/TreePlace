package com.agroneo.droid.creation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CreationViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CreationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is creation fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}