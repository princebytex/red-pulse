package com.redpulse.app.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.redpulse.app.data.model.CreditTransaction;
import com.redpulse.app.utils.Constants;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardRepository {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public void observeMonthlyLeaderboard(MutableLiveData<List<CreditTransaction>> liveData) {
        firestore.collection(Constants.CREDIT_TRANSACTIONS)
                .whereEqualTo("monthKey", Constants.currentMonthKey())
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    List<CreditTransaction> list = new ArrayList<>();
                    value.getDocuments().forEach(snapshot -> {
                        CreditTransaction item = snapshot.toObject(CreditTransaction.class);
                        if (item != null) list.add(item);
                    });
                    liveData.postValue(list);
                });
    }
}
