package com.bignerdranch.android.criminalintent;

import android.support.v4.app.Fragment;

public class CrimeListActivity extends SingleFragmentActivity {

    private boolean pay = false;

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }


    public boolean checkpay(){ //return the pay situation
        return pay;
    }
    public void changepay(){ // change the pay situation is the item pay is clicked
        pay = !pay;
    }
}
