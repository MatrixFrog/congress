package com.sunlightlabs.android.congress;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunlightlabs.android.congress.notifications.Footer;
import com.sunlightlabs.android.congress.notifications.Subscriber;
import com.sunlightlabs.android.congress.notifications.Subscription;
import com.sunlightlabs.android.congress.notifications.subscribers.ActionsBillSubscriber;
import com.sunlightlabs.android.congress.tasks.LoadBillTask;
import com.sunlightlabs.android.congress.test.BillHistoryTest;
import com.sunlightlabs.android.congress.utils.Utils;
import com.sunlightlabs.congress.models.Bill;
import com.sunlightlabs.congress.models.CongressException;
import com.sunlightlabs.congress.models.Bill.Action;

public class BillHistory extends ListActivity implements LoadBillTask.LoadsBill {
	private LoadBillTask loadBillTask;
	private Bill bill;

	private Footer footer;

	private Set<Integer> positionsToShowDate = new HashSet<Integer>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_footer);

		Bundle extras = getIntent().getExtras();
		bill = (Bill) extras.getSerializable("bill");
		
		BillHistoryHolder holder = (BillHistoryHolder) getLastNonConfigurationInstance();
		if (holder != null) {
			this.loadBillTask = holder.loadBillTask;
			this.bill = holder.bill;
			this.positionsToShowDate = holder.positionsToShowDate;
			if (loadBillTask != null)
				loadBillTask.onScreenLoad(this);
		}
		
		if (loadBillTask == null)
			loadBill();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return new BillHistoryHolder(loadBillTask, bill, positionsToShowDate);
	}

	public Context getContext() {
		return this;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (footer != null)
			footer.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (bill.actions != null) {
			if (bill.actions.size() > 0)
				setupSubscription(bill.actions.get(0));
			else
				setupSubscription(null);
		}
	}

	private void setupSubscription(Object lastResult) {
		footer = (Footer) findViewById(R.id.footer);
		String lastSeenId = (lastResult == null) ? null : new ActionsBillSubscriber().decodeId(lastResult);
		footer.init(new Subscription(bill.id,  Subscriber.notificationName(bill), "ActionsBillSubscriber", bill.id, lastSeenId));
	}
	
	private void loadBill() {
		if (bill.actions == null)
			loadBillTask = (LoadBillTask) new LoadBillTask(this, bill.id).execute("actions");
		else
			displayBill(positionsToShowDate);
	}
	
	public void onLoadBill(Bill bill) {
		bill = BillHistoryTest.getTestBill();
		this.loadBillTask = null;
		this.bill.actions = bill.actions;

		positionsToShowDate.clear();
		positionsToShowDate.add(0);

		for (int i=1; i<bill.actions.size(); i++) {
			Action previousAction = bill.actions.get(i-1);
			Action action = bill.actions.get(i);

			if (!Utils.sameDay(action.acted_at, previousAction.acted_at)) {
				positionsToShowDate.add(i);
			}
		}
		displayBill(positionsToShowDate);
	}
	
	public void onLoadBill(CongressException exception) {
		Utils.showRefresh(this, R.string.error_connection);
	}
	
	private void displayBill(Set<Integer> positionsToShowDate) {
		if (bill.actions.size() > 0) {
			setupSubscription(bill.actions.get(0));
			setListAdapter(new BillActionAdapter(this, bill.actions, positionsToShowDate));
		} else {
			setupSubscription(null);
			Utils.showEmpty(this, R.string.bill_actions_empty);
		}
	}
	
	private static class BillActionAdapter extends ArrayAdapter<Bill.Action> {
    	LayoutInflater inflater;
    	Resources resources;
    	Set<Integer> positionsToShowDate;

        public BillActionAdapter(Activity context, ArrayList<Bill.Action> items, Set<Integer> positionsToShowDate) {
            super(context, 0, items);
            inflater = LayoutInflater.from(context);
            resources = context.getResources();
            this.positionsToShowDate = positionsToShowDate;
        }
        
        @Override
        public boolean isEnabled(int position) {
        	return false;
        }
        
        @Override
        public boolean areAllItemsEnabled() {
        	return false;
        }
        
        @Override
        public int getViewTypeCount() {
        	return 1;
        }

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null)
				view = inflater.inflate(R.layout.bill_action, null);
			
			Bill.Action action = getItem(position);
			
			TextView actedAtView = (TextView) view.findViewById(R.id.acted_at);
			TextView textView = (TextView) view.findViewById(R.id.text);
			RelativeLayout.LayoutParams textLayoutParams = (RelativeLayout.LayoutParams) textView.getLayoutParams();
			if (positionsToShowDate.contains(position)) {
				String timestamp = new SimpleDateFormat("MMM dd, yyyy").format(action.acted_at);
				actedAtView.setText(timestamp);
				actedAtView.setVisibility(View.VISIBLE);
				textLayoutParams.addRule(RelativeLayout.BELOW, R.id.acted_at);
			}
			else {
				// Hide the date, and place the text under the type instead of under the date. 
				actedAtView.setVisibility(View.GONE);
				textLayoutParams.addRule(RelativeLayout.BELOW, R.id.type);
			}

			textView.setText(Html.fromHtml("<b>&#183;</b> " + action.text));
			
			TextView typeView = (TextView) view.findViewById(R.id.type);
			typeView.setVisibility(View.VISIBLE);
			String type = action.type;
			if (type.equals("vote") || type.equals("vote2") || type.equals("vote-aux")) {
				typeView.setText("Vote");
				typeView.setTextColor(resources.getColor(R.color.action_vote));
			} else if (type.equals("enacted")) {
				typeView.setText("Enacted");
				typeView.setTextColor(resources.getColor(R.color.action_enacted));
			} else if (type.equals("vetoed")) {
				typeView.setText("Vetoed");
				typeView.setTextColor(resources.getColor(R.color.action_vetoed));
			} else {
				typeView.setVisibility(View.GONE);
			}
			
			return view;
		}

    }
	
	private static class BillHistoryHolder {
		LoadBillTask loadBillTask;
		Bill bill;
		Set<Integer> positionsToShowDate;
		
		public BillHistoryHolder(LoadBillTask loadBillTask, Bill bill, Set<Integer> positionsToShowDate) {
			this.loadBillTask = loadBillTask;
			this.bill = bill;
			this.positionsToShowDate = positionsToShowDate;
		}
	}
}