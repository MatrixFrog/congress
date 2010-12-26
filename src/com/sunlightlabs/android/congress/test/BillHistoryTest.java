package com.sunlightlabs.android.congress.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import android.text.TextUtils;

import com.sunlightlabs.congress.models.Bill;
import com.sunlightlabs.congress.models.Bill.Action;

public class BillHistoryTest {

  private static String getActionText() {
    return TextUtils.join(" ", Collections.nCopies(10, "action text"));
  }

  /**
   * @return a bill with three actions: 
   *   - One that shows a date and a type
   *   - One that shows only a type
   *   - One that shows only a date 
   */
  public static Bill getTestBill() {
    Bill bill = new Bill();
    Action dateAndType = new Action();
    dateAndType.acted_at = new Date(1234, 5, 6, 7, 8, 9);
    dateAndType.text = getActionText();
    dateAndType.type = "vote";

    Action typeOnly = new Action();
    typeOnly.acted_at = new Date(1234, 5, 6, 8, 9, 10);
    typeOnly.text = getActionText();
    typeOnly.type = "vote";

    Action dateOnly = new Action();
    dateOnly.acted_at = new Date(1234, 1, 2, 3, 4, 5);
    dateOnly.text = getActionText();
    dateOnly.type = "";

    Action nothingButText = new Action();
    nothingButText.acted_at = new Date(1234, 1, 2, 6, 7, 8);
    nothingButText.text = getActionText();
    nothingButText.type = "";

    bill.actions = new ArrayList<Action>();
    bill.actions.addAll(Arrays.asList(dateAndType, typeOnly, dateOnly, nothingButText));
    return bill;
  }
}
