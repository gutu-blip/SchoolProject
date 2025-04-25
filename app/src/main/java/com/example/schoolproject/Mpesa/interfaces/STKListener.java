package com.example.schoolproject.Mpesa.interfaces;

import com.example.schoolproject.Mpesa.api.response.STKPushResponse;

/**
 * @author Fredrick Ochieng on 02/02/2018.
 */

public interface STKListener {

    void onResponse(STKPushResponse stkPushResponse);
    void onError(Throwable throwable);
}
