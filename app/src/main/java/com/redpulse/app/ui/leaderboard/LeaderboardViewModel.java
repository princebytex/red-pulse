package com.redpulse.app.ui.leaderboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.redpulse.app.data.model.CreditTransaction;
import com.redpulse.app.data.repository.LeaderboardRepository;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardViewModel extends ViewModel {
    private final MutableLiveData<List<CreditTransaction>> leaderboard = new MutableLiveData<>(new ArrayList<>());
    private final LeaderboardRepository repository = new LeaderboardRepository();

    public LiveData<List<CreditTransaction>> getLeaderboard() { return leaderboard; }
    public void load() { repository.observeMonthlyLeaderboard(leaderboard); }
}
