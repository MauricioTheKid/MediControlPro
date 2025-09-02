package com.example.medicontrolpro.ui.doctores;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DoctoresViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public DoctoresViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Lista de Doctores - Próximamente");
    }

    public LiveData<String> getText() {
        return mText;
    }
}