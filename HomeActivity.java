package com.galactic.transact.home;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.galactic.transact.R;
import com.galactic.transact.activity.MoreSpendActivity;
import com.galactic.transact.activity.ProfileActivity;
import com.galactic.transact.activity.SearchActivity;
import com.galactic.transact.activity.UPiInfoActivity;
import com.galactic.transact.adapter.AccountsAdap;
import com.galactic.transact.adapter.SmsAdap;
import com.galactic.transact.adapter.TagsAdap;
import com.galactic.transact.interfaces.ListItemClickCallback;
import com.galactic.transact.pojo.Sms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends Activity implements ListItemClickCallback {
    private static final int MY_PERMISSIONS_REQUEST_CODE = 123;
    public static List<Sms> smsList = new ArrayList<Sms>();
    //  private ArrayList<String> allMessage = new ArrayList<>();
    public static ArrayList<String> allMessage = new ArrayList<>();
    Cursor c;
    ImageView search, profile;
    private String LOG_TAG = "HomeActivity";
    private RecyclerView rvSpend, rvAccounts, rvTags;
    private TextView moreSpend;
    private String avlBalance, avlBalance2, bankName, bankName2, updateDate, updateDate2;
    private ArrayList<String> allBankBalance = new ArrayList<>();
    private ArrayList<String> allBankName = new ArrayList<>();
    private ArrayList<String> allBankUpdateDate = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home);
        inItViews();
        checkPermissions();
        setTagAdapter();
        setAdapter();

    }

    private void inItViews() {


        search = findViewById(R.id.search);
        profile = findViewById(R.id.profile);


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });


        moreSpend = findViewById(R.id.moreSpend);
        moreSpend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* if (smsList != null) {
                    Intent in = new Intent(HomeActivity.this, MoreSpendActivity.class);
                    startActivity(in);

                }*/

                if (smsList != null) {
                    Intent in = new Intent(HomeActivity.this, MoreSpendActivity.class);
                    startActivity(in);

                }

            }
        });
    }

    private List<Sms> getAllSms() {
        Sms objSms = new Sms();
        Uri message = Uri.parse("content://sms/");
        //    Uri message = Uri.parse("content://sms/inbox");
        ContentResolver cr = HomeActivity.this.getContentResolver();
        c = cr.query(message, null, null, null, null);
        // if (c != null) { // w bf
        if (c != null && !c.isClosed()) {
            try {
                //   startManagingCursor(c);
                int totalSMS = c.getCount();
                //  Log.d(LOG_TAG, " Response: =>" + c.toString());
                if (c.moveToFirst()) {
                    for (int i = 0; i < totalSMS; i++) {
                        objSms = new Sms();
                        String msg = c.getString(c.getColumnIndexOrThrow("body"));
                        Log.d(LOG_TAG, " addr: " + msg);

                        //Todo Get available bal
                        if (msg.contains("Avbl Bal") || msg.contains("Avl bal") || msg.contains("Avl Bal")) {
                            //  Log.d(LOG_TAG, " Balance: " + msg);
                            if (msg.contains("Avbl Bal")) {
                                String[] separated = msg.split("INR");
                                String rs = separated[1];
                                String[] sep = rs.split(",");
                                String rs2 = sep[0];
                                avlBalance = rs2;
                                bankName = c.getString(c.getColumnIndexOrThrow("address"));
                                updateDate = c.getString(c.getColumnIndexOrThrow("date"));
                                break;
                            }
                            if (msg.contains("Avl Bal")) {
                               //Log.d(LOG_TAG, " Balance: " + msg);
                                //ALERT:You've withdrawn Rs.1500.00 via Debit Card xx8484 at SBI SAS NAGAR on 2020-01-10:20:47:20.Avl Bal Rs.74882.15.Not you?Call 18002586161.
                                String[] separated = msg.split("Avl Bal ");
                                String rs1 = separated[1];
                                String[] sep = rs1.split(".Not you?");
                                String rs = sep[0];
                                if (rs.contains("Rs.")) {
                                    String bal = rs.replace("Rs.", "");
                                    Log.d(LOG_TAG, " Avl Balance final: " + bal);
                                    avlBalance2 = bal;
                                    bankName2 = c.getString(c.getColumnIndexOrThrow("address"));
                                    updateDate2 = c.getString(c.getColumnIndexOrThrow("date"));
                                    break;
                                }
                            }
                        }
                        // TODO filter data
                        if (msg.contains("spent") || msg.contains("debited") /*|| msg.contains("Current Balance")*/ || msg.contains("withdrawn")) {
                            allMessage.add(msg);
                            getMsgInfo(msg, objSms, smsList);
                            c.moveToNext();
                        } else {
                            smsList.remove(objSms);
                            c.moveToNext();
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();

            } finally {
                try {
                    if (!c.isClosed()) {
                        c.close();
                    }
                    c = null;
                } catch (Exception e) {
                    Log.e("While closing cursor", String.valueOf(e));
                }
            }
        } else {
            try {
                if (c != null && !c.isClosed()) {
                    Log.d(LOG_TAG, " You have no SMS ");
                    c.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return smsList;
    }


    private void setAdapter() {
        Log.d(LOG_TAG, " setAdapter");
        try {
            if (getAllSms() != null) {
                // if (getAllSms() != null && c != null && !c.isClosed()) {
                Log.d(LOG_TAG, " not Null" + " SIZE:" + getAllSms().size());
                rvSpend = findViewById(R.id.recyclerViewContainer);
                LinearLayoutManager layoutManager = new LinearLayoutManager(HomeActivity.this);
                rvSpend.setLayoutManager(layoutManager);
                rvSpend.setAdapter(new SmsAdap(getAllSms(), allMessage, this));
                setAccountsAdapter();
            } else {
                Log.d(LOG_TAG, " Null" + " SIZE:");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkSim() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
        if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
            //the phone has a sim card
            getAllSms();
            setAdapter();
        } else {
            //no sim card available
            Toast.makeText(HomeActivity.this, "Please insert SIM ", Toast.LENGTH_LONG).show();
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                + ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE)
                + ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Do something, when permissions not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.RECEIVE_SMS)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_PHONE_STATE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(

                    this, Manifest.permission.READ_SMS)) {
                // If we should give explanation of requested permissions

                // Show an alert dialog here with request explanation
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Location,Phone State" +
                        " Storage permissions are required to do the task.");
                builder.setTitle("Please grant those permissions");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(
                                HomeActivity.this,
                                new String[]{
                                        Manifest.permission.RECEIVE_SMS,
                                        Manifest.permission.READ_PHONE_STATE,
                                        Manifest.permission.READ_SMS,
                                },
                                MY_PERMISSIONS_REQUEST_CODE
                        );
                    }
                });
                builder.setNeutralButton("Cancel", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Directly request for required permissions, without explanation
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_SMS,
                        },
                        MY_PERMISSIONS_REQUEST_CODE
                );
            }
        } else {
            // Do something, when permissions are already granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CODE: {
                // When request is cancelled, the results array are empty
                if ((grantResults.length > 0) &&
                        (grantResults[0]
                                + grantResults[1]
                                == PackageManager.PERMISSION_GRANTED
                        )
                ) {
                    // Permissions are granted
                    checkSim();

                } else {
                    // Permissions are denied
                    Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show();
                    // todo check permission again
                    checkPermissions();
                }
                return;
            }
        }
    }

    private void setAccountsAdapter() {
        getBankBalance();
        rvAccounts = findViewById(R.id.recyclerViewAccounts);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));

      rvAccounts.setAdapter(new AccountsAdap(this, getAllSms(), allMessage, allBankBalance, allBankName, allBankUpdateDate));
    }
    private void setTagAdapter() {
        ArrayList<String> tagList = new ArrayList<>();
        tagList.add(getResources().getString(R.string.school));
        tagList.add(getResources().getString(R.string.office));
        tagList.add(getResources().getString(R.string.family));
        tagList.add(getResources().getString(R.string.friends));
        tagList.add(getResources().getString(R.string.online));
        rvTags = findViewById(R.id.recyclerViewTag);
        rvTags.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));
        rvTags.setAdapter(new TagsAdap(tagList));
    }

    private void getMsgInfo(String msg, Sms objSms, List<Sms> lstSms) {
        // TODO NEED TO FILTER THIS DATA AS WELL : LIKE OTHERS
        objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
        objSms.setAddress(c.getString(c
                .getColumnIndexOrThrow("address")));

        // start
        if (msg.contains("spent")) {
            Log.d(LOG_TAG, " msg: " + msg);// ALERT:You've spent Rs.420.00 via Debit Card xx8484 at CITCO PETROL 56 on 2019-07-07:11:52:13.Avl Bal
            String[] separated = msg.split("via");
            String rs = separated[0];
            String[] sep = rs.split("Rs.");
            String rup = sep[1];
            objSms.setMsg(rup);
            // Get Location
            //  Log.d(LOG_TAG, " Location: " + getLocationName(msg));
            objSms.setLocationName(getLocationName(msg));

        } else if (msg.contains("is debited for")) {
            String[] separated = msg.split("is debited for");
            String rs = separated[1];
            // INR 10,000.00 on 10-05-19 & A/c xxxxxxxxxxxx9995 is credited (IMPS Ref No.913008372757).
            if (rs.contains("on")) {
                String[] sepOn = msg.split("on");
                String on = sepOn[0];
                // Log.d(LOG_TAG, " RS debited for 0=> " + on);//Your A/c XX1375 is debited for INR 10,000.00
                objSms.setMsg(on);
            } else {
                objSms.setMsg(rs);
            }
            Log.d(LOG_TAG, "in debited for");
        } else if (msg.contains("debited")) {//UPDATE: INR 5,000.00 debited from A/c XX1375 on 01-JUN-19. Info: IMPS-915223343647--SBIN-xxxxxxx6380-Funds. Avl bal:INR 50,088.31
// Rs 149.50 debited from a/c **1375 on 30-06-19 to VPA zomato@hdfcbank(UPI Ref No 918119377193). Not you? Call on 18002586161 to report
            Log.d(LOG_TAG, " msg Debited: " + msg);
            String[] separated = msg.split("debited");
            String rs = separated[0];
            objSms.setMsg(rs);
            // set location
            if (msg.contains("Info:")) {
                String[] sep = msg.split("Info:");
                String info = sep[1];
                //  Log.d(LOG_TAG, " info: " + info); // TODO can get available bal from here
                // IMPS-915223343647--SBIN-xxxxxxx6380-Funds. Avl bal:INR 50,088.31
                String[] from = info.split("Avl");// Break before Avl bal
                objSms.setLocationName(from[0]);
            }
            if (msg.contains("VPA")) {
                String[] sep = msg.split("VPA");
                String info = sep[1];
                String[] from = info.split("Not");// Break before Not you?
                // Log.d(LOG_TAG, " info split=>" + from[0]);
                objSms.setLocationName(from[0]);
            }

        } else if (msg.contains("withdrawn")) {
            String[] sep = msg.split("via");
            String via = sep[0];
            // Log.d(LOG_TAG, " RS withdrawn via=>>: " + via);
            String[] finalRsSep = via.split("withdrawn");
            String finalRs = finalRsSep[1];
            objSms.setMsg(finalRs);
        }
        // end

        //  objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
        objSms.setReadState(c.getString(c.getColumnIndex("read")));
        objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
        //  String finalTime = new SimpleDateFormat("hh:mm a MMM dd, yyyy", Locale.US).format(new Date(Long.parseLong(tt)));
        if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
            objSms.setFolderName("inbox");
        } else {
            objSms.setFolderName("sent");
        }

        lstSms.add(objSms);

    }

    private String getLocationName(String locName) {
        String[] spent = locName.split("at");
        String spentAt = spent[1];
        Log.d(LOG_TAG, " spentAt: " + spentAt);//LIFE STYLE on 2019-07-07:20:55:00.Avl Bal Rs.101531.37.Not you?Call 18002586161.
        String[] getLoc = spentAt.split("on");
        String onLoc = getLoc[0];
        Log.d(LOG_TAG, " spentAt on: " + onLoc);
        return onLoc;
    }


    @Override
    public void onListItemClick(Sms data, Object object) {
        Sms sms = data;
        /*Log.d(LOG_TAG, " Position: " + sms.getAddress());
        Log.d(LOG_TAG, " Message: " + object.toString());*/

        Intent mIntent = new Intent(this, UPiInfoActivity.class);
        mIntent.putExtra("title", sms.getAddress());
        if (sms.getLocationName() != null) {
            mIntent.putExtra("location", sms.getLocationName());
        } else {
            mIntent.putExtra("location", "UNDEFINED");
        }
        String dateTime = new SimpleDateFormat("MMM dd,hh:mm a", Locale.US).format(new Date(Long.parseLong(sms.getTime())));
        mIntent.putExtra("msg", object.toString());
        mIntent.putExtra("spendMoney", sms.getMsg());

        //
        String[] sep = dateTime.split(",");
        String date = sep[0];
        String time = sep[1];
        mIntent.putExtra("time", time);
        mIntent.putExtra("date", date);
        startActivity(mIntent);
    }


    private void getBankBalance() {
       // Log.d(LOG_TAG, "getBankBalance:=");
        Log.d(LOG_TAG, "avlBalanceM:=" + avlBalance);
        Log.d(LOG_TAG, "avlBalanceM secomdAccount:=" + avlBalance2);

        allBankBalance.add(avlBalance);
        allBankBalance.add(avlBalance2);
        allBankName.add(bankName);
        allBankName.add(bankName2);
        allBankUpdateDate.add(updateDate);
        allBankUpdateDate.add(updateDate2);


    }

}
