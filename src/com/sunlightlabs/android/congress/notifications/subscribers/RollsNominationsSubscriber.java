package com.sunlightlabs.android.congress.notifications.subscribers;

import java.util.List;

import android.content.Intent;
import android.util.Log;

import com.sunlightlabs.android.congress.R;
import com.sunlightlabs.android.congress.RollList;
import com.sunlightlabs.android.congress.notifications.Subscriber;
import com.sunlightlabs.android.congress.notifications.Subscription;
import com.sunlightlabs.android.congress.utils.Utils;
import com.sunlightlabs.congress.models.CongressException;
import com.sunlightlabs.congress.models.Roll;
import com.sunlightlabs.congress.services.RollService;

public class RollsNominationsSubscriber extends Subscriber {
	
	@Override
	public String decodeId(Object result) {
		return ((Roll) result).id;
	}

	@Override
	public List<?> fetchUpdates(Subscription subscription) {
		Utils.setupRTC(context);
		
		try {
			return RollService.latestNominations(1, RollList.PER_PAGE);
		} catch (CongressException e) {
			Log.w(Utils.TAG, "Could not fetch the latest votes for " + subscription, e);
			return null;
		}
	}
	
	@Override
	public String notificationMessage(Subscription subscription, int results) {
		if (results > 1)
			return results + " new votes.";
		else
			return results + " new vote.";
	}

	@Override
	public Intent notificationIntent(Subscription subscription) {
		return new Intent()
			.setClassName("com.sunlightlabs.android.congress", "com.sunlightlabs.android.congress.RollList")
			.putExtra("type", RollList.ROLLS_NOMINATIONS);
	}
	
	@Override
	public String subscriptionName(Subscription subscription) {
		return context.getResources().getString(R.string.menu_votes_nominations);
	}
	
	@Override
	public int subscriptionIcon(Subscription subscription) {
		return R.drawable.votes;
	}
}